/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in file LICENSE, included with
 * the source code and binary code bundles.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.format.InputAccessor;
import com.alibaba.acm.shaded.org.codehaus.jackson.format.MatchStrength;

/**
 * Sub-class of {@link JsonFactory} that will create a proper
 * {@link ObjectCodec} to allow seamless conversions between
 * Json content and Java objects (POJOs).
 * The only addition to regular {@link JsonFactory} currently
 * is that {@link ObjectMapper} is constructed and passed as
 * the codec to use.
 */
public class MappingJsonFactory
    extends JsonFactory
{
    public MappingJsonFactory()
    {
        this(null);
    }

    public MappingJsonFactory(ObjectMapper mapper)
    {
        super(mapper);
        if (mapper == null) {
            setCodec(new ObjectMapper(this));
        }
    }

    /**
     * We'll override the method to return more specific type; co-variance
     * helps here
     */
    @Override
    public final ObjectMapper getCodec() { return (ObjectMapper) _objectCodec; }

    /*
    /**********************************************************
    /* Format detection functionality (since 1.8)
    /**********************************************************
     */
    
    /**
     * Sub-classes need to override this method (as of 1.8)
     */
    @Override
    public String getFormatName()
    {
        /* since non-JSON factories typically should not extend this class,
         * let's just always return JSON as name.
         */
        return FORMAT_NAME_JSON;
    }

    /**
     * Sub-classes need to override this method (as of 1.8)
     */
    @Override
    public MatchStrength hasFormat(InputAccessor acc) throws IOException
    {
        return hasJSONFormat(acc);
    }
}
