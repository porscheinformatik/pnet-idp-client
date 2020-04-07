/**
 * 
 */
package at.porscheinformatik.idp.openidconnect;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * There is no easy way to add custom ID Token validation based on the authorizationrequest.
 * https://github.com/spring-projects/spring-security/issues/8342 So we add our own custom authentication provider, that
 * does the validation afterwards.
 * 
 * @author Daniel Furtlehner
 */
public class PartnerNetOpenIdConnectAuthenticationProvider extends OidcAuthorizationCodeAuthenticationProvider
{

    public PartnerNetOpenIdConnectAuthenticationProvider(
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
        OAuth2UserService<OidcUserRequest, OidcUser> userService)
    {
        super(accessTokenResponseClient, userService);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        Collection<String> requestedAcrValues = getRequestedAcrValues(authentication);

        OAuth2LoginAuthenticationToken openIdAuthentication =
            (OAuth2LoginAuthenticationToken) super.authenticate(authentication);

        validateAcrValues(requestedAcrValues, openIdAuthentication);

        return openIdAuthentication;
    }

    private void validateAcrValues(Collection<String> requestedAcrValues,
        OAuth2LoginAuthenticationToken openIdAuthentication)
    {
        if (requestedAcrValues.isEmpty())
        {
            return;
        }

        OidcUser user = (OidcUser) openIdAuthentication.getPrincipal();

        String audienceContextClass = user.getAuthenticationContextClass();

        if (!requestedAcrValues.contains(audienceContextClass))
        {
            OAuth2Error oauth2Error = new OAuth2Error("invalid_acr");
            throw new OAuth2AuthenticationException(oauth2Error,
                String.format("Requested acrs %s. Response acr %s", requestedAcrValues, audienceContextClass));
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<String> getRequestedAcrValues(Authentication authentication)
    {
        OAuth2LoginAuthenticationToken authorizationCodeAuthentication =
            (OAuth2LoginAuthenticationToken) authentication;

        OAuth2AuthorizationRequest authorizationRequest =
            authorizationCodeAuthentication.getAuthorizationExchange().getAuthorizationRequest();

        Object acrValues = authorizationRequest.getAttribute(PartnerNetOAuth2AuthorizationRequestResolver.ACR_PARAM);

        if (acrValues == null)
        {
            return Collections.emptyList();
        }

        return (Collection<String>) acrValues;
    }

}
