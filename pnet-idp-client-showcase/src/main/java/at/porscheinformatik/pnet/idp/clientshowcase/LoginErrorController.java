package at.porscheinformatik.pnet.idp.clientshowcase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/loginerror")
public class LoginErrorController
{
    private Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping
    @ResponseBody
    public String getErrorMessage(HttpServletRequest request)
    {
        Exception e = extractException(request);

        if (e == null)
        {
            return "Came here without exception";
        }
        else
        {
            logger.error("Error on authentication", e);
        }

        return buildExceptionMessage(e);
    }

    private String buildExceptionMessage(Exception e)
    {
        StringBuilder builder = new StringBuilder("Got authentication exception: ")
            .append(e.getClass().getSimpleName())
            .append(": ")
            .append(e.getMessage());

        return builder.toString();
    }

    private Exception extractException(HttpServletRequest request)
    {
        Exception exception = (Exception) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

        if (exception != null)
        {
            return exception;
        }

        HttpSession session = request.getSession(false);

        if (session != null)
        {
            return (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }

        return null;
    }
}
