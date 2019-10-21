package com.alibaba.acm.shaded.org.codehaus.jackson.node;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer;

/**
 * Node class that represents Arrays mapped from Json content.
 */
public final class ArrayNode
    extends ContainerNode
{
    protected ArrayList<JsonNode> _children;

    public ArrayNode(JsonNodeFactory nc) { super(nc); }

    /*
    /**********************************************************
    /* Implementation of core JsonNode API
    /**********************************************************
     */

    @Override public JsonToken asToken() { return JsonToken.START_ARRAY; }

    @Override
    public boolean isArray() { return true; }

    @Override
    public int size()
    {
        return (_children == null) ? 0 : _children.size();
    }

    @Override
    public Iterator<JsonNode> getElements()
    {
        return (_children == null) ? NoNodesIterator.instance() : _children.iterator();
    }

    @Override
    public JsonNode get(int index)
    {
        if (index >= 0 && (_children != null) && index < _children.size()) {
            return _children.get(index);
        }
        return null;
    }

    @Override
    public JsonNode get(String fieldName) { return null; }

    @Override
    public JsonNode path(String fieldName) { return MissingNode.getInstance(); }

    @Override
    public JsonNode path(int index)
    {
        if (index >= 0 && (_children != null) && index < _children.size()) {
            return _children.get(index);
        }
        return MissingNode.getInstance();
    }
    
    /*
    /**********************************************************
    /* Public API, serialization
    /**********************************************************
     */

    @Override
    public final void serialize(JsonGenerator jg, SerializerProvider provider)
        throws IOException, JsonProcessingException
    {
        jg.writeStartArray();
        if (_children != null) {
            for (JsonNode n : _children) {
                /* 17-Feb-2009, tatu: Can we trust that all nodes will always
                 *   extend BaseJsonNode? Or if not, at least implement
                 *   JsonSerializable? Let's start with former, change if
                 *   we must.
                 */
                ((BaseJsonNode)n).serialize(jg, provider);
            }
        }
        jg.writeEndArray();
    }

    @Override
    public void serializeWithType(JsonGenerator jg, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonProcessingException
    {
        typeSer.writeTypePrefixForArray(this, jg);
        if (_children != null) {
            for (JsonNode n : _children) {
                ((BaseJsonNode)n).serialize(jg, provider);
            }
        }
        typeSer.writeTypeSuffixForArray(this, jg);
    }
    
    /*
    /**********************************************************
    /* Public API, finding value nodes
    /**********************************************************
     */
    
    @Override
    public JsonNode findValue(String fieldName)
    {
        if (_children != null) {
            for (JsonNode node : _children) {
                JsonNode value = node.findValue(fieldName);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }
    
    @Override
    public List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar)
    {
        if (_children != null) {
            for (JsonNode node : _children) {
                foundSoFar = node.findValues(fieldName, foundSoFar);
            }
        }
        return foundSoFar;
    }

    @Override
    public List<String> findValuesAsText(String fieldName, List<String> foundSoFar)
    {
        if (_children != null) {
            for (JsonNode node : _children) {
                foundSoFar = node.findValuesAsText(fieldName, foundSoFar);
            }
        }
        return foundSoFar;
    }
    
    @Override
    public ObjectNode findParent(String fieldName)
    {
        if (_children != null) {
            for (JsonNode node : _children) {
                JsonNode parent = node.findParent(fieldName);
                if (parent != null) {
                    return (ObjectNode) parent;
                }
            }
        }
        return null;        
    }

    @Override
    public List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar)
    {
        if (_children != null) {
            for (JsonNode node : _children) {
                foundSoFar = node.findParents(fieldName, foundSoFar);
            }
        }
        return foundSoFar;
    }
    
    /*
    /**********************************************************
    /* Extended ObjectNode API, accessors
    /**********************************************************
     */

    /**
     * Method that will set specified field, replacing old value,
     * if any.
     *
     * @param value to set field to; if null, will be converted
     *   to a {@link NullNode} first  (to remove field entry, call
     *   {@link #remove} instead)
     *
     * @return Old value of the field, if any; null if there was no
     *   old value.
     */
    public JsonNode set(int index, JsonNode value)
    {
        if (value == null) { // let's not store 'raw' nulls but nodes
            value = nullNode();
        }
        return _set(index, value);
    }

    public void add(JsonNode value)
    {
        if (value == null) { // let's not store 'raw' nulls but nodes
            value = nullNode();
        }
        _add(value);
    }

    /**
     * Method for adding all child nodes of given Array, appending to
     * child nodes this array contains
     * 
     * @param other Array to add contents from
     * 
     * @return This node (to allow chaining)
     * 
     * @since 1.3
     */
    public JsonNode addAll(ArrayNode other)
    {
        int len = other.size();
        if (len > 0) {
            if (_children == null) {
                _children = new ArrayList<JsonNode>(len+2);
            }
            other.addContentsTo(_children);
        }
        return this;
    }

    /**
     * Method for adding given nodes as child nodes of this array node.
     * 
     * @param nodes Nodes to add
     * 
     * @return This node (to allow chaining)
     * 
     * @since 1.3
     */
    public JsonNode addAll(Collection<JsonNode> nodes)
    {
        int len = nodes.size();
        if (len > 0) {
            if (_children == null) {
                _children = new ArrayList<JsonNode>(nodes);
            } else {
                _children.addAll(nodes);
            }
        }
        return this;
    }
    
    /**
     * Method for inserting specified child node as an element
     * of this Array. If index is 0 or less, it will be inserted as
     * the first element; if >= size(), appended at the end, and otherwise
     * inserted before existing element in specified index.
     * No exceptions are thrown for any index.
     */
    public void insert(int index, JsonNode value)
    {
        if (value == null) {
            value = nullNode();
        }
        _insert(index, value);
    }

    /**
     * Method for removing an entry from this ArrayNode.
     * Will return value of the entry at specified index, if entry existed;
     * null if not.
     */
    public JsonNode remove(int index)
    {
        if (index >= 0 && (_children != null) && index < _children.size()) {
            return _children.remove(index);
        }
        return null;
    }

    @Override
    public ArrayNode removeAll()
    {
        _children = null;
        return this;
    }
    
    /*
    /**********************************************************
    /* Extended ObjectNode API, mutators, generic; addXxx()/insertXxx()
    /**********************************************************
     */

    /**
     * Method that will construct an ArrayNode and add it as a
     * field of this ObjectNode, replacing old value, if any.
     *
     * @return Newly constructed ArrayNode
     */
    public ArrayNode addArray()
    {
        ArrayNode n  = arrayNode();
        _add(n);
        return n;
    }

    /**
     * Method that will construct an ObjectNode and add it at the end
     * of this array node.
     *
     * @return Newly constructed ObjectNode
     */
    public ObjectNode addObject()
    {
        ObjectNode n  = objectNode();
        _add(n);
        return n;
    }

    /**
     * Method that will construct a POJONode and add it at the end
     * of this array node.
     */
    public void addPOJO(Object value)
    {
        if (value == null) {
            addNull();
        } else {
            _add(POJONode(value));
        }
    }

    public void addNull()
    {
        _add(nullNode());
    }

    /**
     * Method for setting value of a field to specified numeric value.
     */
    public void add(int v) { _add(numberNode(v)); }

    /**
     * Alternative method that we need to avoid bumping into NPE issues
     * with auto-unboxing.
     * 
     * @since 1.9
     */
    public void add(Integer value) {
        if (value == null) {
            addNull();
        } else {
            _add(numberNode(value.intValue()));
        }
    }
    
    /**
     * Method for setting value of a field to specified numeric value.
     */
    public void add(long v) { _add(numberNode(v)); }

    /**
     * Alternative method that we need to avoid bumping into NPE issues
     * with auto-unboxing.
     * 
     * @since 1.9
     */
    public void add(Long value) {
        if (value == null) {
            addNull();
        } else {
            _add(numberNode(value.longValue()));
        }
    }
    
    /**
     * Method for setting value of a field to specified numeric value.
     */
    public void add(float v) { _add(numberNode(v)); }

    /**
     * Alternative method that we need to avoid bumping into NPE issues
     * with auto-unboxing.
     * 
     * @since 1.9
     */
    public void add(Float value) {
        if (value == null) {
            addNull();
        } else {
            _add(numberNode(value.floatValue()));
        }
    }
    
    /**
     * Method for setting value of a field to specified numeric value.
     */
    public void add(double v) { _add(numberNode(v)); }

    /**
     * Alternative method that we need to avoid bumping into NPE issues
     * with auto-unboxing.
     * 
     * @since 1.9
     */
    public void add(Double value) {
        if (value == null) {
            addNull();
        } else {
            _add(numberNode(value.doubleValue()));
        }
    }
    
    /**
     * Method for setting value of a field to specified numeric value.
     */
    public void add(BigDecimal v) {
        if (v == null) {
            addNull();
        } else {
            _add(numberNode(v));
        }
    }

    /**
     * Method for setting value of a field to specified String value.
     */
    public void add(String v) {
        if (v == null) {
            addNull();
        } else {
            _add(textNode(v));
        }
    }

    /**
     * Method for setting value of a field to specified String value.
     */
    public void add(boolean v) { _add(booleanNode(v)); }

    /**
     * Alternative method that we need to avoid bumping into NPE issues
     * with auto-unboxing.
     * 
     * @since 1.9
     */
    public void add(Boolean value) {
        if (value == null) {
            addNull();
        } else {
            _add(booleanNode(value.booleanValue()));
        }
    }
    
    /**
     * Method for setting value of a field to specified binary value
     */
    public void add(byte[] v) {
        if (v == null) {
            addNull();
        } else {
            _add(binaryNode(v));
        }
    }

    public ArrayNode insertArray(int index)
    {
        ArrayNode n  = arrayNode();
        _insert(index, n);
        return n;
    }

    /**
     * Method that will construct an ObjectNode and add it at the end
     * of this array node.
     *
     * @return Newly constructed ObjectNode
     */
    public ObjectNode insertObject(int index)
    {
        ObjectNode n  = objectNode();
        _insert(index, n);
        return n;
    }

    /**
     * Method that will construct a POJONode and add it at the end
     * of this array node.
     */
    public void insertPOJO(int index, Object value)
    {
        if (value == null) {
            insertNull(index);
        } else {
            _insert(index, POJONode(value));
        }
    }

    public void insertNull(int index)
    {
        _insert(index, nullNode());
    }

    /**
     * Method for setting value of a field to specified numeric value.
     */
    public void insert(int index, int v) { _insert(index, numberNode(v)); }

    /**
     * Alternative method that we need to avoid bumping into NPE issues
     * with auto-unboxing.
     * 
     * @since 1.9
     */
    public void insert(int index, Integer value) {
        if (value == null) {
            insertNull(index);
        } else {
            _insert(index, numberNode(value.intValue()));
        }
    }
    
    /**
     * Method for setting value of a field to specified numeric value.
     */
    public void insert(int index, long v) { _insert(index, numberNode(v)); }

    /**
     * Alternative method that we need to avoid bumping into NPE issues
     * with auto-unboxing.
     * 
     * @since 1.9
     */
    public void insert(int index, Long value) {
        if (value == null) {
            insertNull(index);
        } else {
            _insert(index, numberNode(value.longValue()));
        }
    }
    
    /**
     * Method for setting value of a field to specified numeric value.
     */
    public void insert(int index, float v) { _insert(index, numberNode(v)); }

    /**
     * Alternative method that we need to avoid bumping into NPE issues
     * with auto-unboxing.
     * 
     * @since 1.9
     */
    public void insert(int index, Float value) {
        if (value == null) {
            insertNull(index);
        } else {
            _insert(index, numberNode(value.floatValue()));
        }
    }
    
    /**
     * Method for setting value of a field to specified numeric value.
     */
    public void insert(int index, double v) { _insert(index, numberNode(v)); }

    /**
     * Alternative method that we need to avoid bumping into NPE issues
     * with auto-unboxing.
     * 
     * @since 1.9
     */
    public void insert(int index, Double value) {
        if (value == null) {
            insertNull(index);
        } else {
            _insert(index, numberNode(value.doubleValue()));
        }
    }

    /**
     * Method for setting value of a field to specified numeric value.
     */
    public void insert(int index, BigDecimal v) {
        if (v == null) {
            insertNull(index);
        } else {
            _insert(index, numberNode(v));
        }
    }

    /**
     * Method for setting value of a field to specified String value.
     */
    public void insert(int index, String v) {
        if (v == null) {
            insertNull(index);
        } else {
            _insert(index, textNode(v));
        }
    }

    /**
     * Method for setting value of a field to specified String value.
     */
    public void insert(int index, boolean v) { _insert(index, booleanNode(v)); }

    /**
     * Alternative method that we need to avoid bumping into NPE issues
     * with auto-unboxing.
     * 
     * @since 1.9
     */
    public void insert(int index, Boolean value) {
        if (value == null) {
            insertNull(index);
        } else {
            _insert(index, booleanNode(value.booleanValue()));
        }
    }
    
    /**
     * Method for setting value of a field to specified binary value
     */
    public void insert(int index, byte[] v) {
        if (v == null) {
            insertNull(index);
        } else {
            _insert(index, binaryNode(v));
        }
    }

    /*
    /**********************************************************
    /* Package methods (for other node classes to use)
    /**********************************************************
     */

    /**
     * @since 1.6
     */
    protected void addContentsTo(List<JsonNode> dst)
    {
        if (_children != null) {
            for (JsonNode n : _children) {
                dst.add(n);
            }
        }
    }
    
    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) { // final class, can do this
            return false;
        }
        ArrayNode other = (ArrayNode) o;
        if (_children == null || _children.size() == 0) {
            return other.size() == 0;
        }
        return other._sameChildren(_children);
    }

    @Override
    public int hashCode()
    {
        int hash;
        if (_children == null) {
            hash = 1;
        } else {
            hash = _children.size();
            for (JsonNode n : _children) {
                if (n != null) {
                    hash ^= n.hashCode();
                }
            }
        }
        return hash;
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(16 + (size() << 4));
        sb.append('[');
        if (_children != null) {
            for (int i = 0, len = _children.size(); i < len; ++i) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(_children.get(i).toString());
            }
        }
        sb.append(']');
        return sb.toString();
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    public JsonNode _set(int index, JsonNode value)
    {
        if (_children == null || index < 0 || index >= _children.size()) {
            throw new IndexOutOfBoundsException("Illegal index "+index+", array size "+size());
        }
        return _children.set(index, value);
    }

    private void _add(JsonNode node)
    {
        if (_children == null) {
            _children = new ArrayList<JsonNode>();
        }
        _children.add(node);
    }

    private void _insert(int index, JsonNode node)
    {
        if (_children == null) {
            _children = new ArrayList<JsonNode>();
            _children.add(node);
            return;
        }
        if (index < 0) {
            _children.add(0, node);
        } else if (index >= _children.size()) {
            _children.add(node);
        } else {
            _children.add(index, node);
        }
    }

    /**
     * Note: this method gets called iff <code>otherChildren</code>
     * is non-empty
     */
    private boolean _sameChildren(ArrayList<JsonNode> otherChildren)
    {
        int len = otherChildren.size();
        if (this.size() != len) { // important: call size() to handle case of null list...
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (!_children.get(i).equals(otherChildren.get(i))) {
                return false;
            }
        }
        return true;
    }
}
