package com.alibaba.acm.shaded.org.codehaus.jackson.type;

import java.lang.reflect.Modifier;

/**
 * Base class for type token classes used both to contain information
 * and as keys for deserializers.
 *<p>
 * Instances can (only) be constructed by
 * {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeFactory}.
 */
public abstract class JavaType
{
    /**
     * This is the nominal type-erased Class that would be close to the
     * type represented (but not exactly type, due to type erasure: type
     * instance may have more information on this).
     * May be an interface or abstract class, so instantiation
     * may not be possible.
     */
    protected final Class<?> _class;

    protected final int _hashCode;

    /**
     * Optional handler (codec) that can be attached to indicate 
     * what to use for handling (serializing, deserializing) values of
     * this specific type.
     *<p>
     * Note: untyped (i.e. caller has to cast) because it is used for
     * different kinds of handlers, with unrelated types.
     *<p>
     * TODO: make final and possible promote to sub-classes
     *
     * @since 1.3
     */
    protected /*final*/ Object _valueHandler;

    /**
     * Optional handler that can be attached to indicate how to handle
     * additional type metadata associated with this type.
     *<p>
     * Note: untyped (i.e. caller has to cast) because it is used for
     * different kinds of handlers, with unrelated types.
     *<p>
     * TODO: make final and possible promote to sub-classes
     *
     * @since 1.5
     */
    protected /*final*/ Object _typeHandler;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    /**
     * @param raw "Raw" (type-erased) class for this type
     * @param additionalHash Additional hash code to use, in addition
     *   to hash code of the class name 
     */
    protected JavaType(Class<?> raw, int additionalHash)
    {
        _class = raw;
        _hashCode = raw.getName().hashCode() + additionalHash;
        _valueHandler = null;
        _typeHandler = null;
    }
    
    /**
     * "Copy method" that will construct a new instance that is identical to
     * this instance, except that it will have specified type handler assigned.
     * 
     * @return Newly created type instance
     * 
     * @since 1.7
     */
    public abstract JavaType withTypeHandler(Object h);

    /**
     * "Copy method" that will construct a new instance that is identical to
     * this instance, except that its content type will have specified
     * type handler assigned.
     * 
     * @return Newly created type instance
     * 
     * @since 1.7
     */
    public abstract JavaType withContentTypeHandler(Object h);

    // !!! TODO: in 2.0, change to be abstract method
    //public abstract JavaType withValueHandler(Object h);
    /**
     * @since 1.9
     */
    public JavaType withValueHandler(Object h) {
        /* 16-Aug-2011, tatu: This is not entirely correct, as we can not
         *   create new immutable instances. However, sub-classes can,
         *   so if mapper is version 1.9, things work as expected.
         *   Otherwise, in 2.0 we can make this method abstract.
         */
        setValueHandler(h);
        return this;
    }

    // !!! TODO: in 2.0, change to be abstract method
    //public abstract JavaType withContentValueHandler(Object h);
    /**
     * @since 1.9
     */
    public JavaType withContentValueHandler(Object h) {
        /* 16-Aug-2011, tatu: This is not entirely correct, as we can not
         *   create new immutable instances. However, sub-classes can,
         *   so if mapper is version 1.9, things work as expected.
         *   Otherwise, in 2.0 we can make this method abstract.
         */
        getContentType().setValueHandler(h);
        return this;
    }

    /**
     * Method for assigning handler to associate with this type; or
     * if null passed, to remove such assignment
     * 
     * @since 1.3
     * 
     * @deprecated Since 1.9, should not be used; instead, use
     *   <code>withContentTypeHandler</code> and
     *   <code>withContentValueHandler</code> methods.
     */
    @Deprecated
    public void setValueHandler(Object h) {
        // sanity check, should be assigned just once
        if (h != null && _valueHandler != null) {
            throw new IllegalStateException("Trying to reset value handler for type ["+toString()
                        +"]; old handler of type "+_valueHandler.getClass().getName()+", new handler of type "+h.getClass().getName());
        }
        _valueHandler = h;
    }
    
    /*
    /**********************************************************
    /* Type coercion fluent factory methods
    /**********************************************************
     */
    
