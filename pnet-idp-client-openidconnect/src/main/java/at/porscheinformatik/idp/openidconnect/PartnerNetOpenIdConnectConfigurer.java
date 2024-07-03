/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import java.util.Objects;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
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
    private Customizer<OAuth2LoginConfigurer<HttpSecurity>> customizer = oauth2Login -> {
        // Noop customizer. Users can override this to add custom configurations
    };

    private OidcUserService userService = new PartnerNetOpenIdConnectUserService();

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

    public PartnerNetOpenIdConnectConfigurer userService(OidcUserService userService)
    {
        this.userService = userService;

        return this;
    }

    /**
     * Add a customizer that allows you to further customize the Spring Securities {@link OAuth2LoginConfigurer}. This
     * is equivalent to calling {@link HttpSecurity#oauth2Login(Customizer)} with the advantage of having the default
     * Partner.Net configuration applied. This customizer is called at the very end of the Partner.Net specific
     * configuration. So you can override configurations applied by the Partner.Net configurer.
     *
     * @param customizer the customizer to use
     * @return the builder for a fluent api
     */
    public PartnerNetOpenIdConnectConfigurer customize(Customizer<OAuth2LoginConfigurer<HttpSecurity>> customizer)
    {
        this.customizer = Objects.requireNonNull(customizer, "Customizer must not be null");

        return this;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception
    {
        final ClientRegistrationRepository clientRegistrationRepository = getClientRegistrationRepository();
        final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient =
            new DefaultAuthorizationCodeTokenResponseClient();

        builder
            .authenticationProvider(
                new PartnerNetOpenIdConnectAuthenticationProvider(accessTokenResponseClient, userService));

        builder.oauth2Login(oauth2Login -> {
            oauth2Login.clientRegistrationRepository(clientRegistrationRepository);

            oauth2Login.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
                .authorizationRequestResolver(
                    new PartnerNetOAuth2AuthorizationRequestResolver(clientRegistrationRepository,
                        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI)));

            oauth2Login.tokenEndpoint(tokenEnpoint -> tokenEnpoint.accessTokenResponseClient(accessTokenResponseClient));

            oauth2Login.userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.oidcUserService(userService));

            // Let users add custom configurations if they want to
            customizer.customize(oauth2Login);
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
