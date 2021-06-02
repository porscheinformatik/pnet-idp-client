/**
 * 
 */
package at.porscheinformatik.idp.openidconnect.convert;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import at.porscheinformatik.idp.PartnerNetContractDTO;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetContractConverter extends AbstractCollectionConverter<PartnerNetContractDTO>
{
    private final Converter<Object, ?> intConverter;
    private final Converter<Object, ?> stringConverter;

    public PartnerNetContractConverter(Converter<Object, ?> intConverter, Converter<Object, ?> stringConverter)
    {
        super();

        this.intConverter = intConverter;
        this.stringConverter = stringConverter;
    }

    @Override
    protected PartnerNetContractDTO doConvertEntry(Map<String, Object> entry)
    {
        Integer companyId = (Integer) intConverter.convert(entry.get("company_id"));
        String brandId = (String) stringConverter.convert(entry.get("brand_id"));
        String contractMatchcode = (String) stringConverter.convert(entry.get("contract_matchcode"));

        return new PartnerNetContractDTO(companyId, brandId, contractMatchcode);
    }
}
