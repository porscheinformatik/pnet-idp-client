/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import static at.porscheinformatik.idp.saml2.Saml2Utils.*;

import java.time.Instant;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;

import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationDetailsSource.HttpRequestContext;

/**
 * @author Daniel Furtlehner
 */
public class VerifyAuthnInstantMessageHandler extends AbstractSuccessResponseMessageHandler
{

    @Override
    protected void doInvoke(Response response, MessageContext messageContext) throws MessageHandlerException
    {
        HttpRequestContext httpRequestContext = getHttpRequestContext(messageContext);

        AuthnStatement authnStatement = response.getAssertions().get(0).getAuthnStatements().get(0);
        Instant authnInstant = authnStatement.getAuthnInstant();

        // When authentication is not forced, also old authentications are valid.
        if (httpRequestContext.isForceAuthentication() && isOutdated(authnInstant, 5 * 60))
        {
            throw new MessageHandlerException("Outdated AuthnInstant found for forced authentication.");
        }

        Integer sessionAge = httpRequestContext.getSessionAge();

        if (sessionAge != null && isOutdated(authnInstant, sessionAge))
        {
            throw new MessageHandlerException(
                String.format("Outdated AuthnInstant found for requested session age %s seconds.", sessionAge));
        }
    }

    private boolean isOutdated(Instant authnInstant, int sessionAgeSeconds)
    {
        if (authnInstant == null)
        {
            return true;
        }

        Instant now = Instant.now();
        // Account for clock skew here.
        Instant expiration = authnInstant.plus(CLOCK_SKEW).plusSeconds(sessionAgeSeconds);

        return expiration.isBefore(now);
    }

}
