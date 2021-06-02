/**
 * 
 */
package at.porscheinformatik.pnet.idp.clientshowcase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.porscheinformatik.idp.openidconnect.PartnerNetOpenIdConnectUser;
import at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationPrincipal;

/**
 * @author Daniel Furtlehner
 */
@Controller
@RequestMapping({"/", "/data/authorization"})
public class AuthenticationDisplayController
{
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthenticationDisplayController(ObjectMapper objectMapper)
    {
        super();

        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String getAuthentication(Model model) throws JsonProcessingException
    {
        AuthenticationDTO dto = buildAuthentication();

        model.addAttribute("authData", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto));

        return "index";
    }

    @GetMapping("/logoutinfo")
    public String getLogout() throws JsonProcessingException
    {
        return "logout";
    }

    private AuthenticationDTO buildAuthentication()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof PartnerNetOpenIdConnectUser)
        {
            return AuthenticationDTO.of((PartnerNetOpenIdConnectUser) principal);
        }

        if (principal instanceof PartnerNetSaml2AuthenticationPrincipal)
        {
            return AuthenticationDTO.of((PartnerNetSaml2AuthenticationPrincipal) principal);
        }

        return AuthenticationDTO.info(String.format("Unsupported authentication principal %s", principal.getClass()));
    }
}
