/**
 * 
 */
package at.porscheinformatik.pnet.idp.clientshowcase;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return new AuthenticationDTO("XXX", false);
    }
}
