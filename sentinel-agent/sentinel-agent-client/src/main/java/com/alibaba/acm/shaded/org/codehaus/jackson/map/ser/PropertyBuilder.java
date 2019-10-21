package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JsonSerialize;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Helper class for {@link BeanSerializerFactory} that is used to
 * construct {@link BeanPropertyWriter} instances. Can be sub-classed
 * to change behavior.
 */
public class PropertyBuilder
{
    final protected SerializationConfig _config;
    final protected BasicBeanDescription _beanDesc;
    final protected JsonSerialize.Inclusion _outputProps;

    final protected AnnotationIntrospector _annotationIntrospector;

    /**
     * If a property has serialization inclusion value of
     * {@link Inclusion#ALWAYS}, we need to know the default
     * value of the bean, to know if property value equals default
     * one.
     */
    protected Object _defaultBean;

    public PropertyBuilder(SerializationConfig config, BasicBeanDescription beanDesc)
    {
        _config = config;
        _beanDesc = beanDesc;
        _outputProps = beanDesc.findSerializationInclusion(config.getSerializationInclusion());
        _annotationIntrospector = _config.getAnnotationIntrospector();
    }

    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    public Annotations getClassAnnotations() {
        return _beanDesc.getClassAnnotations();
    }
    
    /**
     * @param contentTypeSer Optional explicit type information serializer
     *    to use for contained values (only used for properties that are
     *    of container type)
     */
    protected BeanPropertyWriter buildWriter(String name, JavaType declaredType,
            JsonSerializer<Object> ser,
            TypeSerializer typeSer, TypeSerializer contentTypeSer,
            AnnotatedMember am, boolean defaultUseStaticTyping)
    {
        Field f;
        Method m;
        if (am instanceof AnnotatedField) {
            m = null;
            f = ((AnnotatedField) am).getAnnotated();
        } else {
            m = ((AnnotatedMethod) am).getAnnotated();
            f = null;
        }

        // do we have annotation that forces type to use (to declared type or its super type)?
        JavaType serializationType = findSerializationType(am, defaultUseStaticTyping, declaredType);

        // Container types can have separate type serializers for content (value / element) type
        if (contentTypeSer != null) {
            /* 04-Feb-2010, tatu: Let's force static typing for collection, if there is
             *    type information for contents. Should work well (for JAXB case); can be
             *    revisited if this causes problems.
             */
            if (serializationType == null) {
//                serializationType = TypeFactory.type(am.getGenericType(), _beanDesc.getType());
                serializationType = declaredType;
            }
            JavaType ct = serializationType.getContentType();
            /* 03-Sep-2010, tatu: This is somehow related to [JACKSON-356], but I don't completely
             *   yet understand how pieces fit together. Still, better be explicit than rely on
             *   NPE to indicate an issue...
             */
            if (ct == null) {
                throw new IllegalStateException("Problem trying to create BeanPropertyWriter for property '"
                        +name+"' (of type "+_beanDesc.getType()+"); serialization type "+serializationType+" has no content");
            }
            serializationType = serializationType.withContentTypeHandler(contentTypeSer);
            ct = serializationType.getContentType();
        }
        
        Object valueToSuppress = null;
        boolean suppressNulls = false;

        JsonSerialize.Inclusion methodProps = _annotationIntrospector.findSerializationInclusion(am, _outputProps);

        if (methodProps != null) {
            switch (methodProps) {
            case NON_DEFAULT:
                valueToSuppress = getDefaultValue(name, m, f);
                if (valueToSuppress == null) {
                    suppressNulls = true;
                } else {
                    // [JACKSON-531]: Allow comparison of arrays too...
                    if (valueToSuppress.getClass().isArray()) {
                        valueToSuppress = Comparators.getArrayComparator(valueToSuppress);
                    }
                }
                break;
            case NON_EMPTY:
                // always suppress nulls
                suppressNulls = true;
                // but possibly also 'empty' values:
                valueToSuppress = getEmptyValueChecker(name, declaredType);
                break;
            case NON_NULL:
                suppressNulls = true;
                // fall through
            case ALWAYS: // default
                // we may still want to suppress empty collections, as per [JACKSON-254]:
                if (declaredType.isContainerType()) {
                    valueToSuppress = getContainerValueChecker(name, declaredType);
                }
                break;
            }
        }

        BeanPropertyWriter bpw = new BeanPropertyWriter(am, _beanDesc.getClassAnnotations(), name, declaredType,
                ser, typeSer, serializationType, m, f, suppressNulls, valueToSuppress);
        
        // [JACKSON-132]: Unwrapping
        Boolean unwrapped = _annotationIntrospector.shouldUnwrapProperty(am);
        if (unwrapped != null && unwrapped.booleanValue()) {
            bpw = bpw.unwrappingWriter();
        }
        return bpw;
    }
    
