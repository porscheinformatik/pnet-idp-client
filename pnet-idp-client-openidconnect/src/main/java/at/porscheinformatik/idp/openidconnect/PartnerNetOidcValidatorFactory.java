/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import java.util.function.Function;

import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetOidcValidatorFactory implements Function<ClientRegistration, OAuth2TokenValidator<Jwt>>
{

    @Override
    public OAuth2TokenValidator<Jwt> apply(ClientRegistration clientRegistration)
    {
        return new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(),
            new OidcIdTokenValidator(clientRegistration), new OidcIssuerValidator(clientRegistration));
    }

}
