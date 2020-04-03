/**
 * 
 */
package at.porscheinformatik.pnet.idp.clientshowcase;

/**
 * @author Daniel Furtlehner
 */
public class AuthenticationDTO
{
    private final String externalId;
    private final boolean secondFactorUsed;

    public AuthenticationDTO(String externalId, boolean secondFactorUsed)
    {
        super();

        this.externalId = externalId;
        this.secondFactorUsed = secondFactorUsed;
    }

    public String getExternalId()
    {
        return externalId;
    }

    public boolean isSecondFactorUsed()
    {
        return secondFactorUsed;
    }

}
