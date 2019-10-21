package com.alibaba.acm.shaded.org.codehaus.jackson.map;

/**
 * Interface used to indicate serializers that want to do post-processing
 * after construction and being added to {@link SerializerProvider},
 * but before being used. This is typically used to resolve references
 * to other contained types; for example, bean serializers use this
 * to eagerly find serializers for contained field types.
 */
public interface ResolvableSerializer
{
    /**
     * Method called after {@link SerializerProvider} has registered
     * the serializer, but before it has returned it to the caller.
     * Called object can then resolve its dependencies to other types,
     * including self-references (direct or indirect).
     *
     * @param provider Provider that has constructed serializer this method
     *   is called on.
     */
    public abstract void resolve(SerializerProvider provider)
        throws JsonMappingException;
}
