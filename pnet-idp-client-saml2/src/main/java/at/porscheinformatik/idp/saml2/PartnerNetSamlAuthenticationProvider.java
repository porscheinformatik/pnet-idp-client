package at.porscheinformatik.idp.saml2;

import java.io.StringReader;
import java.util.Optional;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.security.saml2.core.Saml2ErrorCodes;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationException;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;

import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationDetailsSource.HttpRequestContext;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

public class PartnerNetSamlAuthenticationProvider implements AuthenticationProvider
{
    private final Saml2ResponseProcessor processor;
    private final ParserPool parserPool;
    private final Saml2ResponseParser parser;

    public PartnerNetSamlAuthenticationProvider(Saml2ResponseProcessor processor, Saml2ResponseParser parser)
    {
        super();

        this.processor = processor;
        this.parser = parser;
        parserPool = XMLObjectProviderRegistrySupport.getParserPool();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        try
        {
            Saml2AuthenticationToken token = (Saml2AuthenticationToken) authentication;
            String serializedResponse = token.getSaml2Response();
            Response response = parse(serializedResponse);
            processor.process(token, response);

            HttpRequestContext details = HttpRequestContext.fromToken(token);
            Optional<String> relayState = Saml2Utils.getRelayState(details.getRequest());

            // After we are done with processing the token, we set the details to null.
            // As the details contain the request, and they are not used from this point on, we remove them.
            // Otherwise spring security might copy them over to our final authentication object.
            token.setDetails(null);

            return parser.parseResponse(response, relayState);
        }
        catch (Saml2AuthenticationException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new Saml2AuthenticationException(
                new Saml2Error(Saml2ErrorCodes.INTERNAL_VALIDATION_ERROR, ex.getMessage()), ex);
        }
    }

    private Response parse(String serializedResponse) throws XMLParserException, UnmarshallingException
    {
        return (Response) XMLObjectSupport.unmarshallFromReader(parserPool, new StringReader(serializedResponse));
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return authentication != null && Saml2AuthenticationToken.class.isAssignableFrom(authentication);
    }
}
