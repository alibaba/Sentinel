package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.lang.reflect.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonMappingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedMethod;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Class that represents a "wildcard" set method which can be used
 * to generically set values of otherwise unmapped (aka "unknown")
 * properties read from Json content.
 *<p>
 * !!! Note: might make sense to refactor to share some code
 * with {@link SettableBeanProperty}?
 */
public final class SettableAnyProperty
{
    /**
     * Method used for setting "any" properties, along with annotation
     * information. Retained to allow contextualization of any properties.
     * 
     * @since 1.7
     */
    final protected BeanProperty _property;
    
    /**
     * Physical JDK object used for assigning properties.
     */
    final protected Method _setter;

    final protected JavaType _type;

    protected JsonDeserializer<Object> _valueDeserializer;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */
    
    /**
     * @deprecated Since 1.9 - use variant that takes deserializer
     */
    @Deprecated
    public SettableAnyProperty(BeanProperty property, AnnotatedMethod setter, JavaType type) {
        this(property, setter, type, null);
    }

    public SettableAnyProperty(BeanProperty property, AnnotatedMethod setter, JavaType type,
            JsonDeserializer<Object> valueDeser) {
        this(property, setter.getAnnotated(), type, valueDeser);
    }

    public SettableAnyProperty(BeanProperty property, Method rawSetter, JavaType type,
            JsonDeserializer<Object> valueDeser) {
        _property = property;
        _type = type;
        _setter = rawSetter;
        _valueDeserializer = valueDeser;
    }

    public SettableAnyProperty withValueDeserializer(JsonDeserializer<Object> deser) {
        return new SettableAnyProperty(_property, _setter, _type, deser);
    }
    
    /**
     * @deprecated Since 1.9 - construct with deserializer
     */
    @Deprecated
    public void setValueDeserializer(JsonDeserializer<Object> deser)
    {
        if (_valueDeserializer != null) { // sanity check
            throw new IllegalStateException("Already had assigned deserializer for SettableAnyProperty");
        }
        _valueDeserializer = deser;
    }
    
    /*
    /**********************************************************
    /* Public API, accessors
    /**********************************************************
     */

    public BeanProperty getProperty() { return _property; }
    
    public boolean hasValueDeserializer() { return (_valueDeserializer != null); }

    public JavaType getType() { return _type; }

    /*
    /**********************************************************
    /* Public API, deserialization
    /**********************************************************
     */
    
    /**
     * Method called to deserialize appropriate value, given parser (and
     * context), and set it using appropriate method (a setter method).
     */
    public final void deserializeAndSet(JsonParser jp, DeserializationContext ctxt,
                                        Object instance, String propName)
        throws IOException, JsonProcessingException
    {
        set(instance, propName, deserialize(jp, ctxt));
    }

    public final Object deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NULL) {
            return null;
        }
        return _valueDeserializer.deserialize(jp, ctxt);
    }

    public final void set(Object instance, String propName, Object value)
        throws IOException
    {
        try {
            _setter.invoke(instance, propName, value);
        } catch (Exception e) {
            _throwAsIOE(e, propName, value);
        }
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */

    /**
     * @param e Exception to re-throw or wrap
     * @param propName Name of property (from Json input) to set
     * @param value Value of the property
     */
    protected void _throwAsIOE(Exception e, String propName, Object value)
        throws IOException
    {
        if (e instanceof IllegalArgumentException) {
            String actType = (value == null) ? "[NULL]" : value.getClass().getName();
            StringBuilder msg = new StringBuilder("Problem deserializing \"any\" property '").append(propName);
            msg.append("' of class "+getClassName()+" (expected type: ").append(_type);
            msg.append("; actual type: ").append(actType).append(")");
            String origMsg = e.getMessage();
            if (origMsg != null) {
                msg.append(", problem: ").append(origMsg);
            } else {
                msg.append(" (no error message provided)");
            }
            throw new JsonMappingException(msg.toString(), null, e);
        }
        if (e instanceof IOException) {
            throw (IOException) e;
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        // let's wrap the innermost problem
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        throw new JsonMappingException(t.getMessage(), null, t);
    }

    private String getClassName() { return _setter.getDeclaringClass().getName(); }

    @Override public String toString() { return "[any property on class "+getClassName()+"]"; }
}
