package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;
import java.util.Date;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;

/**
 * Simple deserializer for handling {@link java.util.Date} values.
 *<p>
 * One way to customize Date formats accepted is to override method
 * {@link DeserializationContext#parseDate} that this basic
 * deserializer calls.
 * 
 * @since 1.9 (moved from higher-level package)
 */
public class DateDeserializer
    extends StdScalarDeserializer<Date>
{
    public DateDeserializer() { super(Date.class); }
    
    @Override
    public java.util.Date deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        return _parseDate(jp, ctxt);
    }
}
