package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import java.util.*;

/**
 * Abstract class that defines API for objects that provide value to
 * "inject" during deserialization. An instance of this object
 * 
 * @since 1.9
 */
public abstract class InjectableValues
{
    /**
     * Method called to find value identified by id <code>valueId</code> to
     * inject as value of specified property during deserialization, passing
     * POJO instance in which value will be injected if it is available
     * (will be available when injected via field or setter; not available
     * when injected via constructor or factory method argument).
     * 
     * @param valueId Object that identifies value to inject; may be a simple
     *   name or more complex identifier object, whatever provider needs
     * @param ctxt Deserialization context
     * @param forProperty Bean property in which value is to be injected
     * @param beanInstance Bean instance that contains property to inject,
     *    if available; null if bean has not yet been constructed.
     */
    public abstract Object findInjectableValue(Object valueId,
            DeserializationContext ctxt, BeanProperty forProperty,
            Object beanInstance);

    /*
    /**********************************************************
    /* Standard implementation
    /**********************************************************
     */

    /**
     * Simple standard implementation which uses a simple Map to
     * store values to inject, identified by simple String keys.
     */
    public static class Std
        extends InjectableValues
    {
        protected final Map<String,Object> _values;
        
        public Std() {
            this(new HashMap<String,Object>());
        }

        public Std(Map<String,Object> values) {
            _values = values;
        }

        public Std addValue(String key, Object value)
        {
            _values.put(key, value);
            return this;
        }

        public Std addValue(Class<?> classKey, Object value)
        {
            _values.put(classKey.getName(), value);
            return this;
        }
        
        @Override
        public Object findInjectableValue(Object valueId,
                DeserializationContext ctxt, BeanProperty forProperty,
                Object beanInstance)
        {
            if (!(valueId instanceof String)) {
                String type = (valueId == null) ? "[null]" : valueId.getClass().getName();
                throw new IllegalArgumentException("Unrecognized inject value id type ("+type+"), expecting String");
            }
            String key = (String) valueId;
            Object ob = _values.get(key);
            if (ob == null && !_values.containsKey(key)) {
                throw new IllegalArgumentException("No injectable id with value '"+key+"' found (for property '"
                        +forProperty.getName()+"')");
            }
            return ob;
        }
    }
}
