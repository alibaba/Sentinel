package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.BeanDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.BeanDeserializerFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription;

/**
 * Abstract class that defines API for objects that can be registered (for {@link BeanDeserializerFactory}
 * to participate in constructing {@link BeanDeserializer} instances.
 * This is typically done by modules that want alter some aspects of deserialization
 * process; and is preferable to sub-classing of {@link BeanDeserializerFactory}.
 *<p>
 * Sequence in which callback methods are called is as follows:
 *  <li>updateBuilder is called once all initial pieces for building deserializer
 *    have been collected
 *   </li>
 *  <li><code>modifyDeserializer</code> is called after deserializer has been built
 *    by {@link BeanDeserializerBuilder}
 *    but before it is returned to be used
 *   </li>
 * </ol>
 *<p>
 * Default method implementations are "no-op"s, meaning that methods are implemented
 * but have no effect; this is mostly so that new methods can be added in later
 * versions.
 * 
 * @since 1.7
 */
public abstract class BeanDeserializerModifier
{
    /**
     * Method called by {@link BeanDeserializerFactory} when it has collected
     * basic information such as tentative list of properties to deserialize.
     *
     * Implementations may choose to modify state of builder (to affect deserializer being
     * built), or even completely replace it (if they want to build different kind of
     * deserializer). Typically changes mostly concern set of properties to deserialize.
     */
    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
            BasicBeanDescription beanDesc, BeanDeserializerBuilder builder) {
        return builder;
    }

    /**
     * Method called by {@link BeanDeserializerFactory} after constructing default
     * bean deserializer instance with properties collected and ordered earlier.
     * Implementations can modify or replace given deserializer and return deserializer
     * to use. Note that although initial deserializer being passed is of type
     * {@link BeanDeserializer}, modifiers may return deserializers of other types;
     * and this is why implementations must check for type before casting.
     */
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
            BasicBeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        return deserializer;
    }
}