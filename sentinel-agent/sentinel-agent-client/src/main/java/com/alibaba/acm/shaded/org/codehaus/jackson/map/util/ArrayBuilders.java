package com.alibaba.acm.shaded.org.codehaus.jackson.map.util;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Helper class that contains set of distinct builders for different
 * arrays of primitive values. It also provides trivially simple
 * reuse scheme, which assumes that caller knows not to use instances
 * concurrently (which works ok with primitive arrays since they can
 * not contain other non-primitive types).
 */
public final class ArrayBuilders
{
    BooleanBuilder _booleanBuilder = null;

    // note: no need for char[] builder, assume they are Strings

    ByteBuilder _byteBuilder = null;
    ShortBuilder _shortBuilder = null;
    IntBuilder _intBuilder = null;
    LongBuilder _longBuilder = null;
    
    FloatBuilder _floatBuilder = null;
    DoubleBuilder _doubleBuilder = null;

    public ArrayBuilders() { }

    public BooleanBuilder getBooleanBuilder()
    {
        if (_booleanBuilder == null) {
            _booleanBuilder = new BooleanBuilder();
        }
        return _booleanBuilder;
    }

    public ByteBuilder getByteBuilder()
    {
        if (_byteBuilder == null) {
            _byteBuilder = new ByteBuilder();
        }
        return _byteBuilder;
    }
    public ShortBuilder getShortBuilder()
    {
        if (_shortBuilder == null) {
            _shortBuilder = new ShortBuilder();
        }
        return _shortBuilder;
    }
    public IntBuilder getIntBuilder()
    {
        if (_intBuilder == null) {
            _intBuilder = new IntBuilder();
        }
        return _intBuilder;
    }
    public LongBuilder getLongBuilder()
    {
        if (_longBuilder == null) {
            _longBuilder = new LongBuilder();
        }
        return _longBuilder;
    }

    public FloatBuilder getFloatBuilder()
    {
        if (_floatBuilder == null) {
            _floatBuilder = new FloatBuilder();
        }
        return _floatBuilder;
    }
    public DoubleBuilder getDoubleBuilder()
    {
        if (_doubleBuilder == null) {
            _doubleBuilder = new DoubleBuilder();
        }
        return _doubleBuilder;
    }

    /*
    /**********************************************************
    /* Impl classes
    /**********************************************************
     */

    public final static class BooleanBuilder
        extends PrimitiveArrayBuilder<boolean[]>
    {
        public BooleanBuilder() { }
        @Override
        public final boolean[] _constructArray(int len) { return new boolean[len]; }
    }

    public final static class ByteBuilder
        extends PrimitiveArrayBuilder<byte[]>
    {
        public ByteBuilder() { }
        @Override
        public final byte[] _constructArray(int len) { return new byte[len]; }
    }
    public final static class ShortBuilder
        extends PrimitiveArrayBuilder<short[]>
    {
        public ShortBuilder() { }
        @Override
        public final short[] _constructArray(int len) { return new short[len]; }
    }
    public final static class IntBuilder
        extends PrimitiveArrayBuilder<int[]>
    {
        public IntBuilder() { }
        @Override
        public final int[] _constructArray(int len) { return new int[len]; }
    }
    public final static class LongBuilder
        extends PrimitiveArrayBuilder<long[]>
    {
        public LongBuilder() { }
        @Override
        public final long[] _constructArray(int len) { return new long[len]; }
    }

    public final static class FloatBuilder
        extends PrimitiveArrayBuilder<float[]>
    {
        public FloatBuilder() { }
        @Override
        public final float[] _constructArray(int len) { return new float[len]; }
    }
    public final static class DoubleBuilder
        extends PrimitiveArrayBuilder<double[]>
    {
        public DoubleBuilder() { }
        @Override
        public final double[] _constructArray(int len) { return new double[len]; }
    }
    
    /*
    /**********************************************************
    /* Static helper methods
    /**********************************************************
     */

    public static <T> HashSet<T> arrayToSet(T[] elements)
    {
        HashSet<T> result = new HashSet<T>();
        if (elements != null) {
            for (T elem : elements) {
                result.add(elem);
            }
        }
        return result;
    }

    /**
     * Helper method for adding specified element to a List, but also
     * considering case where the List may not have been yet constructed
     * (that is, null is passed instead).
     * 
     * @param list List to add to; may be null to indicate that a new
     *    List is to be constructed
     * @param element Element to add to list
     * 
     * @return List in which element was added; either <code>list</code>
     *   (if it was not null), or a newly constructed List.
     */
    public static <T> List<T> addToList(List<T> list, T element)
    {
        if (list == null) {
            list = new ArrayList<T>();
        }
        list.add(element);
        return list;
    }

    /**
     * Helper method for constructing a new array that contains specified
     * element followed by contents of the given array. No checking is done
     * to see if element being inserted is duplicate.
     */
    public static <T> T[] insertInList(T[] array, T element)
    {
        int len = array.length;
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), len+1);
        if (len > 0) {
            System.arraycopy(array, 0, result, 1, len);
        }
        result[0] = element;
        return result;
    }

    /**
     * Helper method for constructing a new array that contains specified
     * element followed by contents of the given array but never contains
     * duplicates.
     * If element already existed, one of two things happens: if the element
     * was already the first one in array, array is returned as is; but
     * if not, a new copy is created in which element has moved as the head.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] insertInListNoDup(T[] array, T element)
    {
        final int len = array.length;
        
        // First: see if the element already exists
        for (int ix = 0; ix < len; ++ix) {
            if (array[ix] == element) {
                // if at head already, return as is
                if (ix == 0) {
                    return array;
                }
                // otherwise move things around
                T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), len);
                System.arraycopy(array, 0, result, 1, ix);
                array[0] = element;
                return result;
            }
        }

        // but if not, allocate new array, move
        T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), len+1);
        if (len > 0) {
            System.arraycopy(array, 0, result, 1, len);
        }
        result[0] = element;
        return result;
    }
    
    /**
     * Helper method for exposing contents of arrays using a read-only
     * iterator
     * 
     * @since 1.7
     */
    public static <T> Iterator<T> arrayAsIterator(T[] array)
    {
        return new ArrayIterator<T>(array);
    }

    public static <T> Iterable<T> arrayAsIterable(T[] array)
    {
        return new ArrayIterator<T>(array);
    }

    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * Iterator implementation used to efficiently expose contents of an
     * Array as read-only iterator.
     * 
     * @since 1.7
     */
    private final static class ArrayIterator<T>
        implements Iterator<T>, Iterable<T>
    {
        private final T[] _array;
        
        private int _index;

        public ArrayIterator(T[] array) {
            _array = array;
            _index = 0;
        }
        
        @Override public boolean hasNext() {
            return _index < _array.length;
        }

        @Override
        public T next()
        {
            if (_index >= _array.length) {
                throw new NoSuchElementException();
            }
            return _array[_index++];
        }

        @Override public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<T> iterator() {
            return this;
        }
    }
}
