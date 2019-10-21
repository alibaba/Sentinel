package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

/**
 * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StdScalarDeserializer} instead.
 */
@Deprecated
public abstract class StdScalarDeserializer<T>
    extends com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StdDeserializer<T>
{
    protected StdScalarDeserializer(Class<?> vc) {
        super(vc);
    } 
}
