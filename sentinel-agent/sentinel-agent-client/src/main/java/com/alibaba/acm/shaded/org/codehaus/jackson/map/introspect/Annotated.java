package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeBindings;

/**
 * Shared base class used for anything on which annotations (included
 * within a {@link AnnotationMap}).
 */
public abstract class Annotated
{
    protected Annotated() { }
    
    public abstract <A extends Annotation> A getAnnotation(Class<A> acls);

    public final <A extends Annotation> boolean hasAnnotation(Class<A> acls)
    {
        return getAnnotation(acls) != null;
    }

    /**
     * Fluent factory method that will construct a new instance that uses specified
     * instance annotations instead of currently configured ones.
     * 
     * @since 1.9
     */
    public abstract Annotated withAnnotations(AnnotationMap fallback);

    /**
     * Fluent factory method that will construct a new instance that uses
     * annotations from specified {@link Annotated} as fallback annotations
     * 
     * @since 1.9
     */
    public final Annotated withFallBackAnnotationsFrom(Annotated annotated) {
        return withAnnotations(AnnotationMap.merge(getAllAnnotations(), annotated.getAllAnnotations()));
    }
    
    /**
     * Method that can be used to find actual JDK element that this instance
     * represents. It is non-null, except for method/constructor parameters
     * which do not have a JDK counterpart.
     */
    public abstract AnnotatedElement getAnnotated();

    protected abstract int getModifiers();

    public final boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    public abstract String getName();

    /**
     * Full generic type of the annotated element; definition
     * of what exactly this means depends on sub-class.
     */
    public JavaType getType(TypeBindings context) {
        return context.resolveType(getGenericType());
    }

    /**
     * Full generic type of the annotated element; definition
     * of what exactly this means depends on sub-class.
     * 
     * @since 1.5
     */
    public abstract Type getGenericType();

    /**
     * "Raw" type (type-erased class) of the annotated element; definition
     * of what exactly this means depends on sub-class.
     * 
     * @since 1.5
     */
    public abstract Class<?> getRawType();

    protected abstract AnnotationMap getAllAnnotations();
}

