package at.porscheinformatik.idp;

import java.util.Objects;

public enum PartnerNetAuthenticationProvider {
    UNKNOWN,
    PNET,
    PHS_ENTRA_ID,
    PHS_ENTRA_ID_CF;

    public static PartnerNetAuthenticationProvider valueOfOrUnknown(String value) {
        if (value == null) {
            return UNKNOWN;
        }

        value = value.toLowerCase();

        for (PartnerNetAuthenticationProvider authProvider : values()) {
            if (Objects.equals(authProvider.name().toLowerCase(), value)) {
                return authProvider;
            }
        }

        return UNKNOWN;
    }
}
