/**
 *
 */
package at.porscheinformatik.idp.openidconnect;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetCompanyDTO extends AbstractCompanyDependentClaim
{
    private static final long serialVersionUID = 1L;

    private final String companyNumber;
    private final String name;

    public PartnerNetCompanyDTO(Integer companyId, String companyNumber, String name)
    {
        super(companyId);

        this.companyNumber = companyNumber;
        this.name = name;
    }

    public String getCompanyNumber()
    {
        return companyNumber;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "OpenIdUserInfoCompanyDTO [companyNumber="
            + companyNumber
            + ", name="
            + name
            + ", getCompanyId()="
            + getCompanyId()
            + "]";
    }

}
