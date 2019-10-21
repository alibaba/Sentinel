package com.alibaba.acm.shaded.org.codehaus.jackson.format;

/**
 * Enumeration used to indicate strength of match between data format
 * and piece of data (typically beginning of a data file).
 * Values are in increasing match strength; and detectors should return
 * "strongest" value: that is, it should start with strongest match
 * criteria, and downgrading if criteria is not fulfilled.
 * 
 * @since 1.8
 */
public enum MatchStrength
{
    /**
     * Value that indicates that given data can not be in given format.
     */
    NO_MATCH,
    
    /**
     * Value that indicates that detector can not find out whether could
     * be a match or not.
     * This can occur for example for textual data formats t
     * when there are so many leading spaces that detector can not
     * find the first data byte (because detectors typically limit lookahead
     * to some smallish value).
     */
    INCONCLUSIVE,

    /**
     * Value that indicates that given data could be of specified format (i.e.
     * it can not be ruled out). This can occur for example when seen data
     * is both not in canonical formats (for example: JSON data should be a JSON Array or Object
     * not a scalar value, as per JSON specification) and there are known use case
     * where a format detected is actually used (plain JSON Strings are actually used, even
     * though specification does not indicate that as valid usage: as such, seeing a leading
     * double-quote could indicate a JSON String, which plausibly <b>could</b> indicate
     * non-standard JSON usage).
     */
    WEAK_MATCH,
    
    /**
     * Value that indicates that given data conforms to (one of) canonical form(s) of
     * the data format.
     *<p>
     * For example, when testing for XML data format,
     * seeing a less-than character ("&lt;") alone (with possible leading spaces)
     * would be a strong indication that data could
     * be in xml format (but see below for {@link #FULL_MATCH} description for more)
     */
    SOLID_MATCH,

    /**
     * Value that indicates that given data contains a signature that is deemed
     * specific enough to uniquely indicate data format used.
     *<p>
     * For example, when testing for XML data format,
     * seing "&lt;xml" as the first data bytes ("XML declaration", as per XML specification)
     * could give full confidence that data is indeed in XML format.
     * Not all data formats have unique leading identifiers to allow full matches; for example,
     * JSON only has heuristic matches and can have at most {@link #SOLID_MATCH}) match.
     */
    FULL_MATCH
    ;
}
