package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl;

import java.io.IOException;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.SettableBeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.TokenBuffer;

/**
 * Helper class that is used to flatten JSON structure when using
 * "external type id" (see {@link com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo.As#EXTERNAL_PROPERTY}).
 * This is needed to store temporary state and buffer tokens, as the structure is
 * rearranged a bit so that actual type deserializer can resolve type and 
 * finalize deserialization.
 * 
 * @since 1.9
 */
public class ExternalTypeHandler
{
    private final ExtTypedProperty[] _properties;
    private final HashMap<String, Integer> _nameToPropertyIndex;

    private final String[] _typeIds;
    private final TokenBuffer[] _tokens;
    
    protected ExternalTypeHandler(ExtTypedProperty[] properties,
            HashMap<String, Integer> nameToPropertyIndex,
            String[] typeIds, TokenBuffer[] tokens)
    {
        _properties = properties;        
        _nameToPropertyIndex = nameToPropertyIndex;
        _typeIds = typeIds;
        _tokens = tokens;
    }

    protected ExternalTypeHandler(ExternalTypeHandler h)
    {
        _properties = h._properties;
        _nameToPropertyIndex = h._nameToPropertyIndex;
        int len = _properties.length;
        _typeIds = new String[len];
        _tokens = new TokenBuffer[len];
    }
    
    public ExternalTypeHandler start() {
        return new ExternalTypeHandler(this);
    }

    /**
     * Method called to ask handler to handle 
     */
    public boolean handleToken(JsonParser jp, DeserializationContext ctxt,
            String propName, Object bean)
        throws IOException, JsonProcessingException
    {
        Integer I = _nameToPropertyIndex.get(propName);
        if (I == null) {
            return false;
        }
        int index = I.intValue();
        ExtTypedProperty prop = _properties[index];
        boolean canDeserialize;
        if (prop.hasTypePropertyName(propName)) {
            _typeIds[index] = jp.getText();
            jp.skipChildren();
            canDeserialize = (bean != null) && (_tokens[index] != null);
        } else {
            TokenBuffer tokens = new TokenBuffer(jp.getCodec());
            tokens.copyCurrentStructure(jp);
            _tokens[index] = tokens;
            canDeserialize = (bean != null) && (_typeIds[index] != null);
        }
        /* Minor optimization: let's deserialize properties as soon as
         * we have all pertinent information:
         */
        if (canDeserialize) {
            _deserialize(jp, ctxt, bean, index);
            // clear stored data, to avoid deserializing+setting twice:
            _typeIds[index] = null;
            _tokens[index] = null;
        }
        return true;
    }

    public Object complete(JsonParser jp, DeserializationContext ctxt, Object bean)
        throws IOException, JsonProcessingException
    {
        for (int i = 0, len = _properties.length; i < len; ++i) {
            if (_typeIds[i] == null) {
                // let's allow missing both type and property (may already have been set, too)
                if (_tokens[i] == null) {
                    continue;
                }
                // but not just one
                throw ctxt.mappingException("Missing external type id property '"+_properties[i].getTypePropertyName());
            } else if (_tokens[i] == null) {
                SettableBeanProperty prop = _properties[i].getProperty();
                throw ctxt.mappingException("Missing property '"+prop.getName()+"' for external type id '"+_properties[i].getTypePropertyName());
            }
            _deserialize(jp, ctxt, bean, i);
        }
        return bean;
    }
    
    protected final void _deserialize(JsonParser jp, DeserializationContext ctxt, Object bean, int index)
        throws IOException, JsonProcessingException
    {
        /* Ok: time to mix type id, value; and we will actually use "wrapper-array"
         * style to ensure we can handle all kinds of JSON constructs.
         */
        TokenBuffer merged = new TokenBuffer(jp.getCodec());
        merged.writeStartArray();
        merged.writeString(_typeIds[index]);
        JsonParser p2 = _tokens[index].asParser(jp);
        p2.nextToken();
        merged.copyCurrentStructure(p2);
        merged.writeEndArray();
        // needs to point to START_OBJECT (or whatever first token is)
        p2 = merged.asParser(jp);
        p2.nextToken();
        _properties[index].getProperty().deserializeAndSet(p2, ctxt, bean);
    }
    
    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */
    
    public static class Builder
    {
        private final ArrayList<ExtTypedProperty> _properties = new ArrayList<ExtTypedProperty>();
        private final HashMap<String, Integer> _nameToPropertyIndex = new HashMap<String, Integer>();
        
        public void addExternal(SettableBeanProperty property, String extPropName)
        {
            Integer index = _properties.size();
            _properties.add(new ExtTypedProperty(property, extPropName));
            _nameToPropertyIndex.put(property.getName(), index);
            _nameToPropertyIndex.put(extPropName, index);
        }
        
        public ExternalTypeHandler build() {
            return new ExternalTypeHandler(_properties.toArray(new ExtTypedProperty[_properties.size()]),
                    _nameToPropertyIndex, null, null);
        }
    }

    private final static class ExtTypedProperty
    {
        private final SettableBeanProperty _property;
        private final String _typePropertyName;
        
        public ExtTypedProperty(SettableBeanProperty property, String typePropertyName)
        {
            _property = property;
            _typePropertyName = typePropertyName;
        }

        public boolean hasTypePropertyName(String n) {
            return n.equals(_typePropertyName);
        }

        public String getTypePropertyName() { return _typePropertyName; }
        
        public SettableBeanProperty getProperty() {
            return _property;
        }
    }
}
