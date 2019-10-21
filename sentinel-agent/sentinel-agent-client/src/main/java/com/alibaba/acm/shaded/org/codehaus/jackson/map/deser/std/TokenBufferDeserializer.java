package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.TokenBuffer;

/**
 * We also want to directly support deserialization of
 * {@link TokenBuffer}.
 *<p>
 * Note that we use scalar deserializer base just because we claim
 * to be of scalar for type information inclusion purposes; actual
 * underlying content can be of any (Object, Array, scalar) type.
 *
 * @since 1.9 (moved from higher-level package)
 */
@JacksonStdImpl
public class TokenBufferDeserializer
    extends StdScalarDeserializer<TokenBuffer>
{
    public TokenBufferDeserializer() { super(TokenBuffer.class); }

    @Override
    public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        TokenBuffer tb = new TokenBuffer(jp.getCodec());
        // quite simple, given that TokenBuffer is a JsonGenerator:
        tb.copyCurrentStructure(jp);
        return tb;
    }
}
