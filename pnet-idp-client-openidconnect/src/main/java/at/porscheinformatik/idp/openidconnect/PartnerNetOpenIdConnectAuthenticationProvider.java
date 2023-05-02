/**
 * 
 */
package at.porscheinformatik.idp.openidconnect;

import java.time.Duration;
import java.time.Instant;
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
    public static final Duration CLOCK_SKEW = Duration.ofMinutes(5);

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
        Integer requestedMaxAge = getRequestedMaxAge(authentication);

        OAuth2LoginAuthenticationToken openIdAuthentication =
            (OAuth2LoginAuthenticationToken) super.authenticate(authentication);

        validateAcrValues(requestedAcrValues, openIdAuthentication);
        validateMaxAge(requestedMaxAge, openIdAuthentication);

        return openIdAuthentication;
    }

    private void validateMaxAge(Integer requestedMaxAge, OAuth2LoginAuthenticationToken openIdAuthentication)
    {
        if (requestedMaxAge == null)
        {
            return;
        }

        OidcUser user = (OidcUser) openIdAuthentication.getPrincipal();

        if (user.getAuthenticatedAt() == null)
        {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_id_token", "auth_time claim is required when max_age was specified", null));
        }

        Instant expiration = user.getAuthenticatedAt().plus(CLOCK_SKEW).plusSeconds(requestedMaxAge);

        if (expiration.isBefore(Instant.now()))
        {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_id_token", "max_age exceeded", null));
        }
    }

    private Integer getRequestedMaxAge(Authentication authentication)
    {
        OAuth2LoginAuthenticationToken authorizationCodeAuthentication =
            (OAuth2LoginAuthenticationToken) authentication;

        OAuth2AuthorizationRequest authorizationRequest =
            authorizationCodeAuthentication.getAuthorizationExchange().getAuthorizationRequest();

        Object maxAge = authorizationRequest.getAttribute(PartnerNetOAuth2AuthorizationRequestResolver.MAX_AGE_PARAM);

        if (maxAge == null)
        {
            return null;
        }

        if (maxAge instanceof Integer)
        {
            return (Integer) maxAge;
        }

        if (maxAge instanceof String)
        {
            return Integer.valueOf((String) maxAge);
        }

        throw new IllegalArgumentException("maxAge must be an Integer or a String");
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
