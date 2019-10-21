package com.alibaba.acm.shaded.org.codehaus.jackson.map.util;

/**
 * Node of a forward-only linked list.
 * 
 * @author tatu
 *
 * @param <T> Type of contained object
 */
public final class LinkedNode<T>
{
    final T _value;
    final LinkedNode<T> _next;
    
    public LinkedNode(T value, LinkedNode<T> next)
    {
        _value = value;
        _next = next;
    }
    
    public LinkedNode<T> next() { return _next; }
    
    public T value() { return _value; }
    
    /**
     * Convenience method that can be used to check if a linked list
     * with given head node (which may be null to indicate empty list)
     * contains given value
     * 
     * @param <ST> Type argument that defines contents of the linked list parameter
     * @param node Head node of the linked list
     * @param value Value to look for
     * @return True if linked list contains the value, false otherwise
     */
    public static <ST> boolean contains(LinkedNode<ST> node, ST value)
    {
        while (node != null) {
            if (node.value() == value) {
                return true;
            }
            node = node.next();
        }
        return false;
    }
}
