package at.porscheinformatik.idp.saml2;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.CollectionUtils;

import at.porscheinformatik.idp.PartnerNetRoleDTO;
import at.porscheinformatik.idp.saml2.Saml2ResponseParserBase.Saml2Data;

/**
 * A {@link PartnerNetSaml2AuthoritiesMapper} that converts the roles of the principal to
 * {@link SimpleGrantedAuthority}s.
 *
 * @author ham
 */
public class DefaultPartnerNetSaml2AuthoritiesMapper implements PartnerNetSaml2AuthoritiesMapper
{
    @Override
    public Collection<? extends GrantedAuthority> apply(PartnerNetSaml2AuthenticationPrincipal principal,
        Saml2Data data)
    {
        if (CollectionUtils.isEmpty(principal.getRoles()))
        {
            return Collections.emptyList();
        }

        return principal
            .getRoles()
            .stream()
            .map(PartnerNetRoleDTO::getRoleMatchcode)
            .distinct()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
}
