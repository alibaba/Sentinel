package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;

/**
 * Interface that can be implemented by objects that know how to
 * serialize themselves to JSON, using {@link JsonGenerator}
 * (and {@link SerializerProvider} if necessary).
 *<p>
 * Note that implementing this interface binds implementing object
 * closely to Jackson API, and that it is often not necessary to do
 * so -- if class is a bean, it can be serialized without
 * implementing this interface.
 *<p>
 * NOTE: as of version 1.5, this interface is missing one crucial
 * aspect, that of dealing with type information embedding.
 * Because of this, this interface is deprecated, although will be
 * fully supported for all 1.x releases, and will work except for
 * cases where polymorphic type information handling is needed for
 * type (in which case implementing if {@link JsonSerializableWithType} is crucial).
 *
 * @see com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializableWithType
 *
 * @since 1.5
 */
@Deprecated
public interface JsonSerializable
{
    public void serialize(JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException;
}
