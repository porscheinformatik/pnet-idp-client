package at.porscheinformatik.idp.saml2;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationRequestContext;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationRequestContextResolver;

public class PartnerNetSaml2AuthenticationRequestContextResolver implements Saml2AuthenticationRequestContextResolver
{
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
        Saml2Utils.storeAuthnRequestId(request, authnRequestId);

        return new PartnerNetSaml2AuthenticationRequestContext(relyingParty, relyingParty.getEntityId(),
            relyingParty.getAssertionConsumerServiceLocation(), Saml2Utils.getRelayState(request), authnRequestId);
    }

    public void setRelyingPartyRegistrationResolver(
        Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyRegistrationResolver)
    {
        this.relyingPartyRegistrationResolver = relyingPartyRegistrationResolver;
    }

    public static class PartnerNetSaml2AuthenticationRequestContext extends Saml2AuthenticationRequestContext
    {
        private final String authnRequestId;

        public PartnerNetSaml2AuthenticationRequestContext(RelyingPartyRegistration relyingPartyRegistration,
            String issuer, String assertionConsumerServiceUrl, String relayState, String authnRequestId)
        {
            super(relyingPartyRegistration, issuer, assertionConsumerServiceUrl, relayState);

            this.authnRequestId = authnRequestId;
        }

        public String getAuthnRequestId()
        {
            return authnRequestId;
        }
    }
}
