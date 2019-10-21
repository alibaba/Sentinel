package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.SettableBeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;

/**
 * Object that is used to collect arguments for non-default creator
 * (non-default-constructor, or argument-taking factory method)
 * before creator can be called.
 * Since ordering of JSON properties is not guaranteed, this may
 * require buffering of values other than ones being passed to
 * creator.
 */
public final class PropertyBasedCreator
{
    protected final ValueInstantiator _valueInstantiator;
    
    /**
     * Map that contains property objects for either constructor or factory
     * method (whichever one is null: one property for each
     * parameter for that one), keyed by logical property name
     */
    protected final HashMap<String, SettableBeanProperty> _properties;

    /**
     * If some property values must always have a non-null value (like
     * primitive types do), this array contains such default values.
     */
    protected Object[]  _defaultValues;

    /**
     * Array that contains properties that expect value to inject, if any;
     * null if no injectable values are expected.
     * 
     * @since 1.9
     */
    protected final SettableBeanProperty[] _propertiesWithInjectables;
    
    public PropertyBasedCreator(ValueInstantiator valueInstantiator)
    {
        _valueInstantiator = valueInstantiator;
        _properties = new HashMap<String, SettableBeanProperty>();
        // [JACKSON-372]: primitive types need extra care
        Object[] defValues = null;
        SettableBeanProperty[] creatorProps = valueInstantiator.getFromObjectArguments();
        SettableBeanProperty[] propertiesWithInjectables = null;
        for (int i = 0, len = creatorProps.length; i < len; ++i) {
            SettableBeanProperty prop = creatorProps[i];
            _properties.put(prop.getName(), prop);
            if (prop.getType().isPrimitive()) {
                if (defValues == null) {
                    defValues = new Object[len];
                }
                defValues[i] = ClassUtil.defaultValue(prop.getType().getRawClass());
            }
            Object injectableValueId = prop.getInjectableValueId();
            if (injectableValueId != null) {
                if (propertiesWithInjectables == null) {
                    propertiesWithInjectables = new SettableBeanProperty[len];
                }
                propertiesWithInjectables[i] = prop;
            }
        }
        _defaultValues = defValues;
        _propertiesWithInjectables = propertiesWithInjectables;        
    }

    public Collection<SettableBeanProperty> getCreatorProperties() {
        return _properties.values();
    }
    
    public SettableBeanProperty findCreatorProperty(String name) {
        return _properties.get(name);
    }

    public void assignDeserializer(SettableBeanProperty prop, JsonDeserializer<Object> deser) {
        prop = prop.withValueDeserializer(deser);
        _properties.put(prop.getName(), prop);
        Object nullValue = deser.getNullValue();
        if (nullValue != null) {
            if (_defaultValues == null) {
                _defaultValues = new Object[_properties.size()];
            }
            _defaultValues[prop.getPropertyIndex()] = nullValue;
        }
    }
    
    /**
     * Method called when starting to build a bean instance.
     */
    public PropertyValueBuffer startBuilding(JsonParser jp, DeserializationContext ctxt)
    {
        PropertyValueBuffer buffer = new PropertyValueBuffer(jp, ctxt, _properties.size());
        if (_propertiesWithInjectables != null) {
            buffer.inject(_propertiesWithInjectables);
        }
        return buffer;
    }
    
    public Object build(PropertyValueBuffer buffer) throws IOException
    {
        Object bean = _valueInstantiator.createFromObjectWith(buffer.getParameters(_defaultValues));
        // Anything buffered?
        for (PropertyValue pv = buffer.buffered(); pv != null; pv = pv.next) {
            pv.assign(bean);
        }
        return bean;
    }
}
