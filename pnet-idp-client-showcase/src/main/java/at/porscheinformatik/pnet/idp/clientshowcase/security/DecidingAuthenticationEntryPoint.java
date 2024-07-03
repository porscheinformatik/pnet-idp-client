/**
 *
 */
package at.porscheinformatik.pnet.idp.clientshowcase.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.util.UriComponentsBuilder;

import at.porscheinformatik.idp.openidconnect.PartnerNetOAuth2AuthorizationRequestResolver;
import at.porscheinformatik.idp.saml2.Saml2Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This authentication entry point decides what authentication should be used based on the "authenticationType" query
 * parameter. This is application dependent. The showcase wants to show off different authentication techniques,
 * therefore this entry point. If the parameter "tenant" has been specified, it passes the value as "tenant" to the
 * authentication mechanism.
 *
 * @author Daniel Furtlehner
 */
public class DecidingAuthenticationEntryPoint implements AuthenticationEntryPoint
{
    public static final String TENANT_PARAMETER_NAME = "tenant";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException
    {
        String uri = buildUri(request).toUriString();

        response.sendRedirect(uri);
    }

    private UriComponentsBuilder buildUri(HttpServletRequest request)
    {
        String authenticationType = request.getParameter("authenticationType");

        if (authenticationType == null)
        {
            throw new IllegalArgumentException("The authenticationType query parameter is mandatory.");
        }

        UriComponentsBuilder path = switch (authenticationType)
        {
            case "oidc" -> oidcPath();
            case "oidc_force" -> PartnerNetOAuth2AuthorizationRequestResolver.forceAuthentication(oidcPath());

            // OpenID Connect with multifactor authentication
            case "oidc_mfa" ->
                PartnerNetOAuth2AuthorizationRequestResolver.requestNistAuthenticationLevels(oidcPath(), 3);
            case "saml2" -> saml2Path();

            // SAML 2 with forced authentication
            case "saml2_force" -> Saml2Utils.forceAuthentication(saml2Path());

            // SAML 2 with multifactor authentication
            case "saml2_mfa" -> Saml2Utils.requestNistAuthenticationLevel(saml2Path(), 3);
            default -> throw new IllegalArgumentException("Unsupported authenticationType " + authenticationType);
        };

        String tenant = request.getParameter(TENANT_PARAMETER_NAME);

        if (tenant != null)
        {
            path = switch (authenticationType)
            {
                case "oidc", "oidc_force", "oidc_mfa" ->
                    PartnerNetOAuth2AuthorizationRequestResolver.requestTenant(path, tenant);
                case "saml2", "saml2_force", "saml2_mfa" -> Saml2Utils.requestTenant(path, tenant);
                default -> throw new IllegalArgumentException("Unsupported authenticationType " + authenticationType);
            };
        }

        return path;
    }

    private UriComponentsBuilder saml2Path()
    {
        return UriComponentsBuilder.fromPath("/saml2/authenticate/pnet");
    }

    private UriComponentsBuilder oidcPath()
    {
        return UriComponentsBuilder.fromPath("/oauth2/authorization/pnet");
    }
}
