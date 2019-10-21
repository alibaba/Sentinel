package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Object that represents method parameters, mostly so that associated
 * annotations can be processed conveniently. Note that many of accessors
 * can not return meaningful values since parameters do not have stand-alone
 * JDK objects associated; so access should mostly be limited to checking
 * annotation values which are properly aggregated and included.
 *<p>
 * Note: as of version 1.7, this type extends {@link AnnotatedMember}, since
 * it behaves like a member for the most part, but earlier it just extended
 * {@link Annotated}
 */
public final class AnnotatedParameter
    extends AnnotatedMember
{
    /**
     * Member (method, constructor) that this parameter belongs to
     * 
     * @since 1.7
     */
    protected final AnnotatedWithParams _owner;
    
    /**
     * JDK type of the parameter, possibly contains generic type information
     */
    protected final Type _type;
    
    /**
     * Index of the parameter within argument list
     * 
     * @since 1.9
     */
    protected final int _index;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public AnnotatedParameter(AnnotatedWithParams owner, Type type,  AnnotationMap annotations,
            int index)
    {
        super(annotations);
        _owner = owner;
        _type = type;
        _index = index;
    }

    @Override
    public AnnotatedParameter withAnnotations(AnnotationMap ann) {
        if (ann == _annotations) {
            return this;
        }
        return _owner.replaceParameterAnnotations(_index, ann);
    }
    
    public void addOrOverride(Annotation a)
    {
        _annotations.add(a);
    }

    /*
    /**********************************************************
    /* Annotated impl
    /**********************************************************
     */

    /**
     * Since there is no matching JDK element, this method will
     * always return null
     */
    @Override
    public AnnotatedElement getAnnotated() { return null; }

    /**
     * Returns modifiers of the constructor, as parameters do not
     * have independent modifiers.
     */
    @Override
    public int getModifiers() { return _owner.getModifiers(); }

    /**
     * Parameters have no names in bytecode (unlike in source code),
     * will always return empty String ("").
     */
    @Override
    public String getName() { return ""; }

    /**
     * Accessor for annotations; all annotations associated with parameters
     * are properly passed and accessible.
     */
    @Override
    public <A extends Annotation> A getAnnotation(Class<A> acls)
    {
        return _annotations.get(acls);
    }

    @Override
    public Type getGenericType() {
        return _type;
    }

    @Override
    public Class<?> getRawType()
    {
        if (_type instanceof Class<?>) {
            return (Class<?>) _type;
        }
        // 14-Mar-2011, tatu: Not optimal, but has to do for now...
        JavaType t = TypeFactory.defaultInstance().constructType(_type);
        return t.getRawClass();
    }

    /*
    /**********************************************************
    /* AnnotatedMember extras
    /**********************************************************
     */

    @Override
    public Class<?> getDeclaringClass() {
        return _owner.getDeclaringClass();
    }

    @Override
    public Member getMember() {
        /* This is bit tricky: since there is no JDK equivalent; can either
         * return null or owner... let's do latter, for now.
         */
        return _owner.getMember();
    }

    @Override
    public void setValue(Object pojo, Object value)
        throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Cannot call setValue() on constructor parameter of "
                +getDeclaringClass().getName());
    }
    
    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    public Type getParameterType() { return _type; }

    /**
     * Accessor for 'owner' of this parameter; method or constructor that
     * has this parameter as member of its argument list.
     * 
     * @return Owner (member or creator) object of this parameter
     * 
     * @since 1.9
     */
    public AnnotatedWithParams getOwner() { return _owner; }
    
    /**
     * Accessor for index of this parameter within argument list
     * 
     * @return Index of this parameter within argument list
     * 
     * @since 1.9
     */
    public int getIndex() { return _index; }

    /*
    /********************************************************
    /* Other
    /********************************************************
     */
    
    @Override
    public String toString()
    {
        return "[parameter #"+getIndex()+", annotations: "+_annotations+"]";
    }
}

