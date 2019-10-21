package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

/**
 * Interface for objects that providers instances of {@link BeanPropertyFilter}
 * that match given ids. A provider is configured to be used during serialization,
 * to find filter to used based on id specified by {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JsonFilter}
 * annotation on bean class.
 * 
 * @since 1.7
 */
public abstract class FilterProvider
{
    /**
     * Lookup method used to find {@link BeanPropertyFilter} that has specified id.
     * Note that id is typically a {@link java.lang.String}, but is not necessarily
     * limited to that; that is, while standard components use String, custom
     * implementation can choose other kinds of keys.
     * 
     * @return Filter registered with specified id, if one defined; null if
     *   none found.
     */
    public abstract BeanPropertyFilter findFilter(Object filterId);
}
