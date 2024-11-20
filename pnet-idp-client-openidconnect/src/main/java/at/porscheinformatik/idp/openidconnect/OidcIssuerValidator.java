/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import java.util.Objects;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * @author Daniel Furtlehner
 */
public class OidcIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private final ClientRegistration clientRegistration;

    public OidcIssuerValidator(ClientRegistration clientRegistration) {
        super();
        this.clientRegistration = clientRegistration;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String issuer = (String) clientRegistration.getProviderDetails().getConfigurationMetadata().get("issuer");

        if (issuer == null) {
            return OAuth2TokenValidatorResult.failure(
                invalidIdToken("The issuer is missing in the client registration metadata")
            );
        }

        if (!Objects.equals(issuer, token.getIssuer().toExternalForm())) {
            return OAuth2TokenValidatorResult.failure(
                invalidIdToken("The issuer does not match the client registration issuer")
            );
        }

        return OAuth2TokenValidatorResult.success();
    }

    private static OAuth2Error invalidIdToken(String message) {
        return new OAuth2Error(
            "invalid_id_token",
            message,
            "https://openid.net/specs/openid-connect-core-1_0.html#IDTokenValidation"
        );
    }
}
