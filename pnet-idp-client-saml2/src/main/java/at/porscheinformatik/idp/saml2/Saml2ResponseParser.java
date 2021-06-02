/**
 *
 */
package at.porscheinformatik.idp.saml2;

import org.opensaml.saml.saml2.core.Response;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.Saml2Exception;

/**
 * @author Daniel Furtlehner
 */
public interface Saml2ResponseParser
{

    /**
     * Parses the response and generates a authentication object that is passed to the authentication manager for
     * further processing.
     *
     * @param samlResponse the saml response
     * @param relayState the relay state
     * @return the authentication data build from the response
     * @throws Saml2Exception when something goes wrong parsing the response
     */
    Authentication parseResponse(Response samlResponse, String relayState) throws Saml2Exception;
}
