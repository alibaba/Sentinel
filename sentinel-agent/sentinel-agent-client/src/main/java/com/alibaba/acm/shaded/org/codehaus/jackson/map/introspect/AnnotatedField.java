package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

/**
 * Object that represents non-static (and usually non-transient/volatile)
 * fields of a class.
 * 
 * @author tatu
 */
public final class AnnotatedField
    extends AnnotatedMember
{
    protected final Field _field;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public AnnotatedField(Field field, AnnotationMap annMap)
    {
        super(annMap);
        _field = field;
    }

    @Override
    public AnnotatedField withAnnotations(AnnotationMap ann) {
        return new AnnotatedField(_field, ann);
    }
    
    /**
     * Method called to override an annotation, usually due to a mix-in
     * annotation masking or overriding an annotation 'real' constructor
     * has.
     */
    public void addOrOverride(Annotation a)
    {
        _annotations.add(a);
    }

    /*
    /**********************************************************
    /* Annotated impl
    /**********************************************************
     */

    @Override
    public Field getAnnotated() { return _field; }

    @Override
    public int getModifiers() { return _field.getModifiers(); }

    @Override
    public String getName() { return _field.getName(); }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> acls)
    {
        return _annotations.get(acls);
    }

    @Override
    public Type getGenericType() {
        return _field.getGenericType();
    }

    @Override
    public Class<?> getRawType() {
        return _field.getType();
    }
    
    /*
    /**********************************************************
    /* AnnotatedMember impl
    /**********************************************************
     */

    @Override
    public Class<?> getDeclaringClass() { return _field.getDeclaringClass(); }

    @Override
    public Member getMember() { return _field; }

    @Override
    public void setValue(Object pojo, Object value)
        throws IllegalArgumentException
    {
        try {
            _field.set(pojo, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to setValue() for field "
                    +getFullName()+": "+e.getMessage(), e);
        }
    }
    
    /*
    /**********************************************************
    /* Extended API, generic
    /**********************************************************
     */

    public String getFullName() {
        return getDeclaringClass().getName() + "#" + getName();
    }

    public int getAnnotationCount() { return _annotations.size(); }

    @Override
    public String toString()
    {
        return "[field "+getName()+", annotations: "+_annotations+"]";
    }
}

