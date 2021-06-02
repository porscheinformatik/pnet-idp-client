/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import static at.porscheinformatik.idp.saml2.Saml2Utils.*;

import org.joda.time.DateTime;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;

import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationConverter.HttpRequestContext;

/**
 * @author Daniel Furtlehner
 */
public class VerifyAuthnInstantMessageHandler extends AbstractSuccessResponseMessageHandler
{

    @Override
    protected void doInvoke(Response response, MessageContext<Response> messageContext) throws MessageHandlerException
    {
        HttpRequestContext httpRequestContext = getHttpRequestContext(messageContext);

        // When authentication is not forced, also old authentications are valid.
        if (!httpRequestContext.isForceAuthentication())
        {
            return;
        }

        AuthnStatement authnStatement = response.getAssertions().get(0).getAuthnStatements().get(0);
        DateTime authnInstant = authnStatement.getAuthnInstant();

        if (isOutdated(authnInstant))
        {
            throw new MessageHandlerException("Outdated AuthnInstant found for forced authentication.");
        }
    }

    private boolean isOutdated(DateTime authnInstant)
    {
        if (authnInstant == null)
        {
            return true;
        }

        DateTime now = DateTime.now();
        // The user must be authenticated in the last 5 minutes. Account for clock skew here.
        DateTime expiration = authnInstant.plusMinutes(CLOCK_SKEW_IN_MINUTES).plusMinutes(5);
        return expiration.isBefore(now);
    }

}
