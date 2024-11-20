package at.porscheinformatik.idp.saml2;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public final class PartnerNetSaml2AuthenticationRequestUtils {

    private static final String FORCE_AUTHENTICATION_ATTR = "poi.saml2.force_authn";
    private static final String SESSION_AGE_ATTR = "poi.saml2.session_age";
    private static final String MAX_AGE_MFA_ATTR = "poi.saml2.max_age_mfa";
    private static final String TENANT_ATTR = "poi.saml2.tenant";
    private static final String NIST_LEVEL_ATTR = "poi.saml2.nist_level";

    private PartnerNetSaml2AuthenticationRequestUtils() {
        super();
    }

    public static void storeForceAuthentication(HttpServletRequest request, boolean force) {
        if (force) {
            request.getSession().setAttribute(FORCE_AUTHENTICATION_ATTR, Boolean.TRUE);
        } else {
            request.getSession().removeAttribute(FORCE_AUTHENTICATION_ATTR);
        }
    }

    public static boolean forceAuthenticationRequested(HttpServletRequest request) {
        return Boolean.TRUE.equals(request.getSession().getAttribute(FORCE_AUTHENTICATION_ATTR));
    }

    public static void storeSessionAge(HttpServletRequest request, Optional<Integer> maxSessionAge) {
        if (maxSessionAge.isPresent()) {
            request.getSession().setAttribute(SESSION_AGE_ATTR, maxSessionAge.get());
        } else {
            request.getSession().removeAttribute(SESSION_AGE_ATTR);
        }
    }

    public static void storeMaxAgeMfa(HttpServletRequest request, Optional<Integer> maxAgeMfa) {
        if (maxAgeMfa.isPresent()) {
            request.getSession().setAttribute(MAX_AGE_MFA_ATTR, maxAgeMfa.get());
        } else {
            request.getSession().removeAttribute(MAX_AGE_MFA_ATTR);
        }
    }

    public static Integer sessionAgeRequested(HttpServletRequest request) {
        return (Integer) request.getSession().getAttribute(SESSION_AGE_ATTR);
    }

    public static void storeTenant(HttpServletRequest request, Optional<String> tenant) {
        if (tenant.isPresent()) {
            request.getSession().setAttribute(TENANT_ATTR, tenant.get());
        } else {
            request.getSession().removeAttribute(TENANT_ATTR);
        }
    }

    public static String tenantRequested(HttpServletRequest request) {
        return (String) request.getSession().getAttribute(TENANT_ATTR);
    }

    public static void storeNistLevel(HttpServletRequest request, Optional<Integer> nistLevel) {
        if (nistLevel.isPresent()) {
            request.getSession().setAttribute(NIST_LEVEL_ATTR, nistLevel.get());
        } else {
            request.getSession().removeAttribute(NIST_LEVEL_ATTR);
        }
    }

    public static Integer getRequestedNistLevel(HttpServletRequest request) {
        return (Integer) request.getSession().getAttribute(NIST_LEVEL_ATTR);
    }
}
