package at.porscheinformatik.idp.saml2;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationRequestContext;

import at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationRequestContextResolver.PartnerNetSaml2AuthenticationRequestContext;

public class PartnerNetSaml2AuthenticationRequestContextConverter
    implements Converter<Saml2AuthenticationRequestContext, AuthnRequest>
{

    @Override
    public AuthnRequest convert(Saml2AuthenticationRequestContext context)
    {
        if (!PartnerNetSaml2AuthenticationRequestContext.class.isAssignableFrom(context.getClass()))
        {
            throw new IllegalArgumentException(String
                .format(
                    "Context %s is not a PartnerNet context. Please use PartnerNetSaml2AuthenticationRequestContextResolver to create the context.",
                    context.getClass()));
        }

        PartnerNetSaml2AuthenticationRequestContext partnerNetContext =
            (PartnerNetSaml2AuthenticationRequestContext) context;

        return XmlUtils
            .authnRequest(partnerNetContext.getIssuer(), partnerNetContext.getDestination(),
                partnerNetContext.getAssertionConsumerServiceUrl(), partnerNetContext.getAuthnRequestId());
    }

}
