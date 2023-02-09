/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import javax.annotation.Nonnull;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandler;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;

import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationDetailsSource.HttpRequestContext;
import at.porscheinformatik.idp.saml2.Saml2ResponseProcessor.Saml2AuthenticationTokenContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Simple base class for message handlers that need no initialization
 *
 * @author Daniel Furtlehner
 * @param <MessageT> type of message to handle
 */
public abstract class AbstractSimpleMessageHandler implements MessageHandler
{

    @Override
    public boolean isInitialized()
    {
        return true;
    }

    @Override
    public void initialize() throws ComponentInitializationException
    {
        // Nothing to do here
    }

    protected Saml2AuthenticationToken getAuthenticationToken(MessageContext context)
    {
        return context.getSubcontext(Saml2AuthenticationTokenContext.class).getToken();
    }

    protected Response getResponse(MessageContext messageContext)
    {
        Object message = messageContext.getMessage();

        if (message instanceof Response)
        {
            return (Response) message;
        }

        throw new IllegalArgumentException(String.format("Response expected but got %s", message));
    }

    @Nonnull
    protected HttpRequestContext getHttpRequestContext(MessageContext context)
    {
        return HttpRequestContext.fromToken(getAuthenticationToken(context));
    }
}
