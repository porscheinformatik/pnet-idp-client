/**
 *
 */
package at.porscheinformatik.idp.saml2.response;

import static at.porscheinformatik.idp.saml2.Saml2Utils.*;

import java.time.Instant;
import java.util.Objects;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

/**
 * @author Daniel Furtlehner
 */
public class CheckAudienceRestrictionMessageHandler extends AbstractSuccessResponseMessageHandler {

    @Override
    protected void doInvoke(Response response, MessageContext messageContext) throws MessageHandlerException {
        Conditions conditions = response.getAssertions().get(0).getConditions();

        if (conditions == null) {
            throw new MessageHandlerException(
                "No Conditions set on saml message. Could not verify audience restriction."
            );
        }

        validateConditionsTimeFrame(conditions);
        RelyingPartyRegistration registration = getAuthenticationToken(messageContext).getRelyingPartyRegistration();

        conditions
            .getAudienceRestrictions()
            .stream()
            .flatMap(restriction -> restriction.getAudiences().stream())
            .filter(audience -> Objects.equals(audience.getURI(), registration.getEntityId()))
            .findAny()
            .orElseThrow(() ->
                new MessageHandlerException(String.format("No Audience matching %s found", registration.getEntityId()))
            );
    }

    private void validateConditionsTimeFrame(Conditions conditions) throws MessageHandlerException {
        Instant notBefore = conditions.getNotBefore();
        Instant notOnOrAfter = conditions.getNotOnOrAfter();

        if (notBefore != null && notOnOrAfter != null && notOnOrAfter.isBefore(notBefore)) {
            throw new MessageHandlerException("Contditions notOnOrAfter is before notBefore date");
        }

        Instant now = Instant.now();

        if (notBefore != null) {
            Instant skewedNotBefore = notBefore.minus(CLOCK_SKEW);

            if (skewedNotBefore.isAfter(now)) {
                throw new MessageHandlerException("Contditions is not valid right now based on notBefore value");
            }
        }

        if (notOnOrAfter != null) {
            Instant skewedNotOnOrAfter = notOnOrAfter.plus(CLOCK_SKEW);

            if (skewedNotOnOrAfter.isBefore(now)) {
                throw new MessageHandlerException("Contditions is not valid anymore based on notOnOrAfter value");
            }
        }
    }
}
