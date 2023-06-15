/**
 *
 */
package at.porscheinformatik.pnet.idp.clientshowcase;

import java.util.Optional;

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
        Optional<AuthenticationDTO> dto = buildAuthentication();

        if (dto.isPresent())
        {
            model.addAttribute("authData", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto));
        }
        else
        {
            model
                .addAttribute("authData",
                    String
                        .format("Unsupported authentication principal %s",
                            SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
        }

        return "index";
    }

    @GetMapping("/logoutinfo")
    public String getLogout() throws JsonProcessingException
    {
        return "logout";
    }

    private Optional<AuthenticationDTO> buildAuthentication()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof PartnerNetOpenIdConnectUser)
        {
            return Optional.of(AuthenticationDTO.of((PartnerNetOpenIdConnectUser) principal));
        }

        if (principal instanceof PartnerNetSaml2AuthenticationPrincipal)
        {
            return Optional.of(AuthenticationDTO.of((PartnerNetSaml2AuthenticationPrincipal) principal));
        }

        return Optional.empty();
    }
}
