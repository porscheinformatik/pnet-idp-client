package at.porscheinformatik.idp.saml2.response;

import static at.porscheinformatik.idp.saml2.AuthnContextClass.*;
import static at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationRequestContextResolver.*;
import static org.springframework.util.CollectionUtils.*;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;

import at.porscheinformatik.idp.saml2.AuthnContextClass;
import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationConverter.HttpRequestContext;

public class VerifyAuthenticationStrenghMessageHandler extends AbstractSimpleMessageHandler<Response>
{

    @Override
    public void invoke(MessageContext<Response> messageContext) throws MessageHandlerException
    {
        HttpRequestContext httpRequestContext = getHttpRequestContext(messageContext);

        Integer requestedNistLevel = getRequestedNistLevel(httpRequestContext.getRequest());

        // Nothing special requested, so everything is fine
        if (requestedNistLevel == null)
        {
            return;
        }

        Assertion assertion = firstElement(messageContext.getMessage().getAssertions());
        AuthnStatement authnStatement = firstElement(assertion.getAuthnStatements());
        AuthnContextClassRef authnContextClassRef = authnStatement.getAuthnContext().getAuthnContextClassRef();

        AuthnContextClass authnContextClass = fromReference(authnContextClassRef.getAuthnContextClassRef())
            .orElseThrow(() -> new MessageHandlerException(String
                .format("Could not validate authentiation strenght as %s is unkown",
                    authnContextClassRef.getAuthnContextClassRef())));

        if (authnContextClass.getNistLevel() < requestedNistLevel)
        {
            throw new MessageHandlerException(String
                .format("Authentication strength %s is weaker than requested strength %s", authnContextClass,
                    requestedNistLevel));
        }
    }

}
