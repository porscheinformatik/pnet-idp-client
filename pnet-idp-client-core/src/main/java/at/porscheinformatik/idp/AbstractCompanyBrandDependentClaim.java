/**
 *
 */
package at.porscheinformatik.idp;

/**
 * @author Daniel Furtlehner
 */
public class AbstractCompanyBrandDependentClaim extends AbstractCompanyDependentClaim
{
    private static final long serialVersionUID = 1L;

    private final String brandId;

    public AbstractCompanyBrandDependentClaim(Integer companyId, String brandId)
    {
        super(companyId);

        this.brandId = brandId;
    }

    public String getBrandId()
    {
        return brandId;
    }

}
