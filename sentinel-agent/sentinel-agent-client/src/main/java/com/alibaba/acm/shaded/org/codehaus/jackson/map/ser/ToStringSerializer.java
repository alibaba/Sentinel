package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;

/**
 * @deprecated Since 1.9 use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.ToStringSerializer}
 */
@Deprecated
@JacksonStdImpl
public final class ToStringSerializer
    extends com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.ToStringSerializer
{
    public final static ToStringSerializer instance = new ToStringSerializer();
}
