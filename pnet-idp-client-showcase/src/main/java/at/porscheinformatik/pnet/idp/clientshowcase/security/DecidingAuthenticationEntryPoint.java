/**
 *
 */
package at.porscheinformatik.pnet.idp.clientshowcase.security;

import at.porscheinformatik.idp.openidconnect.PartnerNetOAuth2AuthorizationRequestResolver;
import at.porscheinformatik.idp.saml2.Saml2Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This authentication entry point decides what authentication should be used
 * based on the "protocol" query parameter.
 * It also appends the additional parameters (max_age, tenant, etc.) to the
 * authentication request.
 */
public class DecidingAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        String uri = buildUri(request).toUriString();

        response.sendRedirect(uri);
    }

    private UriComponentsBuilder buildUri(HttpServletRequest request) {
        var protocol = getParameter(request, "protocol");
        if (protocol == null) {
            throw new IllegalArgumentException("Missing protocol parameter");
        }

        var requireMfa = getBooleanParameter(request, "require_mfa");
        var maxAge = getIntParameter(request, "max_age");
        var forceAuthentication = getBooleanParameter(request, "force_authentication");
        var maxAgeMfa = getIntParameter(request, "max_age_mfa");
        var tenant = getParameter(request, "tenant");
        var prompt = getParameter(request, "prompt");
        var loginHint = getParameter(request, "login_hint");

        return switch (protocol) {
            case "oidc" -> buildOidcPath(requireMfa, maxAge, forceAuthentication, maxAgeMfa, tenant, prompt, loginHint);
            case "saml2" -> buildSaml2Path(
                requireMfa,
                maxAge,
                forceAuthentication,
                maxAgeMfa,
                tenant,
                prompt,
                loginHint
            );
            default -> throw new IllegalArgumentException("Unsupported protocol " + protocol);
        };
    }

    private String getParameter(HttpServletRequest request, String paramName) {
        var param = request.getParameter(paramName);

        return !StringUtils.isEmpty(param) ? param : null;
    }

    private Integer getIntParameter(HttpServletRequest request, String paramName) {
        var param = getParameter(request, paramName);

        return param != null ? Integer.parseInt(param) : null;
    }

    private boolean getBooleanParameter(HttpServletRequest request, String paramName) {
        var param = getParameter(request, paramName);

        return param != null;
    }

    private UriComponentsBuilder buildSaml2Path(
        boolean requireMfa,
        Integer maxAge,
        boolean forceAuthentication,
        Integer maxAgeMfa,
        String tenant,
        String prompt,
        String loginHint
    ) {
        var builder = UriComponentsBuilder.fromPath("/saml2/authenticate/pnet");

        if (requireMfa) {
            Saml2Utils.requestNistAuthenticationLevel(builder, 3);
        }

        if (maxAge != null) {
            Saml2Utils.maxSessionAge(builder, maxAge);
        }

        if (forceAuthentication) {
            Saml2Utils.forceAuthentication(builder);
        }

        if (maxAgeMfa != null) {
            Saml2Utils.maxAgeMfa(builder, maxAgeMfa);
        }

        if (tenant != null) {
            Saml2Utils.requestTenant(builder, tenant.toUpperCase());
        }

        if (prompt != null) {
            Saml2Utils.requestPrompt(builder, prompt);
        }

        if (loginHint != null) {
            Saml2Utils.requestLoginHint(builder, loginHint);
        }

        return builder;
    }

    private UriComponentsBuilder buildOidcPath(
        boolean requireMfa,
        Integer maxAge,
        boolean forceAuthentication,
        Integer maxAgeMfa,
        String tenant,
        String prompt,
        String loginHint
    ) {
        var builder = UriComponentsBuilder.fromPath("/oauth2/authorization/pnet");

        if (requireMfa) {
            PartnerNetOAuth2AuthorizationRequestResolver.requestNistAuthenticationLevels(builder, 3);
        }

        if (forceAuthentication) {
            PartnerNetOAuth2AuthorizationRequestResolver.forceAuthentication(builder);
        } else if (maxAge != null) {
            PartnerNetOAuth2AuthorizationRequestResolver.requestMaxAge(builder, maxAge);
        }

        if (maxAgeMfa != null) {
            PartnerNetOAuth2AuthorizationRequestResolver.requestMaxAgeMfa(builder, maxAgeMfa);
        }

        if (tenant != null) {
            PartnerNetOAuth2AuthorizationRequestResolver.requestTenant(builder, tenant.toUpperCase());
        }

        if (prompt != null) {
            PartnerNetOAuth2AuthorizationRequestResolver.requestPrompt(builder, prompt);
        }

        if (loginHint != null) {
            PartnerNetOAuth2AuthorizationRequestResolver.requestLoginHint(builder, loginHint);
        }

        return builder;
    }
}
