package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Defines interface for resolvers that can resolve abstract types into concrete
 * ones; either by using static mappings, or possibly by materializing
 * implementations dynamically.
 *<p>
 * Note that this class was changed in 1.8, to separate two parts of operation
 * (defaulting, which has higher precedence, and fallback, which has lowest
 * precedence). Existing m
 * 
 * @since 1.6
 */
public abstract class AbstractTypeResolver
{
    /**
     * Try to locate a subtype for given abstract type, to either resolve
     * to a concrete type, or at least to a more-specific (and hopefully supported)
     * abstract type, one which may have registered deserializers.
     * Method is called before trying to locate registered deserializers
     * (as well as standard abstract type defaulting that core Jackson does),
     * so it is typically implemented to add custom mappings of common abstract
     * types (like specify which concrete implementation to use for binding
     * {@link java.util.List}s).
     *<p>
     * Note that this method does not necessarily have to do full resolution
     * of bindings; that is, it is legal to return type that could be further
     * resolved: caller is expected to keep calling this method on registered
     * resolvers, until a concrete type is located.
     * 
     * @param config Configuration in use; should always be of type
     *    <code>DeserializationConfig</code>
     * 
     * @since 1.8
     */
    public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
        return null;
    }
    
    /**
     * Method called to try to resolve an abstract type into
     * concrete type (usually for purposes of deserializing),
     * when no concrete implementation was found.
     * It will be called after checking all other possibilities,
     * including defaulting.
     * 
     * @param config Configuration in use; should always be of type
     *    <code>DeserializationConfig</code>
     * @param type Type for which materialization maybe needed
     * 
     * @return Resolved concrete type (which should retain generic
     *    type parameters of input type, if any), if resolution succeeds;
     *    null if resolver does not know how to resolve type
     *    
     * @since 1.6
     */
    public JavaType resolveAbstractType(DeserializationConfig config,
            JavaType type) {
        return null;
    }
}
