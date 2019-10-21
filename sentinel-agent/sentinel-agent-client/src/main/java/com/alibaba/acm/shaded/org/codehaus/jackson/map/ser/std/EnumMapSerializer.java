package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.EnumValues;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.node.JsonNodeFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.node.ObjectNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.schema.JsonSchema;
import com.alibaba.acm.shaded.org.codehaus.jackson.schema.SchemaAware;

/**
 * Specialized serializer for {@link EnumMap}s. Somewhat tricky to
 * implement because actual Enum value type may not be available;
 * and if not, it can only be gotten from actual instance.
 */
@JacksonStdImpl
public class EnumMapSerializer
    extends ContainerSerializerBase<EnumMap<? extends Enum<?>, ?>>
    implements ResolvableSerializer
{
    protected final boolean _staticTyping;

    /**
     * If we know enumeration used as key, this will contain
     * value set to use for serialization
     */
    protected final EnumValues _keyEnums;

    protected final JavaType _valueType;

    /**
     * Property being serialized with this instance
     * 
     * @since 1.7
     */
    protected final BeanProperty _property;
    
    /**
     * Value serializer to use, if it can be statically determined
     * 
     * @since 1.5
     */
    protected JsonSerializer<Object> _valueSerializer;

    /**
     * Type serializer used for values, if any.
     */
    protected final TypeSerializer _valueTypeSerializer;

    /**
     * @deprecated Since 1.8, use variant that takes value serializer
     */
    @Deprecated
    public EnumMapSerializer(JavaType valueType, boolean staticTyping, EnumValues keyEnums,
            TypeSerializer vts, BeanProperty property)
    {
        this(valueType, staticTyping, keyEnums, vts, property, null);
    }

    public EnumMapSerializer(JavaType valueType, boolean staticTyping, EnumValues keyEnums,
            TypeSerializer vts, BeanProperty property, JsonSerializer<Object> valueSerializer)
    {
        super(EnumMap.class, false);
        _staticTyping = staticTyping || (valueType != null && valueType.isFinal());
        _valueType = valueType;
        _keyEnums = keyEnums;
        _valueTypeSerializer = vts;
        _property = property;
        _valueSerializer = valueSerializer;
    }

    @Override
    public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts)
    {
        return new EnumMapSerializer(_valueType, _staticTyping, _keyEnums, vts,  _property, _valueSerializer);
    }
    
    @Override
    public void serialize(EnumMap<? extends Enum<?>,?> value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        jgen.writeStartObject();
        if (!value.isEmpty()) {
            serializeContents(value, jgen, provider);
        }        
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(EnumMap<? extends Enum<?>,?> value, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonGenerationException
    {
        typeSer.writeTypePrefixForObject(value, jgen);
        if (!value.isEmpty()) {
            serializeContents(value, jgen, provider);
        }
        typeSer.writeTypeSuffixForObject(value, jgen);
    }
    
    protected void serializeContents(EnumMap<? extends Enum<?>,?> value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        if (_valueSerializer != null) {
            serializeContentsUsing(value, jgen, provider, _valueSerializer);
            return;
        }
        JsonSerializer<Object> prevSerializer = null;
        Class<?> prevClass = null;
        EnumValues keyEnums = _keyEnums;

        for (Map.Entry<? extends Enum<?>,?> entry : value.entrySet()) {
            // First, serialize key
            Enum<?> key = entry.getKey();
            if (keyEnums == null) {
                /* 15-Oct-2009, tatu: This is clumsy, but still the simplest efficient
                 * way to do it currently, as Serializers get cached. (it does assume we'll always use
                 * default serializer tho -- so ideally code should be rewritten)
                 */
                // ... and lovely two-step casting process too...
                SerializerBase<?> ser = (SerializerBase<?>) provider.findValueSerializer(
                        key.getDeclaringClass(), _property);
                keyEnums = ((EnumSerializer) ser).getEnumValues();
            }
            jgen.writeFieldName(keyEnums.serializedValueFor(key));
            // And then value
            Object valueElem = entry.getValue();
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            } else {
                Class<?> cc = valueElem.getClass();
                JsonSerializer<Object> currSerializer;
                if (cc == prevClass) {
                    currSerializer = prevSerializer;
                } else {
                    currSerializer = provider.findValueSerializer(cc, _property);
                    prevSerializer = currSerializer;
                    prevClass = cc;
                }
                try {
                    currSerializer.serialize(valueElem, jgen, provider);
                } catch (Exception e) {
                    // [JACKSON-55] Need to add reference information
                    wrapAndThrow(provider, e, value, entry.getKey().name());
                }
            }
        }
    }

    protected void serializeContentsUsing(EnumMap<? extends Enum<?>,?> value, JsonGenerator jgen, SerializerProvider provider,
            JsonSerializer<Object> valueSer)
        throws IOException, JsonGenerationException
    {
        EnumValues keyEnums = _keyEnums;
        for (Map.Entry<? extends Enum<?>,?> entry : value.entrySet()) {
            Enum<?> key = entry.getKey();
            if (keyEnums == null) {
                // clumsy, but has to do for now:
                SerializerBase<?> ser = (SerializerBase<?>) provider.findValueSerializer(key.getDeclaringClass(),
                        _property);
                keyEnums = ((EnumSerializer) ser).getEnumValues();
            }
            jgen.writeFieldName(keyEnums.serializedValueFor(key));
            Object valueElem = entry.getValue();
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            } else {
                try {
                    valueSer.serialize(valueElem, jgen, provider);
                } catch (Exception e) {
                    wrapAndThrow(provider, e, value, entry.getKey().name());
                }
            }
        }
    }

    @Override
    public void resolve(SerializerProvider provider)
        throws JsonMappingException
    {
        if (_staticTyping && _valueSerializer == null) {
            _valueSerializer = provider.findValueSerializer(_valueType, _property);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        throws JsonMappingException
    {
        ObjectNode o = createSchemaNode("object", true);
        if (typeHint instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) typeHint).getActualTypeArguments();
            if (typeArgs.length == 2) {
                JavaType enumType = provider.constructType(typeArgs[0]);
                JavaType valueType = provider.constructType(typeArgs[1]);
                ObjectNode propsNode = JsonNodeFactory.instance.objectNode();
                Class<Enum<?>> enumClass = (Class<Enum<?>>) enumType.getRawClass();
                for (Enum<?> enumValue : enumClass.getEnumConstants()) {
                    JsonSerializer<Object> ser = provider.findValueSerializer(valueType.getRawClass(), _property);
                    JsonNode schemaNode = (ser instanceof SchemaAware) ?
                            ((SchemaAware) ser).getSchema(provider, null) :
                            JsonSchema.getDefaultSchemaNode();
                    propsNode.put(provider.getConfig().getAnnotationIntrospector().findEnumValue((Enum<?>)enumValue), schemaNode);
                }
                o.put("properties", propsNode);
            }
        }
        return o;
    }
}
