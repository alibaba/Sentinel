package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

/**
 * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.FromStringDeserializer} instead.
 */
@Deprecated
public abstract class FromStringDeserializer<T>
    extends com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.FromStringDeserializer<T>
{
    protected FromStringDeserializer(Class<?> vc) {
        super(vc);
    }
}
