/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import static java.lang.String.*;

import java.util.List;
import java.util.Objects;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;

/**
 * @author Daniel Furtlehner
 */
public class CheckAssertionStructureMessageHandler extends AbstractSuccessResponseMessageHandler {

    @Override
    protected void doInvoke(Response response, MessageContext messageContext) throws MessageHandlerException {
        Assertion assertion = validateSingleSizeList(response.getAssertions(), "Assertion");
        validateSingleSizeList(assertion.getAuthnStatements(), "AuthnStatement");
        validateSingleSizeList(assertion.getAttributeStatements(), "AttributeStatement");

        if (assertion.getSubject() == null) {
            throw new MessageHandlerException("Assertion is missing a subject");
        }

        if (!Objects.equals(assertion.getVersion(), SAMLVersion.VERSION_20)) {
            throw new MessageHandlerException("Assertion has wrong saml version");
        }
    }

    private <T> T validateSingleSizeList(List<T> list, String typeForMessage) throws MessageHandlerException {
        if (list.size() != 1) {
            throw new MessageHandlerException(
                format("Response must have exactly one %s but has %s", typeForMessage, list.size())
            );
        }

        return list.get(0);
    }
}
