package at.porscheinformatik.idp.saml2;

import at.porscheinformatik.idp.PartnerNetRoleDTO;
import at.porscheinformatik.idp.saml2.Saml2ResponseParserBase.Saml2Data;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.CollectionUtils;

/**
 * @deprecated use {@link DefaultPartnerNetSaml2AuthoritiesMapper} instead
 */
@Deprecated(since = "1.0.0")
public class DefaultPartnerNetAuthoritiesMapper implements PartnerNetSaml2AuthoritiesMapper {

    @Override
    public Collection<? extends GrantedAuthority> apply(
        PartnerNetSaml2AuthenticationPrincipal principal,
        Saml2Data data
    ) {
        if (CollectionUtils.isEmpty(principal.getRoles())) {
            return Collections.emptyList();
        }

        return principal
            .getRoles()
            .stream()
            .map(PartnerNetRoleDTO::getRoleMatchcode)
            .distinct()
            .map(SimpleGrantedAuthority::new)
            .toList();
    }
}
