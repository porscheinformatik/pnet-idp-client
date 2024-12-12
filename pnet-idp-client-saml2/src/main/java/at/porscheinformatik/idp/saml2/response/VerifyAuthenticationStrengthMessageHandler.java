package at.porscheinformatik.idp.saml2.response;

import static at.porscheinformatik.idp.saml2.AuthnContextClass.*;
import static at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationRequestUtils.*;
import static org.springframework.util.CollectionUtils.*;

import at.porscheinformatik.idp.saml2.AuthnContextClass;
import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationDetailsSource.HttpRequestContext;
import javax.annotation.Nonnull;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;

public class VerifyAuthenticationStrengthMessageHandler extends AbstractSimpleMessageHandler {

    @Override
    public void invoke(@Nonnull MessageContext messageContext) throws MessageHandlerException {
        HttpRequestContext httpRequestContext = getHttpRequestContext(messageContext);

        Integer requestedNistLevel = getRequestedNistLevel(httpRequestContext.getRequest());

        // Nothing special requested, so everything is fine
        if (requestedNistLevel == null) {
            return;
        }

        Response response = getResponse(messageContext);
        Assertion assertion = firstElement(response.getAssertions());

        if (assertion == null) {
            throw new MessageHandlerException("No assertion found in response");
        }

        AuthnStatement authnStatement = firstElement(assertion.getAuthnStatements());

        if (authnStatement == null) {
            throw new MessageHandlerException("No authentication statement found in assertion");
        }

        AuthnContextClassRef authnContextClassRef = authnStatement.getAuthnContext().getAuthnContextClassRef();

        AuthnContextClass authnContextClass = fromReference(authnContextClassRef.getURI()).orElseThrow(() ->
            new MessageHandlerException(
                String.format(
                    "Could not validate authentication strength as %s is unknown",
                    authnContextClassRef.getURI()
                )
            )
        );

        if (authnContextClass.getNistLevel() < requestedNistLevel) {
            throw new MessageHandlerException(
                String.format(
                    "Authentication strength %s is weaker than requested strength %s",
                    authnContextClass,
                    requestedNistLevel
                )
            );
        }
    }
}
