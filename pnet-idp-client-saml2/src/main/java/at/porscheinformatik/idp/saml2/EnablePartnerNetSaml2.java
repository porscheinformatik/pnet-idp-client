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
@Import(PartnerNetSaml2ImportSelector.class)
@EnableConfigurationProperties(Saml2CredentialsProperties.class)
public @interface EnablePartnerNetSaml2
{

    /**
     * Enables a workaround where saml 2 authentication requests can not be serialized in spring.
     * https://github.com/spring-projects/spring-security/issues/10550
     * 
     * Enable this when you need to serialize sessions e.g. when using spring-session
     * 
     * @return true when serialization fix should be enabled
     */
    boolean registerSerializationFix() default false;
}
