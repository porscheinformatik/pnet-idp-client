/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import at.porscheinformatik.idp.openidconnect.convert.PartnerNetClaimTypeConverterFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetOpenIdConnectUserService extends OidcUserService {

    public PartnerNetOpenIdConnectUserService() {
        super();
        // Note: setAccessibleScopes was removed in Spring Security 7.x
        // Accessible scopes are now managed through the OAuth2 client configuration
        // The "openid" scope is typically included by default
        setClaimTypeConverterFactory(new PartnerNetClaimTypeConverterFactory());
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser user = super.loadUser(userRequest);

        return new PartnerNetOpenIdConnectUser(user.getAuthorities(), user.getIdToken(), user.getUserInfo());
    }
}
