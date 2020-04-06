/**
 * 
 */
package at.porscheinformatik.idp.openidconnect.convert;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Daniel Furtlehner
 * @param <T> type of object to convert
 */
public abstract class AbstractCollectionConverter<T> implements Converter<Object, Collection<T>>
{

    @Override
    public Collection<T> convert(Object source)
    {
        if (source == null)
        {
            return null;
        }

        Collection<Map<String, Object>> sourceCollection = cast(source);

        return sourceCollection //
            .stream()
            .map(this::doConvertEntry)
            .collect(Collectors.toList());
    }

    protected abstract T doConvertEntry(Map<String, Object> entry);

    @SuppressWarnings("unchecked")
    private Collection<Map<String, Object>> cast(Object source)
    {
        if (!Collection.class.isAssignableFrom(source.getClass()))
        {
            throw new IllegalArgumentException(String.format("Expected a list of maps to convert, but got %s", source));
        }

        Collection<Object> sourceCollection = (Collection<Object>) source;

        for (Object object : sourceCollection)
        {
            if (!Map.class.isAssignableFrom(object.getClass()))
            {
                throw new IllegalArgumentException(
                    String.format("Expected a list of maps to convert, but got %s", source));
            }
        }

        return (Collection<Map<String, Object>>) source;
    }
}
