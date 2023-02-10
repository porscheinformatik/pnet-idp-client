/**
 *
 */
package at.porscheinformatik.idp.saml2;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.saml2.provider.service.metadata.Saml2MetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Daniel Furtlehner
 */
public class Saml2ServiceProviderMetadataFilter extends OncePerRequestFilter
{
    private final RequestMatcher requestMatcher;
    private final RelyingPartyRegistrationResolver relyingPartyRegistrationResolver;
    private final Saml2MetadataResolver metadataResolver;

    public Saml2ServiceProviderMetadataFilter(String metadataProcessingUrl,
        RelyingPartyRegistrationResolver relyingPartyRegistrationResolver, Saml2MetadataResolver metadataResolver)
    {
        super();

        requestMatcher = new AntPathRequestMatcher(metadataProcessingUrl, "GET");
        this.relyingPartyRegistrationResolver = relyingPartyRegistrationResolver;
        this.metadataResolver = metadataResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException
    {
        if (!requestMatcher.matches(request))
        {
            filterChain.doFilter(request, response);

            return;
        }

        try
        {
            RelyingPartyRegistration relyingPartyRegistration = relyingPartyRegistrationResolver.resolve(request, null);

            if (relyingPartyRegistration == null)
            {
                response.sendError(HttpStatus.NOT_FOUND.value());

                return;
            }

            String metadata = metadataResolver.resolve(relyingPartyRegistration);

            response.setContentType("application/samlmetadata+xml");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"saml-metadata.xml\"");
            response.setContentLength(metadata.length());

            response.getWriter().append(metadata);
            response.getWriter().flush();
        }
        catch (Exception e)
        {
            throw new IOException("Error buiding metadata", e);
        }
    }

}
