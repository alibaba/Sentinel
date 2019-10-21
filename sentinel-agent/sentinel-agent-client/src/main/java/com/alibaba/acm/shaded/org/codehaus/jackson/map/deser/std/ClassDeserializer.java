package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonToken;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;

/**
 * 
 * @since 1.9 (renamed from 'com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.StdDeserializer#ClassDeserializer')
 */
@JacksonStdImpl
public class ClassDeserializer
    extends StdScalarDeserializer<Class<?>>
{
    public ClassDeserializer() { super(Class.class); }

    @Override
    public Class<?> deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken curr = jp.getCurrentToken();
        // Currently will only accept if given simple class name
        if (curr == JsonToken.VALUE_STRING) {
            String className = jp.getText();
            // [JACKSON-597]: support primitive types (and void)
            if (className.indexOf('.') < 0) {
                if ("int".equals(className)) return Integer.TYPE;
                if ("long".equals(className)) return Long.TYPE;
                if ("float".equals(className)) return Float.TYPE;
                if ("double".equals(className)) return Double.TYPE;
                if ("boolean".equals(className)) return Boolean.TYPE;
                if ("byte".equals(className)) return Byte.TYPE;
                if ("char".equals(className)) return Character.TYPE;
                if ("short".equals(className)) return Short.TYPE;
                if ("void".equals(className)) return Void.TYPE;
            }
            try {
                return Class.forName(jp.getText());
            } catch (ClassNotFoundException e) {
                throw ctxt.instantiationException(_valueClass, e);
            }
        }
        throw ctxt.mappingException(_valueClass, curr);
    }
}
