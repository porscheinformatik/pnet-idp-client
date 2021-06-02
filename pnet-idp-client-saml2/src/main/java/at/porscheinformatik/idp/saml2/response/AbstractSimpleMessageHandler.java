/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import javax.annotation.Nonnull;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandler;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;

import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationConverter.HttpRequestContext;
import at.porscheinformatik.idp.saml2.Saml2ResponseProcessor.Saml2AuthenticationTokenContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Simple base class for message handlers that need no initialization
 *
 * @author Daniel Furtlehner
 * @param <MessageT> type of message to handle
 */
public abstract class AbstractSimpleMessageHandler<MessageT> implements MessageHandler<MessageT>
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

    protected Saml2AuthenticationToken getAuthenticationToken(MessageContext<MessageT> context)
    {
        return context.getSubcontext(Saml2AuthenticationTokenContext.class).getToken();
    }

    @Nonnull
    protected HttpRequestContext getHttpRequestContext(MessageContext<MessageT> context)
    {
        return HttpRequestContext.fromToken(getAuthenticationToken(context));
    }
}
