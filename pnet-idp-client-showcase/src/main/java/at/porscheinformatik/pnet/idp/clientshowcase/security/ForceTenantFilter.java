package at.porscheinformatik.pnet.idp.clientshowcase.security;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
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
 * If a request contains the parameter {@value DecidingAuthenticationEntryPoint#TENANT_PARAMETER_NAME} and the tenant in
 * the session does not match the specified one, the user gets logged out and redirected to the same page. The
 * authentication request contains the requested tenant.
 */
@Service
public class ForceTenantFilter extends GenericFilter
{
    private static final long serialVersionUID = 1L;

    private final SecurityContextRepository securityContextRepository;

    public ForceTenantFilter(Optional<SecurityContextRepository> securityContextRepository)
    {
        super();

        this.securityContextRepository = securityContextRepository.orElseGet(HttpSessionSecurityContextRepository::new);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null)
        {
            chain.doFilter(request, response);

            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String tenant = request.getParameter(DecidingAuthenticationEntryPoint.TENANT_PARAMETER_NAME);

        if (tenant != null && !tenant.equals(getTenant(authentication).orElse(null)))
        {
            String redirectUrl = buildRedirectUrl(httpRequest, authentication);

            logoutUser(httpRequest, httpResponse);

            httpResponse.sendRedirect(redirectUrl);
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    /**
     * Build a redirect URL based on this request (with the same URI), but make sure, that the request contains correct
     * authentication type.
     *
     * @param request the request
     * @param authentication the authentication object
     * @return the redirect URL
     */
    private String buildRedirectUrl(HttpServletRequest request, Authentication authentication)
    {
        Object principal = authentication.getPrincipal();

        String authenticationType = null;

        if (principal instanceof PartnerNetOpenIdConnectUser)
        {
            authenticationType = "oidc";
        }
        else if (principal instanceof PartnerNetSaml2AuthenticationPrincipal)
        {
            authenticationType = "saml2";
        }
        else
        {
            throw new IllegalStateException("Unknown authentication type " + principal.getClass());
        }

        UriComponents redirectUri = UriComponentsBuilder
            .fromHttpRequest(new ServletServerHttpRequest(request))
            .replaceQueryParam("authenticationType", authenticationType)
            .build();

        validateRedirectUri(request, redirectUri);

        return redirectUri.toUriString();
    }

    /**
     * Verifies that the redirect URI belongs to our server. The referer can not be trusted. We do not want to have an
     * arbitrary redirect to an attacker controlled server.
     *
     * @param request
     * @param redirectUri
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

    private Optional<String> getTenant(Authentication authentication)
    {
        Object principal = authentication.getPrincipal();

        if (principal instanceof PartnerNetOpenIdConnectUser)
        {
            return Optional.of(((PartnerNetOpenIdConnectUser) principal).getCountry());
        }

        if (principal instanceof PartnerNetSaml2AuthenticationPrincipal)
        {
            return Optional.of(((PartnerNetSaml2AuthenticationPrincipal) principal).getTenant());
        }

        return Optional.empty();
    }
}
