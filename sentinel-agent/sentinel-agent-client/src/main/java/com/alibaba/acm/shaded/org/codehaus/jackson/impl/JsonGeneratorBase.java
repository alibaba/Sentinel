package com.alibaba.acm.shaded.org.codehaus.jackson.impl;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.DefaultPrettyPrinter;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.VersionUtil;

/**
 * This base class implements part of API that a JSON generator exposes
 * to applications, adds shared internal methods that sub-classes
 * can use and adds some abstract methods sub-classes must implement.
 */
public abstract class JsonGeneratorBase
    extends JsonGenerator
{
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    protected ObjectCodec _objectCodec;

    /**
     * Bit flag composed of bits that indicate which
     * {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator.Feature}s
     * are enabled.
     */
    protected int _features;

    /**
     * Flag set to indicate that implicit conversion from number
     * to JSON String is needed (as per
     * {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator.Feature#WRITE_NUMBERS_AS_STRINGS}).
     */
    protected boolean _cfgNumbersAsStrings;
    
    /*
    /**********************************************************
    /* State
    /**********************************************************
     */

    /**
     * Object that keeps track of the current contextual state
     * of the generator.
     */
    protected JsonWriteContext _writeContext;

    /**
     * Flag that indicates whether generator is closed or not. Gets
     * set when it is closed by an explicit call
     * ({@link #close}).
     */
    protected boolean _closed;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected JsonGeneratorBase(int features, ObjectCodec codec)
    {
        super();
        _features = features;
        _writeContext = JsonWriteContext.createRootContext();
        _objectCodec = codec;
        _cfgNumbersAsStrings = isEnabled(Feature.WRITE_NUMBERS_AS_STRINGS);
    }

    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }
    
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    @Override
    public JsonGenerator enable(Feature f) {
        _features |= f.getMask();
        if (f == Feature.WRITE_NUMBERS_AS_STRINGS) {
            _cfgNumbersAsStrings = true;
        } else if (f == Feature.ESCAPE_NON_ASCII) {
            setHighestNonEscapedChar(127);
        }
        return this;
    }

    @Override
    public JsonGenerator disable(Feature f) {
        _features &= ~f.getMask();
        if (f == Feature.WRITE_NUMBERS_AS_STRINGS) {
            _cfgNumbersAsStrings = false;
        } else if (f == Feature.ESCAPE_NON_ASCII) {
            setHighestNonEscapedChar(0);
        }
        return this;
    }

    //public JsonGenerator configure(Feature f, boolean state) { }

    @Override
    public final boolean isEnabled(Feature f) {
        return (_features & f.getMask()) != 0;
    }

    @Override
    public JsonGenerator useDefaultPrettyPrinter() {
        return setPrettyPrinter(new DefaultPrettyPrinter());
    }
    
    @Override
    public JsonGenerator setCodec(ObjectCodec oc) {
        _objectCodec = oc;
        return this;
    }

    @Override
    public final ObjectCodec getCodec() { return _objectCodec; }

    /*
    /**********************************************************
    /* Public API, accessors
    /**********************************************************
     */

    /**
     * Note: co-variant return type.
     */
    @Override
    public final JsonWriteContext getOutputContext() { return _writeContext; }

    /*
    /**********************************************************
    /* Public API, write methods, structural
    /**********************************************************
     */

    @Override
    public void writeStartArray() throws IOException, JsonGenerationException
    {
        // Array is a value, need to verify it's allowed
        _verifyValueWrite("start an array");
        _writeContext = _writeContext.createChildArrayContext();
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeStartArray(this);
        } else {
            _writeStartArray();
        }
    }

    /**
     * @deprecated since 1.7, should just override {@link #writeStartArray} instead
     *    of defining this method
     */
    @Deprecated
    protected void _writeStartArray() throws IOException, JsonGenerationException {
    }

    @Override
    public void writeEndArray() throws IOException, JsonGenerationException
    {
        if (!_writeContext.inArray()) {
            _reportError("Current context not an ARRAY but "+_writeContext.getTypeDesc());
        }
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeEndArray(this, _writeContext.getEntryCount());
        } else {
            _writeEndArray();
        }
        _writeContext = _writeContext.getParent();
    }

    /**
     * @deprecated since 1.7, should just override {@link #writeEndArray} instead
     *    of defining this method
     */
    @Deprecated
    protected void _writeEndArray() throws IOException, JsonGenerationException {
    }

    @Override
    public void writeStartObject() throws IOException, JsonGenerationException
    {
        _verifyValueWrite("start an object");
        _writeContext = _writeContext.createChildObjectContext();
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeStartObject(this);
        } else {
            _writeStartObject();
        }
    }

    /**
     * @deprecated since 1.7, should just override {@link #writeStartObject} instead
     *    of defining this method
     */
    @Deprecated
    protected void _writeStartObject() throws IOException, JsonGenerationException {
    }

    @Override
    public void writeEndObject() throws IOException, JsonGenerationException
    {
        if (!_writeContext.inObject()) {
            _reportError("Current context not an object but "+_writeContext.getTypeDesc());
        }
        _writeContext = _writeContext.getParent();
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeEndObject(this, _writeContext.getEntryCount());
        } else {
            _writeEndObject();
        }
    }

    /**
     * @deprecated since 1.7, should just override {@link #writeEndObject} instead
     *    of defining this method
     */
    @Deprecated
    protected void _writeEndObject() throws IOException, JsonGenerationException {
    }
    
    /*
    /**********************************************************
    /* Public API, write methods, textual
    /**********************************************************
     */

    //public abstract void writeString(String text) throws IOException, JsonGenerationException;

    //public abstract void writeString(char[] text, int offset, int len) throws IOException, JsonGenerationException;

    //public abstract void writeRaw(String text) throws IOException, JsonGenerationException;

    //public abstract void writeRaw(char[] text, int offset, int len) throws IOException, JsonGenerationException;

    @Override
    public void writeRawValue(String text)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write raw value");
        writeRaw(text);
    }

    @Override
    public void writeRawValue(String text, int offset, int len)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write raw value");
        writeRaw(text, offset, len);
    }

    @Override
    public void writeRawValue(char[] text, int offset, int len)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write raw value");
        writeRaw(text, offset, len);
    }
    
    //public abstract void writeBinary(byte[] data, int offset, int len, boolean includeLFs) throws IOException, JsonGenerationException;

    
    /*
    /**********************************************************
    /* Public API, write methods, primitive
    /**********************************************************
     */

    // Not implemented at this level, added as placeholders

     /*
    public abstract void writeNumber(int i)
    public abstract void writeNumber(long l)
    public abstract void writeNumber(double d)
    public abstract void writeNumber(float f)
    public abstract void writeNumber(BigDecimal dec)
    public abstract void writeBoolean(boolean state)
    public abstract void writeNull()
    */

    /*
    /**********************************************************
    /* Public API, write methods, POJOs, trees
    /**********************************************************
     */

    @Override
    public void writeObject(Object value)
        throws IOException, JsonProcessingException
    {
        if (value == null) {
            // important: call method that does check value write:
            writeNull();
        } else {
            /* 02-Mar-2009, tatu: we are NOT to call _verifyValueWrite here,
             *   because that will be done when codec actually serializes
             *   contained POJO. If we did call it it would advance state
             *   causing exception later on
             */
            if (_objectCodec != null) {
                _objectCodec.writeValue(this, value);
                return;
            }
            _writeSimpleObject(value);
        }
    }

    @Override
    public void writeTree(JsonNode rootNode)
        throws IOException, JsonProcessingException
    {
        // As with 'writeObject()', we are not check if write would work
        if (rootNode == null) {
            writeNull();
        } else {
            if (_objectCodec == null) {
                throw new IllegalStateException("No ObjectCodec defined for the generator, can not serialize JsonNode-based trees");
            }
            _objectCodec.writeTree(this, rootNode);
        }
    }

    /*
    /**********************************************************
    /* Public API, low-level output handling
    /**********************************************************
     */

    @Override
    public abstract void flush() throws IOException;

    @Override
    public void close() throws IOException
    {
        _closed = true;
    }

    @Override
    public boolean isClosed() { return _closed; }

    /*
    /**********************************************************
    /* Public API, copy-through methods
    /**********************************************************
     */

    @Override
    public final void copyCurrentEvent(JsonParser jp)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        // sanity check; what to do?
        if (t == null) {
            _reportError("No current event to copy");
        }
        switch(t) {
        case START_OBJECT:
            writeStartObject();
            break;
        case END_OBJECT:
            writeEndObject();
            break;
        case START_ARRAY:
            writeStartArray();
            break;
        case END_ARRAY:
            writeEndArray();
            break;
        case FIELD_NAME:
            writeFieldName(jp.getCurrentName());
            break;
        case VALUE_STRING:
            if (jp.hasTextCharacters()) {
                writeString(jp.getTextCharacters(), jp.getTextOffset(), jp.getTextLength());
            } else {
                writeString(jp.getText());
            }
            break;
        case VALUE_NUMBER_INT:
            switch (jp.getNumberType()) {
            case INT:
                writeNumber(jp.getIntValue());
                break;
            case BIG_INTEGER:
                writeNumber(jp.getBigIntegerValue());
                break;
            default:
                writeNumber(jp.getLongValue());
            }
            break;
        case VALUE_NUMBER_FLOAT:
            switch (jp.getNumberType()) {
            case BIG_DECIMAL:
                writeNumber(jp.getDecimalValue());
                break;
            case FLOAT:
                writeNumber(jp.getFloatValue());
                break;
            default:
                writeNumber(jp.getDoubleValue());
            }
            break;
        case VALUE_TRUE:
            writeBoolean(true);
            break;
        case VALUE_FALSE:
            writeBoolean(false);
            break;
        case VALUE_NULL:
            writeNull();
            break;
        case VALUE_EMBEDDED_OBJECT:
            writeObject(jp.getEmbeddedObject());
            break;
        default:
            _cantHappen();
        }
    }

    @Override
    public final void copyCurrentStructure(JsonParser jp)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();

        // Let's handle field-name separately first
        if (t == JsonToken.FIELD_NAME) {
            writeFieldName(jp.getCurrentName());
            t = jp.nextToken();
            // fall-through to copy the associated value
        }

        switch (t) {
        case START_ARRAY:
            writeStartArray();
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                copyCurrentStructure(jp);
            }
            writeEndArray();
            break;
        case START_OBJECT:
            writeStartObject();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                copyCurrentStructure(jp);
            }
            writeEndObject();
            break;
        default: // others are simple:
            copyCurrentEvent(jp);
        }
    }

    /*
    /**********************************************************
    /* Package methods for this, sub-classes
    /**********************************************************
     */

    protected abstract void _releaseBuffers();

    protected abstract void _verifyValueWrite(String typeMsg)
        throws IOException, JsonGenerationException;

    protected void _reportError(String msg)
        throws JsonGenerationException
    {
        throw new JsonGenerationException(msg);
    }

    protected void _cantHappen()
    {
        throw new RuntimeException("Internal error: should never end up through this code path");
    }

    /**
     * Helper method to try to call appropriate write method for given
     * untyped Object. At this point, no structural conversions should be done,
     * only simple basic types are to be coerced as necessary.
     *
     * @param value Non-null value to write
     */
    protected void _writeSimpleObject(Object value) 
        throws IOException, JsonGenerationException
    {
        /* 31-Dec-2009, tatu: Actually, we could just handle some basic
         *    types even without codec. This can improve interoperability,
         *    and specifically help with TokenBuffer.
         */
        if (value == null) {
            writeNull();
            return;
        }
        if (value instanceof String) {
            writeString((String) value);
            return;
        }
        if (value instanceof Number) {
            Number n = (Number) value;
            if (n instanceof Integer) {
                writeNumber(n.intValue());
                return;
            } else if (n instanceof Long) {
                writeNumber(n.longValue());
                return;
            } else if (n instanceof Double) {
                writeNumber(n.doubleValue());
                return;
            } else if (n instanceof Float) {
                writeNumber(n.floatValue());
                return;
            } else if (n instanceof Short) {
                writeNumber(n.shortValue());
                return;
            } else if (n instanceof Byte) {
                writeNumber(n.byteValue());
                return;
            } else if (n instanceof BigInteger) {
                writeNumber((BigInteger) n);
                return;
            } else if (n instanceof BigDecimal) {
                writeNumber((BigDecimal) n);
                return;
                
            // then Atomic types
                
            } else if (n instanceof AtomicInteger) {
                writeNumber(((AtomicInteger) n).get());
                return;
            } else if (n instanceof AtomicLong) {
                writeNumber(((AtomicLong) n).get());
                return;
            }
        } else if (value instanceof byte[]) {
            writeBinary((byte[]) value);
            return;
        } else if (value instanceof Boolean) {
            writeBoolean(((Boolean) value).booleanValue());
            return;
        } else if (value instanceof AtomicBoolean) {
            writeBoolean(((AtomicBoolean) value).get());
            return;
        }
        throw new IllegalStateException("No ObjectCodec defined for the generator, can only serialize simple wrapper types (type passed "
                +value.getClass().getName()+")");
    }    

    protected final void _throwInternal() {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }

    /**
     * @since 1.7
     */
    protected void _reportUnsupportedOperation() {
        throw new UnsupportedOperationException("Operation not supported by generator of type "+getClass().getName());
    }
}
