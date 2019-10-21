package com.alibaba.acm.shaded.org.codehaus.jackson;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonCreator;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonProperty;

/**
 * Object that encapsulates Location information used for reporting
 * parsing (or potentially generation) errors, as well as current location
 * within input streams.
 */
public class JsonLocation
    implements java.io.Serializable // as per [JACKSON-168]
{
    private static final long serialVersionUID = 1L;

    /**
     * Shared immutable "N/A location" that can be returned to indicate
     * that no location information is available
     *
     * @since 1.3
     */
    public final static JsonLocation NA = new JsonLocation("N/A", -1L, -1L, -1, -1);

    final long _totalBytes;
    final long _totalChars;

    final int _lineNr;
    final int _columnNr;

    /**
     * Displayable description for input source: file path, url
     */
    final Object _sourceRef;

    public JsonLocation(Object srcRef, long totalChars, int lineNr, int colNr)
    {
        /* Unfortunately, none of legal encodings are straight single-byte
         * encodings. Could determine offset for UTF-16/UTF-32, but the
         * most important one is UTF-8...
         * so for now, we'll just not report any real byte count
         */
        this(srcRef, -1L, totalChars, lineNr, colNr);
    }

    @JsonCreator
    public JsonLocation(@JsonProperty("sourceRef") Object sourceRef,
                        @JsonProperty("byteOffset") long totalBytes,
                        @JsonProperty("charOffset") long totalChars,
                        @JsonProperty("lineNr") int lineNr,
                        @JsonProperty("columnNr") int columnNr)
    {
        _sourceRef = sourceRef;
        _totalBytes = totalBytes;
        _totalChars = totalChars;
        _lineNr = lineNr;
        _columnNr = columnNr;
    }

    /**
     * Reference to the original resource being read, if one available.
     * For example, when a parser has been constructed by passing
     * a {@link java.io.File} instance, this method would return
     * that File. Will return null if no such reference is available,
     * for example when {@link java.io.InputStream} was used to
     * construct the parser instance.
     */
    public Object getSourceRef() { return _sourceRef; }

    /**
     * @return Line number of the location (1-based)
     */
    public int getLineNr() { return _lineNr; }

    /**
     * @return Column number of the location (1-based)
     */
    public int getColumnNr() { return _columnNr; }

    /**
     * @return Character offset within underlying stream, reader or writer,
     *   if available; -1 if not.
     */
    public long getCharOffset() { return _totalChars; }

    /**
     * @return Byte offset within underlying stream, reader or writer,
     *   if available; -1 if not.
     */
    public long getByteOffset()
    {
        return _totalBytes;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("[Source: ");
        if (_sourceRef == null) {
            sb.append("UNKNOWN");
        } else {
            sb.append(_sourceRef.toString());
        }
        sb.append("; line: ");
        sb.append(_lineNr);
        sb.append(", column: ");
        sb.append(_columnNr);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        int hash = (_sourceRef == null) ? 1 : _sourceRef.hashCode();
        hash ^= _lineNr;
        hash += _columnNr;
        hash ^= (int) _totalChars;
        hash += (int) _totalBytes;
        return hash;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this) return true;
        if (other == null) return false;
        if (!(other instanceof JsonLocation)) return false;
        JsonLocation otherLoc = (JsonLocation) other;

        if (_sourceRef == null) {
            if (otherLoc._sourceRef != null) return false;
        } else if (!_sourceRef.equals(otherLoc._sourceRef)) return false;

        return (_lineNr == otherLoc._lineNr)
            && (_columnNr == otherLoc._columnNr)
            && (_totalChars == otherLoc._totalChars)
            && (getByteOffset() == otherLoc.getByteOffset())
            ;
    }
}
