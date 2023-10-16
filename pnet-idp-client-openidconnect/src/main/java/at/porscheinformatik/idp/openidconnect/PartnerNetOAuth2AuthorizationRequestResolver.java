/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Customized implementation of Spring Securities default request resolver, that allows for a bit more flexibility.
 *
 * @author Daniel Furtlehner
 */
public class PartnerNetOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver
{
    public static final String ACR_PARAM = "acr";
    public static final String MAX_AGE_PARAM = "max_age";
    public static final String TENANT_PARAM = "tenant";
    public static final String PRESELECT_TENANT_PARAM = "preselect_tenant";
    public static final String CUSTOM_STATE = "custom_state";

    private final OAuth2AuthorizationRequestResolver defaultAuthorizationRequestResolver;

    public static UriComponentsBuilder requestNistAuthenticationLevels(UriComponentsBuilder uri,
        int... nistAuthenticationLevel)
    {
        for (int nistLevel : nistAuthenticationLevel)
        {
            uri = uri.queryParam(ACR_PARAM, Objects.toString(nistLevel));
        }

        return uri;
    }

    public static UriComponentsBuilder forceAuthentication(UriComponentsBuilder uri)
    {
        return requestMaxAge(uri, 0);
    }

    public static UriComponentsBuilder requestMaxAge(UriComponentsBuilder uri, int maxAge)
    {
        return uri.queryParam(MAX_AGE_PARAM, maxAge);
    }

    public static UriComponentsBuilder requestTenant(UriComponentsBuilder uri, String tenant)
    {
        return uri.queryParam(TENANT_PARAM, tenant);
    }

    public static UriComponentsBuilder requestPreselectTenant(UriComponentsBuilder uri, String tenant)
    {
        return uri.queryParam(PRESELECT_TENANT_PARAM, tenant);
    }

    public static UriComponentsBuilder requestCustomState(UriComponentsBuilder uri, String customState)
    {
        return uri.queryParam(CUSTOM_STATE, customState);
    }

    public PartnerNetOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
        String authorizationRequestBaseUri)
    {
        super();

        defaultAuthorizationRequestResolver =
            new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request)
    {
        OAuth2AuthorizationRequest authorizationRequest = defaultAuthorizationRequestResolver.resolve(request);

        return resolve(request, authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId)
    {
        OAuth2AuthorizationRequest authorizationRequest = defaultAuthorizationRequestResolver.resolve(request);

        return resolve(request, authorizationRequest);
    }

    private OAuth2AuthorizationRequest resolve(HttpServletRequest request,
        OAuth2AuthorizationRequest authorizationRequest)
    {
        if (authorizationRequest == null)
        {
            return null;
        }

        Map<String, Object> additionalParameters = new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
        Map<String, Object> attributes = new LinkedHashMap<>(authorizationRequest.getAttributes());

        addRequestedAcrParameter(request, additionalParameters, attributes);
        addRequestedMaxAge(request, additionalParameters, attributes);
        addRequestedTenant(request, additionalParameters, attributes);
        addRequestedPreselectTenant(request, additionalParameters, attributes);

        String state = PartnerNetOpenIdConnectStateUtils //
            .buildState(authorizationRequest.getState(), request.getParameter(CUSTOM_STATE));

        return OAuth2AuthorizationRequest
            .from(authorizationRequest) //
            .scope(OidcScopes.OPENID)
            .state(state)
            .additionalParameters(additionalParameters)
            .attributes(attributes)
            .build();
    }

    private void addRequestedTenant(HttpServletRequest request, Map<String, Object> additionalParameters,
        Map<String, Object> attributes)
    {
        String tenant = request.getParameter(TENANT_PARAM);

        if (tenant == null)
        {
            return;
        }

        attributes.put(TENANT_PARAM, tenant);
        additionalParameters.put("tenant", tenant); // not necessarily the same as TENANT_PARAM!
    }

    private void addRequestedPreselectTenant(HttpServletRequest request, Map<String, Object> additionalParameters,
        Map<String, Object> attributes)
    {
        String tenant = request.getParameter(PRESELECT_TENANT_PARAM);

        if (tenant == null)
        {
            return;
        }

        attributes.put(PRESELECT_TENANT_PARAM, tenant);
        additionalParameters.put("preselect_tenant", tenant); // not necessarily the same as PRESELECT_TENANT_PARAM!
    }

    private void addRequestedMaxAge(HttpServletRequest request, Map<String, Object> additionalParameters,
        Map<String, Object> attributes)
    {
        String maxAge = request.getParameter(MAX_AGE_PARAM);

        if (maxAge == null)
        {
            return;
        }

        attributes.put(MAX_AGE_PARAM, maxAge);
        additionalParameters.put("max_age", maxAge); // not necessarily the same as MAX_AGE_PARAM!
    }

    private void addRequestedAcrParameter(HttpServletRequest request, Map<String, Object> additionalParameters,
        Map<String, Object> attributes)
    {
        String[] acr = request.getParameterValues(ACR_PARAM);

        if (acr == null)
        {
            return;
        }

        attributes.put(ACR_PARAM, Arrays.asList(acr));
        additionalParameters.put("claims", buildAcrRequest(acr));
    }

    private String buildAcrRequest(String[] acrs)
    {
        String acr = Arrays.stream(acrs).collect(Collectors.joining(","));

        return String.format("{\"id_token\":{\"acr\": {\"values\": [\"%s\"], \"essential\": true}}}", acr);
    }
}
