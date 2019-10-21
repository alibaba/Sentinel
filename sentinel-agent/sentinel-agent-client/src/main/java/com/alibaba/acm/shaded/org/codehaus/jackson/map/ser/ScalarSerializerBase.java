package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

/**
 * @deprecated Since 1.9 use {@link  com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.SerializerBase} instead.
 */
@Deprecated
public abstract class ScalarSerializerBase<T>
    extends com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.SerializerBase<T>
{
    protected ScalarSerializerBase(Class<T> t) {
        super(t);
    }

    @SuppressWarnings("unchecked")
    protected ScalarSerializerBase(Class<?> t, boolean dummy) {
        super((Class<T>) t);
    }
}
