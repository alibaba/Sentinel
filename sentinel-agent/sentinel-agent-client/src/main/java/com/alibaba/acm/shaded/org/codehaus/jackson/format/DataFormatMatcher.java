package com.alibaba.acm.shaded.org.codehaus.jackson.format;

import java.io.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.MergedStream;

/**
 * Result object constructed by {@link DataFormatDetector} when requested
 * to detect format of given input data.
 */
public class DataFormatMatcher
{
    protected final InputStream _originalStream;

    /**
     * Content read during format matching process
     */
    protected final byte[] _bufferedData;

    /**
     * Number of bytes in {@link #_bufferedData} that were read.
     */
    protected final int _bufferedLength;

    /**
     * Factory that produced sufficient match (if any)
     */
    protected final JsonFactory _match;

    /**
     * Strength of match with {@link #_match}
     */
    protected final MatchStrength _matchStrength;
    
    protected DataFormatMatcher(InputStream in, byte[] buffered, int bufferedLength,
            JsonFactory match, MatchStrength strength)
    {
        _originalStream = in;
        _bufferedData = buffered;
        _bufferedLength = bufferedLength;
        _match = match;
        _matchStrength = strength;
    }

    /*
    /**********************************************************
    /* Public API, simple accessors
    /**********************************************************
     */

    /**
     * Accessor to use to see if any formats matched well enough with
     * the input data.
     */
    public boolean hasMatch() { return _match != null; }

    /**
     * Method for accessing strength of the match, if any; if no match,
     * will return {@link MatchStrength#INCONCLUSIVE}.
     */
    public MatchStrength getMatchStrength() {
        return (_matchStrength == null) ? MatchStrength.INCONCLUSIVE : _matchStrength;
    }

    /**
     * Accessor for {@link JsonFactory} that represents format that data matched.
     */
    public JsonFactory getMatch() { return _match; }

    /**
     * Accessor for getting brief textual name of matched format if any (null
     * if none). Equivalent to:
     *<pre>
     *   return hasMatch() ? getMatch().getFormatName() : null;
     *</pre>
     */
    public String getMatchedFormatName() {
        return _match.getFormatName();
    }
    
    /*
    /**********************************************************
    /* Public API, factory methods
    /**********************************************************
     */
    
    /**
     * Convenience method for trying to construct a {@link JsonParser} for
     * parsing content which is assumed to be in detected data format.
     * If no match was found, returns null.
     */
    public JsonParser createParserWithMatch() throws IOException {
        if (_match == null) {
            return null;
        }
        if (_originalStream == null) {
            return _match.createJsonParser(_bufferedData, 0, _bufferedLength);
        }
        return _match.createJsonParser(getDataStream());
    }
    
    /**
     * Method to use for accessing input for which format detection has been done.
     * This <b>must</b> be used instead of using stream passed to detector
     * unless given stream itself can do buffering.
     * Stream will return all content that was read during matching process, as well
     * as remaining contents of the underlying stream.
     */
    public InputStream getDataStream() {
        if (_originalStream == null) {
            return new ByteArrayInputStream(_bufferedData, 0, _bufferedLength);
        }
        return new MergedStream(null, _originalStream, _bufferedData, 0, _bufferedLength);
    }
}
