/**
 *
 */
package at.porscheinformatik.idp.openidconnect.convert;

import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetContactCompaniesConverter implements Converter<Object, Collection<Integer>> {

    public PartnerNetContactCompaniesConverter() {
        super();
    }

    @Override
    public Collection<Integer> convert(Object source) {
        Collection<Number> collection = ConverterUtils.cast(source, Number.class);

        // We map the collection to a set here to ensure, it always has the same type, regardless of the type parsed by the claims parser.
        return collection //
            .stream()
            .map(Number::intValue)
            .collect(Collectors.toSet());
    }
}
