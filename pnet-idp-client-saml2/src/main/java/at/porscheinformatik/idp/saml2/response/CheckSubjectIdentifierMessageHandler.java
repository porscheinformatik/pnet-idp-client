/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import static at.porscheinformatik.idp.saml2.Saml2Utils.*;

import java.util.List;
import java.util.Objects;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;

/**
 * @author Daniel Furtlehner
 */
public class CheckSubjectIdentifierMessageHandler extends AbstractSuccessResponseMessageHandler
{

    @Override
    protected void doInvoke(Response response, MessageContext<Response> messageContext) throws MessageHandlerException
    {
        List<Attribute> attributes = response.getAssertions().get(0).getAttributeStatements().get(0).getAttributes();

        long numberOfIds = attributes
            .stream()
            .filter(attribute -> Objects.equals(attribute.getName(), SUBJECT_ID_NAME)
                || Objects.equals(attribute.getName(), PAIRWISE_ID_NAME))
            .count();

        if (numberOfIds == 0)
        {
            throw new MessageHandlerException("No subject-identifier found in Response");
        }

        if (numberOfIds > 1)
        {
            throw new MessageHandlerException("Multiple subject-identifiers found in Response");
        }
    }
}
