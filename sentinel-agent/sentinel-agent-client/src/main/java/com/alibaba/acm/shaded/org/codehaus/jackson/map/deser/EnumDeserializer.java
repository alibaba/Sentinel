package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.EnumResolver;

/**
 * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.EnumDeserializer} instead.
 */
@Deprecated
public class EnumDeserializer
    extends com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.EnumDeserializer
{
    public EnumDeserializer(EnumResolver<?> res) {
        super(res);
    }
}
