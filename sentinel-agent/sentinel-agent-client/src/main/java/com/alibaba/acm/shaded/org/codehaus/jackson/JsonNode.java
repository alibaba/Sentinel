package com.alibaba.acm.shaded.org.codehaus.jackson;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Base class for all JSON nodes, which form the basis of JSON
 * Tree Model that Jackson implements.
 * One way to think of these nodes is to consider them
 * similar to DOM nodes in XML DOM trees.
 *<p>
 * As a general design rule, most accessors ("getters") are included
 * in this base class, to allow for traversing structure without
 * type casts. Most mutators, however, need to be accessed through
 * specific sub-classes (such as <code>com.alibaba.acm.shaded.org.codehaus.jackson.node.ObjectNode</code>
 * and <code>com.alibaba.acm.shaded.org.codehaus.jackson.node.ArrayNode</code>).
 * This seems sensible because proper type
 * information is generally available when building or modifying
 * trees, but less often when reading a tree (newly built from
 * parsed JSON content).
 *<p>
 * Actual concrete sub-classes can be found from package
 * {@link com.alibaba.acm.shaded.org.codehaus.jackson.node}, which is in 'mapper' jar
 * (whereas this class is in 'core' jar, since it is declared as
 * nominal type for operations in {@link ObjectCodec})
 */
