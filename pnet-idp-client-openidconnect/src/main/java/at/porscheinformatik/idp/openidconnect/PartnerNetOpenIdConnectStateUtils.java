package at.porscheinformatik.idp.openidconnect;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class PartnerNetOpenIdConnectStateUtils {

    public static final String CUSTOM_STATE_DELIMITER = ":";

    private PartnerNetOpenIdConnectStateUtils() {
        super();
    }

    public static String buildState(String randomPart, String customPart) {
        if (randomPart == null || randomPart.isEmpty()) {
            throw new IllegalArgumentException("randomPart cannot be null or empty");
        }

        String state = customPart == null ? randomPart : randomPart + CUSTOM_STATE_DELIMITER + customPart;

        return Base64.getEncoder().encodeToString(state.getBytes(StandardCharsets.UTF_8));
    }

    public static String getCustomState(String state) {
        if (state == null) {
            return null;
        }

        String decodedState = new String(Base64.getDecoder().decode(state), StandardCharsets.UTF_8);

        int indexOfDelimiter = decodedState.indexOf(CUSTOM_STATE_DELIMITER);
        return indexOfDelimiter != -1 ? decodedState.substring(indexOfDelimiter + 1) : null;
    }

    public static String getCustomState(HttpServletRequest request) {
        String state = request.getParameter("state");
        return getCustomState(state);
    }
}
