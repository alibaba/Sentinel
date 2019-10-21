package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.IOException;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.CollectionSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Dummy container class to group standard container serializers: serializers
 * that can serialize things like {@link java.util.List}s,
 * {@link java.util.Map}s and such.
 */
public class StdContainerSerializers
{
    protected StdContainerSerializers() { }

    /*
    /**********************************************************
    /* Factory methods
    /**********************************************************
     */
        
    public static ContainerSerializerBase<?> indexedListSerializer(JavaType elemType,
            boolean staticTyping, TypeSerializer vts, BeanProperty property,
            JsonSerializer<Object> valueSerializer)
    {
        return new IndexedListSerializer(elemType, staticTyping, vts, property, valueSerializer);
    }

    public static ContainerSerializerBase<?> collectionSerializer(JavaType elemType,
            boolean staticTyping, TypeSerializer vts, BeanProperty property,
            JsonSerializer<Object> valueSerializer)
    {
        return new CollectionSerializer(elemType, staticTyping, vts, property, valueSerializer);
    }

    public static ContainerSerializerBase<?> iteratorSerializer(JavaType elemType,
            boolean staticTyping, TypeSerializer vts, BeanProperty property)
    {
        return new IteratorSerializer(elemType, staticTyping, vts, property);
    }

    public static ContainerSerializerBase<?> iterableSerializer(JavaType elemType,
            boolean staticTyping, TypeSerializer vts, BeanProperty property)
    {
        return new IterableSerializer(elemType, staticTyping, vts, property);
    }

    public static JsonSerializer<?> enumSetSerializer(JavaType enumType, BeanProperty property)
    {
        return new EnumSetSerializer(enumType, property);
    }
    
    /*
    /**********************************************************
    /* Concrete serializers, Lists/collections
    /**********************************************************
     */

    /**
     * This is an optimized serializer for Lists that can be efficiently
     * traversed by index (as opposed to others, such as {@link LinkedList}
     * that can not}.
     */
    @JacksonStdImpl
    public static class IndexedListSerializer
        extends AsArraySerializerBase<List<?>>
    {
        public IndexedListSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts,
                BeanProperty property, JsonSerializer<Object> valueSerializer)
        {
            super(List.class, elemType, staticTyping, vts, property, valueSerializer);
        }

        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
            return new IndexedListSerializer(_elementType, _staticTyping, vts, _property, _elementSerializer);
        }
        
        @Override
        public void serializeContents(List<?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            if (_elementSerializer != null) {
                serializeContentsUsing(value, jgen, provider, _elementSerializer);
                return;
            }
            if (_valueTypeSerializer != null) {
                serializeTypedContents(value, jgen, provider);
                return;
            }
            final int len = value.size();
            if (len == 0) {
                return;
            }
            int i = 0;
            try {
                PropertySerializerMap serializers = _dynamicSerializers;
                for (; i < len; ++i) {
                    Object elem = value.get(i);
                    if (elem == null) {
                        provider.defaultSerializeNull(jgen);
                    } else {
                        Class<?> cc = elem.getClass();
                        JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                        if (serializer == null) {
                            // To fix [JACKSON-508]
                            if (_elementType.hasGenericTypes()) {
                                serializer = _findAndAddDynamic(serializers,
                                        provider.constructSpecializedType(_elementType, cc), provider);
                            } else {
                                serializer = _findAndAddDynamic(serializers, cc, provider);
                            }
                            serializers = _dynamicSerializers;
                        }
                        serializer.serialize(elem, jgen, provider);
                    }
                }
            } catch (Exception e) {
                // [JACKSON-55] Need to add reference information
                wrapAndThrow(provider, e, value, i);
            }
        }
        
        public void serializeContentsUsing(List<?> value, JsonGenerator jgen, SerializerProvider provider,
                JsonSerializer<Object> ser)
            throws IOException, JsonGenerationException
        {
            final int len = value.size();
            if (len == 0) {
                return;
            }
            final TypeSerializer typeSer = _valueTypeSerializer;
            for (int i = 0; i < len; ++i) {
                Object elem = value.get(i);
                try {
                    if (elem == null) {
                        provider.defaultSerializeNull(jgen);
                    } else if (typeSer == null) {
                        ser.serialize(elem, jgen, provider);
                    } else {
                        ser.serializeWithType(elem, jgen, provider, typeSer);
                    }
                } catch (Exception e) {
                    // [JACKSON-55] Need to add reference information
                    wrapAndThrow(provider, e, value, i);
                }
            }
        }

        public void serializeTypedContents(List<?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            final int len = value.size();
            if (len == 0) {
                return;
            }
            int i = 0;
            try {
                final TypeSerializer typeSer = _valueTypeSerializer;
                PropertySerializerMap serializers = _dynamicSerializers;
                for (; i < len; ++i) {
                    Object elem = value.get(i);
                    if (elem == null) {
                        provider.defaultSerializeNull(jgen);
                    } else {
                        Class<?> cc = elem.getClass();
                        JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                        if (serializer == null) {
                            // To fix [JACKSON-508]
                            if (_elementType.hasGenericTypes()) {
                                serializer = _findAndAddDynamic(serializers,
                                        provider.constructSpecializedType(_elementType, cc), provider);
                            } else {
                                serializer = _findAndAddDynamic(serializers, cc, provider);
                            }
                            serializers = _dynamicSerializers;
                        }
                        serializer.serializeWithType(elem, jgen, provider, typeSer);
                    }
                }
            } catch (Exception e) {
                // [JACKSON-55] Need to add reference information
                wrapAndThrow(provider, e, value, i);
            }
        }
    }

    @JacksonStdImpl
    public static class IteratorSerializer
        extends AsArraySerializerBase<Iterator<?>>
    {
        public IteratorSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts,
                BeanProperty property)
        {
            super(Iterator.class, elemType, staticTyping, vts, property, null);
        }

        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
            return new IteratorSerializer(_elementType, _staticTyping, vts, _property);
        }
        
        @Override
        public void serializeContents(Iterator<?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            if (value.hasNext()) {
                final TypeSerializer typeSer = _valueTypeSerializer;
                JsonSerializer<Object> prevSerializer = null;
                Class<?> prevClass = null;
                do {
                    Object elem = value.next();
                    if (elem == null) {
                        provider.defaultSerializeNull(jgen);
                    } else {
                        // Minor optimization to avoid most lookups:
                        Class<?> cc = elem.getClass();
                        JsonSerializer<Object> currSerializer;
                        if (cc == prevClass) {
                            currSerializer = prevSerializer;
                        } else {
                            currSerializer = provider.findValueSerializer(cc, _property);
                            prevSerializer = currSerializer;
                            prevClass = cc;
                        }
                        if (typeSer == null) {
                            currSerializer.serialize(elem, jgen, provider);
                        } else {
                            currSerializer.serializeWithType(elem, jgen, provider, typeSer);
                        }
                    }
                } while (value.hasNext());
            }
        }
    }
}
