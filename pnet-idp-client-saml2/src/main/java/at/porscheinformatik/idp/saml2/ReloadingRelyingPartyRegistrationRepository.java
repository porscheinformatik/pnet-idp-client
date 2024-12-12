package at.porscheinformatik.idp.saml2;

import static org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration.*;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.apache.http.client.HttpClient;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.impl.HTTPMetadataResolver;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.core.Saml2X509Credential.Saml2X509CredentialType;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;

public class ReloadingRelyingPartyRegistrationRepository implements RelyingPartyRegistrationRepository {

    private final String registrationId;
    private final Saml2CredentialsManager credentialsManager;
    private final RelyingPartyRegistrationMetadataResolver resolver;
    private final HttpClientFactory clientFactory;
    private final String loginProcessingUrl;
    private final String entityIdPath;

    public ReloadingRelyingPartyRegistrationRepository(
        String registrationId,
        String idpEntityId,
        String idpMetadataUrl,
        Saml2CredentialsManager credentialsManager,
        HttpClientFactory clientFactory,
        String loginProcessingUrl,
        String entityIdPath
    ) {
        super();
        this.registrationId = registrationId;
        this.credentialsManager = credentialsManager;
        this.clientFactory = clientFactory;
        this.loginProcessingUrl = loginProcessingUrl;
        this.entityIdPath = entityIdPath;
        resolver = buildResolver(idpEntityId, idpMetadataUrl);
    }

    @Override
    public RelyingPartyRegistration findByRegistrationId(String registrationId) {
        if (!Objects.equals(this.registrationId, registrationId)) {
            return null;
        }

        if (!resolver.isInitialized()) {
            try {
                resolver.initialize();
                resolver.prepareRegistration();
            } catch (ComponentInitializationException | ResolverException e) {
                throw new Saml2Exception("Error initializing Metadata. Trying again in a few minutes", e);
            }
        }

        return resolver.getRegistration();
    }

    private RelyingPartyRegistrationMetadataResolver buildResolver(String entityId, String metadataUrl) {
        try {
            return new RelyingPartyRegistrationMetadataResolver(
                clientFactory.newClient(),
                entityId,
                metadataUrl,
                registrationId,
                loginProcessingUrl,
                entityIdPath,
                credentialsManager
            );
        } catch (ResolverException e) {
            throw new Saml2Exception("Error initializing metadata resolver", e);
        }
    }

    private static class RelyingPartyRegistrationMetadataResolver extends HTTPMetadataResolver {

        private final String idpEntityId;
        private final String registrationId;
        private final Saml2CredentialsManager credentialsManager;
        private final String loginProcessingUrl;
        private final String entityIdPath;

        private RelyingPartyRegistration registration;

        public RelyingPartyRegistrationMetadataResolver(
            HttpClient client,
            String idpEntityId,
            String idpMetadataUrl,
            String registrationId,
            String loginProcessingUrl,
            String entityIdPath,
            Saml2CredentialsManager credentialsManager
        ) throws ResolverException {
            super(client, idpMetadataUrl);
            this.idpEntityId = idpEntityId;
            this.registrationId = registrationId;
            this.loginProcessingUrl = loginProcessingUrl;
            this.entityIdPath = entityIdPath;
            this.credentialsManager = credentialsManager;

            this.credentialsManager.onUpdate(() -> {
                    if (!isInitialized()) {
                        initialize();
                    }

                    prepareRegistration();
                });

            setRequireValidMetadata(true);
            setId(idpEntityId);
            setParserPool(XMLObjectProviderRegistrySupport.getParserPool());
        }

        public RelyingPartyRegistration getRegistration() {
            if (registration == null) {
                throw new Saml2Exception("No metadata loaded right now. Maybe the IDP is not available?");
            }

            return registration;
        }

        @Override
        public synchronized void refresh() throws ResolverException {
            super.refresh();

            if (isInitialized()) {
                prepareRegistration();
            }
        }

        void prepareRegistration() throws ResolverException {
            EntityDescriptor descriptor = resolveSingle(new CriteriaSet(new EntityIdCriterion(idpEntityId)));

            registration = parseDescriptor(descriptor);
        }

        private RelyingPartyRegistration parseDescriptor(EntityDescriptor descriptor) {
            return withRegistrationId(registrationId)
                .entityId("{baseUrl}" + entityIdPath)
                .assertionConsumerServiceBinding(Saml2MessageBinding.POST)
                .assertionConsumerServiceLocation("{baseUrl}" + loginProcessingUrl)
                .decryptionX509Credentials(credentials ->
                    credentials.addAll(credentialsManager.getCredentials(Saml2X509CredentialType.DECRYPTION))
                )
                .assertingPartyDetails(builder ->
                    builder
                        .entityId(descriptor.getEntityID())
                        .singleSignOnServiceBinding(Saml2MessageBinding.REDIRECT)
                        .singleSignOnServiceLocation(getSingleSignOnLocation(descriptor))
                        .wantAuthnRequestsSigned(wantsAuthnRequestSigned(descriptor))
                        .verificationX509Credentials(certificates -> certificates.addAll(getSigningKeys(descriptor)))
                )
                .build();
        }

        private Boolean wantsAuthnRequestSigned(EntityDescriptor descriptor) {
            return Objects.equals(Boolean.TRUE, idpSsoDescritpor(descriptor).getWantAuthnRequestsSigned());
        }

        private List<Saml2X509Credential> getSigningKeys(EntityDescriptor descriptor) {
            return idpSsoDescritpor(descriptor) //
                .getKeyDescriptors()
                .stream()
                .filter(this::isSigningKey)
                .map(KeyDescriptor::getKeyInfo)
                .flatMap(this::parseKeyInfo)
                .map(Saml2X509Credential::verification)
                .toList();
        }

        private Stream<X509Certificate> parseKeyInfo(KeyInfo keyInfo) throws Saml2Exception {
            try {
                return KeyInfoSupport.getCertificates(keyInfo).stream();
            } catch (CertificateException e) {
                throw new Saml2Exception("Error parsing certificates", e);
            }
        }

        private boolean isSigningKey(KeyDescriptor keyDescriptor) {
            return (
                keyDescriptor.getUse() == null ||
                keyDescriptor.getUse().equals(UsageType.SIGNING) ||
                keyDescriptor.getUse().equals(UsageType.UNSPECIFIED)
            );
        }

        private String getSingleSignOnLocation(EntityDescriptor descriptor) {
            return idpSsoDescritpor(descriptor)
                .getSingleSignOnServices() //
                .stream()
                .filter(service -> Objects.equals(service.getBinding(), SAMLConstants.SAML2_REDIRECT_BINDING_URI))
                .findAny()
                .map(Endpoint::getLocation)
                .orElseThrow(() -> new Saml2Exception("No SingleSignOnLocation for Redirect binding found"));
        }

        private IDPSSODescriptor idpSsoDescritpor(EntityDescriptor descriptor) throws Saml2Exception {
            IDPSSODescriptor idpssoDescriptor = descriptor.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);

            if (idpssoDescriptor == null) {
                throw new Saml2Exception(
                    String.format("No IdpSsoDescriptor for Saml 2.0 Protocol found. [%s]", descriptor.getEntityID())
                );
            }

            return idpssoDescriptor;
        }
    }
}
