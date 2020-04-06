/**
 * 
 */
package at.porscheinformatik.pnet.idp.clientshowcase;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectUser;

/**
 * @author Daniel Furtlehner
 */
@RestController
@RequestMapping("/data/authentication")
public class AuthenticationDisplayController
{

    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationDTO getAuthentication()
    {
        return buildAuthentication();
    }

    private AuthenticationDTO buildAuthentication()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof PartnerNetOpenIdConnectUser)
        {
            return AuthenticationDTO.of((PartnerNetOpenIdConnectUser) principal);
        }

        return AuthenticationDTO.info(String.format("Unsupported authentication principal %s", principal.getClass()));
    }
}
