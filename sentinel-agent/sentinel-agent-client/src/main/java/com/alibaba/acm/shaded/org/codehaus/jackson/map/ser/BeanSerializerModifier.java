package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import java.util.List;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializationConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription;

/**
 * Abstract class that defines API for objects that can be registered (for {@link BeanSerializerFactory}
 * to participate in constructing {@link BeanSerializer} instances.
 * This is typically done by modules that want alter some aspects of serialization
 * process; and is preferable to sub-classing of {@link BeanSerializerFactory}.
 *<p>
 * Sequence in which callback methods are called is as follows:
 * <ol>
 *  <li>After factory has collected tentative set of properties (instances of
 *     <code>BeanPropertyWriter</code>) is sent for modification via
 *     {@link #changeProperties}. Changes can include removal, addition and
 *     replacement of suggested properties.
 *  <li>Resulting set of properties are ordered (sorted) by factory, as per
 *     configuration, and then {@link #orderProperties} is called to allow
 *     modifiers to alter ordering.
 *  <li>After all bean properties and related information is accumulated,
 *     {@link #updateBuilder} is called with builder, to allow builder state
 *     to be modified (including possibly replacing builder itself if necessary)
 *  <li>Once all bean information has been determined,
 *     factory creates default {@link BeanSerializer} instance and passes
 *     it to modifiers using {@link #modifySerializer}, for possible
 *     modification or replacement (by any {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer} instance)
 * </ol>
 *<p>
 * Default method implementations are "no-op"s, meaning that methods are implemented
 * but have no effect.
 * 
 * @since 1.7
 */
public abstract class BeanSerializerModifier
{
    /**
     * Method called by {@link BeanSerializerFactory} with tentative set
     * of discovered properties.
     * Implementations can add, remove or replace any of passed properties.
     *
     * Properties <code>List</code> passed as argument is modifiable, and returned List must
     * likewise be modifiable as it may be passed to multiple registered
     * modifiers.
     */
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
            BasicBeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        return beanProperties;
    }

    /**
     * Method called by {@link BeanSerializerFactory} with set of properties
     * to serialize, in default ordering (based on defaults as well as 
     * possible type annotations).
     * Implementations can change ordering any way they like.
     *
     * Properties <code>List</code> passed as argument is modifiable, and returned List must
     * likewise be modifiable as it may be passed to multiple registered
     * modifiers.
     */
    public List<BeanPropertyWriter> orderProperties(SerializationConfig config,
            BasicBeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        return beanProperties;
    }

    /**
     * Method called by {@link BeanSerializerFactory} after collecting all information
     * regarding POJO to serialize and updating builder with it, but before constructing
     * serializer.
     * Implementations may choose to modify state of builder (to affect serializer being
     * built), or even completely replace it (if they want to build different kind of
     * serializer). Typically, however, passed-in builder is returned, possibly with
     * some modifications.
     */
    public BeanSerializerBuilder updateBuilder(SerializationConfig config,
            BasicBeanDescription beanDesc, BeanSerializerBuilder builder) {
        return builder;
    }
    
    /**
     * Method called by {@link BeanSerializerFactory} after constructing default
     * bean serializer instance with properties collected and ordered earlier.
     * Implementations can modify or replace given serializer and return serializer
     * to use. Note that although initial serializer being passed is of type
     * {@link BeanSerializer}, modifiers may return serializers of other types;
     * and this is why implementations must check for type before casting.
     */
    public JsonSerializer<?> modifySerializer(SerializationConfig config,
            BasicBeanDescription beanDesc, JsonSerializer<?> serializer) {
        return serializer;
    }
}
