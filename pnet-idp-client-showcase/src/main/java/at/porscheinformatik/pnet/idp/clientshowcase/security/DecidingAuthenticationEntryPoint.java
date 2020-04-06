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
        String uri = buildUri(request);

        response.sendRedirect(uri);
    }

    private String buildUri(HttpServletRequest request)
    {
        String authentiationType = request.getParameter("authenticationType");

        if (authentiationType == null)
        {
            throw new IllegalArgumentException("The authenticationType query parameter is mandatory.");
        }

        switch (authentiationType)
        {
            case "oidc":
                return "/oauth2/authorization/pnet";

            default:
                throw new IllegalArgumentException("Unsupported authenticationType " + authentiationType);
        }
    }

}