    /**
     * Method that can be called to do a "narrowing" conversions; that is,
     * to return a type with a raw class that is assignable to the raw
     * class of this type. If this is not possible, an
     * {@link IllegalArgumentException} is thrown.
     * If class is same as the current raw class, instance itself is
     * returned.
     */
    public JavaType narrowBy(Class<?> subclass)
    {
        // First: if same raw class, just return this instance
        if (subclass == _class) {
            return this;
        }
        // Otherwise, ensure compatibility
        _assertSubclass(subclass, _class);
        JavaType result = _narrow(subclass);

        // TODO: these checks should NOT actually be needed; above should suffice:
        if (_valueHandler != result.getValueHandler()) {
            result = result.withValueHandler(_valueHandler);
        }
        if (_typeHandler != result.getTypeHandler()) {
            result = result.withTypeHandler(_typeHandler);
        }
        return result;
    }

    /**
     * More efficient version of {@link #narrowBy}, called by
     * internal framework in cases where compatibility checks
     * are to be skipped.
     *
     * @since 1.5
     */
    public JavaType forcedNarrowBy(Class<?> subclass)
    {
        if (subclass == _class) { // can still optimize for simple case
            return this;
        }
        JavaType result = _narrow(subclass);
        // TODO: these checks should NOT actually be needed; above should suffice:
        if (_valueHandler != result.getValueHandler()) {
            result = result.withValueHandler(_valueHandler);
        }
        if (_typeHandler != result.getTypeHandler()) {
            result = result.withTypeHandler(_typeHandler);
        }
        return result;
    }

    /**
     * Method that can be called to do a "widening" conversions; that is,
     * to return a type with a raw class that could be assigned from this
     * type.
     * If such conversion is not possible, an
     * {@link IllegalArgumentException} is thrown.
     * If class is same as the current raw class, instance itself is
     * returned.
     */
    public JavaType widenBy(Class<?> superclass)
    {
        // First: if same raw class, just return this instance
        if (superclass == _class) {
            return this;
        }
        // Otherwise, ensure compatibility
        _assertSubclass(_class, superclass);
        return _widen(superclass);
    }

    protected abstract JavaType _narrow(Class<?> subclass);

    /**
     *<p>
     * Default implementation is just to call {@link #_narrow}, since
     * underlying type construction is usually identical
     */
    protected JavaType _widen(Class<?> superclass) {
        return _narrow(superclass);
    }

    public abstract JavaType narrowContentsBy(Class<?> contentClass);

    /**
     * @since 1.8
     */
    public abstract JavaType widenContentsBy(Class<?> contentClass);
    
    /*
    /**********************************************************
    /* Public API, simple accessors
    /**********************************************************
     */

    public final Class<?> getRawClass() { return _class; }

    /**
     * Method that can be used to check whether this type has
     * specified Class as its type erasure. Put another way, returns
     * true if instantiation of this Type is given (type-erased) Class.
     */
    public final boolean hasRawClass(Class<?> clz) {
        return _class == clz;
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(_class.getModifiers());
    }

    /**
     * @since 1.3
     */
    public boolean isConcrete() {
        int mod = _class.getModifiers();
        if ((mod & (Modifier.INTERFACE | Modifier.ABSTRACT)) == 0) {
            return true;
        }
        /* 19-Feb-2010, tatus: Holy mackarel; primitive types
         *    have 'abstract' flag set...
         */
        if (_class.isPrimitive()) {
            return true;
        }
        return false;
    }

    public boolean isThrowable() {
        return Throwable.class.isAssignableFrom(_class);
    }

    public boolean isArrayType() { return false; }

    public final boolean isEnumType() { return _class.isEnum(); }

    public final boolean isInterface() { return _class.isInterface(); }

    public final boolean isPrimitive() { return _class.isPrimitive(); }

    public final boolean isFinal() { return Modifier.isFinal(_class.getModifiers()); }

    /**
     * @return True if type represented is a container type; this includes
     *    array, Map and Collection types.
     */
    public abstract boolean isContainerType();

    /**
     * @return True if type is either true {@link java.util.Collection} type,
     *    or something similar (meaning it has at least one type parameter,
     *    which describes type of contents)
     * 
     * @since 1.8
     */
    public boolean isCollectionLikeType() { return false; }

    /**
     * @return True if type is either true {@link java.util.Map} type,
     *    or something similar (meaning it has at least two type parameter;
     *    first one describing key type, second value type)
     * 
     * @since 1.8
     */
    public boolean isMapLikeType() { return false; }
    
    /*
    /**********************************************************
    /* Public API, type parameter access
    /**********************************************************
     */
    
