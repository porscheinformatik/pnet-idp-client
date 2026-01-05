package at.porscheinformatik.pnet.idp.clientshowcase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

@RestController
public class ShowcaseErrorController implements ErrorController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ErrorAttributes errorAttributes;

    public ShowcaseErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @GetMapping("/error")
    public String getErrorMessage(HttpServletRequest request) {
        ServletWebRequest requestAttributes = new ServletWebRequest(request);

        Map<String, Object> attributes = errorAttributes.getErrorAttributes(
            requestAttributes,
            ErrorAttributeOptions.of(Include.MESSAGE, Include.EXCEPTION, Include.STACK_TRACE)
        );

        String exception = attributes.getOrDefault("exception", "").toString();
        String trace = attributes.getOrDefault("trace", "").toString();
        String message = attributes.getOrDefault("message", "").toString();

        logger.error("Exception {} {}: {}", exception, message, trace);

        return message;
    }

    @GetMapping("/loginerror")
    public String getLoginErrorMessage(HttpServletRequest request) {
        Exception e = extractLoginException(request);

        if (e == null) {
            return "Came here without exception";
        }
        logger.error("Error on authentication", e);

        return buildExceptionMessage(e, "Got authentication exception: ");
    }

    @GetMapping("/accessdenied")
    public String getAccessDeniedErrorMessage(HttpServletRequest request) {
        Exception e = (Exception) request.getAttribute(WebAttributes.ACCESS_DENIED_403);

        if (e == null) {
            return "Came here without exception";
        }
        logger.error("Error on authentication", e);

        return buildExceptionMessage(e, "Got access denied exception: ");
    }

    private String buildExceptionMessage(Exception e, String message) {
        return message + e.getClass().getSimpleName() + ": " + e.getMessage();
    }

    private Exception extractLoginException(HttpServletRequest request) {
        Exception exception = (Exception) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

        if (exception != null) {
            return exception;
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            return (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }

        return null;
    }
}
