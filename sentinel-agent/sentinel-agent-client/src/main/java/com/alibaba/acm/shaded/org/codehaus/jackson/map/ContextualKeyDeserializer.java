package com.alibaba.acm.shaded.org.codehaus.jackson.map;

/**
 * Add-on interface that {@link KeyDeserializer}s can implement to get a callback
 * that can be used to create contextual instances of key deserializer to use for
 * handling Map keys of supported type. This can be useful
 * for key deserializers that can be configured by annotations, or should otherwise
 * have differing behavior depending on what kind of Map property keys are being deserialized.
 * 
 * @since 1.8
 */
public interface ContextualKeyDeserializer
{
    /**
     * Method called to see if a different (or differently configured) key deserializer
     * is needed to deserialize keys of specified Map property.
     * Note that instance that this method is called on is typically shared one and
     * as a result method should <b>NOT</b> modify this instance but rather construct
     * and return a new instance. This instance should only be returned as-is, in case
     * it is already suitable for use.
     * 
     * @param config Current deserialization configuration
     * @param property Method, field or constructor parameter that declared Map for which
     *   contextual instance will be used. Will not be available when deserializing root-level
     *   Map value; otherwise should not be null.
     * 
     * @return Key deserializer to use for deserializing keys specified Map property,
     *   may be this instance or a new instance.
     */
    public KeyDeserializer createContextual(DeserializationConfig config,
            BeanProperty property)
        throws JsonMappingException;
}
