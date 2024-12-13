package at.porscheinformatik.idp.saml2;

import java.io.Serial;
import java.util.Collection;
import java.util.Objects;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class PartnerNetSaml2Authentication extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 4047926380137821978L;

    private final PartnerNetSaml2AuthenticationPrincipal principal;

    public PartnerNetSaml2Authentication(
        PartnerNetSaml2AuthenticationPrincipal principal,
        Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public PartnerNetSaml2AuthenticationPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PartnerNetSaml2Authentication that = (PartnerNetSaml2Authentication) o;
        return Objects.equals(principal, that.principal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), principal);
    }
}
