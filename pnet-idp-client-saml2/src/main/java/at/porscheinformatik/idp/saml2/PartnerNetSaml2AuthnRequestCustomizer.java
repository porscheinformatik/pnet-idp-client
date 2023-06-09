package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationRequestUtils.*;
import static at.porscheinformatik.idp.saml2.Saml2Utils.*;
import static at.porscheinformatik.idp.saml2.XmlUtils.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver.AuthnRequestContext;

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

        boolean forceAuthn = isForceAuthn(request);
        Optional<Integer> maxSessionAge = getMaxSessionAge(request);
        Optional<String> tenant = getTenant(request);
        Optional<Integer> nistLevel = getNistLevel(request);
        String authnRequestId = Saml2Utils.generateId();
        List<AuthnContextClass> authnContextClasses = calculateAuthnContextClasses(nistLevel);

        storeForceAuthentication(request, forceAuthn);
        storeAuthnRequestId(request, authnRequestId);
        storeNistLevel(request, nistLevel);
        storeSessionAge(request, maxSessionAge);
        storeTenant(request, tenant);

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

        if (tenant.isPresent())
        {
            authnRequest.setExtensions(createSamlObject(Extensions.DEFAULT_ELEMENT_NAME));

            authnRequest.getExtensions().getUnknownXMLObjects().add(tenantRequest(tenant.get()));
        }
    }

    protected boolean isForceAuthn(HttpServletRequest request)
    {
        return Saml2Utils.isForceAuthentication(request);
    }

    protected Optional<Integer> getMaxSessionAge(HttpServletRequest request)
    {
        return Saml2Utils.retrieveMaxSessionAge(request);
    }

    protected Optional<String> getTenant(HttpServletRequest request)
    {
        return Saml2Utils.retrieveTenant(request);
    }

    protected Optional<Integer> getNistLevel(HttpServletRequest request)
    {
        return Saml2Utils.getRequestedNistAuthenticationLevel(request);
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
