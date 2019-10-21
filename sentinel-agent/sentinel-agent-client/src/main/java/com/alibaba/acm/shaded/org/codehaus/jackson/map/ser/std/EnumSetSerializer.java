package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.IOException;
import java.util.EnumSet;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

public class EnumSetSerializer
    extends AsArraySerializerBase<EnumSet<? extends Enum<?>>>
{
    public EnumSetSerializer(JavaType elemType, BeanProperty property)
    {
        super(EnumSet.class, elemType, true, null, property, null);
    }

    @Override
    public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
        // no typing for enums (always "hard" type)
        return this;
    }
    
    @Override
    public void serializeContents(EnumSet<? extends Enum<?>> value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        JsonSerializer<Object> enumSer = _elementSerializer;
        /* Need to dynamically find instance serializer; unfortunately
         * that seems to be the only way to figure out type (no accessors
         * to the enum class that set knows)
         */
        for (Enum<?> en : value) {
            if (enumSer == null) {
                /* 12-Jan-2010, tatu: Since enums can not be polymorphic, let's
                 *   not bother with typed serializer variant here
                 */
                enumSer = provider.findValueSerializer(en.getDeclaringClass(), _property);
            }
            enumSer.serialize(en, jgen, provider);
        }
    }
}
