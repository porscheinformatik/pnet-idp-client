package at.porscheinformatik.idp.openidconnect.convert;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

public class AdditionalLocalesConverter implements Converter<Object, List<Locale>>
{

    @Override
    public List<Locale> convert(Object source)
    {
        Collection<String> languageTags = ConverterUtils.cast(source, String.class);

        return languageTags //
            .stream()
            .map(Locale::forLanguageTag)
            .collect(Collectors.toList());
    }

}
