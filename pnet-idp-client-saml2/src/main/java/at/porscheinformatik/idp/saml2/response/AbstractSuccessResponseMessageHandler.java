/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import java.util.Objects;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;

/**
 * @author Daniel Furtlehner
 */
public abstract class AbstractSuccessResponseMessageHandler extends AbstractSimpleMessageHandler
{

    @Override
    public void invoke(MessageContext messageContext) throws MessageHandlerException
    {
        Response response = getResponse(messageContext);

        if (Objects.equals(response.getStatus().getStatusCode().getValue(), StatusCode.SUCCESS))
        {
            doInvoke(response, messageContext);
        }
    }

    protected abstract void doInvoke(Response response, MessageContext messageContext) throws MessageHandlerException;
}
