package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

public class AtomicReferenceDeserializer
    extends StdScalarDeserializer<AtomicReference<?>>
    implements ResolvableDeserializer
{
    /**
     * Type of value that we reference
     */
    protected final JavaType _referencedType;
    
    protected final BeanProperty _property;
    
    protected JsonDeserializer<?> _valueDeserializer;
    
    /**
     * @param referencedType Parameterization of this reference
     */
    public AtomicReferenceDeserializer(JavaType referencedType, BeanProperty property)
    {
        super(AtomicReference.class);
        _referencedType = referencedType;
        _property = property;
    }
    
    @Override
    public AtomicReference<?> deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        return new AtomicReference<Object>(_valueDeserializer.deserialize(jp, ctxt));
    }
    
    @Override
    public void resolve(DeserializationConfig config, DeserializerProvider provider)
        throws JsonMappingException
    {
        _valueDeserializer = provider.findValueDeserializer(config, _referencedType, _property);
    }
}
