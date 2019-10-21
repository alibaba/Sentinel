package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeBindings;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Intermediate base class that encapsulates features that
 * constructors and methods share.
 */
public abstract class AnnotatedWithParams
    extends AnnotatedMember
{
    /**
     * Annotations associated with parameters of the annotated
     * entity (method or constructor parameters)
     */
    protected final AnnotationMap[] _paramAnnotations;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected AnnotatedWithParams(AnnotationMap annotations, AnnotationMap[] paramAnnotations)
    {
        super(annotations);
        _paramAnnotations = paramAnnotations;
    }

    /**
     * Method called to override a class annotation, usually due to a mix-in
     * annotation masking or overriding an annotation 'real' class
     */
    public final void addOrOverride(Annotation a)
    {
        _annotations.add(a);
    }

    /**
     * Method called to override a method parameter annotation,
     * usually due to a mix-in
     * annotation masking or overriding an annotation 'real' method
     * has.
     */
    public final void addOrOverrideParam(int paramIndex, Annotation a)
    {
        AnnotationMap old = _paramAnnotations[paramIndex];
        if (old == null) {
            old = new AnnotationMap();
            _paramAnnotations[paramIndex] = old;
        }
        old.add(a);
    }

    /**
     * Method called to augment annotations, by adding specified
     * annotation if and only if it is not yet present in the
     * annotation map we have.
     */
    public final void addIfNotPresent(Annotation a)
    {
        _annotations.addIfNotPresent(a);
    }

    
    /**
     * Method called by parameter object when an augmented instance is created;
     * needs to replace parameter with new instance
     * 
     * @since 1.9
     */
    protected AnnotatedParameter replaceParameterAnnotations(int index, AnnotationMap ann)
    {
        _paramAnnotations[index] = ann;
        return getParameter(index);
    }    

    /*
    /**********************************************************
    /* Helper methods for subclasses
    /**********************************************************
     */

    protected JavaType getType(TypeBindings bindings, TypeVariable<?>[] typeParams)
    {
        // [JACKSON-468] Need to consider local type binding declarations too...
        if (typeParams != null && typeParams.length > 0) {
            bindings = bindings.childInstance();
            for (TypeVariable<?> var : typeParams) {
                String name = var.getName();
                // to prevent infinite loops, need to first add placeholder ("<T extends Enum<T>>" etc)
                bindings._addPlaceholder(name);
                // About only useful piece of information is the lower bound (which is at least Object.class)
                Type lowerBound = var.getBounds()[0];
                JavaType type = (lowerBound == null) ? TypeFactory.unknownType()
                        : bindings.resolveType(lowerBound);
                bindings.addBinding(var.getName(), type);
            }
        }
        return bindings.resolveType(getGenericType());
    }

    /*
    /**********************************************************
    /* Partial Annotated impl
    /**********************************************************
     */

    @Override
    public final <A extends Annotation> A getAnnotation(Class<A> acls)
    {
        return _annotations.get(acls);
    }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    public final AnnotationMap getParameterAnnotations(int index)
    {
        if (_paramAnnotations != null) {
            if (index >= 0 && index <= _paramAnnotations.length) {
                return _paramAnnotations[index];
            }
        }
        return null;
    }

    public final AnnotatedParameter getParameter(int index) {
        return new AnnotatedParameter(this, getParameterType(index),
                _paramAnnotations[index], index);
    }

    public abstract int getParameterCount();

    public abstract Class<?> getParameterClass(int index);

    public abstract Type getParameterType(int index);

    /**
     * Method called to fully resolve type of one of parameters, given
     * specified type variable bindings.
     * 
     * @since 1.8
     */
    public final JavaType resolveParameterType(int index, TypeBindings bindings) {
        return bindings.resolveType(getParameterType(index));
    }
    
    public final int getAnnotationCount() { return _annotations.size(); }

    /**
     * Method that can be used to (try to) call this object without arguments.
     * This may succeed or fail, depending on expected number
     * of arguments: caller needs to take care to pass correct number.
     * Exceptions are thrown directly from actual low-level call.
     *<p>
     * Note: only works for constructors and static methods.
     * 
     * @since 1.9
     */
    public abstract Object call() throws Exception;

    /**
     * Method that can be used to (try to) call this object with specified arguments.
     * This may succeed or fail, depending on expected number
     * of arguments: caller needs to take care to pass correct number.
     * Exceptions are thrown directly from actual low-level call.
     *<p>
     * Note: only works for constructors and static methods.
     * 
     * @since 1.9
     */
    public abstract Object call(Object[] args) throws Exception;

    /**
     * Method that can be used to (try to) call this object with single arguments.
     * This may succeed or fail, depending on expected number
     * of arguments: caller needs to take care to pass correct number.
     * Exceptions are thrown directly from actual low-level call.
     *<p>
     * Note: only works for constructors and static methods.
     * 
     * @since 1.9
     */
    public abstract Object call1(Object arg) throws Exception;
}
