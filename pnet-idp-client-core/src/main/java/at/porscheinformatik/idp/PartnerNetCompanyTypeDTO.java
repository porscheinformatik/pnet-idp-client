/**
 *
 */
package at.porscheinformatik.idp;

import java.io.Serial;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetCompanyTypeDTO extends AbstractCompanyDependentClaim {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String companyTypeMatchcode;

    public PartnerNetCompanyTypeDTO(Integer companyId, String companyTypeMatchcode) {
        super(companyId);
        this.companyTypeMatchcode = companyTypeMatchcode;
    }

    public String getCompanyTypeMatchcode() {
        return companyTypeMatchcode;
    }

    @Override
    public String toString() {
        return (
            "PartnerNetCompanyTypeDTO [companyTypeMatchcode=" +
            companyTypeMatchcode +
            ", getCompanyId()=" +
            getCompanyId() +
            "]"
        );
    }
}
