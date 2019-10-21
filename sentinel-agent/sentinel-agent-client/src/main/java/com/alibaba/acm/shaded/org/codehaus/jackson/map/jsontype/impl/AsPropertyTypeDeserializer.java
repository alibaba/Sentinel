package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo.As;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.JsonParserSequence;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.TokenBuffer;

/**
 * Type deserializer used with {@link As#PROPERTY}
 * inclusion mechanism.
 * Uses regular form (additional key/value entry before actual data)
 * when typed object is expressed as JSON Object; otherwise behaves similar to how
 * {@link As#WRAPPER_ARRAY} works.
 * Latter is used if JSON representation is polymorphic
 * 
 * @since 1.5
 * @author tatu
 */
public class AsPropertyTypeDeserializer extends AsArrayTypeDeserializer
{
    protected final String _typePropertyName;

    @Deprecated // since 1.9
    public AsPropertyTypeDeserializer(JavaType bt, TypeIdResolver idRes, BeanProperty property,
            String typePropName) {
        this(bt, idRes, property, null, typePropName);
    }
    
    public AsPropertyTypeDeserializer(JavaType bt, TypeIdResolver idRes, BeanProperty property,
            Class<?> defaultImpl,
            String typePropName)
    {
        super(bt, idRes, property, defaultImpl);
        _typePropertyName = typePropName;
    }

    @Override
    public As getTypeInclusion() {
        return As.PROPERTY;
    }

    @Override
    public String getPropertyName() { return _typePropertyName; }

    /**
     * This is the trickiest thing to handle, since property we are looking
     * for may be anywhere...
     */
    @Override
    public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // but first, sanity check to ensure we have START_OBJECT or FIELD_NAME
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = jp.nextToken();
        } else if (t == JsonToken.START_ARRAY) {
            /* This is most likely due to the fact that not all Java types are
             * serialized as JSON Objects; so if "as-property" inclusion is requested,
             * serialization of things like Lists must be instead handled as if
             * "as-wrapper-array" was requested.
             * But this can also be due to some custom handling: so, if "defaultImpl"
             * is defined, it will be asked to handle this case.
             */
            return _deserializeTypedUsingDefaultImpl(jp, ctxt, null);
        } else if (t != JsonToken.FIELD_NAME) {
            return _deserializeTypedUsingDefaultImpl(jp, ctxt, null);
        }
        // Ok, let's try to find the property. But first, need token buffer...
        TokenBuffer tb = null;

        for (; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
            String name = jp.getCurrentName();
            jp.nextToken(); // to point to the value
            if (_typePropertyName.equals(name)) { // gotcha!
                String typeId = jp.getText();
                JsonDeserializer<Object> deser = _findDeserializer(ctxt, typeId);
                // deserializer should take care of closing END_OBJECT as well
               if (tb != null) {
                    jp = JsonParserSequence.createFlattened(tb.asParser(jp), jp);
                }
                /* Must point to the next value; tb had no current, jp
                 * pointed to VALUE_STRING:
                 */
                jp.nextToken(); // to skip past String value
                // deserializer should take care of closing END_OBJECT as well
                return deser.deserialize(jp, ctxt);
            }
            if (tb == null) {
                tb = new TokenBuffer(null);
            }
            tb.writeFieldName(name);
            tb.copyCurrentStructure(jp);
        }
        return _deserializeTypedUsingDefaultImpl(jp, ctxt, tb);
    }
    
    // off-lined to keep main method lean and mean...
    protected Object _deserializeTypedUsingDefaultImpl(JsonParser jp,
            DeserializationContext ctxt, TokenBuffer tb)
        throws IOException, JsonProcessingException
    {
        // As per [JACKSON-614], may have default implementation to use
        if (_defaultImpl != null) { 
            JsonDeserializer<Object> deser = _findDefaultImplDeserializer(ctxt);
            if (tb != null) {
                tb.writeEndObject();
                jp = tb.asParser(jp);
                // must move to point to the first token:
                jp.nextToken();
            }
            return deser.deserialize(jp, ctxt);
        }
        // or, perhaps we just bumped into a "natural" value (boolean/int/double/String)?
        Object result = _deserializeIfNatural(jp, ctxt);
        if (result != null) {
            return result;
        }
        // or, something for which "as-property" won't work, changed into "wrapper-array" type:
        if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
            return super.deserializeTypedFromAny(jp, ctxt);
        }
        throw ctxt.wrongTokenException(jp, JsonToken.FIELD_NAME,
                "missing property '"+_typePropertyName+"' that is to contain type id  (for class "+baseTypeName()+")");
    }

    /* As per [JACKSON-352], also need to re-route "unknown" version. Need to think
     * this through bit more in future, but for now this does address issue and has
     * no negative side effects (at least within existing unit test suite).
     */
    @Override
    public Object deserializeTypedFromAny(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        /* [JACKSON-387]: Sometimes, however, we get an array wrapper; specifically
         *   when an array or list has been serialized with type information.
         */
        if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
            return super.deserializeTypedFromArray(jp, ctxt);
        }
        return deserializeTypedFromObject(jp, ctxt);
    }    
    
    // These are fine from base class:
    //public Object deserializeTypedFromArray(JsonParser jp, DeserializationContext ctxt)
    //public Object deserializeTypedFromScalar(JsonParser jp, DeserializationContext ctxt)    

    /**
     * Helper method used to check if given parser might be pointing to
     * a "natural" value, and one that would be acceptable as the
     * result value (compatible with declared base type)
     */
    protected Object _deserializeIfNatural(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        switch (jp.getCurrentToken()) {
        case VALUE_STRING:
            if (_baseType.getRawClass().isAssignableFrom(String.class)) {
                return jp.getText();
            }
            break;
        case VALUE_NUMBER_INT:
            if (_baseType.getRawClass().isAssignableFrom(Integer.class)) {
                return jp.getIntValue();
            }
            break;

        case VALUE_NUMBER_FLOAT:
            if (_baseType.getRawClass().isAssignableFrom(Double.class)) {
                return Double.valueOf(jp.getDoubleValue());
            }
            break;
        case VALUE_TRUE:
            if (_baseType.getRawClass().isAssignableFrom(Boolean.class)) {
                return Boolean.TRUE;
            }
            break;
        case VALUE_FALSE:
            if (_baseType.getRawClass().isAssignableFrom(Boolean.class)) {
                return Boolean.FALSE;
            }
            break;
        }
        return null;
    }
}