    /*
    /**********************************************************
    /* Helper methods; annotation access
    /**********************************************************
     */

    /**
     * Method that will try to determine statically defined type of property
     * being serialized, based on annotations (for overrides), and alternatively
     * declared type (if static typing for serialization is enabled).
     * If neither can be used (no annotations, dynamic typing), returns null.
     */
    protected JavaType findSerializationType(Annotated a, boolean useStaticTyping, JavaType declaredType)
    {
        // [JACKSON-120]: Check to see if serialization type is fixed
        Class<?> serClass = _annotationIntrospector.findSerializationType(a);
        if (serClass != null) {
            // Must be a super type to be usable
            Class<?> rawDeclared = declaredType.getRawClass();
            if (serClass.isAssignableFrom(rawDeclared)) {
                declaredType = declaredType.widenBy(serClass);
            } else {
                /* 18-Nov-2010, tatu: Related to fixing [JACKSON-416], an issue with such
                 *   check is that for deserialization more specific type makes sense;
                 *   and for serialization more generic. But alas JAXB uses but a single
                 *   annotation to do both... Hence, we must just discard type, as long as
                 *   types are related
                 */
                if (!rawDeclared.isAssignableFrom(serClass)) {
                    throw new IllegalArgumentException("Illegal concrete-type annotation for method '"+a.getName()+"': class "+serClass.getName()+" not a super-type of (declared) class "+rawDeclared.getName());
                }
                /* 03-Dec-2010, tatu: Actually, ugh, to resolve [JACKSON-415] may further relax this
                 *   and actually accept subtypes too for serialization. Bit dangerous in theory
                 *   but need to trust user here...
                 */
                declaredType = _config.constructSpecializedType(declaredType, serClass);
            }
            useStaticTyping = true;
        }

        JavaType secondary = BeanSerializerFactory.modifySecondaryTypesByAnnotation(_config, a, declaredType);
        if (secondary != declaredType) {
            useStaticTyping = true;
            declaredType = secondary;
        }
        
        /* [JACKSON-114]: if using static typing, declared type is known
         * to be the type...
         */
        if (!useStaticTyping) {
            JsonSerialize.Typing typing = _annotationIntrospector.findSerializationTyping(a);
            if (typing != null) {
                useStaticTyping = (typing == JsonSerialize.Typing.STATIC);
            }
        }
        return useStaticTyping ? declaredType : null;
    }

    /*
    /**********************************************************
    /* Helper methods for default value handling
    /**********************************************************
     */
    
