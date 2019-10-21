package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.EnumValues;

/**
 * @deprecated Since 1.9 use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.EnumSerializer}
 */
@Deprecated
@JacksonStdImpl
public class EnumSerializer
    extends com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.EnumSerializer
{
    public EnumSerializer(EnumValues v) {
        super(v);
    }
}
