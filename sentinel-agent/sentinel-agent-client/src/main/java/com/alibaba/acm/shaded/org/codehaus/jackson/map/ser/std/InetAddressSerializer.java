package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.IOException;
import java.net.InetAddress;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer;

/**
 * Simple serializer for {@link java.net.InetAddress}. Main complexity is
 * with registration, since same serializer is to be used for sub-classes.
 *
 * @since 1.8
 */
public class InetAddressSerializer
    extends ScalarSerializerBase<InetAddress>
{
    public final static InetAddressSerializer instance = new InetAddressSerializer();
    
    public InetAddressSerializer() { super(InetAddress.class); }

    @Override
    public void serialize(InetAddress value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        // Ok: get textual description; choose "more specific" part
        String str = value.toString().trim();
        int ix = str.indexOf('/');
        if (ix >= 0) {
            if (ix == 0) { // missing host name; use address
                str = str.substring(1);
            } else { // otherwise use name
                str = str.substring(0, ix);
            }
        }
        jgen.writeString(str);
    }

    @Override
    public void serializeWithType(InetAddress value, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonGenerationException
    {
        // Better ensure we don't use specific sub-classes...
        typeSer.writeTypePrefixForScalar(value, jgen, InetAddress.class);
        serialize(value, jgen, provider);
        typeSer.writeTypeSuffixForScalar(value, jgen);
    }
}
