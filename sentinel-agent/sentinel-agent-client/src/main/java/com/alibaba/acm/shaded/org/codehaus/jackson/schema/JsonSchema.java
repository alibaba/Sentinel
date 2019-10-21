package com.alibaba.acm.shaded.org.codehaus.jackson.schema;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonCreator;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonValue;
import com.alibaba.acm.shaded.org.codehaus.jackson.node.ObjectNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.node.JsonNodeFactory;

/**
 * A {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode} that represents a JSON-Schema instance.
 *
 * @author Ryan Heaton
 * @see <a href="http://json-schema.org/">JSON Schema</a>
 */
public class JsonSchema
{
    private final ObjectNode schema;

    /**
     * Main constructor for schema instances.
     *<p>
     * This is the creator constructor used by Jackson itself when
     * deserializing instances. It is so-called delegating creator, 
     * meaning that its argument will be bound by Jackson before
     * constructor gets called.
     */
    @JsonCreator
    public JsonSchema(ObjectNode schema)
    {
        this.schema = schema;
    }

    /**
     * Method for accessing root JSON object of the contained schema.
     *<p>
     * Note: this method is specified with {@link JsonValue} annotation
     * to represent serialization to use; same as if explicitly
     * serializing returned object.
     *
     * @return Root node of the schema tree
     */
    @JsonValue
    public ObjectNode getSchemaNode()
    {
        return schema;
    }

    @Override
    public String toString()
    {
        return this.schema.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof JsonSchema)) return false;

        JsonSchema other = (JsonSchema) o;
        if (schema == null) {
            return other.schema == null;
        }
        return schema.equals(other.schema);
    }

    /**
     * Get the default schema node.
     *
     * @return The default schema node.
     */
    public static JsonNode getDefaultSchemaNode()
    {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("type", "any");
        // "required" is false by default, no need to include
        //objectNode.put("required", false);
        return objectNode;
    }

}
