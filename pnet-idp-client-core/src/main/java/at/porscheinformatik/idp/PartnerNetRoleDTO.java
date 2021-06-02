/**
 *
 */
package at.porscheinformatik.idp;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetRoleDTO extends AbstractCompanyBrandDependentClaim
{
    private static final long serialVersionUID = 1L;

    private final String roleMatchcode;

    public PartnerNetRoleDTO(Integer companyId, String brandId, String roleMatchcode)
    {
        super(companyId, brandId);

        this.roleMatchcode = roleMatchcode;
    }

    public String getRoleMatchcode()
    {
        return roleMatchcode;
    }

    @Override
    public String toString()
    {
        return "OpenIdUserInfoRoleDTO [roleMatchcode="
            + roleMatchcode
            + ", brandId="
            + getBrandId()
            + ", companyId="
            + getCompanyId()
            + "]";
    }

}
