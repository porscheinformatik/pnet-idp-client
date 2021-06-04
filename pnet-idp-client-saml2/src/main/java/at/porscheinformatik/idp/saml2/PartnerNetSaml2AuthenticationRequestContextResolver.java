package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.Saml2Utils.*;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationRequestContext;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationRequestContextResolver;

public class PartnerNetSaml2AuthenticationRequestContextResolver implements Saml2AuthenticationRequestContextResolver
{
    private static final String FORCE_AUTHENTICATION_ATTR = "poi.saml2.force_authn";

    private Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyRegistrationResolver;

    @Override
    public Saml2AuthenticationRequestContext resolve(HttpServletRequest request)
    {
        RelyingPartyRegistration relyingParty = this.relyingPartyRegistrationResolver.convert(request);

        if (relyingParty == null)
        {
            return null;
        }

        String authnRequestId = Saml2Utils.generateId();
        boolean forceAuthn = Saml2Utils.isForceAuthentication(request);

        storeForceAuthentication(request, forceAuthn);
        storeAuthnRequestId(request, authnRequestId);

        return new PartnerNetSaml2AuthenticationRequestContext(relyingParty, relyingParty.getEntityId(),
            relyingParty.getAssertionConsumerServiceLocation(), Saml2Utils.getRelayState(request), authnRequestId,
            forceAuthn);
    }

    public void setRelyingPartyRegistrationResolver(
        Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyRegistrationResolver)
    {
        this.relyingPartyRegistrationResolver = relyingPartyRegistrationResolver;
    }

    public static class PartnerNetSaml2AuthenticationRequestContext extends Saml2AuthenticationRequestContext
    {
        private final String authnRequestId;
        private final boolean forceAuthn;

        public PartnerNetSaml2AuthenticationRequestContext(RelyingPartyRegistration relyingPartyRegistration,
            String issuer, String assertionConsumerServiceUrl, String relayState, String authnRequestId,
            boolean forceAuthn)
        {
            super(relyingPartyRegistration, issuer, assertionConsumerServiceUrl, relayState);

            this.authnRequestId = authnRequestId;
            this.forceAuthn = forceAuthn;
        }

        public String getAuthnRequestId()
        {
            return authnRequestId;
        }

        public boolean isForceAuthn()
        {
            return forceAuthn;
        }
    }

    public static void storeForceAuthentication(HttpServletRequest request, boolean force)
    {
        if (force)
        {
            request.getSession().setAttribute(FORCE_AUTHENTICATION_ATTR, Boolean.TRUE);
        }
        else
        {
            request.getSession().removeAttribute(FORCE_AUTHENTICATION_ATTR);
        }
    }

    public static boolean forceAuthenticationRequested(HttpServletRequest request)
    {
        return Boolean.TRUE.equals(request.getSession().getAttribute(FORCE_AUTHENTICATION_ATTR));
    }
}
