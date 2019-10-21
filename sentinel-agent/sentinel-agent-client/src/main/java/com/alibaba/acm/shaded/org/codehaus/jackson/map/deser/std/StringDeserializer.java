package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.Base64Variants;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonToken;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;

@JacksonStdImpl
public class StringDeserializer
    extends StdScalarDeserializer<String>
{
    public StringDeserializer() { super(String.class); }

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken curr = jp.getCurrentToken();
        // Usually should just get string value:
        if (curr == JsonToken.VALUE_STRING) {
            return jp.getText();
        }
        // [JACKSON-330]: need to gracefully handle byte[] data, as base64
        if (curr == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object ob = jp.getEmbeddedObject();
            if (ob == null) {
                return null;
            }
            if (ob instanceof byte[]) {
                return Base64Variants.getDefaultVariant().encode((byte[]) ob, false);
            }
            // otherwise, try conversion using toString()...
            return ob.toString();
        }
        // Can deserialize any scalar value, but not markers
        if (curr.isScalarValue()) {
            return jp.getText();
        }
        throw ctxt.mappingException(_valueClass, curr);
    }

    // 1.6: since we can never have type info ("natural type"; String, Boolean, Integer, Double):
    // (is it an error to even call this version?)
    @Override
    public String deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        return deserialize(jp, ctxt);
    }}
