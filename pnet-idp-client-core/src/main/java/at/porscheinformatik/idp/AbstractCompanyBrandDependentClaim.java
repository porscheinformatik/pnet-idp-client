/**
 *
 */
package at.porscheinformatik.idp;

import java.io.Serial;

/**
 * @author Daniel Furtlehner
 */
public class AbstractCompanyBrandDependentClaim extends AbstractCompanyDependentClaim {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String brandId;

    public AbstractCompanyBrandDependentClaim(Integer companyId, String brandId) {
        super(companyId);
        this.brandId = brandId;
    }

    public String getBrandId() {
        return brandId;
    }
}
