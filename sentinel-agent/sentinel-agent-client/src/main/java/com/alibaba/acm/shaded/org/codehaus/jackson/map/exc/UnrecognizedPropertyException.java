package com.alibaba.acm.shaded.org.codehaus.jackson.map.exc;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonLocation;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonMappingException;

/**
 * Specialized {@link JsonMappingException} sub-class specifically used
 * to indicate problems due to encountering a JSON property that could
 * not be mapped to an Object property (via getter, constructor argument
 * or field).
 * 
 * @since 1.6
 */
public class UnrecognizedPropertyException
    extends JsonMappingException
{
    private static final long serialVersionUID = 1L;

    /**
     * Class that does not contain mapping for the unrecognized property.
     */
    protected final Class<?> _referringClass;
    
    /**
     *<p>
     * Note: redundant information since it is also included in the
     * reference path.
     */
    protected final String _unrecognizedPropertyName;
    
    public UnrecognizedPropertyException(String msg, JsonLocation loc,
            Class<?> referringClass, String propName)
    {
        
        super(msg, loc);
        _referringClass = referringClass;
        _unrecognizedPropertyName = propName;
    }

    public static UnrecognizedPropertyException from(JsonParser jp, Object fromObjectOrClass, String propertyName)
    {
        if (fromObjectOrClass == null) {
            throw new IllegalArgumentException();
        }
        Class<?> ref;
        if (fromObjectOrClass instanceof Class<?>) {
            ref = (Class<?>) fromObjectOrClass;
        } else {
            ref = fromObjectOrClass.getClass();
        }
        String msg = "Unrecognized field \""+propertyName+"\" (Class "+ref.getName()+"), not marked as ignorable";
        UnrecognizedPropertyException e = new UnrecognizedPropertyException(msg, jp.getCurrentLocation(), ref, propertyName);
        // but let's also ensure path includes this last (missing) segment
        e.prependPath(fromObjectOrClass, propertyName);
        return e;
    }

    /**
     * Method for accessing type (class) that is missing definition to allow
     * binding of the unrecognized property.
     */
    public Class<?> getReferringClass() {
        return _referringClass;
    }
    
    /**
     * Convenience method for accessing logical property name that could
     * not be mapped. Note that it is the last path reference in the
     * underlying path.
     */
    public String getUnrecognizedPropertyName() {
        return _unrecognizedPropertyName;
    }
}
