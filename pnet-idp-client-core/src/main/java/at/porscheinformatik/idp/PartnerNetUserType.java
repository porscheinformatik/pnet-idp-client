package at.porscheinformatik.idp;

import java.util.Objects;

public enum PartnerNetUserType {
    UNKNOWN,
    PERSON,
    PRIVILEGED,
    BOT,
    TEST_USER,
    PRIVILEGED_TEST_USER;

    public static PartnerNetUserType valueOfOrUnknown(String value) {
        if (value == null) {
            return UNKNOWN;
        }

        value = value.toLowerCase();

        for (PartnerNetUserType userType : values()) {
            if (Objects.equals(userType.name().toLowerCase(), value)) {
                return userType;
            }
        }

        return UNKNOWN;
    }
}
