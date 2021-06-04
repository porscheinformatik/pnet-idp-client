package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationRequestContextResolver.*;
import static at.porscheinformatik.idp.saml2.Saml2Utils.*;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationTokenConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;

public class HttpRequestContextAwareSaml2AuthenticationConverter implements AuthenticationConverter
{

    private final Saml2AuthenticationTokenConverter delegate;

    public HttpRequestContextAwareSaml2AuthenticationConverter(
        Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyRegistrationResolver)
    {
        delegate = new Saml2AuthenticationTokenConverter(relyingPartyRegistrationResolver);
    }

    @Override
    public Authentication convert(HttpServletRequest request)
    {
        Saml2AuthenticationToken authentication = delegate.convert(request);

        authentication.setDetails(new HttpRequestContext(request));

        return authentication;
    }

    public static class HttpRequestContext
    {
        public static HttpRequestContext fromToken(Saml2AuthenticationToken token)
        {
            Object details = Objects
                .requireNonNull(token.getDetails(),
                    "No authentication details found. Ensure to add a HttpRequestContext to the authentication details.");

            if (!HttpRequestContext.class.isAssignableFrom(details.getClass()))
            {
                throw new IllegalArgumentException(
                    String.format("AuthenticationDetails %s are not of type HttpRequestContext", details));
            }

            return (HttpRequestContext) details;
        }

        private final HttpServletRequest request;

        public HttpRequestContext(HttpServletRequest request)
        {
            super();

            this.request = request;
        }

        public HttpServletRequest getRequest()
        {
            return request;
        }

        public String getAuthnRequestId()
        {
            return retrieveAuthnRequestId(getRequest());
        }

        public String getClientAddress()
        {
            return request.getRemoteAddr();
        }

        public boolean isForceAuthentication()
        {
            return forceAuthenticationRequested(request);
        }
    }
}
