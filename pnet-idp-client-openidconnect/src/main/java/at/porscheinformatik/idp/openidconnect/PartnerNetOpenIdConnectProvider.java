/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;

/**
 * @author Daniel Furtlehner
 */
public enum PartnerNetOpenIdConnectProvider
{

    PROD
    {
        @Override
        public String getIssuer()
        {
            return "https://identity.auto-partner.net/identity";
        }
    },
    QA
    {
        @Override
        public String getIssuer()
        {
            return "https://qa-identity.auto-partner.net/identity";
        }
    },
    DEV
    {
        @Override
        public String getIssuer()
        {
            return "https://pnet-identity-web-app-dev.nonprod1.ocp.porscheinformatik.cloud/identity";
        }
    },
    LOCAL
    {
        @Override
        public String getIssuer()
        {
            return "https://localhost:5443/identity";
        }
    };

    /**
     * Create a new {@link org.springframework.security.oauth2.client.registration.ClientRegistration.Builder
     * ClientRegistration.Builder} pre-configured with provider defaults.
     *
     * @return a builder instance
     */
    public ClientRegistration.Builder getBuilder()
    {
        return ClientRegistrations.fromOidcIssuerLocation(getIssuer()).registrationId("pnet");
    }

    /**
     * Get the issuer url of the provider
     *
     * @return the issuer
     */
    public abstract String getIssuer();
}
