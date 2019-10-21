package com.alibaba.acm.shaded.org.codehaus.jackson.node;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.impl.JsonParserMinimalBase;

/**
 * Facade over {@link JsonNode} that implements {@link JsonParser} to allow
 * accessing contents of JSON tree in alternate form (stream of tokens).
 * Useful when a streaming source is expected by code, such as data binding
 * functionality.
 * 
 * @author tatu
 * 
 * @since 1.3
 */
public class TreeTraversingParser extends JsonParserMinimalBase
{
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    protected ObjectCodec _objectCodec;

    /**
     * Traversal context within tree
     */
    protected NodeCursor _nodeCursor;

    /*
    /**********************************************************
    /* State
    /**********************************************************
     */

    /**
     * Sometimes parser needs to buffer a single look-ahead token; if so,
     * it'll be stored here. This is currently used for handling 
     */
    protected JsonToken _nextToken;

    /**
     * Flag needed to handle recursion into contents of child
     * Array/Object nodes.
     */
    protected boolean _startContainer;
    
    /**
     * Flag that indicates whether parser is closed or not. Gets
     * set when parser is either closed by explicit call
     * ({@link #close}) or when end-of-input is reached.
     */
    protected boolean _closed;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public TreeTraversingParser(JsonNode n) { this(n, null); }

    public TreeTraversingParser(JsonNode n, ObjectCodec codec)
    {
        super(0);
        _objectCodec = codec;
        if (n.isArray()) {
            _nextToken = JsonToken.START_ARRAY;
            _nodeCursor = new NodeCursor.Array(n, null);
        } else if (n.isObject()) {
            _nextToken = JsonToken.START_OBJECT;
            _nodeCursor = new NodeCursor.Object(n, null);
        } else { // value node
            _nodeCursor = new NodeCursor.RootValue(n, null);
        }
    }

    @Override
    public void setCodec(ObjectCodec c) {
        _objectCodec = c;
    }

    @Override
    public ObjectCodec getCodec() {
        return _objectCodec;
    }

    /*
    /**********************************************************
    /* Closeable implementation
    /**********************************************************
     */

    @Override
    public void close() throws IOException
    {
        if (!_closed) {
            _closed = true;
            _nodeCursor = null;
            _currToken = null;
        }
    }

    /*
    /**********************************************************
    /* Public API, traversal
    /**********************************************************
     */

