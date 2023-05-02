/**
 * 
 */
package at.porscheinformatik.pnet.idp.clientshowcase.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.util.UriComponentsBuilder;

import at.porscheinformatik.idp.openidconnect.PartnerNetOAuth2AuthorizationRequestResolver;
import at.porscheinformatik.idp.saml2.Saml2Utils;

/**
 * This authentication entry point decides what authentication should be used based on the "authenticationType" query
 * parameter. This is application dependent. The showcase wants to show off different authentication techniques,
 * therefor this entry point.
 * 
 * @author Daniel Furtlehner
 */
public class DecidingAuthenticationEntryPoint implements AuthenticationEntryPoint
{

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException
    {
        String uri = buildUri(request).toUriString();

        response.sendRedirect(uri);
    }

    private UriComponentsBuilder buildUri(HttpServletRequest request)
    {
        String authentiationType = request.getParameter("authenticationType");

        if (authentiationType == null)
        {
            throw new IllegalArgumentException("The authenticationType query parameter is mandatory.");
        }

        switch (authentiationType)
        {
            case "oidc":
                return oidcPath();

            case "oidc_force":
                return PartnerNetOAuth2AuthorizationRequestResolver.forceAuthentication(oidcPath());

            // OpenID Connect with multifactor authentication
            case "oidc_mfa":
                return PartnerNetOAuth2AuthorizationRequestResolver.requestNistAuthenticationLevels(oidcPath(), 3);

            case "saml2":
                return saml2Path();

            // SAML 2 with forced authentication
            case "saml2_force":
                return Saml2Utils.forceAuthentication(saml2Path());

            // SAML 2 with multifactor authentication
            case "saml2_mfa":
                return Saml2Utils.requestNistAuthenticationLevel(saml2Path(), 3);

            default:
                throw new IllegalArgumentException("Unsupported authenticationType " + authentiationType);
        }
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
