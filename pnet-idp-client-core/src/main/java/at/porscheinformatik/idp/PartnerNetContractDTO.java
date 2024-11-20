/**
 *
 */
package at.porscheinformatik.idp;

import java.io.Serial;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetContractDTO extends AbstractCompanyBrandDependentClaim {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String contractMatchcode;

    public PartnerNetContractDTO(Integer companyId, String brandId, String contractMatchcode) {
        super(companyId, brandId);
        this.contractMatchcode = contractMatchcode;
    }

    public String getContractMatchcode() {
        return contractMatchcode;
    }

    @Override
    public String toString() {
        return (
            "OpenIdUserInfoContractDTO [contractMatchcode=" +
            contractMatchcode +
            ", getBrandId()=" +
            getBrandId() +
            ", getCompanyId()=" +
            getCompanyId() +
            "]"
        );
    }
}
