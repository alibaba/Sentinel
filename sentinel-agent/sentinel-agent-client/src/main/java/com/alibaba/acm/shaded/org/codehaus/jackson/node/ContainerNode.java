package com.alibaba.acm.shaded.org.codehaus.jackson.node;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonToken;

/**
 * This intermediate base class is used for all container nodes,
 * specifically, array and object nodes.
 */
public abstract class ContainerNode
    extends BaseJsonNode
{
    /**
     * We will keep a reference to the Object (usually TreeMapper)
     * that can construct instances of nodes to add to this container
     * node.
     */
    JsonNodeFactory _nodeFactory;

    protected ContainerNode(JsonNodeFactory nc)
    {
        _nodeFactory = nc;
    }

    @Override
    public boolean isContainerNode() { return true; }

    @Override
    public abstract JsonToken asToken();

    /* NOTE: must have separate implementations since semantics have
     * slight difference; deprecated method must return null,
     * new method empty string.
     */

    @SuppressWarnings("deprecation")
    @Override
    public String getValueAsText() { return null; }
    
    
    @Override
    public String asText() { return ""; }

    /*
    /**********************************************************
    /* Find methods; made abstract again to ensure implementation
    /**********************************************************
     */

    @Override
    public abstract JsonNode findValue(String fieldName);
    
    @Override
    public abstract ObjectNode findParent(String fieldName);

    @Override
    public abstract List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar);
    
    @Override
    public abstract List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar);

    @Override
    public abstract List<String> findValuesAsText(String fieldName, List<String> foundSoFar);
    
    /*
    /**********************************************************
    /* Methods reset as abstract to force real implementation
    /**********************************************************
     */

    @Override
    public abstract int size();

    @Override
    public abstract JsonNode get(int index);

    @Override
    public abstract JsonNode get(String fieldName);

    /*
    /**********************************************************
    /* NodeCreator implementation, just dispatch to
    /* the real creator
    /**********************************************************
     */

    /**
     * Factory method that constructs and returns an empty {@link ArrayNode}
     * Construction is done using registered {@link JsonNodeFactory}.
     */
    public final ArrayNode arrayNode() { return _nodeFactory.arrayNode(); }

    /**
     * Factory method that constructs and returns an empty {@link ObjectNode}
     * Construction is done using registered {@link JsonNodeFactory}.
     */
    public final ObjectNode objectNode() { return _nodeFactory.objectNode(); }

    public final NullNode nullNode() { return _nodeFactory.nullNode(); }

    public final BooleanNode booleanNode(boolean v) { return _nodeFactory.booleanNode(v); }

    public final NumericNode numberNode(byte v) { return _nodeFactory.numberNode(v); }
    public final NumericNode numberNode(short v) { return _nodeFactory.numberNode(v); }
    public final NumericNode numberNode(int v) { return _nodeFactory.numberNode(v); }
    public final NumericNode numberNode(long v) { return _nodeFactory.numberNode(v); }
    public final NumericNode numberNode(float v) { return _nodeFactory.numberNode(v); }
    public final NumericNode numberNode(double v) { return _nodeFactory.numberNode(v); }
    public final NumericNode numberNode(BigDecimal v) { return (_nodeFactory.numberNode(v)); }

    public final TextNode textNode(String text) { return _nodeFactory.textNode(text); }

    public final BinaryNode binaryNode(byte[] data) { return _nodeFactory.binaryNode(data); }
    public final BinaryNode binaryNode(byte[] data, int offset, int length) { return _nodeFactory.binaryNode(data, offset, length); }

    public final POJONode POJONode(Object pojo) { return _nodeFactory.POJONode(pojo); }

    /*
    /**********************************************************
    /* Common mutators
    /**********************************************************
     */

    /**
     * Method for removing all children container has (if any)
     *
     * @return Container node itself (to allow method call chaining)
     *
     * @since 1.3
     */
    public abstract ContainerNode removeAll();

    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    protected static class NoNodesIterator
        implements Iterator<JsonNode>
    {
        final static NoNodesIterator instance = new NoNodesIterator();

        private NoNodesIterator() { }

        public static NoNodesIterator instance() { return instance; }

        @Override
        public boolean hasNext() { return false; }
        @Override
        public JsonNode next() { throw new NoSuchElementException(); }

        @Override
        public void remove() {
            // could as well throw IllegalOperationException?
            throw new IllegalStateException();
        }
    }

    protected static class NoStringsIterator
        implements Iterator<String>
    {
        final static NoStringsIterator instance = new NoStringsIterator();

        private NoStringsIterator() { }

        public static NoStringsIterator instance() { return instance; }

        @Override
        public boolean hasNext() { return false; }
        @Override
        public String next() { throw new NoSuchElementException(); }

        @Override
        public void remove() {
            // could as well throw IllegalOperationException?
            throw new IllegalStateException();
        }
    }
}
