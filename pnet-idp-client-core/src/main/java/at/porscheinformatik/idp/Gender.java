/**
 *
 */
package at.porscheinformatik.idp;

import java.util.Objects;

/**
 * @author Daniel Furtlehner
 */
public enum Gender {
    UNKNOWN(0, "undefined"),
    MALE(1, "male"),
    FEMALE(2, "female"),
    NOT_APPLICABLE(9, "");

    private final int code;
    private final String oidcValue;

    Gender(int code, String oidcValue) {
        this.code = code;
        this.oidcValue = oidcValue;
    }

    public int getCode() {
        return code;
    }

    public String getOidcValue() {
        return oidcValue;
    }

    public static Gender fromCode(int code) {
        for (Gender gender : values()) {
            if (gender.getCode() == code) {
                return gender;
            }
        }

        return UNKNOWN;
    }

    public static Gender fromOidcValue(String oidcValue) {
        for (Gender gender : values()) {
            if (Objects.equals(gender.getOidcValue(), oidcValue)) {
                return gender;
            }
        }

        return UNKNOWN;
    }
}
