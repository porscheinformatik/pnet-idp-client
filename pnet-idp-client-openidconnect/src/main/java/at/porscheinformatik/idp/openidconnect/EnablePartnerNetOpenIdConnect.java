/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * @author Daniel Furtlehner
 */
@Retention(RUNTIME)
@Target({ TYPE })
@Import(PartnerNetOpenidConnectConfiguration.class)
public @interface EnablePartnerNetOpenIdConnect {
// No more configuration
}
