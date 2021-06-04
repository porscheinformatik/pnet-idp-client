package at.porscheinformatik.idp.saml2;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UriComponentsBuilder;

import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;

public class Saml2Utils
{
    public static final String SUBJECT_ID_NAME = "urn:oasis:names:tc:SAML:attribute:subject-id";
    public static final String PAIRWISE_ID_NAME = "urn:oasis:names:tc:SAML:attribute:pairwise-id";
    public static final int CLOCK_SKEW_IN_MINUTES = 5;

    private static final String AUTHN_REQUEST_ID_ATTR = "poi.saml2.authn_request_id";
    private static final String FORCE_AUTHENTICATION_PARAM = "forceAuthn";

    //Specification says between 128 and 160 bit are perfect
    private static final IdentifierGenerationStrategy ID_GENERATOR = new SecureRandomIdentifierGenerationStrategy(20);

    /**
     * @return a random indentifier for saml messages
     */
    public static String generateId()
    {
        return ID_GENERATOR.generateIdentifier();
    }

    public static void storeAuthnRequestId(HttpServletRequest request, String id)
    {
        request.getSession().setAttribute(AUTHN_REQUEST_ID_ATTR, id);
    }

    public static String retrieveAuthnRequestId(HttpServletRequest request)
    {
        return (String) request.getSession().getAttribute(AUTHN_REQUEST_ID_ATTR);
    }

    public static UriComponentsBuilder forceAuthentication(UriComponentsBuilder uriComponentsBuilder)
    {
        return uriComponentsBuilder.queryParam(FORCE_AUTHENTICATION_PARAM, true);
    }

    public static boolean isForceAuthentication(HttpServletRequest request)
    {
        return Boolean.valueOf(request.getParameter(FORCE_AUTHENTICATION_PARAM));
    }

    public static String getRelayState(HttpServletRequest request)
    {
        return request.getParameter("RelayState");
    }
}
