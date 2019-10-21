package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl.UnwrappingBeanSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.BeanSerializerBase;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Serializer class that can serialize arbitrary bean objects
 *<p>
 * Implementation note: we will post-process resulting serializer,
 * to figure out actual serializers for final types. This must be
 * done from {@link #resolve} method, and NOT from constructor;
 * otherwise we could end up with an infinite loop.
 *<p>
 * Since 1.7 instances are immutable; this is achieved by using a
 * separate builder during construction process.
 */
public class BeanSerializer
    extends BeanSerializerBase
{
    /*
    /**********************************************************
    /* Life-cycle: constructors
    /**********************************************************
     */

    /**
     * @param type Nominal type of values handled by this serializer
     * @param properties Property writers used for actual serialization
     */
    public BeanSerializer(JavaType type,
            BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties,
            AnyGetterWriter anyGetterWriter,
            Object filterId)
    {
        super(type, properties, filteredProperties, anyGetterWriter, filterId);
    }

    public BeanSerializer(Class<?> rawType,
            BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties,
            AnyGetterWriter anyGetterWriter,
            Object filterId)
    {
        super(rawType, properties, filteredProperties, anyGetterWriter, filterId);
    }

    /**
     * Copy-constructor that is useful for sub-classes that just want to
     * copy all super-class properties without modifications.
     * 
     * @since 1.7
     */
    protected BeanSerializer(BeanSerializer src) {
        super(src);
    }

    /**
     * Alternate copy constructor that can be used to construct
     * standard {@link BeanSerializer} passing an instance of
     * "compatible enough" source serializer.
     * 
     * @since 1.9
     */
    protected BeanSerializer(BeanSerializerBase src) {
        super(src);
    }
    
    /*
    /**********************************************************
    /* Life-cycle: factory methods, fluent factories
    /**********************************************************
     */

    /**
     * Method for constructing dummy bean deserializer; one that
     * never outputs any properties
     */
    public static BeanSerializer createDummy(Class<?> forType)
    {
        return new BeanSerializer(forType, NO_PROPS, null, null, null);
    }

    @Override
    public JsonSerializer<Object> unwrappingSerializer() {
        return new UnwrappingBeanSerializer(this);
    }
    
    /*
    /**********************************************************
    /* JsonSerializer implementation that differs between impls
    /**********************************************************
     */

    /**
     * Main serialization method that will delegate actual output to
     * configured
     * {@link BeanPropertyWriter} instances.
     */
    @Override
    public final void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        jgen.writeStartObject();
        if (_propertyFilterId != null) {
            serializeFieldsFiltered(bean, jgen, provider);
        } else {
            serializeFields(bean, jgen, provider);
        }
        jgen.writeEndObject();
    }

    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override public String toString() {
        return "BeanSerializer for "+handledType().getName();
    }
}