    protected Object getDefaultBean()
    {
        if (_defaultBean == null) {
            /* If we can fix access rights, we should; otherwise non-public
             * classes or default constructor will prevent instantiation
             */
            _defaultBean = _beanDesc.instantiateBean(_config.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS));
            if (_defaultBean == null) {
                Class<?> cls = _beanDesc.getClassInfo().getAnnotated();
                throw new IllegalArgumentException("Class "+cls.getName()+" has no default constructor; can not instantiate default bean value to support 'properties=JsonSerialize.Inclusion.NON_DEFAULT' annotation");
            }
        }
        return _defaultBean;
    }

    protected Object getDefaultValue(String name, Method m, Field f)
    {
        Object defaultBean = getDefaultBean();
        try {
            if (m != null) {
                return m.invoke(defaultBean);
            }
            return f.get(defaultBean);
        } catch (Exception e) {
            return _throwWrapped(e, name, defaultBean);
        }
    }

    /**
     * Helper method called to see if we need a comparator Object to check if values
     * of a container (Collection, array) property should be suppressed.
     * This is usually
     * 
     * @param propertyName Name of property to handle
     * @param propertyType Declared type of values of the property to handle
     * @return Object whose <code>equals()</code> method is called to check if given value
     *    is "empty Collection" value to suppress; or null if no such check should be done
     *    (declared type not Collection or array)
     * 
     * @since 1.9
     */
    protected Object getContainerValueChecker(String propertyName, JavaType propertyType)
    {
        // currently we will only check for certain kinds of empty containers:
        if (!_config.isEnabled(SerializationConfig.Feature.WRITE_EMPTY_JSON_ARRAYS)) {
            if (propertyType.isArrayType()) {
                return new EmptyArrayChecker();
            }
            if (Collection.class.isAssignableFrom(propertyType.getRawClass())) {
                return new EmptyCollectionChecker();
            }
        }
        return null;
    }

        
    /**
     * Helper method called to see if we need a comparator Object to check if values
     * of specified type are consider empty.
     * If type has such concept, will build a comparator; otherwise return null, and
     * in latter case, only null values are considered 'empty'.
     * 
     * @param propertyName Name of property to handle
     * @param propertyType Declared type of values of the property to handle
     * @return Object whose <code>equals()</code> method is called to check if given value
     *    is "empty Collection" value to suppress; or null if no such check should be done
     *    (declared type not Collection or array)
     * 
     * @since 1.9
     */
    protected Object getEmptyValueChecker(String propertyName, JavaType propertyType)
    {
        Class<?> rawType = propertyType.getRawClass();
        if (rawType == String.class) {
            return new EmptyStringChecker();
        }
        if (propertyType.isArrayType()) {
            return new EmptyArrayChecker();
        }
        if (Collection.class.isAssignableFrom(rawType)) {
            return new EmptyCollectionChecker();
        }
        if (Map.class.isAssignableFrom(rawType)) {
            return new EmptyMapChecker();
        }
        return null;
    }

    /*
    /**********************************************************
    /* Helper methods for exception handling
    /**********************************************************
     */
    
    protected Object _throwWrapped(Exception e, String propName, Object defaultBean)
    {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof Error) throw (Error) t;
        if (t instanceof RuntimeException) throw (RuntimeException) t;
        throw new IllegalArgumentException("Failed to get property '"+propName+"' of default "+defaultBean.getClass().getName()+" instance");
    }

    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * Helper object used to check if given Collection object is null or empty
     * 
     * @since 1.9
     */
    public static class EmptyCollectionChecker
    {
        @Override public boolean equals(Object other) {
            return (other == null) ||  ((Collection<?>) other).size() == 0;
        }
    }

    /**
     * Helper object used to check if given Map object is null or empty
     * 
     * @since 1.9
     */
    public static class EmptyMapChecker
    {
        @Override public boolean equals(Object other) {
            return (other == null) ||  ((Map<?,?>) other).size() == 0;
        }
    }

    /**
     * Helper object used to check if given array object is null or empty
     * 
     * @since 1.9
     */
    public static class EmptyArrayChecker
    {
        @Override
        public boolean equals(Object other) {
            return (other == null) || Array.getLength(other) == 0;
        }
     }

    /**
     * Helper object used to check if given String object is null or empty
     * 
     * @since 1.9
     */
    public static class EmptyStringChecker
    {
        @Override
        public boolean equals(Object other) {
            return (other == null) || ((String) other).length() == 0;
        }
     }
}
