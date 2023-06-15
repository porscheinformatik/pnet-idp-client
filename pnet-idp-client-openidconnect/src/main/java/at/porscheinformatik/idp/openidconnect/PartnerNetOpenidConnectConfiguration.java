/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;

/**
 * @author Daniel Furtlehner
 */
@Configuration
public class PartnerNetOpenidConnectConfiguration
{

    @Bean
    public JwtDecoderFactory<ClientRegistration> jwtDecoderFactory()
    {
        OidcIdTokenDecoderFactory factory = new OidcIdTokenDecoderFactory();
        factory.setJwtValidatorFactory(new PartnerNetOidcValidatorFactory());
        return factory;
    }
}
