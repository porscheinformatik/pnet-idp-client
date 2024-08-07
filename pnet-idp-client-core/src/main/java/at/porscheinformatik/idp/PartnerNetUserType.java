package at.porscheinformatik.idp;

import java.util.Objects;

public enum PartnerNetUserType
{
    UNKNOWN,
    PERSON,
    BOT,
    TEST_USER;

    public static PartnerNetUserType valueOfOrUnknown(String value)
    {
        value = value.toLowerCase();
        
        for (PartnerNetUserType userType : values())
        {
            if (Objects.equals(userType.name().toLowerCase(), value))
            {
                return userType;
            }
        }

        return UNKNOWN;
    }
}
