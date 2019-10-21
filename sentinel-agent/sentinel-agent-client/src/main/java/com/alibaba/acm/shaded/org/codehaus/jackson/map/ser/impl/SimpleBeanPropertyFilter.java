package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.BeanPropertyFilter;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.BeanPropertyWriter;

/**
 * Simple {@link BeanPropertyFilter} implementation that only uses property name
 * to determine whether to serialize property as is, or to filter it out.
 * 
 * @since 1.7
 */
public abstract class SimpleBeanPropertyFilter implements BeanPropertyFilter
{
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */
    
    protected SimpleBeanPropertyFilter() { }

    /**
     * Factory method to construct filter that filters out all properties <b>except</b>
     * ones includes in set
     */
    public static SimpleBeanPropertyFilter filterOutAllExcept(Set<String> properties) {
        return new FilterExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter filterOutAllExcept(String... propertyArray) {
        HashSet<String> properties = new HashSet<String>(propertyArray.length);
        Collections.addAll(properties, propertyArray);
        return new FilterExceptFilter(properties);
    }
    
    public static SimpleBeanPropertyFilter serializeAllExcept(Set<String> properties) {
        return new SerializeExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter serializeAllExcept(String... propertyArray) {
        HashSet<String> properties = new HashSet<String>(propertyArray.length);
        Collections.addAll(properties, propertyArray);
        return new SerializeExceptFilter(properties);
    }
    
    /*
    /**********************************************************
    /* Sub-classes
    /**********************************************************
     */

    /**
     * Filter implementation which defaults to filtering out unknown
     * properties and only serializes ones explicitly listed.
     */
    public static class FilterExceptFilter
        extends SimpleBeanPropertyFilter
    {
        /**
         * Set of property names to serialize.
         */
        protected final Set<String> _propertiesToInclude;

        public FilterExceptFilter(Set<String> properties) {
            _propertiesToInclude = properties;
        }
        
        @Override
        public void serializeAsField(Object bean, JsonGenerator jgen,
                SerializerProvider provider, BeanPropertyWriter writer)
            throws Exception
        {
            if (_propertiesToInclude.contains(writer.getName())) {
                writer.serializeAsField(bean, jgen, provider);
            }
        }
    }

    /**
     * Filter implementation which defaults to serializing all
     * properties, except for ones explicitly listed to be filtered out.
     */
    public static class SerializeExceptFilter
        extends SimpleBeanPropertyFilter
    {
        /**
         * Set of property names to filter out.
         */
        protected final Set<String> _propertiesToExclude;

        public SerializeExceptFilter(Set<String> properties) {
            _propertiesToExclude = properties;
        }
        
        @Override
        public void serializeAsField(Object bean, JsonGenerator jgen,
                SerializerProvider provider, BeanPropertyWriter writer)
            throws Exception
        {
            if (!_propertiesToExclude.contains(writer.getName())) {
                writer.serializeAsField(bean, jgen, provider);
            }
        }
    }
}
