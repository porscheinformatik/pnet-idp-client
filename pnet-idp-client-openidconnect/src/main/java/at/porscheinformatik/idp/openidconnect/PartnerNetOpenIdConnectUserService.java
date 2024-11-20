/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import at.porscheinformatik.idp.openidconnect.convert.PartnerNetClaimTypeConverterFactory;
import java.util.HashSet;
import java.util.Set;
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
        Set<String> accessibleScopes = new HashSet<>();
        accessibleScopes.add("openid");

        setAccessibleScopes(accessibleScopes);
        setClaimTypeConverterFactory(new PartnerNetClaimTypeConverterFactory());
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser user = super.loadUser(userRequest);

        return new PartnerNetOpenIdConnectUser(user.getAuthorities(), user.getIdToken(), user.getUserInfo());
    }
}
