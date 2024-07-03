package at.porscheinformatik.pnet.idp.clientshowcase;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ShowcaseErrorController implements ErrorController
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ErrorAttributes errorAttributes;

    public ShowcaseErrorController(ErrorAttributes errorAttributes)
    {
        super();

        this.errorAttributes = errorAttributes;
    }

    @GetMapping("/error")
    @ResponseBody
    public String getErrorMessage(HttpServletRequest request)
    {
        ServletWebRequest requestAttributes = new ServletWebRequest(request);

        Map<String, Object> attributes = errorAttributes.getErrorAttributes(requestAttributes,
            ErrorAttributeOptions.of(Include.MESSAGE, Include.EXCEPTION));

        Throwable e = (Throwable) attributes.get("exception");

        if (e == null)
        {
            return "Came here without exception";
        }
        logger.error("Error in app", e);

        return (String) attributes.get("message");
    }

    @GetMapping("/loginerror")
    @ResponseBody
    public String getLoginErrorMessage(HttpServletRequest request)
    {
        Exception e = extractLoginException(request);

        if (e == null)
        {
            return "Came here without exception";
        }
        logger.error("Error on authentication", e);

        return buildExceptionMessage(e, "Got authentication exception: ");
    }

    @GetMapping("/accessdenied")
    @ResponseBody
    public String getAccessDeniedErrorMessage(HttpServletRequest request)
    {
        Exception e = (Exception) request.getAttribute(WebAttributes.ACCESS_DENIED_403);

        if (e == null)
        {
            return "Came here without exception";
        }
        logger.error("Error on authentication", e);

        return buildExceptionMessage(e, "Got access denied exception: ");
    }

    private String buildExceptionMessage(Exception e, String message)
    {
        return message + e.getClass().getSimpleName() + ": " + e.getMessage();
    }

    private Exception extractLoginException(HttpServletRequest request)
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
