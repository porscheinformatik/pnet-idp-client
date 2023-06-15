/**
 *
 */
package at.porscheinformatik.idp.openidconnect.convert;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import at.porscheinformatik.idp.PartnerNetFunctionalNumberDTO;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetFunctionalNumberConverter extends AbstractCollectionConverter<PartnerNetFunctionalNumberDTO>
{
    private final Converter<Object, ?> intConverter;
    private final Converter<Object, ?> stringConverter;

    public PartnerNetFunctionalNumberConverter(Converter<Object, ?> intConverter, Converter<Object, ?> stringConverter)
    {
        super();

        this.intConverter = intConverter;
        this.stringConverter = stringConverter;
    }

    @Override
    protected PartnerNetFunctionalNumberDTO doConvertEntry(Map<String, Object> entry)
    {
        Integer companyId = (Integer) intConverter.convert(entry.get("company_id"));
        String matchcode = (String) stringConverter.convert(entry.get("matchcode"));
        Integer number = (Integer) intConverter.convert(entry.get("number"));

        return new PartnerNetFunctionalNumberDTO(companyId, matchcode, number);

    }
}
