/**
 *
 */
package at.porscheinformatik.pnet.idp.clientshowcase.security;

import at.porscheinformatik.idp.openidconnect.EnablePartnerNetOpenIdConnect;
import at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectConfigurer;
import at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectProvider;
import at.porscheinformatik.idp.saml2.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import java.util.List;

/**
 * @author Daniel Furtlehner
 */
@Configuration
@EnableWebSecurity
@EnablePartnerNetOpenIdConnect
@EnablePartnerNetSaml2
@EnableScheduling
public class ClientShowcaseSecurityConfig
{
    private static final Profiles PROD = Profiles.of("prod");
    private static final Profiles QA = Profiles.of("qa");
    private static final Profiles DEV = Profiles.of("dev");
    private static final Profiles LOCAL = Profiles.of("local");

    @Bean
    public AuthenticationManager authenticationManager(List<AuthenticationProvider> providers)
    {
        /*
         * To get rid of the default AuthenticationManager registered by spring boot, that uses a auto generated
         * password
         * visible in the logs, we register our own AuthenticationManager.
         *
         * If no providers are registered, we register a dummy provider that does nothing.
         * If custom authentication mechanisms are registered, they have to register a authentication provider, or
         * handle the authentication
         * on their own.
         */
        if (providers.isEmpty())
        {
            providers = List.of(new NoopAuthenticationProvider());
        }

        return new ProviderManager(providers);
    }

    @Bean
    public Saml2CredentialsManager saml2CredentialsManager(Saml2CredentialsProperties samlCredentialsConfig)
    {
        return new DefaultSaml2CredentialsManager(samlCredentialsConfig);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, Environment environment,
        Saml2CredentialsManager saml2CredentialsManager) throws Exception
    {
        if (environment.acceptsProfiles(LOCAL))
        {
            http.headers(customizer -> {
                customizer.httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable);

                // We explicitly allow framing for this application for testing purposes only, because the Partner.Net
                // Portal allows some applications to be displayed in a frame. By default, you should keep framing
                // restricted.
                customizer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable);
            });
        }

        http
                .with(new PartnerNetOpenIdConnectConfigurer(getPartnerNetOidcProvider(environment))
                .clientId(environment.getProperty("oidc.client.id"))
                                .clientSecret(environment.getProperty("oidc.client.secret")),
                        customizer -> customizer.customize(oauth -> oauth.failureUrl("/loginerror")));

        PartnerNetSaml2Configurer
            .apply(http, getPartnerNetSaml2Provider(environment))
            .credentials(saml2CredentialsManager)
            .customizer(saml2 -> saml2.failureUrl("/loginerror"));

        http.logout(logout -> {
            logout.logoutSuccessUrl("/");
            logout.deleteCookies("JSESSIONID");
        });

        // Disable the matchingRequestParameter optimization until the following bug is fixed: https://github.com/spring-projects/spring-security/issues/12665
        http.requestCache(cache -> {
            HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
            requestCache.setMatchingRequestParameterName(null);
            cache.requestCache(requestCache);
        });

        http.exceptionHandling(customizer -> {
            customizer.accessDeniedPage("/accessdenied");
            customizer.authenticationEntryPoint(new DecidingAuthenticationEntryPoint());
        });

        http.authorizeHttpRequests(customizer ->
                customizer.requestMatchers("/", "/accessdenied", "/logoutinfo/**", "/logout/**", "/loginerror", "/error", "/favicon.ico")
                        .permitAll()
                        .requestMatchers("/data/authorization")
                        .fullyAuthenticated()
                        .anyRequest()
                        .denyAll());

        http.requiresChannel(customizer -> customizer.anyRequest().requiresSecure());

        return http.build();
    }

    private PartnerNetOpenIdConnectProvider getPartnerNetOidcProvider(Environment environment)
    {
        if (environment.acceptsProfiles(PROD))
        {
            return PartnerNetOpenIdConnectProvider.PROD;
        }

        if (environment.acceptsProfiles(QA))
        {
            return PartnerNetOpenIdConnectProvider.QA;
        }

        if (environment.acceptsProfiles(DEV))
        {
            return PartnerNetOpenIdConnectProvider.DEV;
        }

        if (environment.acceptsProfiles(LOCAL))
        {
            return PartnerNetOpenIdConnectProvider.LOCAL;
        }

        throw new IllegalArgumentException("No supported profile found.");
    }

    private PartnerNetSaml2Provider getPartnerNetSaml2Provider(Environment environment)
    {
        if (environment.acceptsProfiles(PROD))
        {
            return PartnerNetSaml2Provider.PROD;
        }

        if (environment.acceptsProfiles(QA))
        {
            return PartnerNetSaml2Provider.QA;
        }

        if (environment.acceptsProfiles(DEV))
        {
            return PartnerNetSaml2Provider.DEV;
        }

        if (environment.acceptsProfiles(LOCAL))
        {
            return PartnerNetSaml2Provider.LOCAL;
        }

        throw new IllegalArgumentException("No supported profile found.");
    }
}
