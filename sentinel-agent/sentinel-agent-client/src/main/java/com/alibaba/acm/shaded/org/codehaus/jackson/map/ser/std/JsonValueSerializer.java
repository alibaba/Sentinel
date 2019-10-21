package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.BeanSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.schema.SchemaAware;
import com.alibaba.acm.shaded.org.codehaus.jackson.schema.JsonSchema;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Serializer class that can serialize Object that have a
 * {@link com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonValue} annotation to
 * indicate that serialization should be done by calling the method
 * annotated, and serializing result it returns.
 * <p/>
 * Implementation note: we will post-process resulting serializer
 * (much like what is done with {@link BeanSerializer})
 * to figure out actual serializers for final types. This must be
 * done from {@link #resolve} method, and NOT from constructor;
 * otherwise we could end up with an infinite loop.
 */
@JacksonStdImpl
public class JsonValueSerializer
    extends SerializerBase<Object>
    implements ResolvableSerializer, SchemaAware
{
    protected final Method _accessorMethod;

    protected JsonSerializer<Object> _valueSerializer;

    protected final BeanProperty _property;
    
    /**
     * This is a flag that is set in rare (?) cases where this serializer
     * is used for "natural" types (boolean, int, String, double); and where
     * we actually must force type information wrapping, even though
     * one would not normally be added.
     * 
     * @since 1.7
     */
    protected boolean _forceTypeInformation;
    
    /**
     * @param ser Explicit serializer to use, if caller knows it (which
     *            occurs if and only if the "value method" was annotated with
     *            {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JsonSerialize#using}), otherwise
     *            null
     */
    public JsonValueSerializer(Method valueMethod, JsonSerializer<Object> ser, BeanProperty property)
    {
        super(Object.class);
        _accessorMethod = valueMethod;
        _valueSerializer = ser;
        _property = property;
    }

    @Override
    public void serialize(Object bean, JsonGenerator jgen, SerializerProvider prov)
        throws IOException, JsonGenerationException
    {
        try {
            Object value = _accessorMethod.invoke(bean);

            if (value == null) {
                prov.defaultSerializeNull(jgen);
                return;
            }
            JsonSerializer<Object> ser = _valueSerializer;
            if (ser == null) {
                Class<?> c = value.getClass();
                /* 10-Mar-2010, tatu: Ideally we would actually separate out type
                 *   serializer from value serializer; but, alas, there's no access
                 *   to serializer factory at this point... 
                 */
                // let's cache it, may be needed soon again
                ser = prov.findTypedValueSerializer(c, true, _property);
            }
            ser.serialize(value, jgen, prov);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            Throwable t = e;
            // Need to unwrap this specific type, to see infinite recursion...
            while (t instanceof InvocationTargetException && t.getCause() != null) {
                t = t.getCause();
            }
            // Errors shouldn't be wrapped (and often can't, as well)
            if (t instanceof Error) {
                throw (Error) t;
            }
            // let's try to indicate the path best we can...
            throw JsonMappingException.wrapWithPath(t, bean, _accessorMethod.getName() + "()");
        }
    }

    @Override
    public void serializeWithType(Object bean, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonProcessingException
    {
        // Regardless of other parts, first need to find value to serialize:
        Object value = null;
        try {
            value = _accessorMethod.invoke(bean);

            // and if we got null, can also just write it directly
            if (value == null) {
                provider.defaultSerializeNull(jgen);
                return;
            }
            JsonSerializer<Object> ser = _valueSerializer;
            if (ser != null) { // already got a serializer? fabulous, that be easy...
                /* 09-Dec-2010, tatu: To work around natural type's refusal to add type info, we do
                 *    this (note: type is for the wrapper type, not enclosed value!)
                 */
                if (_forceTypeInformation) {
                    typeSer.writeTypePrefixForScalar(bean, jgen);
                } 
                ser.serializeWithType(value, jgen, provider, typeSer);
                if (_forceTypeInformation) {
                    typeSer.writeTypeSuffixForScalar(bean, jgen);
                } 
                return;
            }
            // But if not, it gets tad trickier (copied from main serialize() method)
            Class<?> c = value.getClass();
            ser = provider.findTypedValueSerializer(c, true, _property);
            // note: now we have bundled type serializer, so should NOT call with typed version
            ser.serialize(value, jgen, provider);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            Throwable t = e;
            // Need to unwrap this specific type, to see infinite recursion...
            while (t instanceof InvocationTargetException && t.getCause() != null) {
                t = t.getCause();
            }
            // Errors shouldn't be wrapped (and often can't, as well)
            if (t instanceof Error) {
                throw (Error) t;
            }
            // let's try to indicate the path best we can...
            throw JsonMappingException.wrapWithPath(t, bean, _accessorMethod.getName() + "()");
        }
    }
    
    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        throws JsonMappingException
    {
        return (_valueSerializer instanceof SchemaAware) ?
                ((SchemaAware) _valueSerializer).getSchema(provider, null) :
                JsonSchema.getDefaultSchemaNode();
    }
    
    /*
    /*******************************************************
    /* ResolvableSerializer impl
    /*******************************************************
     */

    /**
     * We can try to find the actual serializer for value, if we can
     * statically figure out what the result type must be.
     */
    @Override
    public void resolve(SerializerProvider provider)
        throws JsonMappingException
    {
        if (_valueSerializer == null) {
            /* Note: we can only assign serializer statically if the
             * declared type is final -- if not, we don't really know
             * the actual type until we get the instance.
             */
            // 10-Mar-2010, tatu: Except if static typing is to be used
            if (provider.isEnabled(SerializationConfig.Feature.USE_STATIC_TYPING)
                    || Modifier.isFinal(_accessorMethod.getReturnType().getModifiers())) {
                JavaType t = provider.constructType(_accessorMethod.getGenericReturnType());
                // false -> no need to cache
                /* 10-Mar-2010, tatu: Ideally we would actually separate out type
                 *   serializer from value serializer; but, alas, there's no access
                 *   to serializer factory at this point... 
                 */
                _valueSerializer = provider.findTypedValueSerializer(t, false, _property);
                /* 09-Dec-2010, tatu: Turns out we must add special handling for
                 *   cases where "native" (aka "natural") type is being serialized,
                 *   using standard serializer
                 */
                _forceTypeInformation = isNaturalTypeWithStdHandling(t, _valueSerializer);
            }
        }
    }

    protected boolean isNaturalTypeWithStdHandling(JavaType type, JsonSerializer<?> ser)
    {
        Class<?> cls = type.getRawClass();
        // First: do we have a natural type being handled?
        if (type.isPrimitive()) {
            if (cls != Integer.TYPE && cls != Boolean.TYPE && cls != Double.TYPE) {
                return false;
            }
        } else {
            if (cls != String.class &&
                    cls != Integer.class && cls != Boolean.class && cls != Double.class) {
                return false;
            }
        }
        // Second: and it's handled with standard serializer?
        return (ser.getClass().getAnnotation(JacksonStdImpl.class)) != null;
    }
    
    /*
    /**********************************************************
    /* Other methods
    /**********************************************************
     */

    @Override
    public String toString()
    {
        return "(@JsonValue serializer for method " + _accessorMethod.getDeclaringClass() + "#" + _accessorMethod.getName() + ")";
    }
}
