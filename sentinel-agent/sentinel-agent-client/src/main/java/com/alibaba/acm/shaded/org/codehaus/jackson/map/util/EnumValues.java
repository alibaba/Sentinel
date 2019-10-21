package com.alibaba.acm.shaded.org.codehaus.jackson.map.util;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.io.SerializedString;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;

/**
 * Helper class used for storing String serializations of
 * enumerations.
 */
public final class EnumValues
{
    /**
     * Since 1.7, we are storing values as SerializedStrings, to further
     * speed up serialization.
     */
    private final EnumMap<?,SerializedString> _values;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private EnumValues(Map<Enum<?>,SerializedString> v) {
        _values = new EnumMap(v);
    }

    public static EnumValues construct(Class<Enum<?>> enumClass, AnnotationIntrospector intr)
    {
        return constructFromName(enumClass, intr);
    }

    public static EnumValues constructFromName(Class<Enum<?>> enumClass, AnnotationIntrospector intr)
    {
        /* [JACKSON-214]: Enum types with per-instance sub-classes
         *   need special handling
         */
    	Class<? extends Enum<?>> cls = ClassUtil.findEnumType(enumClass);
        Enum<?>[] values = cls.getEnumConstants();
        if (values != null) {
            // Type juggling... unfortunate
            Map<Enum<?>,SerializedString> map = new HashMap<Enum<?>,SerializedString>();
            for (Enum<?> en : values) {
                String value = intr.findEnumValue(en);
                map.put(en, new SerializedString(value));
            }
            return new EnumValues(map);
        }
        throw new IllegalArgumentException("Can not determine enum constants for Class "+enumClass.getName());
    }

    public static EnumValues constructFromToString(Class<Enum<?>> enumClass, AnnotationIntrospector intr)
    {
        Class<? extends Enum<?>> cls = ClassUtil.findEnumType(enumClass);
        Enum<?>[] values = cls.getEnumConstants();
        if (values != null) {
            // Type juggling... unfortunate
            Map<Enum<?>,SerializedString> map = new HashMap<Enum<?>,SerializedString>();
            for (Enum<?> en : values) {
                map.put(en, new SerializedString(en.toString()));
            }
            return new EnumValues(map);
        }
        throw new IllegalArgumentException("Can not determine enum constants for Class "+enumClass.getName());
    }

    /**
     * @deprecated since 1.7, use {@link #serializedValueFor} instead
     */
    @Deprecated
    public String valueFor(Enum<?> key)
    {
        SerializedString sstr = _values.get(key);
        return (sstr == null) ? null : sstr.getValue();
    }

    public SerializedString serializedValueFor(Enum<?> key)
    {
        return _values.get(key);
    }
    
    public Collection<SerializedString> values() {
        return _values.values();
    }
}
