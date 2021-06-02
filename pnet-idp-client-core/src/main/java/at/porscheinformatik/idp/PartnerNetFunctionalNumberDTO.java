/**
 *
 */
package at.porscheinformatik.idp;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetFunctionalNumberDTO extends AbstractCompanyDependentClaim
{
    private static final long serialVersionUID = 1L;

    private final String matchcode;
    private final Integer number;

    public PartnerNetFunctionalNumberDTO(Integer companyId, String matchcode, Integer number)
    {
        super(companyId);

        this.matchcode = matchcode;
        this.number = number;
    }

    public String getMatchcode()
    {
        return matchcode;
    }

    public Integer getNumber()
    {
        return number;
    }

    @Override
    public String toString()
    {
        return "OpenIdUserInfoFunctionalNumberDTO [matchcode=" + matchcode + ", number=" + number + "]";
    }

}
