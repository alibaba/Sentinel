package com.alibaba.acm.shaded.org.codehaus.jackson.map.util;

/**
 * Base class for specialized primitive array builders.
 */
public abstract class PrimitiveArrayBuilder<T>
{
    /**
     * Let's start with small chunks; typical usage is for small arrays anyway.
     */
    final static int INITIAL_CHUNK_SIZE = 12;

    /**
     * Also: let's expand by doubling up until 64k chunks (which is 16k entries for
     * 32-bit machines)
     */
    final static int SMALL_CHUNK_SIZE = (1 << 14);

    /**
     * Let's limit maximum size of chunks we use; helps avoid excessive allocation
     * overhead for huge data sets.
     * For now, let's limit to quarter million entries, 1 meg chunks for 32-bit
     * machines.
     */
    final static int MAX_CHUNK_SIZE = (1 << 18);

    // // // Data storage

    T _freeBuffer;

    Node<T> _bufferHead;

    Node<T> _bufferTail;

    /**
     * Number of total buffered entries in this buffer, counting all instances
     * within linked list formed by following {@link #_bufferHead}.
     */
    int _bufferedEntryCount;

    // // // Recycled instances of sub-classes

    // // // Life-cycle

    protected PrimitiveArrayBuilder() { }

    /*
    ////////////////////////////////////////////////////////////////////////
    // Public API
    ////////////////////////////////////////////////////////////////////////
     */

    public T resetAndStart()
    {
        _reset();
        return (_freeBuffer == null) ?
            _constructArray(INITIAL_CHUNK_SIZE) : _freeBuffer;
    }

    /**
     * @return Length of the next chunk to allocate
     */
    public final T appendCompletedChunk(T fullChunk, int fullChunkLength)
    {
        Node<T> next = new Node<T>(fullChunk, fullChunkLength);
        if (_bufferHead == null) { // first chunk
            _bufferHead = _bufferTail = next;
        } else { // have something already
            _bufferTail.linkNext(next);
            _bufferTail = next;
        }
        _bufferedEntryCount += fullChunkLength;
        int nextLen = fullChunkLength; // start with last chunk size
        // double the size for small chunks
        if (nextLen < SMALL_CHUNK_SIZE) {
            nextLen += nextLen;
        } else { // but by +25% for larger (to limit overhead)
            nextLen += (nextLen >> 2);
        }
        return _constructArray(nextLen);
    }

    public T completeAndClearBuffer(T lastChunk, int lastChunkEntries)
    {
        int totalSize = lastChunkEntries + _bufferedEntryCount;
        T resultArray = _constructArray(totalSize);

        int ptr = 0;

        for (Node<T> n = _bufferHead; n != null; n = n.next()) {
            ptr = n.copyData(resultArray, ptr);
        }
        System.arraycopy(lastChunk, 0, resultArray, ptr, lastChunkEntries);
        ptr += lastChunkEntries;

        // sanity check (could have failed earlier due to out-of-bounds, too)
        if (ptr != totalSize) {
            throw new IllegalStateException("Should have gotten "+totalSize+" entries, got "+ptr);
        }
        return resultArray;
    }

    /*
    ////////////////////////////////////////////////////////////////////////
    // Abstract methods for sub-classes to implement
    ////////////////////////////////////////////////////////////////////////
     */

    protected abstract T _constructArray(int len);

    /*
    ////////////////////////////////////////////////////////////////////////
    // Internal methods
    ////////////////////////////////////////////////////////////////////////
     */

    protected void _reset()
    {
        // can we reuse the last (and thereby biggest) array for next time?
        if (_bufferTail != null) {
            _freeBuffer = _bufferTail.getData();
        }
        // either way, must discard current contents
        _bufferHead = _bufferTail = null;
        _bufferedEntryCount = 0;
    }

    /*
    ////////////////////////////////////////////////////////////////////////
    // Helper classes
    ////////////////////////////////////////////////////////////////////////
     */

    /**
     * For actual buffering beyond the current buffer, we can actually
     * use shared class which only deals with opaque "untyped" chunks.
     * This works because {@link java.lang.System#arraycopy} does not
     * take type; hence we can implement some aspects of primitive data
     * handling in generic fashion.
     */
    final static class Node<T>
    {
        /**
         * Data stored in this node.
         */
        final T _data;

        /**
         * Number entries in the (untyped) array. Offset is assumed to be 0.
         */
        final int _dataLength;

        Node<T> _next;

        public Node(T data, int dataLen)
        {
            _data = data;
            _dataLength = dataLen;
        }

        public T getData() { return _data; }

        public int copyData(T dst, int ptr)
        {
            System.arraycopy(_data, 0, dst, ptr, _dataLength);
            ptr += _dataLength;
            return ptr;
        }

        public Node<T> next() { return _next; }

        public void linkNext(Node<T> next)
        {
            if (_next != null) { // sanity check
                throw new IllegalStateException();
            }
            _next = next;
        }
    }
}
