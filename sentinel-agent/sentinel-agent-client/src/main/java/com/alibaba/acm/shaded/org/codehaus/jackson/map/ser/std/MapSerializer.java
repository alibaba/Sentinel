package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.node.ObjectNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Standard serializer implementation for serializing {link java.util.Map} types.
 *<p>
 * Note: about the only configurable setting currently is ability to filter out
 * entries with specified names.
 */
@JacksonStdImpl
public class MapSerializer
    extends ContainerSerializerBase<Map<?,?>>
    implements ResolvableSerializer
{
    protected final static JavaType UNSPECIFIED_TYPE = TypeFactory.unknownType();
    
    /**
     * Map-valued property being serialized with this instance
     * 
     * @since 1.7
     */
    protected final BeanProperty _property;
    
    /**
     * Set of entries to omit during serialization, if any
     */
    protected final HashSet<String> _ignoredEntries;

    /**
     * Whether static types should be used for serialization of values
     * or not (if not, dynamic runtime type is used)
     */
    protected final boolean _valueTypeIsStatic;

    /**
     * Declared type of keys
     * 
     * @since 1.7
     */
    protected final JavaType _keyType;

    /**
     * Declared type of contained values
     */
    protected final JavaType _valueType;

    /**
     * Key serializer to use, if it can be statically determined
     * 
     * @since 1.7
     */
    protected JsonSerializer<Object> _keySerializer;

    /**
     * Value serializer to use, if it can be statically determined
     * 
     * @since 1.5
     */
    protected JsonSerializer<Object> _valueSerializer;

    /**
     * Type identifier serializer used for values, if any.
     */
    protected final TypeSerializer _valueTypeSerializer;

    /**
     * If value type can not be statically determined, mapping from
     * runtime value types to serializers are stored in this object.
     * 
     * @since 1.8
     */
    protected PropertySerializerMap _dynamicValueSerializers;
    
    protected MapSerializer() {
        this((HashSet<String>)null, null, null, false, null, null, null, null);
    }
    
    protected MapSerializer(HashSet<String> ignoredEntries,
            JavaType keyType, JavaType valueType, boolean valueTypeIsStatic,
            TypeSerializer vts,
            JsonSerializer<Object> keySerializer, JsonSerializer<Object> valueSerializer, 
            BeanProperty property)
    {
        super(Map.class, false);
        _property = property;
        _ignoredEntries = ignoredEntries;
        _keyType = keyType;
        _valueType = valueType;
        _valueTypeIsStatic = valueTypeIsStatic;
        _valueTypeSerializer = vts;
        _keySerializer = keySerializer;
        _valueSerializer = valueSerializer;
        _dynamicValueSerializers = PropertySerializerMap.emptyMap();
    }
    
    @Override
    public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts)
    {
        MapSerializer ms = new MapSerializer(_ignoredEntries, _keyType, _valueType, _valueTypeIsStatic, vts,
                _keySerializer, _valueSerializer, _property);
        if (_valueSerializer != null) {
            ms._valueSerializer = _valueSerializer;
        }
        return ms;
    }
    
    /**
     * Factory method used to construct Map serializers.
     * 
     * @param ignoredList Array of entry names that are to be filtered on
     *    serialization; null if none
     * @param mapType Declared type information (needed for static typing)
     * @param staticValueType Whether static typing should be used for the
     *    Map (which includes its contents)
     * @param vts Type serializer to use for map entry values, if any
     * 
     * @deprecated As of 1.8; use the variant with more arguments
     */
    @Deprecated
    public static MapSerializer construct(String[] ignoredList, JavaType mapType,
            boolean staticValueType, TypeSerializer vts, BeanProperty property)
    {
        return construct(ignoredList, mapType, staticValueType, vts, property, null, null);
    }

    public static MapSerializer construct(String[] ignoredList, JavaType mapType,
            boolean staticValueType, TypeSerializer vts, BeanProperty property,
            JsonSerializer<Object> keySerializer, JsonSerializer<Object> valueSerializer)
    {
        HashSet<String> ignoredEntries = toSet(ignoredList);
        JavaType keyType, valueType;
        
        if (mapType == null) {
            keyType = valueType = UNSPECIFIED_TYPE;
        } else { 
            keyType = mapType.getKeyType();
            valueType = mapType.getContentType();
        }
        // If value type is final, it's same as forcing static value typing:
        if (!staticValueType) {
            staticValueType = (valueType != null && valueType.isFinal());
        }
        return new MapSerializer(ignoredEntries, keyType, valueType, staticValueType, vts,
                keySerializer, valueSerializer, property);
    }

    private static HashSet<String> toSet(String[] ignoredEntries) {
        if (ignoredEntries == null || ignoredEntries.length == 0) {
            return null;
        }
        HashSet<String> result = new HashSet<String>(ignoredEntries.length);
        for (String prop : ignoredEntries) {
            result.add(prop);
        }
        return result;
    }
    
    /*
    /**********************************************************
    /* JsonSerializer implementation
    /**********************************************************
     */

    @Override
    public void serialize(Map<?,?> value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        jgen.writeStartObject();
        if (!value.isEmpty()) {
            if (_valueSerializer != null) {
                serializeFieldsUsing(value, jgen, provider, _valueSerializer);
            } else {
                serializeFields(value, jgen, provider);
            }
        }        
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(Map<?,?> value, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonGenerationException
    {
        typeSer.writeTypePrefixForObject(value, jgen);
        if (!value.isEmpty()) {
            if (_valueSerializer != null) {
                serializeFieldsUsing(value, jgen, provider, _valueSerializer);
            } else {
                serializeFields(value, jgen, provider);
            }
        }
        typeSer.writeTypeSuffixForObject(value, jgen);
    }

    /*
    /**********************************************************
    /* JsonSerializer implementation
    /**********************************************************
     */
    
    /**
     * Method called to serialize fields, when the value type is not statically known.
     */
    public void serializeFields(Map<?,?> value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        // If value type needs polymorphic type handling, some more work needed:
        if (_valueTypeSerializer != null) {
            serializeTypedFields(value, jgen, provider);
            return;
        }
        final JsonSerializer<Object> keySerializer = _keySerializer;
        
        final HashSet<String> ignored = _ignoredEntries;
        final boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);

        PropertySerializerMap serializers = _dynamicValueSerializers;

        for (Map.Entry<?,?> entry : value.entrySet()) {
            Object valueElem = entry.getValue();
            // First, serialize key
            Object keyElem = entry.getKey();
            if (keyElem == null) {
                provider.getNullKeySerializer().serialize(null, jgen, provider);
            } else {
                // [JACKSON-314] skip entries with null values?
                if (skipNulls && valueElem == null) continue;
                // One twist: is entry ignorable? If so, skip
                if (ignored != null && ignored.contains(keyElem)) continue;
                keySerializer.serialize(keyElem, jgen, provider);
            }

            // And then value
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            } else {
                Class<?> cc = valueElem.getClass();
                JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                if (serializer == null) {
                    if (_valueType.hasGenericTypes()) {
                        serializer = _findAndAddDynamic(serializers,
                                provider.constructSpecializedType(_valueType, cc), provider);
                    } else {
                        serializer = _findAndAddDynamic(serializers, cc, provider);
                    }
                    serializers = _dynamicValueSerializers;
                }
                try {
                    serializer.serialize(valueElem, jgen, provider);
                } catch (Exception e) {
                    // [JACKSON-55] Need to add reference information
                    String keyDesc = ""+keyElem;
                    wrapAndThrow(provider, e, value, keyDesc);
                }
            }
        }
    }

    /**
     * Method called to serialize fields, when the value type is statically known,
     * so that value serializer is passed and does not need to be fetched from
     * provider.
     */
    protected void serializeFieldsUsing(Map<?,?> value, JsonGenerator jgen, SerializerProvider provider,
            JsonSerializer<Object> ser)
            throws IOException, JsonGenerationException
    {
        final JsonSerializer<Object> keySerializer = _keySerializer;
        final HashSet<String> ignored = _ignoredEntries;
        final TypeSerializer typeSer = _valueTypeSerializer;
        final boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);

        for (Map.Entry<?,?> entry : value.entrySet()) {
            Object valueElem = entry.getValue();
            Object keyElem = entry.getKey();
            if (keyElem == null) {
                provider.getNullKeySerializer().serialize(null, jgen, provider);
            } else {
                // [JACKSON-314] also may need to skip entries with null values
                if (skipNulls && valueElem == null) continue;
                if (ignored != null && ignored.contains(keyElem)) continue;
                keySerializer.serialize(keyElem, jgen, provider);
            }
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            } else {
                try {
                    if (typeSer == null) {
                        ser.serialize(valueElem, jgen, provider);
                    } else {
                        ser.serializeWithType(valueElem, jgen, provider, typeSer);
                    }
                } catch (Exception e) {
                    // [JACKSON-55] Need to add reference information
                    String keyDesc = ""+keyElem;
                    wrapAndThrow(provider, e, value, keyDesc);
                }
            }
        }
    }

    protected void serializeTypedFields(Map<?,?> value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        final JsonSerializer<Object> keySerializer = _keySerializer;
        JsonSerializer<Object> prevValueSerializer = null;
        Class<?> prevValueClass = null;
        final HashSet<String> ignored = _ignoredEntries;
        final boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);
    
        for (Map.Entry<?,?> entry : value.entrySet()) {
            Object valueElem = entry.getValue();
            // First, serialize key
            Object keyElem = entry.getKey();
            if (keyElem == null) {
                provider.getNullKeySerializer().serialize(null, jgen, provider);
            } else {
                // [JACKSON-314] also may need to skip entries with null values
                if (skipNulls && valueElem == null) continue;
                // One twist: is entry ignorable? If so, skip
                if (ignored != null && ignored.contains(keyElem)) continue;
                keySerializer.serialize(keyElem, jgen, provider);
            }
    
            // And then value
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            } else {
                Class<?> cc = valueElem.getClass();
                JsonSerializer<Object> currSerializer;
                if (cc == prevValueClass) {
                    currSerializer = prevValueSerializer;
                } else {
                    currSerializer = provider.findValueSerializer(cc, _property);
                    prevValueSerializer = currSerializer;
                    prevValueClass = cc;
                }
                try {
                    currSerializer.serializeWithType(valueElem, jgen, provider, _valueTypeSerializer);
                } catch (Exception e) {
                    // [JACKSON-55] Need to add reference information
                    String keyDesc = ""+keyElem;
                    wrapAndThrow(provider, e, value, keyDesc);
                }
            }
        }
    }
    
    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint)
    {
        ObjectNode o = createSchemaNode("object", true);
        //(ryan) even though it's possible to statically determine the "value" type of the map,
        // there's no way to statically determine the keys, so the "Entries" can't be determined.
        return o;
    }

    /**
     * Need to get callback to resolve value serializer, if static typing
     * is used (either being forced, or because value type is final)
     */
    @Override
    public void resolve(SerializerProvider provider)
        throws JsonMappingException
    {
        if (_valueTypeIsStatic && _valueSerializer == null) {
            _valueSerializer = provider.findValueSerializer(_valueType, _property);
        }
        /* 10-Dec-2010, tatu: Let's also fetch key serializer; and always assume we'll
         *   do that just by using static type information
         */
        /* 25-Feb-2011, tatu: May need to reconsider this static checking (since it
         *   differs from value handling)... but for now, it's ok to ensure contextual
         *   aspects are handled; this is done by provider.
         */
        if (_keySerializer == null) {
            _keySerializer = provider.findKeySerializer(_keyType, _property);
        }
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */
    
    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
            Class<?> type, SerializerProvider provider) throws JsonMappingException
    {
        PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, _property);
        // did we get a new map of serializers? If so, start using it
        if (map != result.map) {
            _dynamicValueSerializers = result.map;
        }
        return result.serializer;
    }

    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
            JavaType type, SerializerProvider provider) throws JsonMappingException
    {
        PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, _property);
        if (map != result.map) {
            _dynamicValueSerializers = result.map;
        }
        return result.serializer;
    }

}

