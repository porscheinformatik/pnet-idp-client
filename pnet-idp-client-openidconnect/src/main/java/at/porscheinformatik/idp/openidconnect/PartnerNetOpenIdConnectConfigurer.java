/**
 * 
 */
package at.porscheinformatik.idp.openidconnect;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;

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
        final ClientRegistrationRepository clientRegistrationRepository = getClientRegistrationRepository();
        final PartnerNetOpenIdConnectUserService userService = new PartnerNetOpenIdConnectUserService();
        final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient =
            new DefaultAuthorizationCodeTokenResponseClient();

        builder
            .authenticationProvider(
                new PartnerNetOpenIdConnectAuthenticationProvider(accessTokenResponseClient, userService));

        builder.oauth2Login(oauth2Login -> {
            oauth2Login.clientRegistrationRepository(clientRegistrationRepository);

            oauth2Login.authorizationEndpoint(authorizationEndpoint -> {
                authorizationEndpoint
                    .authorizationRequestResolver(
                        new PartnerNetOAuth2AuthorizationRequestResolver(clientRegistrationRepository,
                            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI));
            });

            oauth2Login.tokenEndpoint(tokenEnpoint -> {
                tokenEnpoint.accessTokenResponseClient(accessTokenResponseClient);
            });

            oauth2Login.userInfoEndpoint(userInfoEndpoint -> {
                userInfoEndpoint.oidcUserService(userService);
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
