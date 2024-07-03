package at.porscheinformatik.pnet.idp.clientshowcase.security;

import java.io.IOException;
import java.io.Serial;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectUser;
import at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * When a POST Request is made to /forceauthentication, the user is logged out and redirected to the same page, but with
 * the force authentication parameter appended.
 * <p>
 * We have no session anymore, and Spring Security will call the authentication entry point again, which will then see
 * the force authentication parameter and force the authentication at the IDP.
 */
@Service
public class ForceAuthenticationFilter extends GenericFilter
{
    @Serial
    private static final long serialVersionUID = 1L;

    private final RequestMatcher requestMatcher;
    private final SecurityContextRepository securityContextRepository;

    public ForceAuthenticationFilter(Optional<SecurityContextRepository> securityContextRepository)
    {
        super();

        requestMatcher = AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/forceauthentication");
        this.securityContextRepository = securityContextRepository.orElseGet(HttpSessionSecurityContextRepository::new);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (requestMatcher.matches(httpRequest))
        {
            String redirectUrl = buildRedirectUrl(httpRequest);

            logoutUser(httpRequest, httpResponse);

            httpResponse.sendRedirect(redirectUrl);
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    private String buildRedirectUrl(HttpServletRequest request)
    {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String authenticationType;

        if (principal instanceof PartnerNetOpenIdConnectUser)
        {
            authenticationType = "oidc_force";
        }
        else if (principal instanceof PartnerNetSaml2AuthenticationPrincipal)
        {
            authenticationType = "saml2_force";
        }
        else
        {
            throw new IllegalStateException("Unknown authentication type " + principal.getClass());
        }

        // We simply redirect to the same page displayed in the browser, but with the force authentication parameter
        // appended.
        UriComponents redirectUri = UriComponentsBuilder
            .fromHttpUrl(request.getHeader("Referer"))
            .replaceQueryParam("authenticationType", authenticationType)
            .build();

        validateRedirectUri(request, redirectUri);

        return redirectUri.toUriString();
    }

    /**
     * Verifies that the redirect URI belongs to our server. The referer can not be trusted. We do not want to have an
     * arbitrary redirect to an attacker controlled server.
     *
     * @param request the request
     * @param redirectUri the redirect URI
     */
    private void validateRedirectUri(HttpServletRequest request, UriComponents redirectUri)
    {
        if (!Objects.equals(request.getScheme(), redirectUri.getScheme()))
        {
            throw new IllegalArgumentException(
                String.format("Scheme: expected %s, got %s", request.getScheme(), redirectUri.getScheme()));
        }

        if (!Objects.equals(request.getServerName(), redirectUri.getHost()))
        {
            throw new IllegalArgumentException(
                String.format("Host: expected %s, got %s", request.getServerName(), redirectUri.getHost()));
        }

        if (!Objects.equals(request.getServerPort(), redirectUri.getPort()))
        {
            throw new IllegalArgumentException(
                String.format("Port: expected %s, got %s", request.getServerPort(), redirectUri.getPort()));
        }
    }

    private void logoutUser(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        // At first we need to log the user out.
        SecurityContextHolder.clearContext();
        securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), httpRequest, httpResponse);

        if (httpRequest.getSession(false) != null)
        {
            httpRequest.getSession(false).invalidate();
        }
    }

}
