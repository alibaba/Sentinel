package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Interface that defines standard API for converting types
 * to type identifiers and vice versa. Used by type resolvers
 * ({@link com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer},
 * {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeDeserializer}) for converting
 * between type and matching id; id is stored in JSON and needed for
 * creating instances of proper subtypes when deserializing values.
 * 
 * @since 1.5
 */
public interface TypeIdResolver
{
    /*
    /**********************************************************
    /* Initialization/configuration methods
    /**********************************************************
     */

    /**
     * Method that will be called once before any type resolution calls;
     * used to initialize instance with configuration. This is necessary
     * since instances may be created via reflection, without ability to
     * call specific constructor to pass in configuration settings.
     * 
     * @param baseType Base type for which this id resolver instance is
     *   used
     */
    public void init(JavaType baseType);

    /*
    /**********************************************************
    /* Conversions between types and type ids
    /**********************************************************
     */
    
    /**
     * Method called to serialize type of the type of given value
     * as a String to include in serialized JSON content.
     */
    public String idFromValue(Object value);

    /**
     * Alternative method used for determining type from combination of
     * value and type, using suggested type (that serializer provides)
     * and possibly value of that type. Most common implementation will
     * use suggested type as is.
     * 
     * @since 1.8
     */
    public String idFromValueAndType(Object value, Class<?> suggestedType);

    /**
     * Method called to resolve type from given type identifier.
     */
    public JavaType typeFromId(String id);

    /*
    /**********************************************************
    /* Accessors for metadata
    /**********************************************************
     */

     /**
      * Accessor for mechanism that this resolver uses for determining
      * type id from type. Mostly informational; not required to be called
      * or used.
      */
     public JsonTypeInfo.Id getMechanism();
}
