/**
 *
 */
package at.porscheinformatik.idp.openidconnect.convert;

import at.porscheinformatik.idp.PartnerNetCompanyTypeDTO;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetCompanyTypesConverter extends AbstractCollectionConverter<PartnerNetCompanyTypeDTO> {

    private final Converter<Object, ?> intConverter;
    private final Converter<Object, ?> stringConverter;

    public PartnerNetCompanyTypesConverter(Converter<Object, ?> intConverter, Converter<Object, ?> stringConverter) {
        super();
        this.intConverter = intConverter;
        this.stringConverter = stringConverter;
    }

    @Override
    protected PartnerNetCompanyTypeDTO doConvertEntry(Map<String, Object> entry) {
        Integer companyId = (Integer) intConverter.convert(entry.get("company_id"));
        String matchcode = (String) stringConverter.convert(entry.get("company_type_matchcode"));

        return new PartnerNetCompanyTypeDTO(companyId, matchcode);
    }
}
