/**
 *
 */
package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationRequestUtils.*;
import static at.porscheinformatik.idp.saml2.Saml2Utils.*;
import static at.porscheinformatik.idp.saml2.SamlResponseCustomizer.*;
import static java.time.temporal.ChronoUnit.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationDetailsSource.HttpRequestContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.core.Saml2X509Credential.Saml2X509CredentialType;
import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2PostAuthenticationRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Daniel Furtlehner
 */
class Saml2ResponseProcessorTest {

    private static final String IDP_ENTITY_ID = "https://identity.com/identity/saml2";
    private static final String SP_ENTITY_ID = "https://service.com/service/saml2/pnet";
    private static final String RESPONSE_DESTINATION = SP_ENTITY_ID + "/sso/post/pnet";
    private static final String IDP_ENDPOINT_URL = "https://identity.com/identity/saml2/authorize";

    static {
        Saml2Initializer.initialize();
    }

    private final Saml2CredentialsManager credentialsManager;

    Saml2ResponseProcessorTest() throws Exception {
        super();
        credentialsManager = Saml2TestUtils.defaultCredentialsManager();
    }

    @Test
    void failsOnOutdatedResponse() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(outdatedResponse());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Message was rejected due to issue instant expiration"
        );
    }

    @Test
    void failsOnMissingIssuer() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(noIssuer());

        testException(tokenAndResponse, MessageHandlerException.class, "Saml message has no issuer set");
    }

    @Test
    void failsOnUnknownIssuer() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(issuer("https://unkown.com/identity/saml2"));

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Invalid issuer https://unkown.com/identity/saml2"
        );
    }

    @Test
    void failsOnWrongIssuerFormat() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(issuerFormat(NameIDType.TRANSIENT));

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Saml Message has invalid issuer format set urn:oasis:names:tc:SAML:2.0:nameid-format:transient"
        );
    }

    @Test
    void failsOnMissingId() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(noId());

        testException(tokenAndResponse, MessageHandlerException.class, "Message ID must not be null.");
    }

    @Test
    void failsOnMissingAssertion() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(noAssertion());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Response must have exactly one Assertion but has 0"
        );
    }

    @Test
    void failsOnMultipleAssertions() throws Exception {
        Assertion additionalAssertion = Saml2ObjectUtils.assertion(
            IDP_ENTITY_ID,
            Saml2ObjectUtils.subject(SP_ENTITY_ID, 10, "XYZ"),
            null,
            null,
            null
        );
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(assertion(additionalAssertion));

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Response must have exactly one Assertion but has 2"
        );
    }

    @Test
    void failsOnMissingAuthnStatement() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(noAuthnStatement());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Response must have exactly one AuthnStatement but has 0"
        );
    }

    @Test
    void failsOnMultipleAuthnStatements() throws Exception {
        AuthnStatement additionalAuthnStatement = Saml2ObjectUtils.authnStatement(
            Instant.now(),
            "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport"
        );
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(authnStatement(additionalAuthnStatement));

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Response must have exactly one AuthnStatement but has 2"
        );
    }

    @Test
    void failsOnMissingAttributeStatement() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(noAttributeStatement());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Response must have exactly one AttributeStatement but has 0"
        );
    }

    @Test
    void failsOnMultipleAttributeStatements() throws Exception {
        AttributeStatement additionalAttributeStatement = Saml2ObjectUtils.attributeStatement();
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(attributeStatement(additionalAttributeStatement));

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Response must have exactly one AttributeStatement but has 2"
        );
    }

    @Test
    void failsOnMissingSubject() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(noSubject());

        testException(tokenAndResponse, MessageHandlerException.class, "Assertion is missing a subject");
    }

    @Test
    void failsOnWrongSubjectConfirmationMethod() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(wrongSubjectConfirmationMethod());

        testException(tokenAndResponse, MessageHandlerException.class, "No bearer SubjectConfirmation found");
    }

    @Test
    void failsOnMissingSubjectConfirmationData() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(noSubjectConfirmationData());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "No SubjectConfirmationData for bearer Subject found"
        );
    }

    @Test
    void failsOnWrongRecipient() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(wrongRecipient());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Invalid Recipient attribute. Expected https://service.com/service/saml2/pnet/sso/post/pnet but got wrong"
        );
    }

    @Test
    void failsOnOutdatedSubjectConfirmation() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(outdatedSubjectConfirmation());

        testException(tokenAndResponse, MessageHandlerException.class, "SubjectConfirmationData already outdated");
    }

    @Test
    void failsOnNotBeforeSubjectConfirmation() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(notBeforeOnSubjectConfirmation());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "NotBefore must not be set on SubjectConfirmationData elements"
        );
    }

    @Test
    void failsOnWrongInResponseTo() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(wrongInResponseTo());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Wrong inResponseTo on SubjectConfirmationData. Expected XYZ but got wrong"
        );
    }

    @Test
    void failsOnMissingAudience() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(noAudienceRestriction());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "No Audience matching https://service.com/service/saml2/pnet found"
        );
    }

    @Test
    void failsOnEmptyConditions() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(emptyConditions());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "No Audience matching https://service.com/service/saml2/pnet found"
        );
    }

    @Test
    void failsOnWrongNotBefore() throws Exception {
        // Have to account for clock skew so add a bigger value than 5 minutes
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(
            conditionsValidity(Instant.now().plus(10, MINUTES), Instant.now().plus(15, MINUTES))
        );

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Contditions is not valid right now based on notBefore value"
        );
    }

    @Test
    void failsOnWrongNotOnOrAfter() throws Exception {
        // Have to account for clock skew so add a bigger value than 5 minutes
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(
            conditionsValidity(Instant.now().minus(10, MINUTES), Instant.now().minus(9, MINUTES))
        );

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Contditions is not valid anymore based on notOnOrAfter value"
        );
    }

    @Test
    void failsOnConditionsValidityReversed() throws Exception {
        // Have to account for clock skew so add a bigger value than 5 minutes
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(
            conditionsValidity(Instant.now().minus(1, MINUTES), Instant.now().minus(5, MINUTES))
        );

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Contditions notOnOrAfter is before notBefore date"
        );
    }

    @Test
    void failsOnWrongAudienceRestriction() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(wrongAudienceRestriction());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "No Audience matching https://service.com/service/saml2/pnet found"
        );
    }

    @Test
    void failsOnUnsignedRequest() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(false, false, false, false, null, null, null);

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Response must be signed but no signature present"
        );
    }

    @Test
    void failsOnWrongSignature() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(true, true, false, false, null, null, null);

        testException(tokenAndResponse, MessageHandlerException.class, "Error validating signature");
    }

    @Test
    void failsOnMissingDestination() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(missingDestination());

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "SAML message intended destination (required by binding) was not present"
        );
    }

    @Test
    void failsOnToOldAuthnInstantWhenForcedAuthentication() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(
            true,
            false,
            false,
            true,
            null,
            null,
            null,
            oldAuthnInstant()
        );

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Outdated AuthnInstant found for forced authentication."
        );
    }

    @Test
    void failsOnToOldAuthnInstantWhenRequestedSessionAge() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(
            true,
            false,
            false,
            false,
            null,
            5,
            null,
            oldAuthnInstant()
        );

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Outdated AuthnInstant found for requested session age 5 seconds."
        );
    }

    @Test
    void failsOnWrongDestination() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(wrongDestination());

        testException(tokenAndResponse, MessageHandlerException.class, "SAML message failed received endpoint check");
    }

    @Test
    void failsOnMissingSubjectIdentifier() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(
            true,
            false,
            false,
            false,
            2,
            null,
            null,
            noAttributes()
        );

        testException(tokenAndResponse, MessageHandlerException.class, "No subject-identifier found in Response");
    }

    @Test
    void failsOnErrorResponse() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(true, false, true, false, null, null, null);

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Unsuccessful Response: urn:oasis:names:tc:SAML:2.0:status:Requester "
        );
    }

    @Test
    void failsOnWeakAuthentication() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(true, false, false, false, 3, null, null);

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Authentication strength USERPASS is weaker than requested strength 3"
        );
    }

    @Test
    void successOnStrongEnoughAuthentication() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(true, false, false, false, 2, null, null);

        Saml2ResponseProcessor processor = Saml2ResponseProcessor.withDefaultHandlers();

        processor.process(tokenAndResponse.getToken(), tokenAndResponse.getResponse());
    }

    @Test
    void failsOnMissingResponseRelayState() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(
            true,
            false,
            false,
            false,
            2,
            null,
            null,
            UUID.randomUUID().toString(),
            null
        );

        testException(tokenAndResponse, MessageHandlerException.class, "Relay state is missing in response.");
    }

    @Test
    void failsOnMissingRequestRelayState() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(
            true,
            false,
            false,
            false,
            2,
            null,
            null,
            null,
            UUID.randomUUID().toString()
        );

        testException(tokenAndResponse, MessageHandlerException.class, "Requested relay state is missing.");
    }

    @Test
    void failsOnNotMatchingRelayState() throws Exception {
        TokenAndResponse tokenAndResponse = buildTokenAndResponse(
            true,
            false,
            false,
            false,
            2,
            null,
            null,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );

        testException(
            tokenAndResponse,
            MessageHandlerException.class,
            "Requested relay state doesn't match relay state in response"
        );
    }

    private void testException(
        TokenAndResponse tokenAndResponse,
        Class<MessageHandlerException> expectedException,
        String message
    ) {
        Saml2ResponseProcessor processor = Saml2ResponseProcessor.withDefaultHandlers();

        MessageHandlerException actual = assertThrows(MessageHandlerException.class, () ->
            processor.process(tokenAndResponse.getToken(), tokenAndResponse.getResponse())
        );

        assertThat(actual.getMessage(), equalTo(message));
    }

    protected TokenAndResponse buildTokenAndResponse(SamlResponseCustomizer... customizers)
        throws MarshallingException, KeyStoreException, NoSuchAlgorithmException, CertificateException, SecurityException, SignatureException, IOException, EncryptionException {
        return buildTokenAndResponse(true, false, false, false, null, null, null, customizers);
    }

    protected TokenAndResponse buildTokenAndResponse(
        boolean signed,
        boolean invalidateSignature,
        boolean errorResponse,
        boolean forceAuthn,
        Integer nistLevel,
        Integer sessionAge,
        String tenant,
        SamlResponseCustomizer... customizers
    )
        throws MarshallingException, EncryptionException, CertificateException, SecurityException, KeyStoreException, NoSuchAlgorithmException, SignatureException, IOException {
        String relayState = UUID.randomUUID().toString();
        return buildTokenAndResponse(
            signed,
            invalidateSignature,
            errorResponse,
            forceAuthn,
            nistLevel,
            sessionAge,
            tenant,
            relayState,
            relayState,
            customizers
        );
    }

    protected TokenAndResponse buildTokenAndResponse(
        boolean signed,
        boolean invalidateSignature,
        boolean errorResponse,
        boolean forceAuthn,
        Integer nistLevel,
        Integer sessionAge,
        String tenant,
        String requestedRelayState,
        String responseRelayState,
        SamlResponseCustomizer... customizers
    )
        throws MarshallingException, KeyStoreException, NoSuchAlgorithmException, CertificateException, SecurityException, SignatureException, IOException, EncryptionException {
        String authnRequestId = "XYZ";

        Response response = Saml2ObjectUtils.response(IDP_ENTITY_ID, RESPONSE_DESTINATION, authnRequestId);

        if (errorResponse) {
            response.setStatus(Saml2ObjectUtils.status(StatusCode.REQUESTER, null));
        } else {
            response.setStatus(Saml2ObjectUtils.status(StatusCode.SUCCESS, null));
            Subject subject = Saml2ObjectUtils.subject(RESPONSE_DESTINATION, 5 * 60, authnRequestId);
            Conditions conditions = Saml2ObjectUtils.conditions(SP_ENTITY_ID);
            AuthnStatement authnStatement = Saml2ObjectUtils.authnStatement(
                Instant.now(),
                "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport"
            );
            AttributeStatement attributeStatement = Saml2ObjectUtils.attributeStatement();
            response
                .getAssertions()
                .add(
                    Saml2ObjectUtils.assertion(IDP_ENTITY_ID, subject, conditions, authnStatement, attributeStatement)
                );

            fillAttributes(attributeStatement);
        }

        for (SamlResponseCustomizer customizer : customizers) {
            customizer.doWithResponse(response);
        }

        for (Assertion assertion : response.getAssertions()) {
            Saml2X509Credential credential = credentialsManager
                .getCredentials(Saml2X509CredentialType.DECRYPTION)
                .get(0);

            response
                .getEncryptedAssertions()
                .add(Saml2ObjectUtils.encryptAssertion(assertion, SP_ENTITY_ID, credential));
        }

        response.getAssertions().clear();

        if (signed) {
            Saml2X509Credential credential = credentialsManager.getCredentials(Saml2X509CredentialType.SIGNING).get(0);

            Saml2ObjectUtils.sign(response, credential);
        }

        if (invalidateSignature) {
            // Create a new id. So the response content is not the same as the signed content anymore
            response.setID(Saml2ObjectUtils.generateId());
        }

        String stringResponse = XmlUtils.marshall(response);
        String base64Response = Base64.encodeBase64String(stringResponse.getBytes(StandardCharsets.UTF_8));

        MockHttpServletRequest request = buildRequestFromUrl(RESPONSE_DESTINATION);
        request.setMethod("POST");

        Saml2Utils.storeAuthnRequestId(request, authnRequestId);

        if (forceAuthn) {
            storeForceAuthentication(request, true);
        }

        storeNistLevel(request, Optional.ofNullable(nistLevel));
        storeSessionAge(request, Optional.ofNullable(sessionAge));
        storeTenant(request, Optional.ofNullable(tenant));

        request.addParameter("SAMLResponse", base64Response);
        request.addParameter(RELAY_STATE_PARAM, responseRelayState);

        RelyingPartyRegistration relyingPartyRegistration = buildRelyingPartyRegistration();
        AbstractSaml2AuthenticationRequest authenticationRequest =
            Saml2PostAuthenticationRequest.withRelyingPartyRegistration(relyingPartyRegistration)
                .samlRequest("dummy request")
                .relayState(requestedRelayState)
                .build();
        Saml2AuthenticationToken token = new Saml2AuthenticationToken(
            relyingPartyRegistration,
            stringResponse,
            authenticationRequest
        );
        token.setDetails(new HttpRequestContext(request));

        return new TokenAndResponse(token, response);
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

    private void fillAttributes(AttributeStatement attributeStatement) {
        attributeStatement.getAttributes().add(singleStringAttribute(SUBJECT_ID_NAME, "1234@localhost"));
        attributeStatement.getAttributes().add(singleStringAttribute(IDP_ENTITY_ID + "/attributes/name", "Jane Doe"));
    }

    private Attribute singleStringAttribute(String name, String value) {
        if (value == null) {
            return null;
        }

        Attribute attribute = Saml2ObjectUtils.attribute(name, Attribute.URI_REFERENCE);

        attribute.getAttributeValues().add(XmlUtils.xmlString(value));

        return attribute;
    }

    private RelyingPartyRegistration buildRelyingPartyRegistration() {
        return RelyingPartyRegistration.withRegistrationId("pnet")
            .entityId(SP_ENTITY_ID)
            .assertionConsumerServiceBinding(Saml2MessageBinding.POST)
            .assertionConsumerServiceLocation(RESPONSE_DESTINATION)
            .decryptionX509Credentials(credentials ->
                credentials.addAll(credentialsManager.getCredentials(Saml2X509CredentialType.DECRYPTION))
            )
            .assertingPartyDetails(builder ->
                builder
                    .entityId(IDP_ENTITY_ID)
                    .singleSignOnServiceBinding(Saml2MessageBinding.REDIRECT)
                    .singleSignOnServiceLocation(IDP_ENDPOINT_URL)
                    .wantAuthnRequestsSigned(false)
                    .verificationX509Credentials(credentials ->
                        credentialsManager
                            .getCredentials(Saml2X509CredentialType.SIGNING)
                            .stream()
                            .map(Saml2X509Credential::getCertificate)
                            .map(Saml2X509Credential::verification)
                            .forEach(credentials::add)
                    )
            )
            .build();
    }

    private static final class TokenAndResponse {

        private final Saml2AuthenticationToken token;
        private final Response response;

        TokenAndResponse(Saml2AuthenticationToken token, Response response) {
            super();
            this.token = token;
            this.response = response;
        }

        Saml2AuthenticationToken getToken() {
            return token;
        }

        Response getResponse() {
            return response;
        }
    }
}
