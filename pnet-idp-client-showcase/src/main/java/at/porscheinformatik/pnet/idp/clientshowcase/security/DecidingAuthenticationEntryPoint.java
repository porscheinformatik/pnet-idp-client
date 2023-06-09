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

        UriComponentsBuilder path;

        switch (authenticationType)
        {
            case "oidc":
                path = oidcPath();
                break;

            case "oidc_force":
                path = PartnerNetOAuth2AuthorizationRequestResolver.forceAuthentication(oidcPath());
                break;

            // OpenID Connect with multifactor authentication
            case "oidc_mfa":
                path = PartnerNetOAuth2AuthorizationRequestResolver.requestNistAuthenticationLevels(oidcPath(), 3);
                break;

            case "saml2":
                path = saml2Path();
                break;

            // SAML 2 with forced authentication
            case "saml2_force":
                path = Saml2Utils.forceAuthentication(saml2Path());
                break;

            // SAML 2 with multifactor authentication
            case "saml2_mfa":
                path = Saml2Utils.requestNistAuthenticationLevel(saml2Path(), 3);
                break;

            default:
                throw new IllegalArgumentException("Unsupported authenticationType " + authenticationType);
        }

        String tenant = request.getParameter(TENANT_PARAMETER_NAME);

        if (tenant != null)
        {
            switch (authenticationType)
            {
                case "oidc":
                case "oidc_force":
                case "oidc_mfa":
                    path = PartnerNetOAuth2AuthorizationRequestResolver.requestTenant(path, tenant);
                    break;

                case "saml2":
                case "saml2_force":
                case "saml2_mfa":
                    path = Saml2Utils.requestTenant(path, tenant);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported authenticationType " + authenticationType);
            }
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
