/**
 *
 */
package at.porscheinformatik.idp.saml2;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2alg.DigestMethod;
import org.opensaml.saml.ext.saml2alg.SigningMethod;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EncryptionMethod;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.saml2.core.Saml2X509Credential.Saml2X509CredentialType;
import org.springframework.security.saml2.provider.service.metadata.Saml2MetadataResolver;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Daniel Furtlehner
 */
class Saml2ServiceProviderMetadataFilterTest {
    static {
        Saml2Initializer.initialize();
    }

    private static final String SP_ENTITY_ID = "https://service.com:443/saml2/pnet";
    private static final String RESPONSE_DESTINATION = SP_ENTITY_ID + "/authenticate/pnet";

    @Test
    void filterNotCalled() throws Exception {
        TestFilterChain chain = new TestFilterChain();
        Saml2ServiceProviderMetadataFilter filter = buildFilter();
        HttpServletRequest request = buildRequestFromUrl("https://service.com/something");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(chain.numberOfTimesCalled, equalTo(1));
        assertThat(response.getContentAsString(), equalTo(""));
    }

    @Test
    void metadataInResponse() throws Exception {
        TestFilterChain chain = new TestFilterChain();
        Saml2ServiceProviderMetadataFilter filter = buildFilter();
        HttpServletRequest request = buildRequestFromUrl(SP_ENTITY_ID);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(chain.numberOfTimesCalled, equalTo(0));
        EntityDescriptor entityDescriptor = Saml2ObjectUtils.unmarshal(response.getContentAsString());
        SPSSODescriptor ssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);

        assertThat(entityDescriptor.getEntityID(), equalTo(SP_ENTITY_ID));
        assertThat(entityDescriptor.getValidUntil(), notNullValue());
        assertThat(ssoDescriptor, notNullValue());

        AssertionConsumerService assertionConsumerService = assertSingleList(
            ssoDescriptor.getAssertionConsumerServices()
        );

        assertThat(assertionConsumerService, notNullValue());
        assertThat(assertionConsumerService.getBinding(), equalTo(SAMLConstants.SAML2_POST_BINDING_URI));
        assertThat(assertionConsumerService.getLocation(), equalTo(RESPONSE_DESTINATION));

        List<KeyDescriptor> keyDescriptors = ssoDescriptor.getKeyDescriptors();

        assertThat(keyDescriptors.size(), equalTo(2));

        KeyDescriptor signingKey = assertKeyOfType(keyDescriptors, UsageType.SIGNING);
        assertThat(signingKey.getEncryptionMethods(), empty());

        KeyDescriptor encryptionKey = assertKeyOfType(keyDescriptors, UsageType.ENCRYPTION);
        List<String> encryptionMethods = encryptionKey
            .getEncryptionMethods()
            .stream()
            .map(EncryptionMethod::getAlgorithm)
            .toList();
        assertThat(
            encryptionMethods.toString(),
            encryptionMethods,
            containsInAnyOrder(
                EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM,
                EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256_GCM
            )
        );

        EntityAttributes entityAttributes = getObjectOfType(
            entityDescriptor.getExtensions().getUnknownXMLObjects(),
            EntityAttributes.class
        );

        Attribute attribute = entityAttributes.getAttributes().get(0);
        assertThat(attribute.getName(), equalTo(PartnerNetSaml2MetadataResolver.SUBJECT_ID_REQUIREMENT_NAME));
        assertThat(((XSString) attribute.getAttributeValues().get(0)).getValue(), equalTo("subject-id"));

        List<String> signingMethods = getObjectsOfType(
            entityDescriptor.getExtensions().getUnknownXMLObjects(),
            SigningMethod.class
        )
            .stream()
            .map(SigningMethod::getAlgorithm)
            .toList();
        assertThat(
            signingMethods,
            containsInAnyOrder(
                SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
                SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256
            )
        );

        List<String> digestMethods = getObjectsOfType(
            entityDescriptor.getExtensions().getUnknownXMLObjects(),
            DigestMethod.class
        )
            .stream()
            .map(DigestMethod::getAlgorithm)
            .toList();
        assertThat(digestMethods, containsInAnyOrder(SignatureConstants.ALGO_ID_DIGEST_SHA256));
    }

    private KeyDescriptor assertKeyOfType(List<KeyDescriptor> keyDescriptors, UsageType usage) {
        KeyDescriptor keyDescriptor = keyDescriptors //
            .stream()
            .filter(key -> key.getUse() == usage)
            .findAny()
            .get();

        assertThat(keyDescriptor.getKeyInfo(), notNullValue());

        X509Data x509Data = assertSingleList(keyDescriptor.getKeyInfo().getX509Datas());
        assertSingleList(x509Data.getX509Certificates());

        return keyDescriptor;
    }

    @SuppressWarnings("unchecked")
    private <T> T getObjectOfType(List<XMLObject> objects, Class<T> type) {
        for (XMLObject xmlObject : objects) {
            if (type.isAssignableFrom(xmlObject.getClass())) {
                return (T) xmlObject;
            }
        }

        throw new AssertionError("No object of type " + type + " found in list " + objects);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getObjectsOfType(List<XMLObject> objects, Class<T> type) {
        return objects
            .stream()
            .filter(xmlObject -> type.isAssignableFrom(xmlObject.getClass()))
            .map(o -> (T) o)
            .toList();
    }

    private <T> T assertSingleList(List<T> list) {
        assertThat(list.size(), equalTo(1));

        return list.get(0);
    }

    private Saml2ServiceProviderMetadataFilter buildFilter() throws Exception {
        RelyingPartyRegistrationResolver registrationResolver = new DefaultRelyingPartyRegistrationResolver(
            buildRelyingPartyRepository()
        );
        Saml2MetadataResolver metadataResolver = new PartnerNetSaml2MetadataResolver();

        return new Saml2ServiceProviderMetadataFilter(
            "/saml2/{registrationId}",
            registrationResolver,
            metadataResolver
        );
    }

    private RelyingPartyRegistrationRepository buildRelyingPartyRepository() throws Exception {
        Saml2CredentialsManager credentialsManager = Saml2TestUtils.defaultCredentialsManager();

        RelyingPartyRegistration registration = withRegistrationId("pnet")
            .entityId(SP_ENTITY_ID)
            .assertionConsumerServiceBinding(Saml2MessageBinding.POST)
            .assertionConsumerServiceLocation(RESPONSE_DESTINATION)
            .decryptionX509Credentials(credentials ->
                credentials.addAll(credentialsManager.getCredentials(Saml2X509CredentialType.DECRYPTION))
            )
            .signingX509Credentials(credentials ->
                credentials.addAll(credentialsManager.getCredentials(Saml2X509CredentialType.SIGNING))
            )
            .assertingPartyDetails(builder ->
                builder
                    .entityId("https://idp.com/saml2")
                    .singleSignOnServiceBinding(Saml2MessageBinding.REDIRECT)
                    .singleSignOnServiceLocation("https://idp.com/saml2/sso")
                    .wantAuthnRequestsSigned(false)
            )
            .build();

        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

    private MockHttpServletRequest buildRequestFromUrl(String url) {
        MockHttpServletRequest request = new MockHttpServletRequest();

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url).build();

        request.setScheme(uriComponents.getScheme());
        request.setServerName(uriComponents.getHost());
        request.setServerPort(uriComponents.getPort());
        request.setPathInfo(uriComponents.getPath());
        request.setRequestURI(uriComponents.getPath());

        return request;
    }

    private static class TestFilterChain implements FilterChain {

        private int numberOfTimesCalled = 0;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            numberOfTimesCalled++;
        }
    }
}
