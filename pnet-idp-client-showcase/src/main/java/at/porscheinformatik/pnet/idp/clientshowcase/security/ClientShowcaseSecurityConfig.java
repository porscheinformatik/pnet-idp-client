/**
 * 
 */
package at.porscheinformatik.pnet.idp.clientshowcase.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

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
public class ClientShowcaseSecurityConfig extends WebSecurityConfigurerAdapter implements EnvironmentAware
{
    private static final Profiles PROD = Profiles.of("prod");
    private static final Profiles QA = Profiles.of("qa");
    private static final Profiles DEV = Profiles.of("dev");
    private static final Profiles LOCAL = Profiles.of("local");

    private Environment environment;

    @Autowired
    private Saml2CredentialsProperties samlCredentialsConfig;

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        if (environment.acceptsProfiles(LOCAL))
        {
            http.headers().httpStrictTransportSecurity().disable();
        }

        http
            .apply(new PartnerNetOpenIdConnectConfigurer(getPartnerNetOidcProvider())
                .clientId(environment.getProperty("oidc.client.id"))
                .clientSecret(environment.getProperty("oidc.client.secret")));

        PartnerNetSaml2Configurer
            .apply(http, getPartnerNetSaml2Provider(), false)
            .credentials(samlCredentialsConfig)
            .failureUrl("/loginerror");

        http.logout(logout -> {
            logout.logoutSuccessUrl("/logoutinfo");
            logout.deleteCookies("JSESSIONID");
        });

        http.exceptionHandling().authenticationEntryPoint(new DecidingAuthenticationEntryPoint());

        http //
            .authorizeRequests()
            .antMatchers("/logoutinfo/**", "/logout/**", "/loginerror")
            .permitAll()
            .antMatchers("/**")
            .fullyAuthenticated();
        http.requiresChannel().anyRequest().requiresSecure();
    }

    private PartnerNetOpenIdConnectProvider getPartnerNetOidcProvider()
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

    private PartnerNetSaml2Provider getPartnerNetSaml2Provider()
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

    @Override
    public void setEnvironment(Environment environment)
    {
        this.environment = environment;
    }
}
