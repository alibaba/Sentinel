package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype;

import java.util.Collection;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.AnnotationIntrospector;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.MapperConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedClass;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedMember;

/**
 * Helper object used for handling registration on resolving of supertypes
 * to subtypes.
 * 
 * @since 1.5
 */
public abstract class SubtypeResolver
{
    /**
     * Method for registering specified subtypes (possibly including type
     * names); for type entries without name, non-qualified class name
     * as used as name (unless overridden by annotation).
     */
    public abstract void registerSubtypes(NamedType... types);

    public abstract void registerSubtypes(Class<?>... classes);
    
    /**
     * Method for finding out all reachable subtypes for a property specified
     * by given element (method or field)
     */
    public abstract Collection<NamedType> collectAndResolveSubtypes(AnnotatedMember property,
            MapperConfig<?> config, AnnotationIntrospector ai);

    /**
     * Method for finding out all reachable subtypes for given type.
     */
    public abstract Collection<NamedType> collectAndResolveSubtypes(AnnotatedClass basetype,
            MapperConfig<?> config, AnnotationIntrospector ai);
}
