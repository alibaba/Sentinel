package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonToken;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * @since 1.9
 */
public class JavaTypeDeserializer
    extends StdScalarDeserializer<JavaType>
{
    public JavaTypeDeserializer() { super(JavaType.class); }
    
    @Override
    public JavaType deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken curr = jp.getCurrentToken();
        // Usually should just get string value:
        if (curr == JsonToken.VALUE_STRING) {
            String str = jp.getText().trim();
            if (str.length() == 0) {
                return getEmptyValue();
            }
            return ctxt.getTypeFactory().constructFromCanonical(str);
        }
        // or occasionally just embedded object maybe
        if (curr == JsonToken.VALUE_EMBEDDED_OBJECT) {
            return (JavaType) jp.getEmbeddedObject();
        }
        throw ctxt.mappingException(_valueClass);
    }
}
