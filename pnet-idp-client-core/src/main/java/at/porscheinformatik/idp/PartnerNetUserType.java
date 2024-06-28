package at.porscheinformatik.idp;

import java.util.Objects;

public enum PartnerNetUserType
{
    UNKNOWN(0, "undefined"),
    PERSON(0, "person"),
    BOT(1, "bot"),
    TEST_USER(2, "test_user");

    private final int code;
    private final String oidcValue;

    PartnerNetUserType(int code, String oidcValue)
    {
        this.code = code;
        this.oidcValue = oidcValue;
    }

    public int getCode()
    {
        return code;
    }

    public String getOidcValue()
    {
        return oidcValue;
    }

    public static PartnerNetUserType fromCode(int code)
    {
        for (PartnerNetUserType type : values())
        {
            if (type.getCode() == code)
            {
                return type;
            }
        }

        return UNKNOWN;
    }

    public static PartnerNetUserType fromOidcValue(String oidcValue)
    {
        for (PartnerNetUserType type : values())
        {
            if (Objects.equals(type.getOidcValue(), oidcValue))
            {
                return type;
            }
        }

        return UNKNOWN;
    }

}
