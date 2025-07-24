package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationRequestUtils.*;
import static at.porscheinformatik.idp.saml2.Saml2Utils.storeAuthnRequestId;
import static at.porscheinformatik.idp.saml2.XmlUtils.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver.AuthnRequestContext;

/**
 * Customize the authentication request with Partner.Net related features.
 *
 * @author Daniel Furtlehner
 */
public class PartnerNetSaml2AuthnRequestCustomizer implements Consumer<AuthnRequestContext> {

    @Override
    public void accept(AuthnRequestContext context) {
        HttpServletRequest request = context.getRequest();
        AuthnRequest authnRequest = context.getAuthnRequest();

        boolean forceAuthn = isForceAuthn(request);
        Optional<Integer> maxSessionAge = getMaxSessionAge(request);
        Optional<Integer> maxAgeMfa = getMaxAgeMfa(request);
        Optional<String> tenant = getTenant(request);
        Optional<String> prompt = getPrompt(request);
        Optional<String> loginHint = getLoginHint(request);
        Optional<Integer> nistLevel = getNistLevel(request);
        String authnRequestId = Saml2Utils.generateId();
        List<AuthnContextClass> authnContextClasses = calculateAuthnContextClasses(nistLevel);

        storeForceAuthentication(request, forceAuthn);
        storeAuthnRequestId(request, authnRequestId);
        storeNistLevel(request, nistLevel);
        storeSessionAge(request, maxSessionAge);
        storeMaxAgeMfa(request, maxAgeMfa);
        storeTenant(request, tenant);
        storePrompt(request, prompt);
        storeLoginHint(request, loginHint);

        authnRequest.setID(authnRequestId);

        if (forceAuthn) {
            authnRequest.setForceAuthn(Boolean.TRUE);
        }

        if (!authnContextClasses.isEmpty()) {
            authnRequest.setRequestedAuthnContext(requestedAuthnContext(authnContextClasses));
        }

        // Add Extensions:
        if (maxSessionAge.isPresent() || maxAgeMfa.isPresent() || tenant.isPresent() || prompt.isPresent() || loginHint.isPresent()) {
            authnRequest.setExtensions(createSamlObject(Extensions.DEFAULT_ELEMENT_NAME));
        }

        maxSessionAge.ifPresent(maxAge ->
            authnRequest.getExtensions().getUnknownXMLObjects().add(maxSessionAgeRequest(maxAge))
        );
        maxAgeMfa.ifPresent(maxAge -> authnRequest.getExtensions().getUnknownXMLObjects().add(maxAgeMfaRequest(maxAge))
        );
        tenant.ifPresent(t -> authnRequest.getExtensions().getUnknownXMLObjects().add(tenantRequest(t)));
        prompt.ifPresent(p -> authnRequest.getExtensions().getUnknownXMLObjects().add(promptRequest(p)));
        loginHint.ifPresent(hint -> authnRequest.getExtensions().getUnknownXMLObjects().add(loginHintRequest(hint)));
    }

    protected boolean isForceAuthn(HttpServletRequest request) {
        return Saml2Utils.isForceAuthentication(request);
    }

    protected Optional<Integer> getMaxSessionAge(HttpServletRequest request) {
        return Saml2Utils.retrieveMaxSessionAge(request);
    }

    protected Optional<Integer> getMaxAgeMfa(HttpServletRequest request) {
        return Saml2Utils.retrieveMaxAgeMfa(request);
    }

    protected Optional<String> getTenant(HttpServletRequest request) {
        return Saml2Utils.retrieveTenant(request);
    }

    protected Optional<String> getPrompt(HttpServletRequest request) {
        return Saml2Utils.retrievePrompt(request);
    }

    protected Optional<String> getLoginHint(HttpServletRequest request) {
        return Saml2Utils.retrieveLoginHint(request);
    }

    protected Optional<Integer> getNistLevel(HttpServletRequest request) {
        return Saml2Utils.getRequestedNistAuthenticationLevel(request);
    }

    private List<AuthnContextClass> calculateAuthnContextClasses(Optional<Integer> nistLevel) {
        List<AuthnContextClass> authnContextClasses = Collections.emptyList();

        if (nistLevel.isPresent()) {
            authnContextClasses = AuthnContextClass.getAsLeastAsStrongAs(nistLevel.get());
        }

        return authnContextClasses;
    }
}
