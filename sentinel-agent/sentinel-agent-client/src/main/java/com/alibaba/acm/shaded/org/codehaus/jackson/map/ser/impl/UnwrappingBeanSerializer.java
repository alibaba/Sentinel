package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.BeanSerializerBase;

public class UnwrappingBeanSerializer
    extends BeanSerializerBase
{
    /*
    /**********************************************************
    /* Life-cycle: constructors
    /**********************************************************
     */

    /**
     * Constructor used for creating unwrapping instance of a
     * standard <code>BeanSerializer</code>
     */
    public UnwrappingBeanSerializer(BeanSerializerBase src) {
        super(src);
    }

    /*
    /**********************************************************
    /* Life-cycle: factory methods, fluent factories
    /**********************************************************
     */

    @Override
    public JsonSerializer<Object> unwrappingSerializer() {
        // already unwrapping, nothing more to do:
        return this;
    }

    @Override
    public boolean isUnwrappingSerializer() {
        return true; // sure is
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
        if (_propertyFilterId != null) {
            serializeFieldsFiltered(bean, jgen, provider);
        } else {
            serializeFields(bean, jgen, provider);
        }
    }

    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override public String toString() {
        return "UnwrappingBeanSerializer for "+handledType().getName();
    }
}
