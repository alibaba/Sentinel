package com.alibaba.acm.shaded.org.codehaus.jackson;

/**
 * Intermediate base class for all problems encountered when
 * processing (parsing, generating) JSON content
 * that are not pure I/O problems.
 * Regular {@link java.io.IOException}s will be passed through as is.
 * Sub-class of {@link java.io.IOException} for convenience.
 */
public class JsonProcessingException
    extends java.io.IOException
{
    final static long serialVersionUID = 123; // Stupid eclipse...
	
    protected JsonLocation mLocation;

    protected JsonProcessingException(String msg, JsonLocation loc, Throwable rootCause)
    {
        /* Argh. IOException(Throwable,String) is only available starting
         * with JDK 1.6...
         */
        super(msg);
        if (rootCause != null) {
            initCause(rootCause);
        }
        mLocation = loc;
    }

    protected JsonProcessingException(String msg)
    {
        super(msg);
    }

    protected JsonProcessingException(String msg, JsonLocation loc)
    {
        this(msg, loc, null);
    }

    protected JsonProcessingException(String msg, Throwable rootCause)
    {
        this(msg, null, rootCause);
    }

    protected JsonProcessingException(Throwable rootCause)
    {
        this(null, null, rootCause);
    }

    public JsonLocation getLocation()
    {
        return mLocation;
    }

    /**
     * Default method overridden so that we can add location information
     */
    @Override
    public String getMessage()
    {
        String msg = super.getMessage();
        if (msg == null) {
            msg = "N/A";
        }
        JsonLocation loc = getLocation();
        if (loc != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(msg);
            sb.append('\n');
            sb.append(" at ");
            sb.append(loc.toString());
            return sb.toString();
        }
        return msg;
    }

    @Override
    public String toString() {
        return getClass().getName()+": "+getMessage();
    }
}
