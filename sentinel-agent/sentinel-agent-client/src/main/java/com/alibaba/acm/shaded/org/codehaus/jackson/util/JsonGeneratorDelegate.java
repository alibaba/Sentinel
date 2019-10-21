package com.alibaba.acm.shaded.org.codehaus.jackson.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.SerializedString;

public class JsonGeneratorDelegate extends JsonGenerator
{
    /**
     * Delegate object that method calls are delegated to.
     */
    protected JsonGenerator delegate;

    public JsonGeneratorDelegate(JsonGenerator d) {
        delegate = d;
    }   

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void copyCurrentEvent(JsonParser jp) throws IOException, JsonProcessingException {
        delegate.copyCurrentEvent(jp);
    }

    @Override
    public void copyCurrentStructure(JsonParser jp) throws IOException, JsonProcessingException {
        delegate.copyCurrentStructure(jp);
    }

    @Override
    public JsonGenerator disable(Feature f) {
        return delegate.disable(f);
    }

    @Override
    public JsonGenerator enable(Feature f) {
        return delegate.enable(f);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public ObjectCodec getCodec() {
        return delegate.getCodec();
    }

    @Override
    public JsonStreamContext getOutputContext() {
        return delegate.getOutputContext();
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
    public Object getOutputTarget() {
        return delegate.getOutputTarget();
    }
    
    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public boolean isEnabled(Feature f) {
        return delegate.isEnabled(f);
    }

    @Override
    public JsonGenerator setCodec(ObjectCodec oc) {
        delegate.setCodec(oc);
        return this;
    }

    @Override
    public JsonGenerator useDefaultPrettyPrinter() {
        delegate.useDefaultPrettyPrinter();
        return this;
    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len)
        throws IOException, JsonGenerationException
    {
        delegate.writeBinary(b64variant, data, offset, len);
    }

    @Override
    public void writeBoolean(boolean state) throws IOException, JsonGenerationException {
        delegate.writeBoolean(state);
    }

    @Override
    public void writeEndArray() throws IOException, JsonGenerationException {
        delegate.writeEndArray();
    }

    @Override
    public void writeEndObject() throws IOException, JsonGenerationException {
        delegate.writeEndObject();
    }

    @Override
    public void writeFieldName(String name)
        throws IOException, JsonGenerationException
    {
        delegate.writeFieldName(name);
    }

    @Override
    public void writeFieldName(SerializedString name)
        throws IOException, JsonGenerationException
    {
        delegate.writeFieldName(name);
    }

    @Override
    public void writeFieldName(SerializableString name)
        throws IOException, JsonGenerationException
    {
        delegate.writeFieldName(name);
    }
    
    @Override
    public void writeNull() throws IOException, JsonGenerationException {
        delegate.writeNull();
    }

    @Override
    public void writeNumber(int v) throws IOException, JsonGenerationException {
        delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(long v) throws IOException, JsonGenerationException {
        delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(BigInteger v) throws IOException,
            JsonGenerationException {
        delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(double v) throws IOException,
            JsonGenerationException {
        delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(float v) throws IOException,
            JsonGenerationException {
        delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(BigDecimal v) throws IOException,
            JsonGenerationException {
        delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException, JsonGenerationException, UnsupportedOperationException {
        delegate.writeNumber(encodedValue);
    }

    @Override
    public void writeObject(Object pojo) throws IOException,JsonProcessingException {
        delegate.writeObject(pojo);
    }

    @Override
    public void writeRaw(String text) throws IOException, JsonGenerationException {
        delegate.writeRaw(text);
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException, JsonGenerationException {
        delegate.writeRaw(text, offset, len);
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException, JsonGenerationException {
        delegate.writeRaw(text, offset, len);
    }

    @Override
    public void writeRaw(char c) throws IOException, JsonGenerationException {
        delegate.writeRaw(c);
    }

    @Override
    public void writeRawValue(String text) throws IOException, JsonGenerationException {
        delegate.writeRawValue(text);
    }

    @Override
    public void writeRawValue(String text, int offset, int len) throws IOException, JsonGenerationException {
         delegate.writeRawValue(text, offset, len);
    }

    @Override
    public void writeRawValue(char[] text, int offset, int len) throws IOException, JsonGenerationException {
         delegate.writeRawValue(text, offset, len);
    }

    @Override
    public void writeStartArray() throws IOException, JsonGenerationException {
         delegate.writeStartArray();
    }

    @Override
    public void writeStartObject() throws IOException, JsonGenerationException {
        delegate.writeStartObject();
    }

    @Override
    public void writeString(String text) throws IOException,JsonGenerationException {
        delegate.writeString(text);
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException, JsonGenerationException {
        delegate.writeString(text, offset, len);
    }

    @Override
    public void writeString(SerializableString text) throws IOException, JsonGenerationException {
        delegate.writeString(text);
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length)
        throws IOException, JsonGenerationException
    {
        delegate.writeRawUTF8String(text, offset, length);
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length)
        throws IOException, JsonGenerationException
    {
        delegate.writeUTF8String(text, offset, length);
    }
    
    @Override
    public void writeTree(JsonNode rootNode) throws IOException, JsonProcessingException {
        delegate.writeTree(rootNode);
    }
}
