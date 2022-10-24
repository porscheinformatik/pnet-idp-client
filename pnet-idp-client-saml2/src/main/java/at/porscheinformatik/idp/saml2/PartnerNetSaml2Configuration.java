/**
 * 
 */
package at.porscheinformatik.idp.saml2;

import org.springframework.context.annotation.Configuration;

/**
 * @author Daniel Furtlehner
 */
@Configuration
public class PartnerNetSaml2Configuration
{

    static
    {
        Saml2Initializer.initialize();
    }
}
