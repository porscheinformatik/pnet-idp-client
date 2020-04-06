/**
 * 
 */
package at.porscheinformatik.idp.openidconnect;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetOpenIdConnectConfigurer
    extends AbstractHttpConfigurer<PartnerNetOpenIdConnectConfigurer, HttpSecurity>
{
    private final String issuerUrl;

    private boolean failOnStartup;
    private String clientId;
    private String clientSecret;

    public PartnerNetOpenIdConnectConfigurer(PartnerNetOpenIdConnectProvider provider)
    {
        this(provider.getIssuer());
    }

    public PartnerNetOpenIdConnectConfigurer(String issuerUrl)
    {
        super();

        this.issuerUrl = issuerUrl;
    }

    /**
     * When called, that application will fail to start, when the metadata of the provider could not be loaded.
     * Otherwise it will gracefully start, and try to load the metadata until the metadata could be fetched.
     * 
     * @return the builder for a fluent api
     */
    public PartnerNetOpenIdConnectConfigurer failOnStartup()
    {
        failOnStartup = true;

        return this;
    }

    public PartnerNetOpenIdConnectConfigurer clientId(String clientId)
    {
        this.clientId = clientId;

        return this;
    }

    public PartnerNetOpenIdConnectConfigurer clientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;

        return this;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception
    {
        builder.oauth2Login(oauth2Login -> {
            oauth2Login.clientRegistrationRepository(getClientRegistrationRepository());
            oauth2Login.userInfoEndpoint(userInfoEndpoint -> {
                userInfoEndpoint.oidcUserService(new PartnerNetOpenIdConnectUserService());
            });
        });
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception
    {
        // Nothing to do here
    }

    private ClientRegistrationRepository getClientRegistrationRepository()
    {
        if (failOnStartup)
        {
            ClientRegistration clientRegistration = ClientRegistrations
                .fromOidcIssuerLocation(issuerUrl)
                .registrationId("pnet")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientName(issuerUrl)
                .build();

            return new InMemoryClientRegistrationRepository(clientRegistration);
        }

        return new LazyLoadingClientRegistrationRepository(issuerUrl, "pnet", clientId, clientSecret);
    }
}
