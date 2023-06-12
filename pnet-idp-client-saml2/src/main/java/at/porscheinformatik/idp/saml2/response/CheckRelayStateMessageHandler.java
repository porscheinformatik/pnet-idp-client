package at.porscheinformatik.idp.saml2.response;

import java.util.Objects;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;

import at.porscheinformatik.idp.saml2.Saml2ResponseProcessor.Saml2AuthenticationTokenContext;

public class CheckRelayStateMessageHandler extends AbstractSimpleMessageHandler
{

    @Override
    public void invoke(MessageContext messageContext) throws MessageHandlerException
    {
        Saml2AuthenticationTokenContext authenticationContext =
            messageContext.getSubcontext(Saml2AuthenticationTokenContext.class);
        String responseRelayState = authenticationContext
            .getResponseRelayState()
            .orElseThrow(() -> new MessageHandlerException("Relay state is missing in response."));
        String requestedRelayState = authenticationContext
            .getRequestedRelayState()
            .orElseThrow(() -> new MessageHandlerException("Requested relay state is missing."));

        if (!Objects.equals(responseRelayState, requestedRelayState))
        {
            throw new MessageHandlerException("Requested relay state doesn't match relay state in response");
        }
    }

}
