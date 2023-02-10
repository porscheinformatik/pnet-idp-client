package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationRequestUtils.*;
import static at.porscheinformatik.idp.saml2.Saml2Utils.*;
import static at.porscheinformatik.idp.saml2.XmlUtils.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver.AuthnRequestContext;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Customize the authentication request with Partner.Net related features.
 * 
 * @author Daniel Furtlehner
 */
public class PartnerNetSaml2AuthnRequestCustomizer implements Consumer<AuthnRequestContext>
{

    @Override
    public void accept(AuthnRequestContext t)
    {
        HttpServletRequest request = t.getRequest();
        AuthnRequest authnRequest = t.getAuthnRequest();

        boolean forceAuthn = Saml2Utils.isForceAuthentication(request);
        Optional<Integer> maxSessionAge = Saml2Utils.retrieveMaxSessionAge(request);
        Optional<Integer> nistLevel = Saml2Utils.getRequestedNistAuthenticationLevel(request);
        String authnRequestId = Saml2Utils.generateId();
        List<AuthnContextClass> authnContextClasses = calculateAuthnContextClasses(nistLevel);

        storeForceAuthentication(request, forceAuthn);
        storeAuthnRequestId(request, authnRequestId);
        storeNistLevel(request, nistLevel);
        storeSessionAge(request, maxSessionAge);

        authnRequest.setID(authnRequestId);

        if (forceAuthn)
        {
            authnRequest.setForceAuthn(Boolean.TRUE);
        }

        if (!authnContextClasses.isEmpty())
        {
            authnRequest.setRequestedAuthnContext(requestedAuthnContext(authnContextClasses));
        }

        if (maxSessionAge.isPresent())
        {
            authnRequest.setExtensions(createSamlObject(Extensions.DEFAULT_ELEMENT_NAME));

            authnRequest.getExtensions().getUnknownXMLObjects().add(maxSessionAgeRequest(maxSessionAge.get()));
        }
    }

    private List<AuthnContextClass> calculateAuthnContextClasses(Optional<Integer> nistLevel)
    {
        List<AuthnContextClass> authnContextClasses = Collections.emptyList();

        if (nistLevel.isPresent())
        {
            authnContextClasses = AuthnContextClass.getAsLeastAsStrongAs(nistLevel.get());
        }

        return authnContextClasses;
    }

}
