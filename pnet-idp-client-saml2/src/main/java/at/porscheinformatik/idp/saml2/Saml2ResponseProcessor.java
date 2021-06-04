/**
 *
 */
package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.Saml2Utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandler;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.binding.security.impl.MessageLifetimeSecurityHandler;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;

import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationConverter.HttpRequestContext;
import at.porscheinformatik.idp.saml2.response.CheckAssertionStructureMessageHandler;
import at.porscheinformatik.idp.saml2.response.CheckAudienceRestrictionMessageHandler;
import at.porscheinformatik.idp.saml2.response.CheckEndpointMessageHandler;
import at.porscheinformatik.idp.saml2.response.CheckIssuerMessageHandler;
import at.porscheinformatik.idp.saml2.response.CheckSubjectIdentifierMessageHandler;
import at.porscheinformatik.idp.saml2.response.CheckSubjectMessageHandler;
import at.porscheinformatik.idp.saml2.response.DecryptAssertionsMessageHandler;
import at.porscheinformatik.idp.saml2.response.ThrowOnMissingIdMessageHandler;
import at.porscheinformatik.idp.saml2.response.VerifyAuthenticationStrenghMessageHandler;
import at.porscheinformatik.idp.saml2.response.VerifyAuthnInstantMessageHandler;
import at.porscheinformatik.idp.saml2.response.VerifySignatureMessageHandler;
import at.porscheinformatik.idp.saml2.response.VerifyStatusMessageHandler;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * @author Daniel Furtlehner
 */
public class Saml2ResponseProcessor
{
    @SuppressWarnings("unchecked")
    public static Saml2ResponseProcessor withDefaultHandlers()
    {
        MessageLifetimeSecurityHandler lifetimeHandler = new MessageLifetimeSecurityHandler();
        lifetimeHandler.setClockSkew(CLOCK_SKEW_IN_MINUTES * 60 * 1000L);

        List<MessageHandler<Response>> handlers = new ArrayList<>();
        handlers.add(lifetimeHandler);
        handlers.add(new ThrowOnMissingIdMessageHandler());
        handlers.add(new CheckIssuerMessageHandler());
        handlers.add(new VerifySignatureMessageHandler()); // Have to check signature before decryption
        handlers.add(new DecryptAssertionsMessageHandler()); // Decrypt all assertions and add them to the normal assertions
        handlers.add(new VerifyStatusMessageHandler());
        handlers.add(new CheckAssertionStructureMessageHandler());
        handlers.add(new CheckSubjectMessageHandler());
        handlers.add(new CheckAudienceRestrictionMessageHandler());
        handlers.add(new CheckEndpointMessageHandler());
        handlers.add(new VerifyAuthnInstantMessageHandler());
        handlers.add(new CheckSubjectIdentifierMessageHandler());
        handlers.add(new VerifyAuthenticationStrenghMessageHandler());

        return new Saml2ResponseProcessor(handlers);
    }

    private final List<MessageHandler<Response>> handlers = new ArrayList<>();

    public Saml2ResponseProcessor(List<MessageHandler<Response>> handlers)
    {
        this.handlers.addAll(handlers);
    }

    public void process(Saml2AuthenticationToken token, Response response) throws MessageHandlerException
    {
        MessageContext<Response> messageContext = buildMessageContext(token, response);

        for (MessageHandler<Response> handler : handlers)
        {
            handler.invoke(messageContext);
        }
    }

    private MessageContext<Response> buildMessageContext(Saml2AuthenticationToken token, Response response)
    {
        HttpRequestContext details = HttpRequestContext.fromToken(token);
        boolean isPost = Objects.equals("POST", details.getRequest().getMethod());

        MessageContext<Response> messageContext = new MessageContext<Response>();
        messageContext.setMessage(response);
        messageContext.addSubcontext(new Saml2AuthenticationTokenContext(token));

        SAMLBindingContext bindingContext = messageContext.getSubcontext(SAMLBindingContext.class, true);
        bindingContext
            .setBindingUri(isPost ? SAMLConstants.SAML2_POST_BINDING_URI : SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        bindingContext.setHasBindingSignature(!isPost);
        bindingContext.setIntendedDestinationEndpointURIRequired(response.isSigned());

        return messageContext;
    }

    @PostConstruct
    public void initialize() throws ComponentInitializationException
    {
        for (MessageHandler<Response> handler : handlers)
        {
            handler.initialize();
        }
    }

    public static final class Saml2AuthenticationTokenContext extends BaseContext
    {
        private final Saml2AuthenticationToken token;

        public Saml2AuthenticationTokenContext(Saml2AuthenticationToken token)
        {
            super();

            this.token = token;
        }

        public Saml2AuthenticationToken getToken()
        {
            return token;
        }

    }
}
