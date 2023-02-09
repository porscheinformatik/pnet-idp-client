/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

/**
 * @author Daniel Furtlehner
 */
public class CheckIssuerMessageHandler extends AbstractSimpleMessageHandler
{
    @Override
    public void invoke(MessageContext messageContext) throws MessageHandlerException
    {
        Response response = getResponse(messageContext);

        Issuer issuer = response.getIssuer();

        if (issuer == null || StringUtils.isBlank(issuer.getValue()))
        {
            throw new MessageHandlerException("Saml message has no issuer set");
        }

        if (issuer.getFormat() != null && !NameIDType.ENTITY.equals(issuer.getFormat()))
        {
            throw new MessageHandlerException("Saml Message has invalid issuer format set " + issuer.getFormat());
        }

        RelyingPartyRegistration relyingParty = getAuthenticationToken(messageContext).getRelyingPartyRegistration();

        String entityId = relyingParty.getAssertingPartyDetails().getEntityId();

        if (!Objects.equals(entityId, issuer.getValue()))
        {
            throw new MessageHandlerException(String.format("Invalid issuer %s", issuer.getValue()));
        }
    }
}
