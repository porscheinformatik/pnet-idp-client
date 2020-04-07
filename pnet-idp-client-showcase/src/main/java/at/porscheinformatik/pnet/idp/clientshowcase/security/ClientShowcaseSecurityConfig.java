/**
 * 
 */
package at.porscheinformatik.pnet.idp.clientshowcase.security;

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

/**
 * @author Daniel Furtlehner
 */
@Configuration
@EnableWebSecurity
@EnablePartnerNetOpenIdConnect
public class ClientShowcaseSecurityConfig extends WebSecurityConfigurerAdapter implements EnvironmentAware
{
    private static final Profiles PROD = Profiles.of("prod");
    private static final Profiles QA = Profiles.of("qa");
    private static final Profiles DEV = Profiles.of("dev");
    private static final Profiles LOCAL = Profiles.of("local");

    private Environment environment;

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        if (environment.acceptsProfiles(LOCAL))
        {
            http.headers().httpStrictTransportSecurity().disable();
        }

        http
            .apply(new PartnerNetOpenIdConnectConfigurer(getPartnerNetProvider())
                .clientId(environment.getProperty("oidc.client.id"))
                .clientSecret(environment.getProperty("oidc.client.secret")));

        http.logout(logout -> {
            logout.logoutSuccessUrl("/logoutinfo");
            logout.deleteCookies("JSESSIONID");
        });

        http.exceptionHandling().authenticationEntryPoint(new DecidingAuthenticationEntryPoint());

        http //
            .authorizeRequests()
            .antMatchers("/logoutinfo/**", "/logout/**")
            .permitAll()
            .antMatchers("/**")
            .fullyAuthenticated();
        http.requiresChannel().anyRequest().requiresSecure();
    }

    private PartnerNetOpenIdConnectProvider getPartnerNetProvider()
    {
        if (environment.acceptsProfiles(PROD))
        {
            return PartnerNetOpenIdConnectProvider.PROD;
        }

        if (environment.acceptsProfiles(QA))
        {
            return PartnerNetOpenIdConnectProvider.PROD;
        }

        if (environment.acceptsProfiles(DEV))
        {
            return PartnerNetOpenIdConnectProvider.PROD;
        }

        if (environment.acceptsProfiles(LOCAL))
        {
            return PartnerNetOpenIdConnectProvider.LOCAL;
        }

        throw new IllegalArgumentException("No supported profile found.");
    }

    @Override
    public void setEnvironment(Environment environment)
    {
        this.environment = environment;
    }
}
