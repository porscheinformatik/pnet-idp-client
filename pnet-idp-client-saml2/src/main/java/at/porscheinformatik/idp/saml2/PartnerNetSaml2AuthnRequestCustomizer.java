package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationRequestUtils.*;
import static at.porscheinformatik.idp.saml2.Saml2Utils.*;
import static at.porscheinformatik.idp.saml2.XmlUtils.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml3AuthenticationRequestResolver.AuthnRequestContext;

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
        Integer maxSessionAge = Saml2Utils.retrieveMaxSessionAge(request);
        Integer nistLevel = Saml2Utils.getRequestedNistAuthenticationLevel(request);
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

        if (maxSessionAge != null)
        {
            authnRequest.setExtensions(createSamlObject(Extensions.DEFAULT_ELEMENT_NAME));

            authnRequest.getExtensions().getUnknownXMLObjects().add(maxSessionAgeRequest(maxSessionAge));
        }
    }

    private List<AuthnContextClass> calculateAuthnContextClasses(Integer nistLevel)
    {
        List<AuthnContextClass> authnContextClasses = Collections.emptyList();

        if (nistLevel != null)
        {
            authnContextClasses = AuthnContextClass.getAsLeastAsStrongAs(nistLevel);
        }

        return authnContextClasses;
    }

}
