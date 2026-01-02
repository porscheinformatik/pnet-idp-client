/**
 *
 */
package at.porscheinformatik.idp;

import java.io.Serial;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetCompanyDTO extends AbstractCompanyDependentClaim {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String companyNumber;
    private final String name;

    public PartnerNetCompanyDTO(Integer companyId, String companyNumber, String name) {
        super(companyId);
        this.companyNumber = companyNumber;
        this.name = name;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return (
            String.format(
                "OpenIdUserInfoCompanyDTO [companyNumber=%s, name=%s, getCompanyId()=%s]",
                companyNumber,
                name,
                getCompanyId()
            )
        );
    }
}
