package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonMappingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.SettableBeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl.CreatorProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedWithParams;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Basic {@link ValueInstantiator} implementation, which only
 * supports use of default constructor. Sub-types can add
 * support for alternate construction methods, such as using
 * argument-taking constructors or static factory methods.
 * 
 * @since 1.9.0
 */
public class StdValueInstantiator
    extends ValueInstantiator
{
    /**
     * Type of values that are instantiated; used
     * for error reporting purposes.
     */
    protected final String _valueTypeDesc;

    /**
     * Are we allowed to convert empty Strings to null objects?
     */
    protected final boolean _cfgEmptyStringsAsObjects;
    
    // // // Default (no-args) construction

    /**
     * Default (no-argument) constructor to use for instantiation
     * (with {@link #createUsingDefault})
     */
    protected AnnotatedWithParams _defaultCreator;

    // // // With-args (property-based) construction

    protected CreatorProperty[] _constructorArguments;
    protected AnnotatedWithParams _withArgsCreator;

    // // // Delegate construction
    
    protected JavaType _delegateType;
    protected AnnotatedWithParams _delegateCreator;
    
    // // // Scalar construction

    protected AnnotatedWithParams _fromStringCreator;
    protected AnnotatedWithParams _fromIntCreator;
    protected AnnotatedWithParams _fromLongCreator;
    protected AnnotatedWithParams _fromDoubleCreator;
    protected AnnotatedWithParams _fromBooleanCreator;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public StdValueInstantiator(DeserializationConfig config, Class<?> valueType)
    {
        _cfgEmptyStringsAsObjects = (config == null) ? false
                : config.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        _valueTypeDesc = (valueType == null) ? "UNKNOWN TYPE" : valueType.getName();
    }
    
    public StdValueInstantiator(DeserializationConfig config, JavaType valueType)
    {
        _cfgEmptyStringsAsObjects = (config == null) ? false
                : config.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        _valueTypeDesc = (valueType == null) ? "UNKNOWN TYPE" : valueType.toString();
    }
    
    /**
     * Copy-constructor that sub-classes can use when creating new instances
     * by fluent-style construction
     */
    protected StdValueInstantiator(StdValueInstantiator src)
    {
        _cfgEmptyStringsAsObjects = src._cfgEmptyStringsAsObjects;
        _valueTypeDesc = src._valueTypeDesc;

        _defaultCreator = src._defaultCreator;

        _constructorArguments = src._constructorArguments;
        _withArgsCreator = src._withArgsCreator;

        _delegateType = src._delegateType;
        _delegateCreator = src._delegateCreator;
        
        _fromStringCreator = src._fromStringCreator;
        _fromIntCreator = src._fromIntCreator;
        _fromLongCreator = src._fromLongCreator;
        _fromDoubleCreator = src._fromDoubleCreator;
        _fromBooleanCreator = src._fromBooleanCreator;
    }

    /**
     * Method for setting properties related to instantiating values
     * from JSON Object. We will choose basically only one approach (out of possible
     * three), and clear other properties
     */
    public void configureFromObjectSettings(AnnotatedWithParams defaultCreator,
            AnnotatedWithParams delegateCreator, JavaType delegateType,
            AnnotatedWithParams withArgsCreator, CreatorProperty[] constructorArgs)
    {
        _defaultCreator = defaultCreator;
        _delegateCreator = delegateCreator;
        _delegateType = delegateType;
        _withArgsCreator = withArgsCreator;
        _constructorArguments = constructorArgs;
    }

    public void configureFromStringCreator(AnnotatedWithParams creator) {
        _fromStringCreator = creator;
    }

    public void configureFromIntCreator(AnnotatedWithParams creator) {
        _fromIntCreator = creator;
    }

    public void configureFromLongCreator(AnnotatedWithParams creator) {
        _fromLongCreator = creator;
    }

    public void configureFromDoubleCreator(AnnotatedWithParams creator) {
        _fromDoubleCreator = creator;
    }

    public void configureFromBooleanCreator(AnnotatedWithParams creator) {
        _fromBooleanCreator = creator;
    }
    
    /*
    /**********************************************************
    /* Public API implementation; metadata
    /**********************************************************
     */

    @Override
    public String getValueTypeDesc() {
        return _valueTypeDesc;
    }
    
    @Override
    public boolean canCreateFromString() {
        return (_fromStringCreator != null);
    }

    @Override
    public boolean canCreateFromInt() {
        return (_fromIntCreator != null);
    }

    @Override
    public boolean canCreateFromLong() {
        return (_fromLongCreator != null);
    }

    @Override
    public boolean canCreateFromDouble() {
        return (_fromDoubleCreator != null);
    }

    @Override
    public boolean canCreateFromBoolean() {
        return (_fromBooleanCreator != null);
    }
    
    @Override
    public boolean canCreateUsingDefault() {
        return (_defaultCreator != null);
    }

    @Override
    public boolean canCreateFromObjectWith() {
        return (_withArgsCreator != null);
    }

    @Override
    public JavaType getDelegateType() {
        return _delegateType;
    }

    @Override
    public SettableBeanProperty[] getFromObjectArguments() {
        return _constructorArguments;
    }
    
    /*
    /**********************************************************
    /* Public API implementation; instantiation from JSON Object
    /**********************************************************
     */
    
    @Override
    public Object createUsingDefault()
        throws IOException, JsonProcessingException
    {
        if (_defaultCreator == null) { // sanity-check; caller should check
            throw new IllegalStateException("No default constructor for "+getValueTypeDesc());
        }
        try {
            return _defaultCreator.call();
        } catch (ExceptionInInitializerError e) {
            throw wrapException(e);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }
    
    @Override
    public Object createFromObjectWith(Object[] args)
        throws IOException, JsonProcessingException
    {
        if (_withArgsCreator == null) { // sanity-check; caller should check
            throw new IllegalStateException("No with-args constructor for "+getValueTypeDesc());
        }
        try {
            return _withArgsCreator.call(args);
        } catch (ExceptionInInitializerError e) {
            throw wrapException(e);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public Object createUsingDelegate(Object delegate)
        throws IOException, JsonProcessingException
    {
        if (_delegateCreator == null) { // sanity-check; caller should check
            throw new IllegalStateException("No delegate constructor for "+getValueTypeDesc());
        }
        try {
            return _delegateCreator.call1(delegate);
        } catch (ExceptionInInitializerError e) {
            throw wrapException(e);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }
    
    /*
    /**********************************************************
    /* Public API implementation; instantiation from JSON scalars
    /**********************************************************
     */
    
    @Override
    public Object createFromString(String value) throws IOException, JsonProcessingException
    {
        if (_fromStringCreator != null) {
            try {
                return _fromStringCreator.call1(value);
            } catch (Exception e) {
                throw wrapException(e);
            }
        }
        return _createFromStringFallbacks(value);
    }
    
    @Override
    public Object createFromInt(int value) throws IOException, JsonProcessingException
    {
        try {
            // First: "native" int methods work best:
            if (_fromIntCreator != null) {
                return _fromIntCreator.call1(Integer.valueOf(value));
            }
            // but if not, can do widening conversion
            if (_fromLongCreator != null) {
                return _fromLongCreator.call1(Long.valueOf(value));
            }
        } catch (Exception e) {
            throw wrapException(e);
        }
        throw new JsonMappingException("Can not instantiate value of type "+getValueTypeDesc()
                +" from JSON integral number; no single-int-arg constructor/factory method");
    }

    @Override
    public Object createFromLong(long value) throws IOException, JsonProcessingException
    {
        try {
            if (_fromLongCreator != null) {
                return _fromLongCreator.call1(Long.valueOf(value));
            }
        } catch (Exception e) {
            throw wrapException(e);
        }
        throw new JsonMappingException("Can not instantiate value of type "+getValueTypeDesc()
                +" from JSON long integral number; no single-long-arg constructor/factory method");
    }

    @Override
    public Object createFromDouble(double value) throws IOException, JsonProcessingException
    {
        try {
            if (_fromDoubleCreator != null) {
                return _fromDoubleCreator.call1(Double.valueOf(value));
            }
        } catch (Exception e) {
            throw wrapException(e);
        }
        throw new JsonMappingException("Can not instantiate value of type "+getValueTypeDesc()
                +" from JSON floating-point number; no one-double/Double-arg constructor/factory method");
    }

    @Override
    public Object createFromBoolean(boolean value) throws IOException, JsonProcessingException
    {
        try {
            if (_fromBooleanCreator != null) {
                return _fromBooleanCreator.call1(Boolean.valueOf(value));
            }
        } catch (Exception e) {
            throw wrapException(e);
        }
        throw new JsonMappingException("Can not instantiate value of type "+getValueTypeDesc()
                +" from JSON boolean value; no single-boolean/Boolean-arg constructor/factory method");
    }
    
    /*
    /**********************************************************
    /* Extended API: configuration mutators, accessors
    /**********************************************************
     */

    @Override
    public AnnotatedWithParams getDelegateCreator() {
        return _delegateCreator;
    }

    @Override
    public AnnotatedWithParams getDefaultCreator() {
        return _defaultCreator;
    }

    @Override
    public AnnotatedWithParams getWithArgsCreator() {
        return _withArgsCreator;
    }
    
    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    protected Object _createFromStringFallbacks(String value) throws IOException, JsonProcessingException
    {
        /* 28-Sep-2011, tatu: Ok this is not clean at all; but since there are legacy
         *   systems that expect conversions in some cases, let's just add a minimal
         *   patch (note: same could conceivably be used for numbers too).
         */
        if (_fromBooleanCreator != null) {
            String str = value.trim();
            if ("true".equals(str)) {
                return createFromBoolean(true);
            }
            if ("false".equals(str)) {
                return createFromBoolean(false);
            }
        }
        
        // and finally, empty Strings might be accepted as null Object...
        if (_cfgEmptyStringsAsObjects && value.length() == 0) {
            return null;
        }
        throw new JsonMappingException("Can not instantiate value of type "+getValueTypeDesc()
                +" from JSON String; no single-String constructor/factory method");
    }
    
    protected JsonMappingException wrapException(Throwable t)
    {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return new JsonMappingException("Instantiation of "+getValueTypeDesc()+" value failed: "+t.getMessage(), t);
    }
}


