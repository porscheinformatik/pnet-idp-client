/**
 * 
 */
package at.porscheinformatik.idp.openidconnect.convert;

import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

/**
 * @author Daniel Furtlehner
 */
public class ObjectToIntegerConverter implements GenericConverter
{

    @Override
    public Set<ConvertiblePair> getConvertibleTypes()
    {
        return Collections.singleton(new ConvertiblePair(Object.class, Integer.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType)
    {
        if (source == null)
        {
            return null;
        }

        if (source instanceof Number)
        {
            return ((Number) source).intValue();
        }

        return Integer.valueOf(source.toString());
    }
}
