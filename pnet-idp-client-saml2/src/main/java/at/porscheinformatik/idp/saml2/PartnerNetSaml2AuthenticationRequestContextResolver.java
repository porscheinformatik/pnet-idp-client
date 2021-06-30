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
    private static final String SESSION_AGE_ATTR = "poi.saml2.session_age";
    private static final String NIST_LEVEL_ATTR = "poi.saml2.nist_level";

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
        Integer maxSessionAge = Saml2Utils.retrieveMaxSessionAge(request);
        Integer nistLevel = Saml2Utils.getRequestedNistAuthenticationLevel(request);

        storeForceAuthentication(request, forceAuthn);
        storeAuthnRequestId(request, authnRequestId);
        storeNistLevel(request, nistLevel);

        return new PartnerNetSaml2AuthenticationRequestContext(relyingParty, relyingParty.getEntityId(),
            relyingParty.getAssertionConsumerServiceLocation(), Saml2Utils.getRelayState(request), authnRequestId,
            forceAuthn, maxSessionAge, nistLevel);
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
        private final Integer maxSessionAge;
        private final Integer nistLevel;

        public PartnerNetSaml2AuthenticationRequestContext(RelyingPartyRegistration relyingPartyRegistration,
            String issuer, String assertionConsumerServiceUrl, String relayState, String authnRequestId,
            boolean forceAuthn, Integer maxSessionAge, Integer nistLevel)
        {
            super(relyingPartyRegistration, issuer, assertionConsumerServiceUrl, relayState);

            this.authnRequestId = authnRequestId;
            this.forceAuthn = forceAuthn;
            this.maxSessionAge = maxSessionAge;
            this.nistLevel = nistLevel;
        }

        public String getAuthnRequestId()
        {
            return authnRequestId;
        }

        public boolean isForceAuthn()
        {
            return forceAuthn;
        }

        public Integer getMaxSessionAge()
        {
            return maxSessionAge;
        }

        public Integer getNistLevel()
        {
            return nistLevel;
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

    public static void storeSessionAge(HttpServletRequest request, Integer maxSessionAge)
    {
        request.getSession().setAttribute(SESSION_AGE_ATTR, maxSessionAge);
    }

    public static Integer sessionAgeRequested(HttpServletRequest request)
    {
        return (Integer) request.getSession().getAttribute(SESSION_AGE_ATTR);
    }

    public static void storeNistLevel(HttpServletRequest request, Integer nistLevel)
    {
        if (nistLevel != null)
        {
            request.getSession().setAttribute(NIST_LEVEL_ATTR, nistLevel);
        }
        else
        {
            request.getSession().removeAttribute(NIST_LEVEL_ATTR);
        }
    }

    public static Integer getRequestedNistLevel(HttpServletRequest request)
    {
        return (Integer) request.getSession().getAttribute(NIST_LEVEL_ATTR);
    }
}
