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

package com.alibaba.acm.shaded.org.codehaus.jackson;

/**
 * Shared base class for streaming processing contexts used during
 * reading and writing of Json content using Streaming API.
 * This context is also exposed to applications:
 * context object can be used by applications to get an idea of
 * relative position of the parser/generator within json content
 * being processed. This allows for some contextual processing: for
 * example, output within Array context can differ from that of
 * Object context.
 */
public abstract class JsonStreamContext
{
    // // // Type constants used internally

    protected final static int TYPE_ROOT = 0;
    protected final static int TYPE_ARRAY = 1;
    protected final static int TYPE_OBJECT = 2;

    protected int _type;

    /**
     * Index of the currently processed entry. Starts with -1 to signal
     * that no entries have been started, and gets advanced each
     * time a new entry is started, either by encountering an expected
     * separator, or with new values if no separators are expected
     * (the case for root context).
     */
    protected int _index;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected JsonStreamContext() { }

    /*
    /**********************************************************
    /* Public API, accessors
    /**********************************************************
     */

    /**
     * Accessor for finding parent context of this context; will
     * return null for root context.
     */
    public abstract JsonStreamContext getParent();

    /**
     * Method that returns true if this context is an Array context;
     * that is, content is being read from or written to a Json Array.
     */
    public final boolean inArray() { return _type == TYPE_ARRAY; }

    /**
     * Method that returns true if this context is a Root context;
     * that is, content is being read from or written to without
     * enclosing array or object structure.
     */
    public final boolean inRoot() { return _type == TYPE_ROOT; }

    /**
     * Method that returns true if this context is an Object context;
     * that is, content is being read from or written to a Json Object.
     */
    public final boolean inObject() { return _type == TYPE_OBJECT; }

    /**
     * Method for accessing simple type description of current context;
     * either ROOT (for root-level values), OBJECT (for field names and
     * values of JSON Objects) or ARRAY (for values of JSON Arrays)
     */
    public final String getTypeDesc() {
        switch (_type) {
        case TYPE_ROOT: return "ROOT";
        case TYPE_ARRAY: return "ARRAY";
        case TYPE_OBJECT: return "OBJECT";
        }
        return "?";
    }

    /**
     * @return Number of entries that are complete and started.
     */
    public final int getEntryCount()
    {
        return _index + 1;
    }

    /**
     * @return Index of the currently processed entry, if any
     */
    public final int getCurrentIndex()
    {
        return (_index < 0) ? 0 : _index;
    }

    /**
     * Method for accessing name associated with the current location.
     * Non-null for <code>FIELD_NAME</code> and value events that directly
     * follow field names; null for root level and array values.
     */
    public abstract String getCurrentName();
}
