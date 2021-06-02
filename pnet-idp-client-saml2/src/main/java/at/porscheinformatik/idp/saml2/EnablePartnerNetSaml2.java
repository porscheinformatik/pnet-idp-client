package at.porscheinformatik.idp.saml2;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * @author Furtlehner Daniel
 *
 */
@Retention(RUNTIME)
@Target({TYPE})
@Import(PartnerNetSaml2Configuration.class)
@EnableConfigurationProperties(Saml2CredentialsProperties.class)
public @interface EnablePartnerNetSaml2
{

}
