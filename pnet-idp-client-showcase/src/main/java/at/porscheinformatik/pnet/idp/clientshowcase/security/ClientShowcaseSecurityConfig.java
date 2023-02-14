/**
 * 
 */
package at.porscheinformatik.pnet.idp.clientshowcase.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import at.porscheinformatik.idp.openidconnect.EnablePartnerNetOpenIdConnect;
import at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectConfigurer;
import at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectProvider;
import at.porscheinformatik.idp.saml2.EnablePartnerNetSaml2;
import at.porscheinformatik.idp.saml2.PartnerNetSaml2Configurer;
import at.porscheinformatik.idp.saml2.PartnerNetSaml2Provider;
import at.porscheinformatik.idp.saml2.Saml2CredentialsProperties;

/**
 * @author Daniel Furtlehner
 */
@Configuration
@EnableWebSecurity
@EnablePartnerNetOpenIdConnect
@EnablePartnerNetSaml2
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
         * To get rid of the default AuthenticationManager registered by spring boot, that uses a auto generated password
         * visible in the logs, we register our own AuthenticationManager.
         * 
         * If no providers are registered, we register a dummy provider that does nothing.
         * If custom authentication mechanisms are registered, they have to register a authentication provider, or handle the authentication
         * on their own.
         */
        if (providers.isEmpty())
        {
            providers = List.of(new NoopAuthenticationProvider());
        }

        return new ProviderManager(providers);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, Environment environment,
        Saml2CredentialsProperties samlCredentialsConfig) throws Exception
    {
        if (environment.acceptsProfiles(LOCAL))
        {
            http.headers().httpStrictTransportSecurity().disable();
        }

        http
            .apply(new PartnerNetOpenIdConnectConfigurer(getPartnerNetOidcProvider(environment))
                .clientId(environment.getProperty("oidc.client.id"))
                .clientSecret(environment.getProperty("oidc.client.secret")))
            .customize(oauth -> {
                oauth.failureUrl("/loginerror");
            });

        PartnerNetSaml2Configurer
            .apply(http, getPartnerNetSaml2Provider(environment))
            .credentials(samlCredentialsConfig)
            .customizer(saml2 -> {
                saml2.failureUrl("/loginerror");
            });

        http.logout(logout -> {
            logout.logoutSuccessUrl("/logoutinfo");
            logout.deleteCookies("JSESSIONID");
        });

        http
            .exceptionHandling()
            .accessDeniedPage("/accessdenied")
            .authenticationEntryPoint(new DecidingAuthenticationEntryPoint());

        http //
            .authorizeHttpRequests()
            .shouldFilterAllDispatcherTypes(true)
            .requestMatchers("/accessdenied", "/logoutinfo/**", "/logout/**", "/loginerror", "/error", "/favicon.ico")
            .permitAll()
            .requestMatchers("/data/authorization")
            .fullyAuthenticated()
            .anyRequest()
            .denyAll();

        http.requiresChannel().anyRequest().requiresSecure();

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
