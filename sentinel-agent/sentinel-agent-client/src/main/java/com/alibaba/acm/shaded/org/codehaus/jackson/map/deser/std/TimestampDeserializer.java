package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;
import java.sql.Timestamp;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;

/**
 * Simple deserializer for handling {@link java.sql.Timestamp} values.
 *<p>
 * One way to customize Timestamp formats accepted is to override method
 * {@link DeserializationContext#parseDate} that this basic
 * deserializer calls.
 *
 * @since 1.9 (moved from higher-level package)
 */
public class TimestampDeserializer
    extends StdScalarDeserializer<Timestamp>
{
    public TimestampDeserializer() { super(Timestamp.class); }

    @Override
    public java.sql.Timestamp deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        return new Timestamp(_parseDate(jp, ctxt).getTime());
    }
}
