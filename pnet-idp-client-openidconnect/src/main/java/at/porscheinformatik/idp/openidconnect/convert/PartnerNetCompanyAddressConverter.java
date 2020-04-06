/**
 * 
 */
package at.porscheinformatik.idp.openidconnect.convert;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import at.porscheinformatik.idp.openidconnect.PartnerNetCompanyAddressDTO;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetCompanyAddressConverter extends AbstractCollectionConverter<PartnerNetCompanyAddressDTO>
{
    private final Converter<Object, ?> intConverter;
    private final Converter<Object, ?> stringConverter;

    public PartnerNetCompanyAddressConverter(Converter<Object, ?> intConverter, Converter<Object, ?> stringConverter)
    {
        super();

        this.intConverter = intConverter;
        this.stringConverter = stringConverter;
    }

    @Override
    protected PartnerNetCompanyAddressDTO doConvertEntry(Map<String, Object> entry)
    {
        Integer companyId = (Integer) intConverter.convert(entry.get("company_id"));
        String street = (String) stringConverter.convert(entry.get("street_address"));
        String postalCode = (String) stringConverter.convert(entry.get("postal_code"));
        String city = (String) stringConverter.convert(entry.get("locality"));
        String countryCode = (String) stringConverter.convert(entry.get("country_code"));

        return new PartnerNetCompanyAddressDTO(companyId, street, postalCode, city, countryCode);

    }
}
