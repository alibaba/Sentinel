package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import com.alibaba.acm.shaded.org.codehaus.jackson.node.ObjectNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.schema.JsonSchema;
import com.alibaba.acm.shaded.org.codehaus.jackson.schema.SchemaAware;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Base class for serializers that will output contents as JSON
 * arrays; typically serializers used for {@link java.util.Collection}
 * and array types.
 */
public abstract class AsArraySerializerBase<T>
    extends ContainerSerializerBase<T>
    implements ResolvableSerializer
{
    protected final boolean _staticTyping;

    protected final JavaType _elementType;

    /**
     * Type serializer used for values, if any.
     */
    protected final TypeSerializer _valueTypeSerializer;
    
    /**
     * Value serializer to use, if it can be statically determined
     * 
     * @since 1.5
     */
    protected JsonSerializer<Object> _elementSerializer;

    /**
     * Collection-valued property being serialized with this instance
     * 
     * @since 1.7
     */
    protected final BeanProperty _property;

    /**
     * If element type can not be statically determined, mapping from
     * runtime type to serializer is handled using this object
     * 
     * @since 1.7
     */
    protected PropertySerializerMap _dynamicSerializers;

    /**
     * @deprecated since 1.8
     */
    @Deprecated
    protected AsArraySerializerBase(Class<?> cls, JavaType et, boolean staticTyping,
            TypeSerializer vts, BeanProperty property)
    {
        this(cls, et, staticTyping, vts, property, null);
    }

    protected AsArraySerializerBase(Class<?> cls, JavaType et, boolean staticTyping,
            TypeSerializer vts, BeanProperty property, JsonSerializer<Object> elementSerializer)
    {
        // typing with generics is messy... have to resort to this:
        super(cls, false);
        _elementType = et;
        // static if explicitly requested, or if element type is final
        _staticTyping = staticTyping || (et != null && et.isFinal());
        _valueTypeSerializer = vts;
        _property = property;
        _elementSerializer = elementSerializer;
        _dynamicSerializers = PropertySerializerMap.emptyMap();
    }

    @Override
    public final void serialize(T value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        jgen.writeStartArray();
        serializeContents(value, jgen, provider);
        jgen.writeEndArray();
    }
    
    @Override
    public final void serializeWithType(T value, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonGenerationException
    {
        typeSer.writeTypePrefixForArray(value, jgen);
        serializeContents(value, jgen, provider);
        typeSer.writeTypeSuffixForArray(value, jgen);
    }

    protected abstract void serializeContents(T value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException;

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        throws JsonMappingException
    {
        /* 15-Jan-2010, tatu: This should probably be rewritten, given that
         *    more information about content type is actually being explicitly
         *    passed. So there should be less need to try to re-process that
         *    information.
         */
        ObjectNode o = createSchemaNode("array", true);
        JavaType contentType = null;
        if (typeHint != null) {
            JavaType javaType = provider.constructType(typeHint);
            contentType = javaType.getContentType();
            if (contentType == null) { // could still be parametrized (Iterators)
                if (typeHint instanceof ParameterizedType) {
                    Type[] typeArgs = ((ParameterizedType) typeHint).getActualTypeArguments();
                    if (typeArgs.length == 1) {
                        contentType = provider.constructType(typeArgs[0]);
                    }
                }
            }
        }
        if (contentType == null && _elementType != null) {
            contentType = _elementType;
        }
        if (contentType != null) {
            JsonNode schemaNode = null;
            // 15-Oct-2010, tatu: We can't serialize plain Object.class; but what should it produce here? Untyped?
            if (contentType.getRawClass() != Object.class) {
                JsonSerializer<Object> ser = provider.findValueSerializer(contentType, _property);
                if (ser instanceof SchemaAware) {
                    schemaNode = ((SchemaAware) ser).getSchema(provider, null);
                }
            }
            if (schemaNode == null) {
                schemaNode = JsonSchema.getDefaultSchemaNode();
            }
            o.put("items", schemaNode);
        }
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
        if (_staticTyping && _elementType != null && _elementSerializer == null) {
            _elementSerializer = provider.findValueSerializer(_elementType, _property);
        }
    }

    /**
     * @since 1.7
     */
    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
            Class<?> type, SerializerProvider provider) throws JsonMappingException
    {
        PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, _property);
        // did we get a new map of serializers? If so, start using it
        if (map != result.map) {
            _dynamicSerializers = result.map;
        }
        return result.serializer;
    }

    /**
     * @since 1.8
     */
    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
            JavaType type, SerializerProvider provider) throws JsonMappingException
    {
        PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, _property);
        if (map != result.map) {
            _dynamicSerializers = result.map;
        }
        return result.serializer;
    }
}
