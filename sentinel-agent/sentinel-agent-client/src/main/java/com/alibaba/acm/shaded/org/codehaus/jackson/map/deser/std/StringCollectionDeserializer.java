package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;
import java.util.Collection;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonToken;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedWithParams;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * @since 1.9 (moved from higher-level package)
 */
@JacksonStdImpl
public final class StringCollectionDeserializer
    extends ContainerDeserializerBase<Collection<String>>
    implements ResolvableDeserializer
{
    // // Configuration

    protected final JavaType _collectionType;

    /**
     * Value deserializer; needed even if it is the standard String
     * deserializer
     */
    protected final JsonDeserializer<String> _valueDeserializer;

    /**
     * Flag that indicates whether value deserializer is the standard
     * Jackson-provided one; if it is, we can use more efficient
     * handling.
     */
    protected final boolean _isDefaultDeserializer;

    // // Instance construction settings:
    
    /**
     * @since 1.9
     */
    protected final ValueInstantiator _valueInstantiator;

    /**
     * Deserializer that is used iff delegate-based creator is
     * to be used for deserializing from JSON Object.
     */
    protected JsonDeserializer<Object> _delegateDeserializer;

    // NOTE: no PropertyBasedCreator, as JSON Arrays have no properties

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */
    
    @SuppressWarnings("unchecked")
    public StringCollectionDeserializer(JavaType collectionType, JsonDeserializer<?> valueDeser,
            ValueInstantiator valueInstantiator)
    {
        super(collectionType.getRawClass());
        _collectionType = collectionType;
        _valueDeserializer = (JsonDeserializer<String>) valueDeser;
        _valueInstantiator = valueInstantiator;
        _isDefaultDeserializer = isDefaultSerializer(valueDeser);
    }

    /**
     * Copy-constructor that can be used by sub-classes to allow
     * copy-on-write styling copying of settings of an existing instance.
     * 
     * @since 1.9
     */
    protected StringCollectionDeserializer(StringCollectionDeserializer src)
    {
        super(src._valueClass);
        _collectionType = src._collectionType;
        _valueDeserializer = src._valueDeserializer;
        _valueInstantiator = src._valueInstantiator;
        _isDefaultDeserializer = src._isDefaultDeserializer;
    }

    /*
    /**********************************************************
    /* Validation, post-processing (ResolvableDeserializer)
    /**********************************************************
     */

    /**
     * Method called to finalize setup of this deserializer,
     * after deserializer itself has been registered. This
     * is needed to handle recursive and transitive dependencies.
     */
    @Override
    public void resolve(DeserializationConfig config, DeserializerProvider provider)
        throws JsonMappingException
    {
        // May need to resolve types for delegate-based creators:
        AnnotatedWithParams delegateCreator = _valueInstantiator.getDelegateCreator();
        if (delegateCreator != null) {
            JavaType delegateType = _valueInstantiator.getDelegateType();
            // Need to create a temporary property to allow contextual deserializers:
            BeanProperty.Std property = new BeanProperty.Std(null,
                    delegateType, null, delegateCreator);
            _delegateDeserializer = findDeserializer(config, provider, delegateType, property);
        }
    }
    
    /*
    /**********************************************************
    /* ContainerDeserializerBase API
    /**********************************************************
     */

    @Override
    public JavaType getContentType() {
        return _collectionType.getContentType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonDeserializer<Object> getContentDeserializer() {
        JsonDeserializer<?> deser = _valueDeserializer;
        return (JsonDeserializer<Object>) deser;
    }
    
    /*
    /**********************************************************
    /* JsonDeserializer API
    /**********************************************************
     */
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        if (_delegateDeserializer != null) {
            return (Collection<String>) _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
        }
        final Collection<String> result = (Collection<String>) _valueInstantiator.createUsingDefault();
        return deserialize(jp, ctxt, result);
    }

    @Override
    public Collection<String> deserialize(JsonParser jp, DeserializationContext ctxt,
                                          Collection<String> result)
        throws IOException, JsonProcessingException
    {
        // Ok: must point to START_ARRAY
        if (!jp.isExpectedStartArrayToken()) {
            return handleNonArray(jp, ctxt, result);
        }

        if (!_isDefaultDeserializer) {
            return deserializeUsingCustom(jp, ctxt, result);
        }
        JsonToken t;

        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            result.add((t == JsonToken.VALUE_NULL) ? null : jp.getText());
        }
        return result;
    }
    
    private Collection<String> deserializeUsingCustom(JsonParser jp, DeserializationContext ctxt,
            Collection<String> result)
        throws IOException, JsonProcessingException
    {
        JsonToken t;
        final JsonDeserializer<String> deser = _valueDeserializer;

        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            String value;

            if (t == JsonToken.VALUE_NULL) {
                value = null;
            } else {
                value = deser.deserialize(jp, ctxt);
            }
            result.add(value);
        }
        return result;
    }
    
    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        // In future could check current token... for now this should be enough:
        return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
    }

    /**
     * Helper method called when current token is no START_ARRAY. Will either
     * throw an exception, or try to handle value as if member of implicit
     * array, depending on configuration.
     */
    private final Collection<String> handleNonArray(JsonParser jp, DeserializationContext ctxt,
            Collection<String> result)
        throws IOException, JsonProcessingException
    {
        // [JACKSON-526]: implicit arrays from single values?
        if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            throw ctxt.mappingException(_collectionType.getRawClass());
        }
        // Strings are one of "native" (intrinsic) types, so there's never type deserializer involved
        JsonDeserializer<String> valueDes = _valueDeserializer;
        JsonToken t = jp.getCurrentToken();

        String value;
        
        if (t == JsonToken.VALUE_NULL) {
            value = null;
        } else {
            value = (valueDes == null) ? jp.getText() : valueDes.deserialize(jp, ctxt);
        }
        result.add(value);
        return result;
    }
    
}
