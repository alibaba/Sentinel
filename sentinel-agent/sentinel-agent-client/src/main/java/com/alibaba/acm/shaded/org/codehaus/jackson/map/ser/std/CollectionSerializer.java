package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Fallback serializer for cases where Collection is not known to be
 * of type for which more specializer serializer exists (such as
 * index-accessible List).
 * If so, we will just construct an {@link java.util.Iterator}
 * to iterate over elements.
 */
public class CollectionSerializer
    extends AsArraySerializerBase<Collection<?>>
{
    public CollectionSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts,
            BeanProperty property, JsonSerializer<Object> valueSerializer)
    {
        super(Collection.class, elemType, staticTyping, vts, property, valueSerializer);
    }
    
    @Override
    public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
        return new CollectionSerializer(_elementType, _staticTyping, vts, _property, _elementSerializer);
    }
    
    @Override
    public void serializeContents(Collection<?> value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        if (_elementSerializer != null) {
            serializeContentsUsing(value, jgen, provider, _elementSerializer);
            return;
        }
        Iterator<?> it = value.iterator();
        if (!it.hasNext()) {
            return;
        }
        PropertySerializerMap serializers = _dynamicSerializers;
        final TypeSerializer typeSer = _valueTypeSerializer;

        int i = 0;
        try {
            do {
                Object elem = it.next();
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
                    if (typeSer == null) {
                        serializer.serialize(elem, jgen, provider);
                    } else {
                        serializer.serializeWithType(elem, jgen, provider, typeSer);
                    }
                }
                ++i;
            } while (it.hasNext());
        } catch (Exception e) {
            // [JACKSON-55] Need to add reference information
            wrapAndThrow(provider, e, value, i);
        }
    }

    public void serializeContentsUsing(Collection<?> value, JsonGenerator jgen, SerializerProvider provider,
            JsonSerializer<Object> ser)
        throws IOException, JsonGenerationException
    {
        Iterator<?> it = value.iterator();
        if (it.hasNext()) {
            TypeSerializer typeSer = _valueTypeSerializer;
            int i = 0;
            do {
                Object elem = it.next();
                try {
                    if (elem == null) {
                        provider.defaultSerializeNull(jgen);
                    } else {
                        if (typeSer == null) {
                            ser.serialize(elem, jgen, provider);
                        } else {
                            ser.serializeWithType(elem, jgen, provider, typeSer);
                        }
                    }
                    ++i;
                } catch (Exception e) {
                    // [JACKSON-55] Need to add reference information
                    wrapAndThrow(provider, e, value, i);
                }
            } while (it.hasNext());
        }
    }
}
