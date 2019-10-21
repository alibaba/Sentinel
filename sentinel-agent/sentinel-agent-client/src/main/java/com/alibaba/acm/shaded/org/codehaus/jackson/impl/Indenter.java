package com.alibaba.acm.shaded.org.codehaus.jackson.impl;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;

/**
 * Interface that defines objects that can produce indentation used
 * to separate object entries and array values. Indentation in this
 * context just means insertion of white space, independent of whether
 * linefeeds are output.
 */
public interface Indenter
{
    public void writeIndentation(JsonGenerator jg, int level)
        throws IOException, JsonGenerationException;

    /**
     * @return True if indenter is considered inline (does not add linefeeds),
     *   false otherwise
     */
    public boolean isInline();
}
