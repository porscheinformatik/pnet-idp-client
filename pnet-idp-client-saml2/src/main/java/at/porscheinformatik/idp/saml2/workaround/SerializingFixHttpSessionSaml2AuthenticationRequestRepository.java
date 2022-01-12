package at.porscheinformatik.idp.saml2.workaround;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationRequestContext;
import org.springframework.security.saml2.provider.service.authentication.Saml2PostAuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2RedirectAuthenticationRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationRequestRepository;

/**
 * In Spring Security 5.6 the Implementations of the AbstractSaml2AuthenticationRequest are not serializable. This
 * causes issues when used in combination with spring-session where session attributes are serialized. To overcome this
 * issue until it is fixed, we use a custom repository that uses a custom object to serialize and deserialize the
 * request.
 * 
 * @author Furtlehner Daniel
 *
 */
public class SerializingFixHttpSessionSaml2AuthenticationRequestRepository
    implements Saml2AuthenticationRequestRepository<AbstractSaml2AuthenticationRequest>
{

    private static final String SAML2_AUTHN_REQUEST_ATTR_NAME =
        SerializingFixHttpSessionSaml2AuthenticationRequestRepository.class.getName().concat(".SAML2_AUTHN_REQUEST");

    private Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyResolver;

    @Override
    public AbstractSaml2AuthenticationRequest loadAuthenticationRequest(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);

        if (session == null)
        {
            return null;
        }

        Object value = session.getAttribute(SAML2_AUTHN_REQUEST_ATTR_NAME);

        if (value == null)
        {
            return null;
        }

        if (value instanceof SerializableSaml2PostAuthenticationRequest)
        {
            return ((SerializableSaml2PostAuthenticationRequest) value).toOriginal(relyingPartyResolver, request);
        }

        if (value instanceof SerializableSaml2RedirectAuthenticationRequest)
        {
            return ((SerializableSaml2RedirectAuthenticationRequest) value).toOriginal(relyingPartyResolver, request);
        }

        throw new IllegalArgumentException(
            String.format("Can not construct auth request. Unsupported object %s", value));
    }

    @Override
    public void saveAuthenticationRequest(AbstractSaml2AuthenticationRequest authenticationRequest,
        HttpServletRequest request, HttpServletResponse response)
    {
        if (authenticationRequest == null)
        {
            removeAuthenticationRequest(request, response);
            return;
        }

        Object sessionAttribute = null;

        if (authenticationRequest instanceof Saml2PostAuthenticationRequest)
        {
            sessionAttribute =
                new SerializableSaml2PostAuthenticationRequest((Saml2PostAuthenticationRequest) authenticationRequest);
        }
        else if (authenticationRequest instanceof Saml2RedirectAuthenticationRequest)
        {
            sessionAttribute = new SerializableSaml2RedirectAuthenticationRequest(
                (Saml2RedirectAuthenticationRequest) authenticationRequest);
        }
        else
        {
            throw new IllegalArgumentException(
                String.format("Objects of type %s are not supported yet", authenticationRequest.getClass()));
        }

        request.getSession().setAttribute(SAML2_AUTHN_REQUEST_ATTR_NAME, sessionAttribute);
    }

    @Override
    public AbstractSaml2AuthenticationRequest removeAuthenticationRequest(HttpServletRequest request,
        HttpServletResponse response)
    {
        AbstractSaml2AuthenticationRequest authenticationRequest = loadAuthenticationRequest(request);

        if (authenticationRequest != null)
        {
            request.getSession().removeAttribute(SAML2_AUTHN_REQUEST_ATTR_NAME);
        }

        return authenticationRequest;
    }

    public void setRelyingPartyRegistrationResolver(
        Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyResolver)
    {
        this.relyingPartyResolver = relyingPartyResolver;
    }

    private static final class SerializableSaml2PostAuthenticationRequest implements Serializable
    {
        private static final long serialVersionUID = -384758620696384295L;

        private final String samlRequest;
        private final String relayState;
        private final String authenticationRequestUri;

        public SerializableSaml2PostAuthenticationRequest(Saml2PostAuthenticationRequest original)
        {
            this.samlRequest = original.getSamlRequest();
            this.relayState = original.getRelayState();
            this.authenticationRequestUri = original.getAuthenticationRequestUri();
        }

        public Saml2PostAuthenticationRequest toOriginal(
            Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyResolver, HttpServletRequest request)
        {
            Saml2AuthenticationRequestContext context = buildContext(relyingPartyResolver, request);

            return Saml2PostAuthenticationRequest
                .withAuthenticationRequestContext(context)
                .samlRequest(samlRequest)
                .relayState(relayState)
                .authenticationRequestUri(authenticationRequestUri)
                .build();
        }
    }

    private static final class SerializableSaml2RedirectAuthenticationRequest implements Serializable
    {
        private static final long serialVersionUID = -3746936696891082461L;

        private final String samlRequest;
        private final String sigAlg;
        private final String signature;
        private final String relayState;
        private final String authenticationRequestUri;

        public SerializableSaml2RedirectAuthenticationRequest(Saml2RedirectAuthenticationRequest original)
        {
            this.samlRequest = original.getSamlRequest();
            this.sigAlg = original.getSigAlg();
            this.signature = original.getSignature();
            this.relayState = original.getRelayState();
            this.authenticationRequestUri = original.getAuthenticationRequestUri();
        }

        public Saml2RedirectAuthenticationRequest toOriginal(
            Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyResolver, HttpServletRequest request)
        {
            Saml2AuthenticationRequestContext context = buildContext(relyingPartyResolver, request);

            return Saml2RedirectAuthenticationRequest
                .withAuthenticationRequestContext(context)
                .samlRequest(samlRequest)
                .relayState(relayState)
                .authenticationRequestUri(authenticationRequestUri)
                .sigAlg(sigAlg)
                .signature(signature)
                .build();
        }
    }

    private static Saml2AuthenticationRequestContext buildContext(
        Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyResolver, HttpServletRequest request)
    {
        return Saml2AuthenticationRequestContext
            .builder()
            .relyingPartyRegistration(relyingPartyResolver.convert(request))
            .assertionConsumerServiceUrl("Not important")
            .issuer("Not important")
            .build();
    }
}
