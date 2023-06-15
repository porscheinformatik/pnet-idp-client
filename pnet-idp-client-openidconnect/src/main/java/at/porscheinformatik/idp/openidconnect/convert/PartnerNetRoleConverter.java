/**
 *
 */
package at.porscheinformatik.idp.openidconnect.convert;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import at.porscheinformatik.idp.PartnerNetRoleDTO;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetRoleConverter extends AbstractCollectionConverter<PartnerNetRoleDTO>
{
    private final Converter<Object, ?> intConverter;
    private final Converter<Object, ?> stringConverter;

    public PartnerNetRoleConverter(Converter<Object, ?> intConverter, Converter<Object, ?> stringConverter)
    {
        super();

        this.intConverter = intConverter;
        this.stringConverter = stringConverter;
    }

    @Override
    protected PartnerNetRoleDTO doConvertEntry(Map<String, Object> entry)
    {
        Integer companyId = (Integer) intConverter.convert(entry.get("company_id"));
        String brandId = (String) stringConverter.convert(entry.get("brand_id"));
        String roleMatchcode = (String) stringConverter.convert(entry.get("role_matchcode"));

        return new PartnerNetRoleDTO(companyId, brandId, roleMatchcode);
    }
}
