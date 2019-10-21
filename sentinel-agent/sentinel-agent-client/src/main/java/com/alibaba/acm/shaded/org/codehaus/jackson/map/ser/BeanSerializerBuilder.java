package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription;

/**
 * Builder class used for aggregating deserialization information about
 * a POJO, in order to build a {@link JsonSerializer} for serializing
 * intances.
 * Main reason for using separate builder class is that this makes it easier
 * to make actual serializer class fully immutable.
 * 
 * @since 1.7
 */
public class BeanSerializerBuilder
{
    private final static BeanPropertyWriter[] NO_PROPERTIES = new BeanPropertyWriter[0];

    /*
    /**********************************************************
    /* General information about POJO
    /**********************************************************
     */

    final protected BasicBeanDescription _beanDesc;

    /*
    /**********************************************************
    /* Accumulated information about properties
    /**********************************************************
     */

    /**
     * Bean properties, in order of serialization
     */
    protected List<BeanPropertyWriter> _properties;

    /**
     * Optional array of filtered property writers; if null, no
     * view-based filtering is performed.
     */
    protected BeanPropertyWriter[] _filteredProperties;
    
    /**
     * Writer used for "any getter" properties, if any.
     */
    protected AnyGetterWriter _anyGetter;

    /**
     * Id of the property filter to use for POJO, if any.
     */
    protected Object _filterId;

    /*
    /**********************************************************
    /* Construction and setter methods
    /**********************************************************
     */
    
    public BeanSerializerBuilder(BasicBeanDescription beanDesc) {
        _beanDesc = beanDesc;
    }

    /**
     * Copy-constructor that may be used for sub-classing
     */
    protected BeanSerializerBuilder(BeanSerializerBuilder src) {
        _beanDesc = src._beanDesc;
        _properties = src._properties;
        _filteredProperties = src._filteredProperties;
        _anyGetter = src._anyGetter;
        _filterId = src._filterId;
    }
    
    public BasicBeanDescription getBeanDescription() { return _beanDesc; }
    public List<BeanPropertyWriter> getProperties() { return _properties; }
    public BeanPropertyWriter[] getFilteredProperties() { return _filteredProperties; }
    
    /**
     * @since 1.9
     */
    public boolean hasProperties() {
        return (_properties != null) && (_properties.size() > 0);
    }

    public void setProperties(List<BeanPropertyWriter> properties) {
        _properties = properties;
    }

    public void setFilteredProperties(BeanPropertyWriter[] properties) {
        _filteredProperties = properties;
    }
    
    public void setAnyGetter(AnyGetterWriter anyGetter) {
        _anyGetter = anyGetter;
    }

    public void setFilterId(Object filterId) {
        _filterId = filterId;
    }
    
    /*
    /**********************************************************
    /* Build methods for actually creating serializer instance
    /**********************************************************
     */
    
    /**
     * Method called to create {@link BeanSerializer} instance with
     * all accumulated information. Will construct a serializer if we
     * have enough information, or return null if not.
     */
    public JsonSerializer<?> build()
    {
        BeanPropertyWriter[] properties;
        // No properties or any getter? No real serializer; caller gets to handle
        if (_properties == null || _properties.isEmpty()) {
            if (_anyGetter == null) {
                return null;
            }
            properties = NO_PROPERTIES;
        } else {
            properties = _properties.toArray(new BeanPropertyWriter[_properties.size()]);
            
        }
        return new BeanSerializer(_beanDesc.getType(), properties, _filteredProperties, _anyGetter, _filterId);
    }
    
    /**
     * Factory method for constructing an "empty" serializer; one that
     * outputs no properties (but handles JSON objects properly, including
     * type information)
     */
    public BeanSerializer createDummy() {
        return BeanSerializer.createDummy(_beanDesc.getBeanClass());
    }
}

