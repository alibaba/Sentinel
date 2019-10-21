package com.alibaba.acm.shaded.org.codehaus.jackson.map.module;

import java.lang.reflect.Modifier;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.AbstractTypeResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.ClassKey;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Simple {@link AbstractTypeResolver} implementation, which is
 * based on static mapping from abstract super types into
 * sub types (concrete or abstract), but retaining generic
 * parameterization.
 * Can be used for things like specifying which implementation of
 * {@link java.util.Collection} to use:
 *<pre>
 *  SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
 *  // To make all properties declared as Collection, List, to LinkedList
 *  resolver.addMapping(Collection.class, LinkedList.class);
 *  resolver.addMapping(List.class, LinkedList.class);
 *</pre>
 * Can also be used as an alternative to per-class annotations when defining
 * concrete implementations; however, only works with abstract types (since
 * this is only called for abstract types)
 *
 * @since 1.8
 */
public class SimpleAbstractTypeResolver extends AbstractTypeResolver
{
    /**
     * Mappings from super types to subtypes
     */
    protected final HashMap<ClassKey,Class<?>> _mappings = new HashMap<ClassKey,Class<?>>();

    /**
     * Method for adding a mapping from super type to specific subtype.
     * Arguments will be checked by method, to ensure that <code>superType</code>
     * is abstract (since resolver is never called for concrete classes);
     * as well as to ensure that there is supertype/subtype relationship
     * (to ensure there won't be cycles during resolution).
     * 
     * @param superType Abstract type to resolve
     * @param subType Sub-class of superType, to map superTo to
     * 
     * @return This resolver, to allow chaining of initializations
     */
    public <T> SimpleAbstractTypeResolver addMapping(Class<T> superType, Class<? extends T> subType)
    {
        // Sanity checks, just in case someone tries to force typing...
        if (superType == subType) {
            throw new IllegalArgumentException("Can not add mapping from class to itself");
        }
        if (!superType.isAssignableFrom(subType)) {
            throw new IllegalArgumentException("Can not add mapping from class "+superType.getName()
                    +" to "+subType.getName()+", as latter is not a subtype of former");
        }
        if (!Modifier.isAbstract(superType.getModifiers())) {
            throw new IllegalArgumentException("Can not add mapping from class "+superType.getName()
                    +" since it is not abstract");
        }
        _mappings.put(new ClassKey(superType), subType);
        return this;
    }

    @Override
    public JavaType findTypeMapping(DeserializationConfig config, JavaType type)
    {
        // this is the main mapping base, so let's 
        Class<?> src = type.getRawClass();
        Class<?> dst = _mappings.get(new ClassKey(src));
        if (dst == null) {
            return null;
        }
        return type.narrowBy(dst);
    }

    
    @Override
    public JavaType resolveAbstractType(DeserializationConfig config, JavaType type)
    {
        // never materialize anything, so:
        return null;
    }
}
