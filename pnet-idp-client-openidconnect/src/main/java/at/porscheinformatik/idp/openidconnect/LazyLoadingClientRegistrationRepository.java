/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import static java.util.Objects.*;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ClientSettings;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;

/**
 * @author Daniel Furtlehner
 */
public class LazyLoadingClientRegistrationRepository implements ClientRegistrationRepository {

    private static final Logger LOG = LoggerFactory.getLogger(LazyLoadingClientRegistrationRepository.class);

    private final String issuerUrl;
    private final String registrationId;
    private final String clientId;
    private final String clientSecret;
    private final boolean requireProofKey;

    private ClientRegistration registration;

    public LazyLoadingClientRegistrationRepository(
        String issuerUrl,
        String registrationId,
        String clientId,
        String clientSecret,
        boolean requireProofKey
    ) {
        this.issuerUrl = issuerUrl;
        this.registrationId = registrationId;
        this.clientId = requireNonNull(clientId, "Client Id must not be null");
        this.clientSecret = clientSecret;
        this.requireProofKey = requireProofKey;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        if (!Objects.equals(registrationId, this.registrationId)) {
            return null;
        }

        if (registration == null) {
            tryToLoadRegistration();
        }

        return registration;
    }

    private void tryToLoadRegistration() {
        ClientSettings settings = ClientSettings.builder().requireProofKey(requireProofKey).build();

        try {
            registration = ClientRegistrations.fromOidcIssuerLocation(issuerUrl)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientSettings(settings)
                .registrationId(registrationId)
                .build();
        } catch (Exception e) {
            LOG.error("Could not fetch client registration for Open ID Connect. Trying again on next call.", e);
        }
    }
}
