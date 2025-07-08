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
 * <a href="https://github.com/spring-projects/spring-security/issues/8342">https://github.com/spring-projects/spring-security/issues/8342</a>
 * So we add our own custom authentication provider, that does the validation afterwards.
 *
 * @author Daniel Furtlehner
 */
public class PartnerNetOpenIdConnectAuthenticationProvider extends OidcAuthorizationCodeAuthenticationProvider {

    public static final Duration CLOCK_SKEW = Duration.ofMinutes(5);

    public PartnerNetOpenIdConnectAuthenticationProvider(
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
        OAuth2UserService<OidcUserRequest, OidcUser> userService
    ) {
        super(accessTokenResponseClient, userService);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Collection<String> requestedAcrValues = getRequestedAcrValues(authentication);
        Integer requestedMaxAge = getRequestedMaxAge(authentication);
        Integer requestedMaxAgeMfa = getRequestedMaxAgeMfa(authentication);
        String requestedTenant = getRequestedTenant(authentication);

        OAuth2LoginAuthenticationToken openIdAuthentication = (OAuth2LoginAuthenticationToken) super.authenticate(
            authentication
        );

        validateAcrValues(requestedAcrValues, openIdAuthentication);
        validateMaxAge(requestedMaxAge, openIdAuthentication);
        validateMaxAgeMfa(requestedMaxAgeMfa, openIdAuthentication);
        validateTenant(requestedTenant, openIdAuthentication);

        return openIdAuthentication;
    }

    private void validateMaxAge(Integer requestedMaxAge, OAuth2LoginAuthenticationToken openIdAuthentication) {
        OidcUser user = (OidcUser) openIdAuthentication.getPrincipal();
        Instant authenticatedAt = user.getAuthenticatedAt();

        System.err.println("Authenticated at: " + authenticatedAt);

        if (requestedMaxAge == null) {
            return;
        }

        if (authenticatedAt == null) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error(INVALID_ID_TOKEN, "auth_time claim is required when max_age was specified", null)
            );
        }

        Instant expiration = authenticatedAt.plus(CLOCK_SKEW).plusSeconds(requestedMaxAge);

        if (expiration.isBefore(Instant.now())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(INVALID_ID_TOKEN, "max_age exceeded", null));
        }
    }

    private void validateMaxAgeMfa(Integer requestedMaxAgeMfa, OAuth2LoginAuthenticationToken openIdAuthentication) {
        OidcUser user = (OidcUser) openIdAuthentication.getPrincipal();
        Instant mfaAuthenticatedAt = user.getClaimAsInstant(PartnerNetOpenIdConnectUser.ID_TOKEN_AUTH_TIME_MFA);

        if (requestedMaxAgeMfa == null) {
            return;
        }

        if (mfaAuthenticatedAt == null) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error(INVALID_ID_TOKEN, "auth_time_mfa claim is required when max_age_mfa was specified", null)
            );
        }

        Instant expiration = mfaAuthenticatedAt.plus(CLOCK_SKEW).plusSeconds(requestedMaxAgeMfa);

        if (expiration.isBefore(Instant.now())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(INVALID_ID_TOKEN, "max_age_mfa exceeded", null));
        }
    }

    private void validateTenant(String requestedTenant, OAuth2LoginAuthenticationToken openIdAuthentication) {
        if (requestedTenant == null) {
            return;
        }

        PartnerNetOpenIdConnectUser user = (PartnerNetOpenIdConnectUser) openIdAuthentication.getPrincipal();
        String tenant = user.getCountry();

        if (tenant == null) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error(INVALID_ID_TOKEN, "tenant claim is required when tenant was specified", null)
            );
        }

        if (!requestedTenant.equals(tenant)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(INVALID_ID_TOKEN, "invalid tenant", null));
        }
    }

    private Integer getRequestedMaxAge(Authentication authentication) {
        OAuth2LoginAuthenticationToken authorizationCodeAuthentication =
            (OAuth2LoginAuthenticationToken) authentication;

        OAuth2AuthorizationRequest authorizationRequest = authorizationCodeAuthentication
            .getAuthorizationExchange()
            .getAuthorizationRequest();

        Object maxAge = authorizationRequest.getAttribute(PartnerNetOAuth2AuthorizationRequestResolver.MAX_AGE_PARAM);

        if (maxAge == null) {
            return null;
        }

        if (maxAge instanceof Integer maxAgeInteger) {
            return maxAgeInteger;
        }

        if (maxAge instanceof String maxAgeString) {
            return Integer.valueOf(maxAgeString);
        }

        throw new IllegalArgumentException("maxAge must be an Integer or a String");
    }

    private Integer getRequestedMaxAgeMfa(Authentication authentication) {
        OAuth2LoginAuthenticationToken authorizationCodeAuthentication =
            (OAuth2LoginAuthenticationToken) authentication;

        OAuth2AuthorizationRequest authorizationRequest = authorizationCodeAuthentication
            .getAuthorizationExchange()
            .getAuthorizationRequest();

        Object maxAgeMfa = authorizationRequest.getAttribute(PartnerNetOAuth2AuthorizationRequestResolver.MAX_AGE_MFA_PARAM);

        if (maxAgeMfa == null) {
            return null;
        }

        if (maxAgeMfa instanceof Integer maxAgeMfaInteger) {
            return maxAgeMfaInteger;
        }

        if (maxAgeMfa instanceof String maxAgeMfaString) {
            return Integer.valueOf(maxAgeMfaString);
        }

        throw new IllegalArgumentException("maxAgeMfa must be an Integer or a String");
    }

    private String getRequestedTenant(Authentication authentication) {
        OAuth2LoginAuthenticationToken authorizationCodeAuthentication =
            (OAuth2LoginAuthenticationToken) authentication;

        OAuth2AuthorizationRequest authorizationRequest = authorizationCodeAuthentication
            .getAuthorizationExchange()
            .getAuthorizationRequest();

        Object tenant = authorizationRequest.getAttribute(PartnerNetOAuth2AuthorizationRequestResolver.TENANT_PARAM);

        if (tenant == null) {
            return null;
        }

        if (tenant instanceof String stringTenant) {
            return stringTenant;
        }

        throw new IllegalArgumentException("tenant must be a String");
    }

    private void validateAcrValues(
        Collection<String> requestedAcrValues,
        OAuth2LoginAuthenticationToken openIdAuthentication
    ) {
        if (requestedAcrValues.isEmpty()) {
            return;
        }

        OidcUser user = (OidcUser) openIdAuthentication.getPrincipal();

        String audienceContextClass = user.getAuthenticationContextClass();

        if (!requestedAcrValues.contains(audienceContextClass)) {
            OAuth2Error oauth2Error = new OAuth2Error("invalid_acr");
            throw new OAuth2AuthenticationException(
                oauth2Error,
                String.format("Requested acrs %s. Response acr %s", requestedAcrValues, audienceContextClass)
            );
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<String> getRequestedAcrValues(Authentication authentication) {
        OAuth2LoginAuthenticationToken authorizationCodeAuthentication =
            (OAuth2LoginAuthenticationToken) authentication;

        OAuth2AuthorizationRequest authorizationRequest = authorizationCodeAuthentication
            .getAuthorizationExchange()
            .getAuthorizationRequest();

        Object acrValues = authorizationRequest.getAttribute(PartnerNetOAuth2AuthorizationRequestResolver.ACR_PARAM);

        if (acrValues == null) {
            return Collections.emptyList();
        }

        return (Collection<String>) acrValues;
    }

    private static final String INVALID_ID_TOKEN = "invalid_id_token";
}
