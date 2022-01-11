/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.binding.security.impl.ReceivedEndpointSecurityHandler;
import org.opensaml.saml.saml2.core.Response;

import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationDetailsSource.HttpRequestContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * @author Daniel Furtlehner
 */
public class CheckEndpointMessageHandler extends AbstractSimpleMessageHandler<Response>
{

    @SuppressWarnings("unchecked")
    @Override
    public void invoke(MessageContext<Response> messageContext) throws MessageHandlerException
    {
        HttpRequestContext httpRequestContext = getHttpRequestContext(messageContext);

        ReceivedEndpointSecurityHandler handler = new ReceivedEndpointSecurityHandler();
        handler.setHttpServletRequest(httpRequestContext.getRequest());

        try
        {
            handler.initialize();
        }
        catch (ComponentInitializationException e)
        {
            throw new MessageHandlerException("Error initializing endpoint handler", e);
        }

        try
        {
            handler.invoke(messageContext);
        }
        finally
        {
            handler.destroy();
        }
    }

}
