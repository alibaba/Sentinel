package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonMappingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedMember;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl.UnwrappingBeanPropertyWriter;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.Annotations;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.SerializedString;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Base bean property handler class, which implements common parts of
 * reflection-based functionality for accessing a property value
 * and serializing it.
 */
public class BeanPropertyWriter
    implements BeanProperty
{
    /*
    /**********************************************************
    /* Settings for accessing property value to serialize
    /**********************************************************
     */

    /**
     * Member (field, method) that represents property and allows access
     * to associated annotations.
     * 
     * @since 1.7
     */
    protected final AnnotatedMember _member;

    /**
     * Annotations from context (most often, class that declares property,
     * or in case of sub-class serializer, from that sub-class)
     */
    protected final Annotations _contextAnnotations;
    
    /**
     * Type property is declared to have, either in class definition 
     * or associated annotations.
     */
    protected final JavaType _declaredType;
    
    /**
     * Accessor method used to get property value, for
     * method-accessible properties.
     * Null if and only if {@link #_field} is null.
     */
    protected final Method _accessorMethod;
    
    /**
     * Field that contains the property value for field-accessible
     * properties.
     * Null if and only if {@link #_accessorMethod} is null.
     */
    protected final Field _field;
    
    /*
    /**********************************************************
    /* Opaque internal data that bean serializer factory and
    /* bean serializers can add.
    /* 
    /* @since 1.7
    /**********************************************************
     */

    protected HashMap<Object,Object> _internalSettings;
    
    /*
    /**********************************************************
    /* Serialization settings
    /**********************************************************
     */
    
    /**
     * Logical name of the property; will be used as the field name
     * under which value for the property is written.
     */
    protected final SerializedString _name;

    /**
     * Type to use for locating serializer; normally same as return
     * type of the accessor method, but may be overridden by annotations.
     */
    protected final JavaType _cfgSerializationType;

    /**
     * Serializer to use for writing out the value: null if it can not
     * be known statically; non-null if it can.
     */
    protected final JsonSerializer<Object> _serializer;

    /**
     * In case serializer is not known statically (i.e. <code>_serializer</code>
     * is null), we will use a lookup structure for storing dynamically
     * resolved mapping from type(s) to serializer(s).
     * 
     * @since 1.7
     */
    protected PropertySerializerMap _dynamicSerializers;
    
    /**
     * Flag to indicate that null values for this property are not
     * to be written out. That is, if property has value null,
     * no entry will be written
     */
    protected final boolean _suppressNulls;

    /**
     * Value that is considered default value of the property; used for
     * default-value-suppression if enabled.
     */
    protected final Object _suppressableValue;

    /**
     * Alternate set of property writers used when view-based filtering
     * is available for the Bean.
     * 
     * @since 1.4
     */
    protected Class<?>[] _includeInViews;

    /**
     * If property being serialized needs type information to be
     * included this is the type serializer to use.
     * Declared type (possibly augmented with annotations) of property
     * is used for determining exact mechanism to use (compared to
     * actual runtime type used for serializing actual state).
     */
    protected TypeSerializer _typeSerializer;
    
    /**
     * Base type of the property, if the declared type is "non-trivial";
     * meaning it is either a structured type (collection, map, array),
     * or parametrized. Used to retain type information about contained
     * type, which is mostly necessary if type metadata is to be
     * included.
     *
     * @since 1.5
     */
    protected JavaType _nonTrivialBaseType;

    /*
    /**********************************************************
    /* Construction, configuration
    /**********************************************************
     */

    public BeanPropertyWriter(AnnotatedMember member, Annotations contextAnnotations,
            String name, JavaType declaredType,
            JsonSerializer<Object> ser, TypeSerializer typeSer, JavaType serType,
            Method m, Field f,
            boolean suppressNulls, Object suppressableValue)
    {
        this(member, contextAnnotations, new SerializedString(name), declaredType,
                ser, typeSer, serType,
                m, f, suppressNulls, suppressableValue);
    }
    
    public BeanPropertyWriter(AnnotatedMember member, Annotations contextAnnotations,
            SerializedString name, JavaType declaredType,
            JsonSerializer<Object> ser, TypeSerializer typeSer, JavaType serType,
            Method m, Field f, boolean suppressNulls, Object suppressableValue)
    {
        _member = member;
        _contextAnnotations = contextAnnotations;
        _name = name;
        _declaredType = declaredType;
        _serializer = ser;
        _dynamicSerializers = (ser == null) ? PropertySerializerMap.emptyMap() : null;
        _typeSerializer = typeSer;
        _cfgSerializationType = serType;
        _accessorMethod = m;
        _field = f;
        _suppressNulls = suppressNulls;
        _suppressableValue = suppressableValue;
    }

    /**
     * "Copy constructor" to be used by filtering sub-classes
     */
    protected BeanPropertyWriter(BeanPropertyWriter base)
    {
        this(base, base._serializer);
    }
    
    /**
     * "Copy constructor" to be used by filtering sub-classes
     */
    protected BeanPropertyWriter(BeanPropertyWriter base, JsonSerializer<Object> ser)
    {
        _serializer = ser;
        
        _member = base._member;
        _contextAnnotations = base._contextAnnotations;
        _declaredType = base._declaredType;
        _accessorMethod = base._accessorMethod;
        _field = base._field;
        // one more thing: copy internal settings, if any (since 1.7)
        if (base._internalSettings != null) {
            _internalSettings = new HashMap<Object,Object>(base._internalSettings);
        }
        _name = base._name;
        _cfgSerializationType = base._cfgSerializationType;
        _dynamicSerializers = base._dynamicSerializers;
        _suppressNulls = base._suppressNulls;
        _suppressableValue = base._suppressableValue;
        _includeInViews = base._includeInViews;
        _typeSerializer = base._typeSerializer;
        _nonTrivialBaseType = base._nonTrivialBaseType;
   }

    /**
     * Method that will construct and return a new writer that has
     * same properties as this writer, but uses specified serializer
     * instead of currently configured one (if any).
     */
    public BeanPropertyWriter withSerializer(JsonSerializer<Object> ser)
    {
        // sanity check to ensure sub-classes override...
        if (getClass() != BeanPropertyWriter.class) {
            throw new IllegalStateException("BeanPropertyWriter sub-class does not override 'withSerializer()'; needs to!");
        }
        return new BeanPropertyWriter(this, ser);
    }

    /**
     * Method called create an instance that handles details of unwrapping
     * contained value.
     * 
     * @since 1.9
     */
    public BeanPropertyWriter unwrappingWriter() {
        return new UnwrappingBeanPropertyWriter(this);
    }
    
    /**
     * Method for defining which views to included value of this
     * property in. If left undefined, will always be included;
     * otherwise active view definition will be checked against
     * definition list and value is only included if active
     * view is one of defined views, or its sub-view (as defined
     * by class/sub-class relationship).
     */
    public void setViews(Class<?>[] views) { _includeInViews = views; }

    /**
     * Method called to define type to consider as "non-trivial" basetype,
     * needed for dynamic serialization resolution for complex (usually container)
     * types
     *
     * @since 1.5
     */
    public void setNonTrivialBaseType(JavaType t) {
        _nonTrivialBaseType = t;
    }

    /*
    /**********************************************************
    /* BeanProperty impl
    /**********************************************************
     */
    
    @Override
    public String getName() {
        return _name.getValue();
    }

    @Override
    public JavaType getType() {
        return _declaredType;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> acls) {
        return _member.getAnnotation(acls);
    }

    @Override
    public <A extends Annotation> A getContextAnnotation(Class<A> acls) {
        return _contextAnnotations.get(acls);
    }
    
    @Override
    public AnnotatedMember getMember() {
        return _member;
    }
    
    /*
    /**********************************************************
    /* Managing and accessing of opaque internal settings
    /* (used by extensions)
    /**********************************************************
     */
    
    /**
     * Method for accessing value of specified internal setting.
     * 
     * @return Value of the setting, if any; null if none.
     * 
     * @since 1.7
     */
    public Object getInternalSetting(Object key)
    {
        if (_internalSettings == null) {
            return null;
        }
        return _internalSettings.get(key);
    }
    
    /**
     * Method for setting specific internal setting to given value
     * 
     * @return Old value of the setting, if any (null if none)
     * 
     * @since 1.7
     */
    public Object setInternalSetting(Object key, Object value)
    {
        if (_internalSettings == null) {
            _internalSettings = new HashMap<Object,Object>();
        }
        return _internalSettings.put(key, value);
    }

    /**
     * Method for removing entry for specified internal setting.
     * 
     * @return Existing value of the setting, if any (null if none)
     * 
     * @since 1.7
     */
    public Object removeInternalSetting(Object key)
    {
        Object removed = null;
        if (_internalSettings != null) {
            removed = _internalSettings.remove(key);
            // to reduce memory usage, let's also drop the Map itself, if empty
            if (_internalSettings.size() == 0) {
                _internalSettings = null;
            }
        }
        return removed;
    }
    
    /*
    /**********************************************************
    /* Accessors
    /**********************************************************
     */

    public SerializedString getSerializedName() { return _name; }
    
    public boolean hasSerializer() { return _serializer != null; }
    
    // Needed by BeanSerializer#getSchema
    public JsonSerializer<Object> getSerializer() {
        return _serializer;
    }

    public JavaType getSerializationType() {
        return _cfgSerializationType;
    }

    public Class<?> getRawSerializationType() {
        return (_cfgSerializationType == null) ? null : _cfgSerializationType.getRawClass();
    }
    
    public Class<?> getPropertyType() 
    {
        if (_accessorMethod != null) {
            return _accessorMethod.getReturnType();
        }
        return _field.getType();
    }

    /**
     * Get the generic property type of this property writer.
     *
     * @return The property type, or null if not found.
     */
    public Type getGenericPropertyType()
    {
        if (_accessorMethod != null) {
            return _accessorMethod.getGenericReturnType();
        }
        return _field.getGenericType();
    }

    public Class<?>[] getViews() { return _includeInViews; }
    
    /*
    /**********************************************************
    /* Serialization functionality
    /**********************************************************
     */

    /**
     * Method called to access property that this bean stands for, from
     * within given bean, and to serialize it as a JSON Object field
     * using appropriate serializer.
     */
    public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov)
        throws Exception
    {
        Object value = get(bean);
        // Null handling is bit different, check that first
        if (value == null) {
            if (!_suppressNulls) {
                jgen.writeFieldName(_name);
                prov.defaultSerializeNull(jgen);
            }
            return;
        }
        // For non-nulls, first: simple check for direct cycles
        if (value == bean) {
            _reportSelfReference(bean);
        }
        if (_suppressableValue != null && _suppressableValue.equals(value)) {
            return;
        }

        JsonSerializer<Object> ser = _serializer;
        if (ser == null) {
            Class<?> cls = value.getClass();
            PropertySerializerMap map = _dynamicSerializers;
            ser = map.serializerFor(cls);
            if (ser == null) {
                ser = _findAndAddDynamic(map, cls, prov);
            }
        }
        jgen.writeFieldName(_name);
        if (_typeSerializer == null) {
            ser.serialize(value, jgen, prov);
        } else {
            ser.serializeWithType(value, jgen, prov, _typeSerializer);
        }
    }

    /**
     * @since 1.7
     */
    protected JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
            Class<?> type, SerializerProvider provider) throws JsonMappingException
    {
        PropertySerializerMap.SerializerAndMapResult result;
        if (_nonTrivialBaseType != null) {
            JavaType t = provider.constructSpecializedType(_nonTrivialBaseType, type);
            result = map.findAndAddSerializer(t, provider, this);
        } else {
            result = map.findAndAddSerializer(type, provider, this);
        }
        // did we get a new map of serializers? If so, start using it
        if (map != result.map) {
            _dynamicSerializers = result.map;
        }
        return result.serializer;
    }
    
    /**
     * Method that can be used to access value of the property this
     * Object describes, from given bean instance.
     *<p>
     * Note: method is final as it should not need to be overridden -- rather,
     * calling method(s) ({@link #serializeAsField}) should be overridden
     * to change the behavior
     */
    public final Object get(Object bean) throws Exception
    {
        if (_accessorMethod != null) {
            return _accessorMethod.invoke(bean);
        }
        return _field.get(bean);
    }

    protected void _reportSelfReference(Object bean)
        throws JsonMappingException
    {
        throw new JsonMappingException("Direct self-reference leading to cycle");
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(40);
        sb.append("property '").append(getName()).append("' (");
        if (_accessorMethod != null) {
            sb.append("via method ").append(_accessorMethod.getDeclaringClass().getName()).append("#").append(_accessorMethod.getName());
        } else {
            sb.append("field \"").append(_field.getDeclaringClass().getName()).append("#").append(_field.getName());
        }
        if (_serializer == null) {
            sb.append(", no static serializer");
        } else {
            sb.append(", static serializer of type "+_serializer.getClass().getName());
        }
        sb.append(')');
        return sb.toString();
    }
}
