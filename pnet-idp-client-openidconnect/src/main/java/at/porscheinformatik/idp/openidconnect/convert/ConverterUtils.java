package at.porscheinformatik.idp.openidconnect.convert;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import org.springframework.core.ParameterizedTypeReference;

public final class ConverterUtils {

    private ConverterUtils() {
        super();
    }

    @SuppressWarnings("unchecked")
    public static <ValueT> Collection<ValueT> cast(Object source, ParameterizedTypeReference<ValueT> valueType) {
        ParameterizedType type = (ParameterizedType) valueType.getType();
        Class<ValueT> valueClass = (Class<ValueT>) type.getRawType();

        return cast(source, valueClass);
    }

    @SuppressWarnings("unchecked")
    public static <ValueT> Collection<ValueT> cast(Object source, Class<ValueT> valueClass) {
        if (!Collection.class.isAssignableFrom(source.getClass())) {
            throw new IllegalArgumentException(String.format("Expected a list of maps to convert, but got %s", source));
        }

        Collection<Object> sourceCollection = (Collection<Object>) source;

        for (Object object : sourceCollection) {
            if (!valueClass.isAssignableFrom(object.getClass())) {
                throw new IllegalArgumentException(
                    String.format("Expected a list of maps to convert, but got %s", source)
                );
            }
        }

        return (Collection<ValueT>) source;
    }
}
