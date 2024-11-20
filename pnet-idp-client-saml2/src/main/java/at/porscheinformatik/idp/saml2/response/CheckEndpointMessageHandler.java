/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationDetailsSource.HttpRequestContext;
import jakarta.servlet.http.HttpServletRequest;
import javax.annotation.Nonnull;
import net.shibboleth.utilities.java.support.net.URIComparator;
import net.shibboleth.utilities.java.support.net.URIException;
import net.shibboleth.utilities.java.support.net.impl.BasicURLComparator;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import org.opensaml.messaging.MessageException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Furtlehner
 */
public class CheckEndpointMessageHandler extends AbstractSimpleMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final URIComparator uriComparator = new BasicURLComparator();

    @Override
    public void invoke(MessageContext messageContext) throws MessageHandlerException {
        HttpRequestContext httpRequestContext = getHttpRequestContext(messageContext);

        checkEndpointURI(messageContext, httpRequestContext.getRequest());
    }

    /*
     * Unfortunately opensaml is not compatibly with the jakarta namespace yet. So we can not use the ReceivedEndpointSecurityHandler directly
     * and have to reimplement the logic here.
     */
    protected void checkEndpointURI(@Nonnull final MessageContext messageContext, HttpServletRequest request)
        throws MessageHandlerException {
        final String messageDestination;

        try {
            messageDestination = StringSupport.trimOrNull(
                SAMLBindingSupport.getIntendedDestinationEndpointURI(messageContext)
            );
        } catch (final MessageException e) {
            throw new MessageHandlerException("Error obtaining message intended destination endpoint URI", e);
        }

        final boolean bindingRequires = SAMLBindingSupport.isIntendedDestinationEndpointURIRequired(messageContext);

        if (messageDestination == null) {
            if (bindingRequires) {
                throw new MessageHandlerException(
                    "SAML message intended destination (required by binding) was not present"
                );
            }

            logger.debug("SAML message intended destination endpoint was empty, not required by binding, skipping");

            return;
        }

        final String receiverEndpoint = StringSupport.trimOrNull(request.getRequestURL().toString());

        logger.debug("message destination endpoint: intended {} / actual {}", messageDestination, receiverEndpoint);

        try {
            if (!uriComparator.compare(messageDestination, receiverEndpoint)) {
                throw new MessageHandlerException("SAML message failed received endpoint check");
            }
        } catch (final URIException e) {
            throw new MessageHandlerException("Error comparing endpoint URI's", e);
        }

        logger.debug("SAML message intended destination endpoint matched recipient endpoint");
    }
}
