/**
 *
 */
package at.porscheinformatik.idp.openidconnect.convert;

import at.porscheinformatik.idp.PartnerNetCompanyDTO;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetCompanyConverter extends AbstractCollectionConverter<PartnerNetCompanyDTO> {

    private final Converter<Object, ?> intConverter;
    private final Converter<Object, ?> stringConverter;

    public PartnerNetCompanyConverter(Converter<Object, ?> intConverter, Converter<Object, ?> stringConverter) {
        super();
        this.intConverter = intConverter;
        this.stringConverter = stringConverter;
    }

    @Override
    protected PartnerNetCompanyDTO doConvertEntry(Map<String, Object> entry) {
        Integer companyId = (Integer) intConverter.convert(entry.get("company_id"));
        String companyNumber = (String) stringConverter.convert(entry.get("company_number"));
        String name = (String) stringConverter.convert(entry.get("name"));

        return new PartnerNetCompanyDTO(companyId, companyNumber, name);
    }
}
