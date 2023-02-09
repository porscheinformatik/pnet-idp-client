/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import static at.porscheinformatik.idp.saml2.Saml2Utils.*;

import java.time.Instant;
import java.util.Objects;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationDetailsSource.HttpRequestContext;

/**
 * @author Daniel Furtlehner
 */
public class CheckSubjectMessageHandler extends AbstractSuccessResponseMessageHandler
{

    @Override
    protected void doInvoke(Response response, MessageContext messageContext) throws MessageHandlerException
    {
        Subject subject = response.getAssertions().get(0).getSubject();

        SubjectConfirmation bearerConfirmation = subject
            .getSubjectConfirmations()
            .stream()
            .filter(confirmation -> Objects.equals(confirmation.getMethod(), SubjectConfirmation.METHOD_BEARER))
            .findAny()
            .orElseThrow(() -> new MessageHandlerException("No bearer SubjectConfirmation found"));

        SubjectConfirmationData subjectConfirmationData = bearerConfirmation.getSubjectConfirmationData();

        if (subjectConfirmationData == null)
        {
            throw new MessageHandlerException("No SubjectConfirmationData for bearer Subject found");
        }

        RelyingPartyRegistration registration = getAuthenticationToken(messageContext).getRelyingPartyRegistration();

        if (!Objects.equals(subjectConfirmationData.getRecipient(), registration.getAssertionConsumerServiceLocation()))
        {
            throw new MessageHandlerException(String
                .format("Invalid Recipient attribute. Expected %s but got %s",
                    registration.getAssertionConsumerServiceLocation(), subjectConfirmationData.getRecipient()));
        }

        if (subjectConfirmationData.getNotBefore() != null)
        {
            throw new MessageHandlerException("NotBefore must not be set on SubjectConfirmationData elements");
        }

        if (isOutdated(subjectConfirmationData.getNotOnOrAfter()))
        {
            throw new MessageHandlerException("SubjectConfirmationData already outdated");
        }

        HttpRequestContext httpContext = getHttpRequestContext(messageContext);

        if (!Objects.equals(httpContext.getAuthnRequestId(), subjectConfirmationData.getInResponseTo()))
        {
            throw new MessageHandlerException(String
                .format("Wrong inResponseTo on SubjectConfirmationData. Expected %s but got %s",
                    httpContext.getAuthnRequestId(), subjectConfirmationData.getInResponseTo()));
        }

        if (subjectConfirmationData.getAddress() != null
            && !Objects.equals(httpContext.getClientAddress(), subjectConfirmationData.getAddress()))
        {
            throw new MessageHandlerException(String
                .format("Wrong Address on SubjectConfirmationData. Expected %s but got %s",
                    httpContext.getClientAddress(), subjectConfirmationData.getAddress()));
        }
    }

    private boolean isOutdated(Instant notOnOrAfter)
    {
        if (notOnOrAfter == null)
        {
            return true;
        }

        Instant now = Instant.now();
        Instant expiration = notOnOrAfter.plus(CLOCK_SKEW);

        return expiration.isBefore(now);
    }
}