    @Override
    public JsonToken nextToken() throws IOException, JsonParseException
    {
        if (_nextToken != null) {
            _currToken = _nextToken;
            _nextToken = null;
            return _currToken;
        }
        // are we to descend to a container child?
        if (_startContainer) {
            _startContainer = false;
            // minor optimization: empty containers can be skipped
            if (!_nodeCursor.currentHasChildren()) {
                _currToken = (_currToken == JsonToken.START_OBJECT) ?
                    JsonToken.END_OBJECT : JsonToken.END_ARRAY;
                return _currToken;
            }
            _nodeCursor = _nodeCursor.iterateChildren();
            _currToken = _nodeCursor.nextToken();
            if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
                _startContainer = true;
            }
            return _currToken;
        }
        // No more content?
        if (_nodeCursor == null) {
            _closed = true; // if not already set
            return null;
        }
        // Otherwise, next entry from current cursor
        _currToken = _nodeCursor.nextToken();
        if (_currToken != null) {
            if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
                _startContainer = true;
            }
            return _currToken;
        }
        // null means no more children; need to return end marker
        _currToken = _nodeCursor.endToken();
        _nodeCursor = _nodeCursor.getParent();
        return _currToken;
    }
    
    // default works well here:
    //public JsonToken nextValue() throws IOException, JsonParseException

    @Override
    public JsonParser skipChildren() throws IOException, JsonParseException
    {
        if (_currToken == JsonToken.START_OBJECT) {
            _startContainer = false;
            _currToken = JsonToken.END_OBJECT;
        } else if (_currToken == JsonToken.START_ARRAY) {
            _startContainer = false;
            _currToken = JsonToken.END_ARRAY;
        }
        return this;
    }

    @Override
    public boolean isClosed() {
        return _closed;
    }

    /*
    /**********************************************************
    /* Public API, token accessors
    /**********************************************************
     */

    @Override
    public String getCurrentName() {
        return (_nodeCursor == null) ? null : _nodeCursor.getCurrentName();
    }

    @Override
    public JsonStreamContext getParsingContext() {
        return _nodeCursor;
    }

    @Override
    public JsonLocation getTokenLocation() {
        return JsonLocation.NA;
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return JsonLocation.NA;
    }

    /*
    /**********************************************************
    /* Public API, access to textual content
    /**********************************************************
     */

    @Override
    public String getText()
    {
        if (_closed) {
            return null;
        }
        // need to separate handling a bit...
        switch (_currToken) {
        case FIELD_NAME:
            return _nodeCursor.getCurrentName();
        case VALUE_STRING:
            return currentNode().getTextValue();
        case VALUE_NUMBER_INT:
        case VALUE_NUMBER_FLOAT:
            return String.valueOf(currentNode().getNumberValue());
        case VALUE_EMBEDDED_OBJECT:
            JsonNode n = currentNode();
            if (n != null && n.isBinary()) {
                // this will convert it to base64
                return n.asText();
            }
        }

        return (_currToken == null) ? null : _currToken.asString();
    }

    @Override
    public char[] getTextCharacters() throws IOException, JsonParseException {
        return getText().toCharArray();
    }

    @Override
    public int getTextLength() throws IOException, JsonParseException {
        return getText().length();
    }

    @Override
    public int getTextOffset() throws IOException, JsonParseException {
        return 0;
    }

    @Override
    public boolean hasTextCharacters() {
        // generally we do not have efficient access as char[], hence:
        return false;
    }
    
    /*
    /**********************************************************
    /* Public API, typed non-text access
    /**********************************************************
     */

    //public byte getByteValue() throws IOException, JsonParseException

    @Override
    public NumberType getNumberType() throws IOException, JsonParseException {
        JsonNode n = currentNumericNode();
        return (n == null) ? null : n.getNumberType();
    }

    @Override
    public BigInteger getBigIntegerValue() throws IOException, JsonParseException
    {
        return currentNumericNode().getBigIntegerValue();
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException, JsonParseException {
        return currentNumericNode().getDecimalValue();
    }

    @Override
    public double getDoubleValue() throws IOException, JsonParseException {
        return currentNumericNode().getDoubleValue();
    }

    @Override
    public float getFloatValue() throws IOException, JsonParseException {
        return (float) currentNumericNode().getDoubleValue();
    }

    @Override
    public long getLongValue() throws IOException, JsonParseException {
        return currentNumericNode().getLongValue();
    }

    @Override
    public int getIntValue() throws IOException, JsonParseException {
        return currentNumericNode().getIntValue();
    }

    @Override
    public Number getNumberValue() throws IOException, JsonParseException {
        return currentNumericNode().getNumberValue();
    }

    @Override
    public Object getEmbeddedObject()
    {
        if (!_closed) {
            JsonNode n = currentNode();
            if (n != null) {
                if (n.isPojo()) {
                    return ((POJONode) n).getPojo();
                }
                if (n.isBinary()) {
                    return ((BinaryNode) n).getBinaryValue();
                }
            }
        }
        return null;
    }

    /*
    /**********************************************************
    /* Public API, typed binary (base64) access
    /**********************************************************
     */

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant)
        throws IOException, JsonParseException
    {
        // Multiple possibilities...
        JsonNode n = currentNode();
        if (n != null) { // binary node?
            byte[] data = n.getBinaryValue();
            // (or TextNode, which can also convert automatically!)
            if (data != null) {
                return data;
            }
            // Or maybe byte[] as POJO?
            if (n.isPojo()) {
                Object ob = ((POJONode) n).getPojo();
                if (ob instanceof byte[]) {
                    return (byte[]) ob;
                }
            }
        }
        // otherwise return null to mark we have no binary content
        return null;
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    protected JsonNode currentNode() {
        if (_closed || _nodeCursor == null) {
            return null;
        }
        return _nodeCursor.currentNode();
    }

    protected JsonNode currentNumericNode()
        throws JsonParseException
    {
        JsonNode n = currentNode();
        if (n == null || !n.isNumber()) {
            JsonToken t = (n == null) ? null : n.asToken();
            throw _constructError("Current token ("+t+") not numeric, can not use numeric value accessors");
        }
        return n;
    }

    @Override
    protected void _handleEOF() throws JsonParseException {
        _throwInternal(); // should never get called
    }
}
