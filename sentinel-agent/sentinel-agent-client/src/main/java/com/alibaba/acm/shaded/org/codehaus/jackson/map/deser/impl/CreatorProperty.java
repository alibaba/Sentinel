package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.SettableBeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedMember;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedParameter;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.Annotations;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * This concrete sub-class implements property that is passed
 * via Creator (constructor or static factory method).
 * It is not a full-featured implementation in that its set method
 * should never be called -- instead, value must separately passed.
 *<p>
 * Note on injectable values (1.9): unlike with other mutators, where
 * deserializer and injecting are separate, here we deal the two as related
 * things. This is necessary to add proper priority, as well as to simplify
 * coordination.
 *<p>
 * Note that this class was moved in Jackson 1.9
 * from being a static sub-class of "com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.SettableBeanProperty"
 * into separate class, to make it easier to use it for custom creators.
 */
public class CreatorProperty
    extends SettableBeanProperty
{
    /**
     * Placeholder that represents constructor parameter, when it is created
     * from actual constructor.
     * May be null when a synthetic instance is created.
     */
    protected final AnnotatedParameter _annotated;

    /**
     * Id of value to inject, if value injection should be used for this parameter
     * (in addition to, or instead of, regular deserialization).
     * 
     * @since 1.9
     */
    protected final Object _injectableValueId;
    
    /**
     * @param name Name of the logical property
     * @param type Type of the property, used to find deserializer
     * @param typeDeser Type deserializer to use for handling polymorphic type
     *    information, if one is needed
     * @param contextAnnotations Contextual annotations (usually by class that
     *    declares creator [constructor, factory method] that includes
     *    this property)
     * @param param Representation of property, constructor or factory
     *    method parameter; used for accessing annotations of the property
     */
    public CreatorProperty(String name, JavaType type, TypeDeserializer typeDeser,
            Annotations contextAnnotations, AnnotatedParameter param,
            int index, Object injectableValueId)
    {
        super(name, type, typeDeser, contextAnnotations);
        _annotated = param;
        _propertyIndex = index;
        _injectableValueId = injectableValueId;
    }

    protected CreatorProperty(CreatorProperty src, JsonDeserializer<Object> deser) {
        super(src, deser);
        _annotated = src._annotated;
        _injectableValueId = src._injectableValueId;
    }
    
    @Override
    public CreatorProperty withValueDeserializer(JsonDeserializer<Object> deser) {
        return new CreatorProperty(this, deser);
    }

    /**
     * Method that can be called to locate value to be injected for this
     * property, if it is configured for this.
     * 
     * @since 1.9
     */
    public Object findInjectableValue(DeserializationContext context, Object beanInstance)
    {
        if (_injectableValueId == null) {
            throw new IllegalStateException("Property '"+getName()
                    +"' (type "+getClass().getName()+") has no injectable value id configured");
        }
        return context.findInjectableValue(_injectableValueId, this, beanInstance);
    }
    
    /**
     * Method to find value to inject, and inject it to this property.
     * 
     * @since 1.9
     */
    public void inject(DeserializationContext context, Object beanInstance)
        throws IOException
    {
        set(beanInstance, findInjectableValue(context, beanInstance));
    }
    
    /*
    /**********************************************************
    /* BeanProperty impl
    /**********************************************************
     */
    
    @Override
    public <A extends Annotation> A getAnnotation(Class<A> acls) {
        if (_annotated == null) {
            return null;
        }
        return _annotated.getAnnotation(acls);
    }

    @Override public AnnotatedMember getMember() {  return _annotated; }
    
    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    @Override
    public void deserializeAndSet(JsonParser jp, DeserializationContext ctxt,
                                  Object instance)
        throws IOException, JsonProcessingException
    {
        set(instance, deserialize(jp, ctxt));
    }

    @Override
    public void set(Object instance, Object value)
        throws IOException
    {
        /* Hmmmh. Should we return quietly (NOP), or error?
         * For now, let's just bail out without fuss.
         */
        //throw new IllegalStateException("Method should never be called on a "+getClass().getName());
    }

    @Override
    public Object getInjectableValueId() {
        return _injectableValueId;
    }
}
