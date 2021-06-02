/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Response;

/**
 * @author Daniel Furtlehner
 */
public class ThrowOnMissingIdMessageHandler extends AbstractSimpleMessageHandler<Response>
{

    @Override
    public void invoke(MessageContext<Response> messageContext) throws MessageHandlerException
    {
        Response response = messageContext.getMessage();

        if (response.getID() == null)
        {
            throw new MessageHandlerException("Message ID must not be null.");
        }
    }

}
