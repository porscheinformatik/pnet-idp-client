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
public class ThrowOnMissingIdMessageHandler extends AbstractSimpleMessageHandler {

    @Override
    public void invoke(MessageContext messageContext) throws MessageHandlerException {
        Response response = getResponse(messageContext);

        if (response.getID() == null) {
            throw new MessageHandlerException("Message ID must not be null.");
        }
    }
}
