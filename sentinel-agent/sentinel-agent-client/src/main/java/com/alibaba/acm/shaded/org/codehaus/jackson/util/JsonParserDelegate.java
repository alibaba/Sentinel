package com.alibaba.acm.shaded.org.codehaus.jackson.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;

/**
 * Helper class that implements
 * <a href="http://en.wikipedia.org/wiki/Delegation_pattern">delegation pattern</a> for {@link JsonParser},
 * to allow for simple overridability of basic parsing functionality.
 * The idea is that any functionality to be modified can be simply
 * overridden; and anything else will be delegated by default.
 * 
 * @since 1.4
 */
public class JsonParserDelegate extends JsonParser
{
    /**
     * Delegate object that method calls are delegated to.
     */
    protected JsonParser delegate;

    public JsonParserDelegate(JsonParser d) {
        delegate = d;
    }

    /*
    /**********************************************************
    /* Public API, configuration
    /**********************************************************
     */

    @Override
    public void setCodec(ObjectCodec c) {
        delegate.setCodec(c);
    }

    @Override
    public ObjectCodec getCodec() {
        return delegate.getCodec();
    }

    @Override
    public JsonParser enable(Feature f) {
        delegate.enable(f);
        return this;
    }

    @Override
    public JsonParser disable(Feature f) {
        delegate.disable(f);
        return this;
    }
 
    @Override
    public boolean isEnabled(Feature f) {
        return delegate.isEnabled(f);
    }

    @Override
    public void setSchema(FormatSchema schema) {
        delegate.setSchema(schema);
    }

    @Override
    public boolean canUseSchema(FormatSchema schema) {
        return delegate.canUseSchema(schema);
    }
    
    @Override
    public Version version() {
        return delegate.version();
    }

    @Override
    public Object getInputSource() {
        return delegate.getInputSource();
    }
    
    /*
    /**********************************************************
    /* Closeable impl
    /**********************************************************
     */

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    /*
    /**********************************************************
    /* Public API, token accessors
    /**********************************************************
     */

    @Override
    public JsonToken getCurrentToken() {
        return delegate.getCurrentToken();
    }

    @Override
    public boolean hasCurrentToken() {
        return delegate.hasCurrentToken();
    }

    @Override
    public void clearCurrentToken() {
        delegate.clearCurrentToken();        
    }

    @Override
    public String getCurrentName() throws IOException, JsonParseException {
        return delegate.getCurrentName();
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return delegate.getCurrentLocation();
    }

    @Override
    public JsonToken getLastClearedToken() {
        return delegate.getLastClearedToken();
    }

    @Override
    public JsonStreamContext getParsingContext() {
        return delegate.getParsingContext();
    }

    /*
    /**********************************************************
    /* Public API, access to token information, text
    /**********************************************************
     */

    @Override
    public String getText() throws IOException, JsonParseException {
        return delegate.getText();
    }

    @Override
    public char[] getTextCharacters() throws IOException, JsonParseException {
        return delegate.getTextCharacters();
    }

    @Override
    public int getTextLength() throws IOException, JsonParseException {
        return delegate.getTextLength();
    }

    @Override
    public int getTextOffset() throws IOException, JsonParseException {
        return delegate.getTextOffset();
    }

    /*
    /**********************************************************
    /* Public API, access to token information, numeric
    /**********************************************************
     */

    public boolean getBooleanValue() throws IOException, JsonParseException {
        return delegate.getBooleanValue();
    }
    
    @Override
    public BigInteger getBigIntegerValue() throws IOException,JsonParseException {
        return delegate.getBigIntegerValue();
    }

    @Override
    public byte getByteValue() throws IOException, JsonParseException {
        return delegate.getByteValue();
    }

    @Override
    public short getShortValue() throws IOException, JsonParseException {
        return delegate.getShortValue();
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException, JsonParseException {
        return delegate.getDecimalValue();
    }

    @Override
    public double getDoubleValue() throws IOException, JsonParseException {
        return delegate.getDoubleValue();
    }

    @Override
    public float getFloatValue() throws IOException, JsonParseException {
        return delegate.getFloatValue();
    }

    @Override
    public int getIntValue() throws IOException, JsonParseException {
        return delegate.getIntValue();
    }

    @Override
    public long getLongValue() throws IOException, JsonParseException {
        return delegate.getLongValue();
    }

    @Override
    public NumberType getNumberType() throws IOException, JsonParseException {
        return delegate.getNumberType();
    }

    @Override
    public Number getNumberValue() throws IOException, JsonParseException {
        return delegate.getNumberValue();
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws IOException, JsonParseException {
        return delegate.getBinaryValue(b64variant);
    }

    @Override
    public Object getEmbeddedObject() throws IOException, JsonParseException {
        return delegate.getEmbeddedObject();
    }
    
    @Override
    public JsonLocation getTokenLocation() {
        return delegate.getTokenLocation();
    }

    @Override
    public JsonToken nextToken() throws IOException, JsonParseException {
        return delegate.nextToken();
    }
    
    @Override
    public JsonParser skipChildren() throws IOException, JsonParseException {
        delegate.skipChildren();
        // NOTE: must NOT delegate this method to delegate, needs to be self-reference for chaining
        return this;
    }
}