public abstract class JsonNode
    implements Iterable<JsonNode>
{
    protected final static List<JsonNode> NO_NODES = Collections.emptyList();
    protected final static List<String> NO_STRINGS = Collections.emptyList();

    protected JsonNode() { }

    /*
    /**********************************************************
    /* Public API, type introspection
    /**********************************************************
     */

    // // First high-level division between values, containers and "missing"

    /**
     * Method that returns true for all value nodes: ones that 
     * are not containers, and that do not represent "missing" nodes
     * in the path. Such value nodes represent String, Number, Boolean
     * and null values from JSON.
     *<p>
     * Note: one and only one of methods {@link #isValueNode},
     * {@link #isContainerNode} and {@link #isMissingNode} ever
     * returns true for any given node.
     */
    public boolean isValueNode() { return false; }

    /**
     * Method that returns true for container nodes: Arrays and Objects.
     *<p>
     * Note: one and only one of methods {@link #isValueNode},
     * {@link #isContainerNode} and {@link #isMissingNode} ever
     * returns true for any given node.
     */
    public boolean isContainerNode() { return false; }

    /**
     * Method that returns true for "virtual" nodes which represent
     * missing entries constructed by path accessor methods when
     * there is no actual node matching given criteria.
     *<p>
     * Note: one and only one of methods {@link #isValueNode},
     * {@link #isContainerNode} and {@link #isMissingNode} ever
     * returns true for any given node.
     */
    public boolean isMissingNode() { return false; }

    // // Then more specific type introspection
    // // (along with defaults to be overridden)

    /**
     * @return True if this node represents Json Array
     */
    public boolean isArray() { return false; }

    /**
     * @return True if this node represents Json Object
     */
    public boolean isObject() { return false; }

    /**
     * Method that can be used to check if the node is a wrapper
     * for a POJO ("Plain Old Java Object" aka "bean".
     * Returns true only for
     * instances of {@link com.alibaba.acm.shaded.org.codehaus.jackson.node.POJONode}.
     *
     * @return True if this node wraps a POJO
     */
    public boolean isPojo() { return false; }

    /**
     * @return True if this node represents a numeric Json
     *   value
     */
    public boolean isNumber() { return false; }

    /**
     * @return True if this node represents an integral (integer)
     *   numeric Json value
     */
    public boolean isIntegralNumber() { return false; }

    /**
     * @return True if this node represents a non-integral
     *   numeric Json value
     */
    public boolean isFloatingPointNumber() { return false; }

    /**
     * @return True if this node represents an integral
     *   numeric Json value that withs in Java int value space
     */
    public boolean isInt() { return false; }

    /**
     * @return True if this node represents an integral
     *   numeric Json value that fits in Java long value space
     *   (but not int value space, i.e. {@link #isInt} returns false)
     */
    public boolean isLong() { return false; }

    public boolean isDouble() { return false; }
    public boolean isBigDecimal() { return false; }
    public boolean isBigInteger() { return false; }

    public boolean isTextual() { return false; }

    /**
     * Method that can be used to check if this node was created from
     * Json boolean value (literals "true" and "false").
     */
    public boolean isBoolean() { return false; }

    /**
     * Method that can be used to check if this node was created from
     * Json liternal null value.
     */
    public boolean isNull() { return false; }

    /**
     * Method that can be used to check if this node represents
     * binary data (Base64 encoded). Although this will be externally
     * written as Json String value, {@link #isTextual} will
     * return false if this method returns true.
     *
     * @return True if this node represents base64 encoded binary data
     */
    public boolean isBinary() { return false; }

    /**
     * Method that can be used for efficient type detection
     * when using stream abstraction for traversing nodes.
     * Will return the first {@link JsonToken} that equivalent
     * stream event would produce (for most nodes there is just
     * one token but for structured/container types multiple)
     *
     * @since 1.3
     */
    public abstract JsonToken asToken();

    /**
     * If this node is a numeric type (as per {@link #isNumber}),
     * returns native type that node uses to store the numeric
     * value.
     */
    public abstract JsonParser.NumberType getNumberType();

    /*
    /**********************************************************
    /* Public API, straight value access
    /**********************************************************
     */

    /**
     * Method to use for accessing String values.
     * Does <b>NOT</b> do any conversions for non-String value nodes;
     * for non-String values (ones for which {@link #isTextual} returns
     * false) null will be returned.
     * For String values, null is never returned (but empty Strings may be)
     *
     * @return Textual value this node contains, iff it is a textual
     *   json node (comes from Json String value entry)
     */
    public String getTextValue() { return null; }

    /**
     * Method to use for accessing binary content of binary nodes (nodes
     * for which {@link #isBinary} returns true); or for Text Nodes
     * (ones for which {@link #getTextValue} returns non-null value),
     * to read decoded base64 data.
     * For other types of nodes, returns null.
     *
     * @return Binary data this node contains, iff it is a binary
     *   node; null otherwise
     */
    public byte[] getBinaryValue() throws IOException
    {
        return null;
    }

    /**
     * Method to use for accessing JSON boolean values (value
     * literals 'true' and 'false').
     * For other types, always returns false.
     *
     * @return Textual value this node contains, iff it is a textual
     *   json node (comes from Json String value entry)
     */
    public boolean getBooleanValue() { return false; }

    /**
     * Returns numeric value for this node, <b>if and only if</b>
     * this node is numeric ({@link #isNumber} returns true); otherwise
     * returns null
     *
     * @return Number value this node contains, if any (null for non-number
     *   nodes).
     */
    public Number getNumberValue() { return null; }

    /**
     * Returns integer value for this node, <b>if and only if</b>
     * this node is numeric ({@link #isNumber} returns true). For other
     * types returns 0.
     * For floating-point numbers, value is truncated using default
     * Java coercion, similar to how cast from double to int operates.
     *
     * @return Integer value this node contains, if any; 0 for non-number
     *   nodes.
     */
    public int getIntValue() { return 0; }

    public long getLongValue() { return 0L; }
    public double getDoubleValue() { return 0.0; }
    public BigDecimal getDecimalValue() { return BigDecimal.ZERO; }
    public BigInteger getBigIntegerValue() { return BigInteger.ZERO; }

    /**
     * Method for accessing value of the specified element of
     * an array node. For other nodes, null is always returned.
     *<p>
     * For array nodes, index specifies
     * exact location within array and allows for efficient iteration
     * over child elements (underlying storage is guaranteed to
     * be efficiently indexable, i.e. has random-access to elements).
     * If index is less than 0, or equal-or-greater than
     * <code>node.size()</code>, null is returned; no exception is
     * thrown for any index.
     *
     * @return Node that represent value of the specified element,
     *   if this node is an array and has specified element.
     *   Null otherwise.
     */
    public JsonNode get(int index) { return null; }

    /**
     * Method for accessing value of the specified field of
     * an object node. If this node is not an object (or it
     * does not have a value for specified field name), or
     * if there is no field with such name, null is returned.
     *
     * @return Node that represent value of the specified field,
     *   if this node is an object and has value for the specified
     *   field. Null otherwise.
     */
    public JsonNode get(String fieldName) { return null; }
    
    /*
    /**********************************************************
    /* Public API, value access with conversion(s)/coercion(s)
    /**********************************************************
     */

    /**
     * Method that will return valid String representation of
     * the container value, if the node is a value node
     * (method {@link #isValueNode} returns true), otherwise
     * empty String.
     * 
     * @since 1.9 (replaces <code>getValueAsText</code>)
     */
    public abstract String asText();

    /**
     * Method that will try to convert value of this node to a Java <b>int</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured types
     * like Objects and Arrays),
     * default value of <b>0</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.9 (replaces <code>getValueAsInt</code>)
     */
    public int asInt() {
        return asInt(0);
    }
    
    /**
     * Method that will try to convert value of this node to a Java <b>int</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured types
     * like Objects and Arrays),
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.9 (replaces <code>getValueAsInt</code>)
     */
    public int asInt(int defaultValue) {
        return defaultValue;
    }

    /**
     * Method that will try to convert value of this node to a Java <b>long</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an long (including structured types
     * like Objects and Arrays),
     * default value of <b>0</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.9 (replaces <code>getValueAsLong</code>)
     */
    public long asLong() {
        return asLong(0L);
    }
    
    /**
     * Method that will try to convert value of this node to a Java <b>long</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an long (including structured types
     * like Objects and Arrays),
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.9 (replaces <code>getValueAsLong</code>)
     */
    public long asLong(long defaultValue) {
        return defaultValue;
    }
    
    /**
     * Method that will try to convert value of this node to a Java <b>double</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0.0 (false)
     * and 1.0 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured types
     * like Objects and Arrays),
     * default value of <b>0.0</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.9 (replaces <code>getValueAsDouble</code>)
     */
    public double asDouble() {
        return asDouble(0.0);
    }
    
    /**
     * Method that will try to convert value of this node to a Java <b>double</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0.0 (false)
     * and 1.0 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured types
     * like Objects and Arrays),
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.9 (replaces <code>getValueAsLong</code>)
     */
    public double asDouble(double defaultValue) {
        return defaultValue;
    }

    /**
     * Method that will try to convert value of this node to a Java <b>boolean</b>.
     * JSON booleans map naturally; integer numbers other than 0 map to true, and
     * 0 maps to false
     * and Strings 'true' and 'false' map to corresponding values.
     *<p>
     * If representation can not be converted to a boolean value (including structured types
     * like Objects and Arrays),
     * default value of <b>false</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.9 (replaces <code>getValueAsBoolean</code>)
     */
    public boolean asBoolean() {
        return asBoolean(false);
    }
    
    /**
     * Method that will try to convert value of this node to a Java <b>boolean</b>.
     * JSON booleans map naturally; integer numbers other than 0 map to true, and
     * 0 maps to false
     * and Strings 'true' and 'false' map to corresponding values.
     *<p>
     * If representation can not be converted to a boolean value (including structured types
     * like Objects and Arrays),
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.9 (replaces <code>getValueAsBoolean</code>)
     */
    public boolean asBoolean(boolean defaultValue) {
        return defaultValue;
    }
    
    /*
    /**********************************************************
    /* Public API, value access with conversion(s)/coercion(s)
    /**********************************************************
     */
    
    /**
     * Method that will return valid String representation of
     * the container value, if the node is a value node
     * (method {@link #isValueNode} returns true), otherwise null.
     *<p>
     * Note: to serialize nodes of any type, you should call
     * {@link #toString} instead.
     * 
     * @deprecated Since 1.9, use {@link #asText} instead
     */
    @Deprecated
    public String getValueAsText() { return asText(); }

    /**
     * Method that will try to convert value of this node to a Java <b>int</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured types
     * like Objects and Arrays),
     * default value of <b>0</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     * 
     * @deprecated Since 1.9, use {@link #asInt} instead
     */
    @Deprecated
    public int getValueAsInt() { return asInt(0); }
    
    /**
     * Method that will try to convert value of this node to a Java <b>int</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured types
     * like Objects and Arrays),
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     * 
     * @deprecated Since 1.9, use {@link #asInt} instead
     */
    @Deprecated
    public int getValueAsInt(int defaultValue) { return asInt(defaultValue); }

    /**
     * Method that will try to convert value of this node to a Java <b>long</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an long (including structured types
     * like Objects and Arrays),
     * default value of <b>0</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     * 
     * @deprecated Since 1.9, use {@link #asLong} instead
     */
    @Deprecated
    public long getValueAsLong() { return asLong(0L); }
    
    /**
     * Method that will try to convert value of this node to a Java <b>long</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an long (including structured types
     * like Objects and Arrays),
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     * 
     * @deprecated Since 1.9, use {@link #asLong} instead
     */
    @Deprecated
    public long getValueAsLong(long defaultValue) { return asLong(defaultValue); }
    
    /**
     * Method that will try to convert value of this node to a Java <b>double</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0.0 (false)
     * and 1.0 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured types
     * like Objects and Arrays),
     * default value of <b>0.0</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     * 
     * @deprecated Since 1.9, use {@link #asDouble} instead
     */
    @Deprecated
    public double getValueAsDouble() { return asDouble(0.0); }
    
    /**
     * Method that will try to convert value of this node to a Java <b>double</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0.0 (false)
     * and 1.0 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured types
     * like Objects and Arrays),
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     * 
     * @deprecated Since 1.9, use {@link #asDouble} instead
     */
    @Deprecated
    public double getValueAsDouble(double defaultValue) { return asDouble(defaultValue); }

    /**
     * Method that will try to convert value of this node to a Java <b>boolean</b>.
     * JSON booleans map naturally; integer numbers other than 0 map to true, and
     * 0 maps to false
     * and Strings 'true' and 'false' map to corresponding values.
     *<p>
     * If representation can not be converted to a boolean value (including structured types
     * like Objects and Arrays),
     * default value of <b>false</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.7
     * 
     * @deprecated Since 1.9, use {@link #asBoolean} instead
     */
    @Deprecated
    public boolean getValueAsBoolean() { return asBoolean(false); }
    
    /**
     * Method that will try to convert value of this node to a Java <b>boolean</b>.
     * JSON booleans map naturally; integer numbers other than 0 map to true, and
     * 0 maps to false
     * and Strings 'true' and 'false' map to corresponding values.
     *<p>
     * If representation can not be converted to a boolean value (including structured types
     * like Objects and Arrays),
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.7
     * 
     * @deprecated Since 1.9, use {@link #asBoolean} instead
     */
    @Deprecated
    public boolean getValueAsBoolean(boolean defaultValue) { return asBoolean(defaultValue); }
    
    /*
    /**********************************************************
    /* Public API, value find / existence check methods
    /**********************************************************
     */
    
    /**
     * Method that allows checking whether this node is JSON Object node
     * and contains value for specified property. If this is the case
     * (including properties with explicit null values), returns true;
     * otherwise returns false.
     *<p>
     * This method is equivalent to:
     *<pre>
     *   node.get(fieldName) != null
     *</pre>
     * (since return value of get() is node, not value node contains)
     *
     * @param fieldName Name of element to check
     * 
     * @return True if this node is a JSON Object node, and has a property
     *   entry with specified name (with any value, including null value)
     *   
     * @since 1.6
     */
    public boolean has(String fieldName) {
        return get(fieldName) != null;
    }

    /**
     * Method that allows checking whether this node is JSON Array node
     * and contains a value for specified index
     * If this is the case
     * (including case of specified indexing having null as value), returns true;
     * otherwise returns false.
     *<p>
     * Note: array element indexes are 0-based.
     *<p>
     * This method is equivalent to:
     *<pre>
     *   node.get(index) != null
     *</pre>
     *
     * @param index Index to check
     * 
     * @return True if this node is a JSON Object node, and has a property
     *   entry with specified name (with any value, including null value)
     *   
     * @since 1.6
     */
    public boolean has(int index) {
        return get(index) != null;
    }

    /**
     * Method for finding a JSON Object field with specified name in this
     * node or its child nodes, and returning value it has.
     * If no matching field is found in this node or its descendants, returns null.
     * 
     * @param fieldName Name of field to look for
     * 
     * @return Value of first matching node found, if any; null if none
     * 
     * @since 1.6
     */
    public abstract JsonNode findValue(String fieldName);

    /**
     * Method for finding JSON Object fields with specified name, and returning
     * found ones as a List. Note that sub-tree search ends if a field is found,
     * so possible children of result nodes are <b>not</b> included.
     * If no matching fields are found in this node or its descendants, returns
     * an empty List.
     * 
     * @param fieldName Name of field to look for
     * 
     * @since 1.6
     */
    public final List<JsonNode> findValues(String fieldName)
    {
        List<JsonNode> result = findValues(fieldName, null);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    /**
     * Similar to {@link #findValues}, but will additionally convert
     * values into Strings, calling {@link #getValueAsText}.
     * 
     * @since 1.6
     */
    public final List<String> findValuesAsText(String fieldName)
    {
        List<String> result = findValuesAsText(fieldName, null);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
    
    /**
     * Method similar to {@link #findValue}, but that will return a
     * "missing node" instead of null if no field is found. Missing node
     * is a specific kind of node for which {@link #isMissingNode}
     * returns true; and all value access methods return empty or
     * missing value.
     * 
     * @param fieldName Name of field to look for
     * 
     * @return Value of first matching node found; or if not found, a
     *    "missing node" (non-null instance that has no value)
     * 
     * @since 1.6
     */
    public abstract JsonNode findPath(String fieldName);
    
    /**
     * Method for finding a JSON Object that contains specified field,
     * within this node or its descendants.
     * If no matching field is found in this node or its descendants, returns null.
     * 
     * @param fieldName Name of field to look for
     * 
     * @return Value of first matching node found, if any; null if none
     * 
     * @since 1.6
     */
    public abstract JsonNode findParent(String fieldName);

    /**
     * Method for finding a JSON Object that contains specified field,
     * within this node or its descendants.
     * If no matching field is found in this node or its descendants, returns null.
     * 
     * @param fieldName Name of field to look for
     * 
     * @return Value of first matching node found, if any; null if none
     * 
     * @since 1.6
     */
    public final List<JsonNode> findParents(String fieldName)
    {
        List<JsonNode> result = findParents(fieldName, null);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public abstract List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar);
    public abstract List<String> findValuesAsText(String fieldName, List<String> foundSoFar);
    public abstract List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar);

    /*
    /**********************************************************
    /* Public API, container access
    /**********************************************************
     */

    /**
     * Method that returns number of child nodes this node contains:
     * for Array nodes, number of child elements, for Object nodes,
     * number of fields, and for all other nodes 0.
     *
     * @return For non-container nodes returns 0; for arrays number of
     *   contained elements, and for objects number of fields.
     */
    public int size() { return 0; }

    /**
     * Same as calling {@link #getElements}; implemented so that
     * convenience "for-each" loop can be used for looping over elements
     * of JSON Array constructs.
     */
    @Override
    public final Iterator<JsonNode> iterator() { return getElements(); }

    /**
     * Method for accessing all value nodes of this Node, iff
     * this node is a JSON Array or Object node. In case of Object node,
     * field names (keys) are not included, only values.
     * For other types of nodes, returns empty iterator.
     */
    public Iterator<JsonNode> getElements() { return NO_NODES.iterator(); }

    /**
     * Method for accessing names of all fields for this Node, iff
     * this node is a JSON Object node.
     */
    public Iterator<String> getFieldNames() { return NO_STRINGS.iterator(); }

    /**
     * @return Iterator that can be used to traverse all key/value pairs for
     *   object nodes; empty iterator (no contents) for other types
     * 
     * @since 1.8 (although existed in ObjectNode since 1.0 or so)
     */
    public Iterator<Map.Entry<String, JsonNode>> getFields() {
        Collection<Map.Entry<String, JsonNode>> coll = Collections.emptyList();
        return coll.iterator();
    }
    
    /*
    /**********************************************************
    /* Public API, path handling
    /**********************************************************
     */

    /**
     * This method is similar to {@link #get(String)}, except
     * that instead of returning null if no such value exists (due
     * to this node not being an object, or object not having value
     * for the specified field),
     * a "missing node" (node that returns true for
     * {@link #isMissingNode}) will be returned. This allows for
     * convenient and safe chained access via path calls.
     */
    public abstract JsonNode path(String fieldName);

    /**
     * Alias of {@link #path(String)}.
     *
     * @deprecated Use {@link #path(String)} instead
     */
    @Deprecated
    public final JsonNode getPath(String fieldName) { return path(fieldName); }

    /**
     * This method is similar to {@link #get(int)}, except
     * that instead of returning null if no such element exists (due
     * to index being out of range, or this node not being an array),
     * a "missing node" (node that returns true for
     * {@link #isMissingNode}) will be returned. This allows for
     * convenient and safe chained access via path calls.
     */
    public abstract JsonNode path(int index);

    /**
     * Alias of {@link #path(int)}.
     *
     * @deprecated Use {@link #path(int)} instead
     */
    @Deprecated
    public final JsonNode getPath(int index) { return path(index); }

    /**
     * Method that can be called on object nodes, to access a property
     * that has object value; or if no such property exists, to create and
     * return such object node.
     * If node method is called on is not Object node,
     * or if property exists and has value that is not object node,
     * {@link UnsupportedOperationException} is thrown
     * 
     * @since 1.8
     */
    public JsonNode with(String propertyName) {
        throw new UnsupportedOperationException("JsonNode not of type ObjectNode (but "
                +getClass().getName()+"), can not call with() on it");
    }

    /*
    /**********************************************************
    /* Public API: converting to/from Streaming API
    /**********************************************************
     */

    /**
     * Method for constructing a {@link JsonParser} instance for
     * iterating over contents of the tree that this
     * node is root of.
     * Functionally equivalent to first serializing tree using
     * {@link ObjectCodec} and then re-parsing but
     * more efficient.
     */
    public abstract JsonParser traverse();

    /*
    /**********************************************************
    /* Overridden standard methods
    /**********************************************************
     */
    
    /**
     *<p>
     * Note: marked as abstract to ensure all implementation
     * classes define it properly.
     */
    @Override
    public abstract String toString();

    /**
     * Equality for node objects is defined as full (deep) value
     * equality. This means that it is possible to compare complete
     * JSON trees for equality by comparing equality of root nodes.
     *<p>
     * Note: marked as abstract to ensure all implementation
     * classes define it properly and not rely on definition
     * from {@link java.lang.Object}.
     */
    @Override
    public abstract boolean equals(Object o);
}
