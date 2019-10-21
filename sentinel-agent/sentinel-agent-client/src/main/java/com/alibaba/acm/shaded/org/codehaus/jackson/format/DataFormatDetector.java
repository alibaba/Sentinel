package com.alibaba.acm.shaded.org.codehaus.jackson.format;

import java.io.*;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;

/**
 * Simple helper class that allows data format (content type) auto-detection,
 * given an ordered set of {@link JsonFactory} instances to use for actual low-level
 * detection.
 *
 * @since 1.7
 */
public class DataFormatDetector
{
    /**
     * By default we will look ahead at most 64 bytes; in most cases,
     * much less (4 bytes or so) is needed, but we will allow bit more
     * leniency to support data formats that need more complex heuristics.
     */
    public final static int DEFAULT_MAX_INPUT_LOOKAHEAD = 64;
    
    /**
     * Ordered list of factories which both represent data formats to
     * detect (in precedence order, starting with highest) and are used
     * for actual detection.
     */
    protected final JsonFactory[] _detectors;

    /**
     * Strength of match we consider to be good enough to be used
     * without checking any other formats.
     * Default value is {@link MatchStrength#SOLID_MATCH}, 
     */
    protected final MatchStrength _optimalMatch;

    /**
     * Strength of minimal match we accept as the answer, unless
     * better matches are found. 
     * Default value is {@link MatchStrength#WEAK_MATCH}, 
     */
    protected final MatchStrength _minimalMatch;

    /**
     * Maximum number of leading bytes of the input that we can read
     * to determine data format.
     *<p>
     * Default value is {@link #DEFAULT_MAX_INPUT_LOOKAHEAD}.
     */
    protected final int _maxInputLookahead;
    
    /*
    /**********************************************************
    /* Construction
    /**********************************************************
     */
    
    public DataFormatDetector(JsonFactory... detectors) {
        this(detectors, MatchStrength.SOLID_MATCH, MatchStrength.WEAK_MATCH,
            DEFAULT_MAX_INPUT_LOOKAHEAD);
    }

    public DataFormatDetector(Collection<JsonFactory> detectors) {
        this(detectors.toArray(new JsonFactory[detectors.size()]));
    }

    /**
     * Method that will return a detector instance that uses given
     * optimal match level (match that is considered sufficient to return, without
     * trying to find stronger matches with other formats).
     */
    public DataFormatDetector withOptimalMatch(MatchStrength optMatch) {
        if (optMatch == _optimalMatch) {
            return this;
        }
        return new DataFormatDetector(_detectors, optMatch, _minimalMatch, _maxInputLookahead);
    }
    /**
     * Method that will return a detector instance that uses given
     * minimal match level; match that may be returned unless a stronger match
     * is found with other format detectors.
     */
    public DataFormatDetector withMinimalMatch(MatchStrength minMatch) {
        if (minMatch == _minimalMatch) {
            return this;
        }
        return new DataFormatDetector(_detectors, _optimalMatch, minMatch, _maxInputLookahead);
    }

    /**
     * Method that will return a detector instance that allows detectors to
     * read up to specified number of bytes when determining format match strength.
     */
    public DataFormatDetector withMaxInputLookahead(int lookaheadBytes)
    {
        if (lookaheadBytes == _maxInputLookahead) {
            return this;
        }
        return new DataFormatDetector(_detectors, _optimalMatch, _minimalMatch, lookaheadBytes);
    }
    
    private DataFormatDetector(JsonFactory[] detectors,
            MatchStrength optMatch, MatchStrength minMatch,
            int maxInputLookahead)
    {
        _detectors = detectors;
        _optimalMatch = optMatch;
        _minimalMatch = minMatch;
        _maxInputLookahead = maxInputLookahead;
    }

    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    /**
     * Method to call to find format that content (accessible via given
     * {@link InputStream}) given has, as per configuration of this detector
     * instance.
     * 
     * @return Matcher object which contains result; never null, even in cases
     *    where no match (with specified minimal match strength) is found.
     */
    public DataFormatMatcher findFormat(InputStream in) throws IOException
    {
        return _findFormat(new InputAccessor.Std(in, new byte[_maxInputLookahead]));
    }

    /**
     * Method to call to find format that given content (full document)
     * has, as per configuration of this detector instance.
     * 
     * @return Matcher object which contains result; never null, even in cases
     *    where no match (with specified minimal match strength) is found.
     */
    public DataFormatMatcher findFormat(byte[] fullInputData) throws IOException
    {
        return _findFormat(new InputAccessor.Std(fullInputData));
    }
    
    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    private DataFormatMatcher _findFormat(InputAccessor.Std acc) throws IOException
    {
        JsonFactory bestMatch = null;
        MatchStrength bestMatchStrength = null;
        for (JsonFactory f : _detectors) {
            acc.reset();
            MatchStrength strength = f.hasFormat(acc);
            // if not better than what we have so far (including minimal level limit), skip
            if (strength == null || strength.ordinal() < _minimalMatch.ordinal()) {
                continue;
            }
            // also, needs to better match than before
            if (bestMatch != null) {
                if (bestMatchStrength.ordinal() >= strength.ordinal()) {
                    continue;
                }
            }
            // finally: if it's good enough match, we are done
            bestMatch = f;
            bestMatchStrength = strength;
            if (strength.ordinal() >= _optimalMatch.ordinal()) {
                break;
            }
        }
        return acc.createMatcher(bestMatch, bestMatchStrength);
    }
}
