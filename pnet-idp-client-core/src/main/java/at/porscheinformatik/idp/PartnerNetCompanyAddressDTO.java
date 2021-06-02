/**
 *
 */
package at.porscheinformatik.idp;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetCompanyAddressDTO extends AbstractCompanyDependentClaim
{
    private static final long serialVersionUID = 1L;

    private final String street;
    private final String postalCode;
    private final String city;
    private final String countryCode;

    public PartnerNetCompanyAddressDTO(Integer companyId, String street, String postalCode, String city,
        String countryCode)
    {
        super(companyId);

        this.street = street;
        this.postalCode = postalCode;
        this.city = city;
        this.countryCode = countryCode;
    }

    public String getStreet()
    {
        return street;
    }

    public String getPostalCode()
    {
        return postalCode;
    }

    public String getCity()
    {
        return city;
    }

    public String getCountryCode()
    {
        return countryCode;
    }

    @Override
    public String toString()
    {
        return "OpenIdUserInfoCompanyAddressDTO [street="
            + street
            + ", postalCode="
            + postalCode
            + ", city="
            + city
            + ", countryCode="
            + countryCode
            + "]";
    }

}
