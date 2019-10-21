package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import java.io.IOException;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Iterator exposed by {@link ObjectMapper} when binding sequence of
 * objects. Extension is done to allow more convenient exposing of
 * {@link IOException} (which basic {@link Iterator} does not expose)
 * 
 * @since 1.8
 */
public class MappingIterator<T> implements Iterator<T>
{
    protected final static MappingIterator<?> EMPTY_ITERATOR =
        new MappingIterator<Object>(null, null, null, null, false, null);
    
    protected final JavaType _type;

    protected final DeserializationContext _context;
    
    protected final JsonDeserializer<T> _deserializer;

    protected JsonParser _parser;
    
    /**
     * Flag that indicates whether input {@link JsonParser} should be closed
     * when we are done or not; generally only called when caller did not
     * pass JsonParser.
     */
    protected final boolean _closeParser;

    /**
     * Flag that is set when we have determined what {@link #hasNextValue()}
     * should value; reset when {@link #nextValue} is called
     */
    protected boolean _hasNextChecked;
    
    /**
     * If not null, "value to update" instead of creating a new instance
     * for each call.
     */
    protected final T _updatedValue;

    protected MappingIterator(JavaType type, JsonParser jp, DeserializationContext ctxt,
            JsonDeserializer<?> deser)
    {
        this(type, jp, ctxt, deser, true, null);
    }
    
    /**
     * @since 1.9.3
     */
    @SuppressWarnings("unchecked")
    protected MappingIterator(JavaType type, JsonParser jp, DeserializationContext ctxt, JsonDeserializer<?> deser,
            boolean closeParser, Object valueToUpdate)
    {
        _type = type;
        _parser = jp;
        _context = ctxt;
        _deserializer = (JsonDeserializer<T>) deser;

        /* One more thing: if we are at START_ARRAY (but NOT root-level
         * one!), advance to next token (to allow matching END_ARRAY)
         */
        if (jp != null && jp.getCurrentToken() == JsonToken.START_ARRAY) {
            JsonStreamContext sc = jp.getParsingContext();
            // safest way to skip current token is to clear it (so we'll advance soon)
            if (!sc.inRoot()) {
                jp.clearCurrentToken();
            }
        }
        _closeParser = closeParser;
        if (valueToUpdate == null) {
            _updatedValue = null;
        } else {
            _updatedValue = (T) valueToUpdate;
        }
    }

    @SuppressWarnings("unchecked")
    protected static <T> MappingIterator<T> emptyIterator() {
        return (MappingIterator<T>) EMPTY_ITERATOR;
    }
    
    /*
    /**********************************************************
    /* Basic iterator impl
    /**********************************************************
     */

    @Override
    public boolean hasNext()
    {
        try {
            return hasNextValue();
        } catch (JsonMappingException e) {
            throw new RuntimeJsonMappingException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public T next()
    {
        try {
            return nextValue();
        } catch (JsonMappingException e) {
            throw new RuntimeJsonMappingException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override public void remove() {
        throw new UnsupportedOperationException();
    }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    /**
     * Equivalent of {@link #next} but one that may throw checked
     * exceptions from Jackson due to invalid input.
     */
    public boolean hasNextValue() throws IOException
    {
        if (_parser == null) {
            return false;
        }
        if (!_hasNextChecked) {
            JsonToken t = _parser.getCurrentToken();
            _hasNextChecked = true;
            if (t == null) { // un-initialized or cleared; find next
                t = _parser.nextToken();
                // If EOF, no more
                if (t == null) {
                    JsonParser jp = _parser;
                    _parser = null;
                    if (_closeParser) {
                        jp.close();
                    }
                    return false;
                }
                /* And similarly if we hit END_ARRAY; except that we won't close parser
                 * (because it's not a root-level iterator)
                 */
                if (t == JsonToken.END_ARRAY) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public T nextValue() throws IOException
    {
        // caller should always call 'hasNext[Value]' first; but let's ensure:
        if (!_hasNextChecked) {
            if (!hasNextValue()) {
                throw new NoSuchElementException();
            }
        }
        if (_parser == null) {
            throw new NoSuchElementException();
        }
        _hasNextChecked = false;
        T result;
        
        if (_updatedValue == null) {
            result = _deserializer.deserialize(_parser, _context);
        } else{
            _deserializer.deserialize(_parser, _context, _updatedValue);
            result = _updatedValue;
        }
        // Need to consume the token too
        _parser.clearCurrentToken();
        return result;
    }
}
