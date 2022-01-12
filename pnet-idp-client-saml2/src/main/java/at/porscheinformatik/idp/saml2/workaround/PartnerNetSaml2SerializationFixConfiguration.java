/**
 * 
 */
package at.porscheinformatik.idp.saml2.workaround;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is only a workaround until the serialization problem in spring security is fixed
 * https://github.com/spring-projects/spring-security/issues/10550
 * 
 * @author Daniel Furtlehner
 */
@Configuration
public class PartnerNetSaml2SerializationFixConfiguration
{
    @Bean
    public SerializingFixHttpSessionSaml2AuthenticationRequestRepository authenticationRequestRepository()
    {
        return new SerializingFixHttpSessionSaml2AuthenticationRequestRepository();
    }
}
