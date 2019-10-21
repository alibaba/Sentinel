package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

/**
 * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.ContainerDeserializerBase} instead.
 */
@Deprecated
public abstract class ContainerDeserializer<T>
    extends com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.ContainerDeserializerBase<T>
{
    protected ContainerDeserializer(Class<?> selfType)
    {
        super(selfType);
    }
}
