package com.alibaba.acm.shaded.org.codehaus.jackson;

/**
 * Exception type for parsing problems, used when non-well-formed content
 * (content that does not conform to JSON syntax as per specification)
 * is encountered.
 */
public class JsonParseException
    extends JsonProcessingException
{
    @SuppressWarnings("hiding")
    final static long serialVersionUID = 123; // Stupid eclipse...

    public JsonParseException(String msg, JsonLocation loc)
    {
        super(msg, loc);
    }

    public JsonParseException(String msg, JsonLocation loc, Throwable root)
    {
        super(msg, loc, root);
    }
}
