package at.porscheinformatik.idp.saml2.response;

import java.util.Objects;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

public class VerifyStatusMessageHandler extends AbstractSimpleMessageHandler {

    @Override
    public void invoke(MessageContext messageContext) throws MessageHandlerException {
        Response response = getResponse(messageContext);
        Status status = response.getStatus();

        if (!Objects.equals(status.getStatusCode().getValue(), StatusCode.SUCCESS)) {
            String message = status.getStatusMessage() != null ? status.getStatusMessage().getValue() : "";

            throw new MessageHandlerException(
                String.format("Unsuccessful Response: %s %s", status.getStatusCode().getValue(), message)
            );
        }
    }
}
