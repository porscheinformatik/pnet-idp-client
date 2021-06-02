/**
 *
 */
package at.porscheinformatik.idp.saml2;

/**
 * @author Daniel Furtlehner
 */
public enum Gender
{
    UNKNOWN(0),
    MALE(1),
    FEMALE(2),
    NOT_APPLICABLE(9);

    private final int code;

    Gender(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }

    public static Gender fromCode(int code)
    {
        for (Gender gender : values())
        {
            if (gender.getCode() == code)
            {
                return gender;
            }
        }

        return UNKNOWN;
    }
}
