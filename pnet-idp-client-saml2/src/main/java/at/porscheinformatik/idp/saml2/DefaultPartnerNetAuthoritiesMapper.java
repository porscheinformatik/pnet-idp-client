package at.porscheinformatik.idp.saml2;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.CollectionUtils;

import at.porscheinformatik.idp.PartnerNetRoleDTO;
import at.porscheinformatik.idp.saml2.Saml2ResponseParserBase.Saml2Data;

public class DefaultPartnerNetAuthoritiesMapper
    implements BiFunction<PartnerNetSaml2AuthenticationPrincipal, Saml2Data, Collection<? extends GrantedAuthority>>
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
