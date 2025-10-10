/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Customized implementation of Spring Securities default request resolver, that
 * allows for a bit more flexibility.
 *
 * @author Daniel Furtlehner
 */
public class PartnerNetOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    public static final String ACR_PARAM = "acr";
    public static final String MAX_AGE_PARAM = "max_age";
    public static final String MAX_AGE_ADDITIONAL_PARAM = MAX_AGE_PARAM;
    public static final String MAX_AGE_MFA_PARAM = "max_age_mfa";
    public static final String MAX_AGE_MFA_ADDITIONAL_PARAM = MAX_AGE_MFA_PARAM;
    public static final String TENANT_PARAM = "tenant";
    public static final String TENANT_ADDITIONAL_PARAM = TENANT_PARAM;
    public static final String PRESELECT_TENANT_PARAM = "preselect_tenant";
    public static final String PRESELECT_TENANT_ADDITIONAL_PARAM = PRESELECT_TENANT_PARAM;
    public static final String PROMPT_PARAM = "prompt";
    public static final String PROMPT_ADDITIONAL_PARAM = PROMPT_PARAM;
    public static final String LOGIN_HINT_PARAM = "login_hint";
    public static final String LOGIN_HINT_ADDITIONAL_PARAM = LOGIN_HINT_PARAM;
    public static final String CUSTOM_STATE = "custom_state";

    private final OAuth2AuthorizationRequestResolver defaultAuthorizationRequestResolver;

    public static UriComponentsBuilder requestNistAuthenticationLevels(
        UriComponentsBuilder uri,
        int... nistAuthenticationLevel
    ) {
        for (int nistLevel : nistAuthenticationLevel) {
            uri = uri.queryParam(ACR_PARAM, Objects.toString(nistLevel));
        }

        return uri;
    }

    public static UriComponentsBuilder forceAuthentication(UriComponentsBuilder uri) {
        return requestMaxAge(uri, 0);
    }

    public static UriComponentsBuilder requestMaxAge(UriComponentsBuilder uri, int maxAge) {
        return uri.queryParam(MAX_AGE_PARAM, maxAge);
    }

    public static UriComponentsBuilder requestMaxAgeMfa(UriComponentsBuilder uri, int maxAgeMfa) {
        return uri.queryParam(MAX_AGE_MFA_PARAM, maxAgeMfa);
    }

    public static UriComponentsBuilder requestTenant(UriComponentsBuilder uri, String tenant) {
        return uri.queryParam(TENANT_PARAM, tenant);
    }

    public static UriComponentsBuilder requestPrompt(UriComponentsBuilder uri, String prompt) {
        return uri.queryParam(PROMPT_PARAM, prompt);
    }

    public static UriComponentsBuilder requestLoginHint(UriComponentsBuilder uri, String loginHint) {
        return uri.queryParam(LOGIN_HINT_PARAM, loginHint);
    }

    public static UriComponentsBuilder requestPreselectTenant(UriComponentsBuilder uri, String tenant) {
        return uri.queryParam(PRESELECT_TENANT_PARAM, tenant);
    }

    public static UriComponentsBuilder requestCustomState(UriComponentsBuilder uri, String customState) {
        return uri.queryParam(CUSTOM_STATE, customState);
    }

    public PartnerNetOAuth2AuthorizationRequestResolver(
        ClientRegistrationRepository clientRegistrationRepository,
        String authorizationRequestBaseUri
    ) {
        super();
        defaultAuthorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            authorizationRequestBaseUri
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultAuthorizationRequestResolver.resolve(request);

        return resolve(request, authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultAuthorizationRequestResolver.resolve(
            request,
            registrationId
        );

        return resolve(request, authorizationRequest);
    }

    private OAuth2AuthorizationRequest resolve(
        HttpServletRequest request,
        OAuth2AuthorizationRequest authorizationRequest
    ) {
        if (authorizationRequest == null) {
            return null;
        }

        Map<String, Object> additionalParameters = new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
        Map<String, Object> attributes = new LinkedHashMap<>(authorizationRequest.getAttributes());

        addRequestedAcrParameter(request, additionalParameters, attributes);
        addRequestedMaxAge(request, additionalParameters, attributes);
        addRequestedMaxAgeMfa(request, additionalParameters, attributes);
        addRequestedTenant(request, additionalParameters, attributes);
        addRequestedPreselectTenant(request, additionalParameters, attributes);
        addRequestedPrompt(request, additionalParameters, attributes);
        addRequestedLoginHint(request, additionalParameters, attributes);

        String state = PartnerNetOpenIdConnectStateUtils.buildState(
            //
            authorizationRequest.getState(),
            request.getParameter(CUSTOM_STATE)
        );

        return OAuth2AuthorizationRequest.from(authorizationRequest) //
            .scope(OidcScopes.OPENID)
            .state(state)
            .additionalParameters(additionalParameters)
            .attributes(attributes)
            .build();
    }

    private void addRequestedLoginHint(
        HttpServletRequest request,
        Map<String, Object> additionalParameters,
        Map<String, Object> attributes
    ) {
        String loginHint = request.getParameter(LOGIN_HINT_PARAM);

        if (loginHint == null || loginHint.isEmpty()) {
            return;
        }

        attributes.put(LOGIN_HINT_PARAM, loginHint);
        additionalParameters.put(LOGIN_HINT_ADDITIONAL_PARAM, loginHint);
    }

    private void addRequestedPrompt(
        HttpServletRequest request,
        Map<String, Object> additionalParameters,
        Map<String, Object> attributes
    ) {
        String prompt = request.getParameter(PROMPT_PARAM);

        if (prompt == null || prompt.isEmpty()) {
            return;
        }

        attributes.put(PROMPT_PARAM, prompt);
        additionalParameters.put(PROMPT_ADDITIONAL_PARAM, prompt);
    }

    private void addRequestedTenant(
        HttpServletRequest request,
        Map<String, Object> additionalParameters,
        Map<String, Object> attributes
    ) {
        String tenant = request.getParameter(TENANT_PARAM);

        if (tenant == null) {
            return;
        }

        attributes.put(TENANT_PARAM, tenant);
        additionalParameters.put(TENANT_ADDITIONAL_PARAM, tenant);
    }

    private void addRequestedPreselectTenant(
        HttpServletRequest request,
        Map<String, Object> additionalParameters,
        Map<String, Object> attributes
    ) {
        String tenant = request.getParameter(PRESELECT_TENANT_PARAM);

        if (tenant == null) {
            return;
        }

        attributes.put(PRESELECT_TENANT_PARAM, tenant);
        additionalParameters.put(PRESELECT_TENANT_ADDITIONAL_PARAM, tenant);
    }

    private void addRequestedMaxAge(
        HttpServletRequest request,
        Map<String, Object> additionalParameters,
        Map<String, Object> attributes
    ) {
        String maxAge = request.getParameter(MAX_AGE_PARAM);

        if (maxAge == null) {
            return;
        }

        attributes.put(MAX_AGE_PARAM, maxAge);
        additionalParameters.put(MAX_AGE_ADDITIONAL_PARAM, maxAge);
    }

    private void addRequestedMaxAgeMfa(
        HttpServletRequest request,
        Map<String, Object> additionalParameters,
        Map<String, Object> attributes
    ) {
        String maxAgeMfa = request.getParameter(MAX_AGE_MFA_PARAM);

        if (maxAgeMfa == null) {
            return;
        }

        attributes.put(MAX_AGE_MFA_PARAM, maxAgeMfa);
        additionalParameters.put(MAX_AGE_MFA_ADDITIONAL_PARAM, maxAgeMfa);
    }

    private void addRequestedAcrParameter(
        HttpServletRequest request,
        Map<String, Object> additionalParameters,
        Map<String, Object> attributes
    ) {
        String[] acr = request.getParameterValues(ACR_PARAM);

        if (acr == null) {
            return;
        }

        attributes.put(ACR_PARAM, Arrays.asList(acr));
        additionalParameters.put("claims", buildAcrRequest(acr));
    }

    private String buildAcrRequest(String[] acrs) {
        String acr = String.join(",", acrs);

        return String.format("{\"id_token\":{\"acr\": {\"values\": [\"%s\"], \"essential\": true}}}", acr);
    }
}
