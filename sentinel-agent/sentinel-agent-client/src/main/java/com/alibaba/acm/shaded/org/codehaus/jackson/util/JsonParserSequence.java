package com.alibaba.acm.shaded.org.codehaus.jackson.util;

import java.io.IOException;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;

/**
 * Helper class that can be used to sequence multiple physical
 * {@link JsonParser}s to create a single logical sequence of
 * tokens, as a single {@link JsonParser}.
 *<p>
 * Fairly simple use of {@link JsonParserDelegate}: only need
 * to override {@link #nextToken} to handle transition
 * 
 * @author tatu
 * @since 1.5
 */
public class JsonParserSequence extends JsonParserDelegate
{
    /**
     * Parsers other than the first one (which is initially assigned
     * as delegate)
     */
    protected final JsonParser[] _parsers;
    
    /**
     * Index of the next parser in {@link #_parsers}.
     */
    protected int _nextParser;
    
    /*
     *******************************************************
     * Construction
     *******************************************************
     */

    protected JsonParserSequence(JsonParser[] parsers)
    {
        super(parsers[0]);
        _parsers = parsers;
        _nextParser = 1;
    }

    /**
     * Method that will construct a parser (possibly a sequence) that
     * contains all given sub-parsers.
     * All parsers given are checked to see if they are sequences: and
     * if so, they will be "flattened", that is, contained parsers are
     * directly added in a new sequence instead of adding sequences
     * within sequences. This is done to minimize delegation depth,
     * ideally only having just a single level of delegation.
     */
    public static JsonParserSequence createFlattened(JsonParser first, JsonParser second)
    {
        if (!(first instanceof JsonParserSequence || second instanceof JsonParserSequence)) {
            // simple:
            return new JsonParserSequence(new JsonParser[] { first, second });
        }
        ArrayList<JsonParser> p = new ArrayList<JsonParser>();
        if (first instanceof JsonParserSequence) {
            ((JsonParserSequence) first).addFlattenedActiveParsers(p);
        } else {
            p.add(first);
        }
        if (second instanceof JsonParserSequence) {
            ((JsonParserSequence) second).addFlattenedActiveParsers(p);
        } else {
            p.add(second);
        }
        return new JsonParserSequence(p.toArray(new JsonParser[p.size()]));
    }

    protected void addFlattenedActiveParsers(List<JsonParser> result)
    {
        for (int i = _nextParser-1, len = _parsers.length; i < len; ++i) {
            JsonParser p = _parsers[i];
            if (p instanceof JsonParserSequence) {
                ((JsonParserSequence) p).addFlattenedActiveParsers(result);
            } else {
                result.add(p);
            }
        }
    }
    
    /*
     *******************************************************
     * Overridden methods, needed: cases where default
     * delegation does not work
     *******************************************************
     */
    
    @Override
    public void close() throws IOException
    {
        do {
            delegate.close();
        } while (switchToNext());
    }

    @Override
    public JsonToken nextToken() throws IOException, JsonParseException
    {
        JsonToken t = delegate.nextToken();
        if (t != null) return t;
        while (switchToNext()) {
            t = delegate.nextToken();
            if (t != null) return t;
        }
        return null;
    }

    /*
    /*******************************************************
    /* Additional extended API
    /*******************************************************
     */

    /**
     * Method that is most useful for debugging or testing;
     * returns actual number of underlying parsers sequence
     * was constructed with (nor just ones remaining active)
     */
    public int containedParsersCount() {
        return _parsers.length;
    }
    
    /*
    /*******************************************************
    /* Helper methods
    /*******************************************************
     */

    /**
     * Method that will switch active parser from the current one
     * to next parser in sequence, if there is another parser left,
     * making this the new delegate. Old delegate is returned if
     * switch succeeds.
     * 
     * @return True if switch succeeded; false otherwise
     */
    protected boolean switchToNext()
    {
        if (_nextParser >= _parsers.length) {
            return false;
        }
        delegate = _parsers[_nextParser++];
        return true;
    }
}
