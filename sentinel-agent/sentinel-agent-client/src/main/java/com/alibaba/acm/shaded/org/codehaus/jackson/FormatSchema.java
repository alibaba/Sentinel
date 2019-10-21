package com.alibaba.acm.shaded.org.codehaus.jackson;

/**
 * Simple tag interface used to mark schema objects that are used by some
 * {@link JsonParser} and {@link JsonGenerator} implementations to further
 * specify structure of expected format.
 * Basic JSON-based parsers and generators do not use schemas, but some data
 * formats (like many binary data formats like Thrift, protobuf) mandate
 * use of schemas.
 *<p>
 * Since there is little commonality between schemas for different data formats,
 * this interface does not define much meaningful functionality for accessing
 * schema details; rather, specific parser and generator implementations need
 * to cast to schema implementations they use. This marker interface is mostly
 * used for tagging "some kind of schema" -- instead of passing opaque
 * {@link java.lang.Object} -- for documentation purposes.
 * 
 * @since 1.8
 */
public interface FormatSchema
{
    /**
     * Method that can be used to get an identifier that can be used for diagnostics
     * purposes, to indicate what kind of data format this schema is used for: typically
     * it is a short name of format itself, but it can also contain additional information
     * in cases where data format supports multiple types of schemas.
     */
    public String getSchemaType();
}
