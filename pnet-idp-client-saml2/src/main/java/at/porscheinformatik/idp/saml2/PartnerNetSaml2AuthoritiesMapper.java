package at.porscheinformatik.idp.saml2;

import at.porscheinformatik.idp.saml2.Saml2ResponseParserBase.Saml2Data;
import java.util.Collection;
import java.util.function.BiFunction;
import org.springframework.security.core.GrantedAuthority;

/**
 * Create {@link GrantedAuthority}s based on the {@link PartnerNetSaml2AuthenticationPrincipal} and the
 * {@link Saml2Data}.
 */
@FunctionalInterface
public interface PartnerNetSaml2AuthoritiesMapper
    extends BiFunction<PartnerNetSaml2AuthenticationPrincipal, Saml2Data, Collection<? extends GrantedAuthority>> {
    static PartnerNetSaml2AuthoritiesMapper defaultInstance() {
        return new DefaultPartnerNetSaml2AuthoritiesMapper();
    }
}
