package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.SettableAnyProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.SettableBeanProperty;

/**
 * Simple container used for temporarily buffering a set of
 * <code>PropertyValue</code>s.
 * Using during construction of beans (and Maps) that use Creators, 
 * and hence need buffering before instance (that will have properties
 * to assign values to) is constructed.
 */
public final class PropertyValueBuffer
{
    final JsonParser _parser;
    final DeserializationContext _context;
    
    /**
     * Buffer used for storing creator parameters for constructing
     * instance
     */
    final Object[] _creatorParameters;
    
    /**
     * Number of creator parameters we are still missing.
     *<p>
     * NOTE: assumes there are no duplicates, for now.
     */
    private int _paramsNeeded;
    
    /**
     * If we get non-creator parameters before or between
     * creator parameters, those need to be buffered. Buffer
     * is just a simple linked list
     */
    private PropertyValue _buffered;
    
    public PropertyValueBuffer(JsonParser jp, DeserializationContext ctxt, int paramCount)
    {
        _parser = jp;
        _context = ctxt;
        _paramsNeeded = paramCount;
        _creatorParameters = new Object[paramCount];
    }

    public void inject(SettableBeanProperty[] injectableProperties)
    {
        for (int i = 0, len = injectableProperties.length; i < len; ++i) {
            SettableBeanProperty prop = injectableProperties[i];
            if (prop != null) {
                // null since there is no POJO yet
                _creatorParameters[i] = _context.findInjectableValue(prop.getInjectableValueId(),
                        prop, null);
            }
        }
    }
    
    /**
     * @param defaults If any of parameters requires nulls to be replaced with a non-null
     *    object (usually primitive types), this is a non-null array that has such replacement
     *    values (and nulls for cases where nulls are ok)
     */
    protected final Object[] getParameters(Object[] defaults)
    {
        if (defaults != null) {
            for (int i = 0, len = _creatorParameters.length; i < len; ++i) {
                if (_creatorParameters[i] == null) {
                    Object value = defaults[i];
                    if (value != null) {
                        _creatorParameters[i] = value;
                    }
                }
            }
        }
        return _creatorParameters;
    }

    protected PropertyValue buffered() { return _buffered; }
    
    /**
     * @return True if we have received all creator parameters
     */
    public boolean assignParameter(int index, Object value) {
        _creatorParameters[index] = value;
        return --_paramsNeeded <= 0;
    }
    
    public void bufferProperty(SettableBeanProperty prop, Object value) {
        _buffered = new PropertyValue.Regular(_buffered, value, prop);
    }
    
    public void bufferAnyProperty(SettableAnyProperty prop, String propName, Object value) {
        _buffered = new PropertyValue.Any(_buffered, value, prop, propName);
    }

    public void bufferMapProperty(Object key, Object value) {
        _buffered = new PropertyValue.Map(_buffered, value, key);
    }
}

