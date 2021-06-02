package at.porscheinformatik.idp.saml2.response;

import java.util.Objects;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

public class VerifyStatusMessageHandler extends AbstractSimpleMessageHandler<Response>
{

    @Override
    public void invoke(MessageContext<Response> messageContext) throws MessageHandlerException
    {
        Status status = messageContext.getMessage().getStatus();

        if (!Objects.equals(status.getStatusCode().getValue(), StatusCode.SUCCESS))
        {
            String message = status.getStatusMessage() != null ? status.getStatusMessage().getMessage() : "";

            throw new MessageHandlerException(
                String.format("Unsuccessful Response: %s %s", status.getStatusCode().getValue(), message));
        }
    }

}