    /**
     * Method that can be used to find out if the type directly declares generic
     * parameters (for its direct super-class and/or super-interfaces).
     * 
     * @since 1.6
     */
    public boolean hasGenericTypes()
    {
        return containedTypeCount() > 0;
    }
    
    /**
     * Method for accessing key type for this type, assuming type
     * has such a concept (only Map types do)
     */
    public JavaType getKeyType() { return null; }

    /**
     * Method for accessing content type of this type, if type has
     * such a thing: simple types do not, structured types do
     * (like arrays, Collections and Maps)
     */
    public JavaType getContentType() { return null; }

    /**
     * Method for checking how many contained types this type
     * has. Contained types are usually generic types, so that
     * generic Maps have 2 contained types.
     * 
     * @since 1.5
     */
    public int containedTypeCount() { return 0; }

    /**
     * Method for accessing definitions of contained ("child")
     * types.
     * 
     * @param index Index of contained type to return
     * 
     * @return Contained type at index, or null if no such type
     *    exists (no exception thrown)
     * 
     * @since 1.5
     */
    public JavaType containedType(int index) { return null; }
    
    /**
     * Method for accessing name of type variable in indicated
     * position. If no name is bound, will use placeholders (derived
     * from 0-based index); if no type variable or argument exists
     * with given index, null is returned.
     * 
     * @param index Index of contained type to return
     * 
     * @return Contained type at index, or null if no such type
     *    exists (no exception thrown)
     * 
     * @since 1.5
     */
    public String containedTypeName(int index) { return null; }

    /*
    /**********************************************************
    /* Semi-public API, accessing handlers
    /**********************************************************
     */
    
    /**
     * Method for accessing value handler associated with this type, if any
     * 
     * @since 1.3
     */
    @SuppressWarnings("unchecked")
    public <T> T getValueHandler() { return (T) _valueHandler; }

    /**
     * Method for accessing type handler associated with this type, if any
     * 
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public <T> T getTypeHandler() { return (T) _typeHandler; }

    /*
    /**********************************************************
    /* Support for producing signatures (1.6+)
    /**********************************************************
     */
    
    /**
     * Method that can be used to serialize type into form from which
     * it can be fully deserialized from at a later point (using
     * <code>TypeFactory</code> from mapper package).
     * For simple types this is same as calling
     * {@link Class#getName}, but for structured types it may additionally
     * contain type information about contents.
     * 
     * @since 1.5
     */
    public abstract String toCanonical();

    /**
     * Method for accessing signature that contains generic
     * type information, in form compatible with JVM 1.5
     * as per JLS. It is a superset of {@link #getErasedSignature},
     * in that generic information can be automatically removed
     * if necessary (just remove outermost
     * angle brackets along with content inside)
     * 
     * @since 1.6
     */
    public String getGenericSignature() {
        StringBuilder sb = new StringBuilder(40);
        getGenericSignature(sb);
        return sb.toString();        
    }

    /**
     * 
     * @param sb StringBuilder to append signature to
     * 
     * @return StringBuilder that was passed in; returned to allow
     * call chaining
     * 
     * @since 1.6
     */
    public abstract StringBuilder getGenericSignature(StringBuilder sb);
    
    /**
     * Method for accessing signature without generic
     * type information, in form compatible with all versions
     * of JVM, and specifically used for type descriptions
     * when generating byte code.
     * 
     * @since 1.6
     */
    public String getErasedSignature() {
        StringBuilder sb = new StringBuilder(40);
        getErasedSignature(sb);
        return sb.toString();
    }

    /**
     * Method for accessing signature without generic
     * type information, in form compatible with all versions
     * of JVM, and specifically used for type descriptions
     * when generating byte code.
     * 
     * @param sb StringBuilder to append signature to
     * 
     * @return StringBuilder that was passed in; returned to allow
     * call chaining
     * 
     * @since 1.6
     */
    public abstract StringBuilder getErasedSignature(StringBuilder sb);
    
    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */

    protected void _assertSubclass(Class<?> subclass, Class<?> superClass)
    {
        if (!_class.isAssignableFrom(subclass)) {
            throw new IllegalArgumentException("Class "+subclass.getName()+" is not assignable to "+_class.getName());
        }
    }

    /*
    /**********************************************************
    /* Standard methods; let's make them abstract to force override
    /**********************************************************
     */

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object o);

    @Override
    public final int hashCode() { return _hashCode; }
}
