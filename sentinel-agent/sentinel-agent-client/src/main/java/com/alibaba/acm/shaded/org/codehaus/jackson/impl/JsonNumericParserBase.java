package com.alibaba.acm.shaded.org.codehaus.jackson.impl;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.IOContext;

/**
 * Another intermediate base class used by all Jackson {@link JsonParser}
 * implementations. Contains shared functionality for dealing with
 * number parsing aspects, independent of input source decoding.
 *
 * @deprecated Since 1.9.0: functionality demoted down to JsonParserBase
 */
@Deprecated
public abstract class JsonNumericParserBase
    extends JsonParserBase
{
    protected JsonNumericParserBase(IOContext ctxt, int features) {
        super(ctxt, features);
    }
}
