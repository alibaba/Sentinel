package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JsonSerialize;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ext.OptionalHandlerFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.NamedType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.StdArraySerializers;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.CalendarSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.StdJdkSerializers;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.StdContainerSerializers;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.DateSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.EnumMapSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.EnumSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.IndexedStringListSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.InetAddressSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.JsonValueSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.MapSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.NullSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.ObjectArraySerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.SerializableSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.SerializableWithTypeSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.StringCollectionSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.StringSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.TimeZoneSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.ToStringSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.TokenBufferSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.EnumValues;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.TokenBuffer;

/**
 * Factory class that can provide serializers for standard JDK classes,
 * as well as custom classes that extend standard classes or implement
 * one of "well-known" interfaces (such as {@link java.util.Collection}).
 *<p>
 * Since all the serializers are eagerly instantiated, and there is
 * no additional introspection or customizability of these types,
 * this factory is essentially stateless.
 */
public abstract class BasicSerializerFactory
    extends SerializerFactory
{
    
    /*
    /**********************************************************
    /* Configuration, lookup tables/maps
    /**********************************************************
     */

    /**
     * Since these are all JDK classes, we shouldn't have to worry
     * about ClassLoader used to load them. Rather, we can just
     * use the class name, and keep things simple and efficient.
     */
    protected final static HashMap<String, JsonSerializer<?>> _concrete =
        new HashMap<String, JsonSerializer<?>>();
    
    /**
     * Actually it may not make much sense to eagerly instantiate all
     * kinds of serializers: so this Map actually contains class references,
     * not instances
     *
     * @since 1.6
     */
    protected final static HashMap<String, Class<? extends JsonSerializer<?>>> _concreteLazy =
        new HashMap<String, Class<? extends JsonSerializer<?>>>();
    
    static {
        /* String and string-like types (note: date types explicitly
         * not included -- can use either textual or numeric serialization)
         */
        _concrete.put(String.class.getName(), new StringSerializer());
        final ToStringSerializer sls = ToStringSerializer.instance;
        _concrete.put(StringBuffer.class.getName(), sls);
        _concrete.put(StringBuilder.class.getName(), sls);
        _concrete.put(Character.class.getName(), sls);
        _concrete.put(Character.TYPE.getName(), sls);

        // Primitives/wrappers for primitives (primitives needed for Beans)
        _concrete.put(Boolean.TYPE.getName(), new StdSerializers.BooleanSerializer(true));
        _concrete.put(Boolean.class.getName(), new StdSerializers.BooleanSerializer(false));
        final JsonSerializer<?> intS = new StdSerializers.IntegerSerializer();
        _concrete.put(Integer.class.getName(), intS);
        _concrete.put(Integer.TYPE.getName(), intS);
        _concrete.put(Long.class.getName(), StdSerializers.LongSerializer.instance);
        _concrete.put(Long.TYPE.getName(), StdSerializers.LongSerializer.instance);
        _concrete.put(Byte.class.getName(), StdSerializers.IntLikeSerializer.instance);
        _concrete.put(Byte.TYPE.getName(), StdSerializers.IntLikeSerializer.instance);
        _concrete.put(Short.class.getName(), StdSerializers.IntLikeSerializer.instance);
        _concrete.put(Short.TYPE.getName(), StdSerializers.IntLikeSerializer.instance);

        // Numbers, limited length floating point
        _concrete.put(Float.class.getName(), StdSerializers.FloatSerializer.instance);
        _concrete.put(Float.TYPE.getName(), StdSerializers.FloatSerializer.instance);
        _concrete.put(Double.class.getName(), StdSerializers.DoubleSerializer.instance);
        _concrete.put(Double.TYPE.getName(), StdSerializers.DoubleSerializer.instance);

        // Other numbers, more complicated
        final JsonSerializer<?> ns = new StdSerializers.NumberSerializer();
        _concrete.put(BigInteger.class.getName(), ns);
        _concrete.put(BigDecimal.class.getName(), ns);
        
        // Other discrete non-container types:
        // First, Date/Time zoo:
        _concrete.put(Calendar.class.getName(), CalendarSerializer.instance);
        DateSerializer dateSer = DateSerializer.instance;
        _concrete.put(java.util.Date.class.getName(), dateSer);
        // note: timestamps are very similar to java.util.Date, thus serialized as such
        _concrete.put(java.sql.Timestamp.class.getName(), dateSer);
        _concrete.put(java.sql.Date.class.getName(), new StdSerializers.SqlDateSerializer());
        _concrete.put(java.sql.Time.class.getName(), new StdSerializers.SqlTimeSerializer());

        // And then other standard non-structured JDK types
        for (Map.Entry<Class<?>,Object> en : new StdJdkSerializers().provide()) {
            Object value = en.getValue();
            if (value instanceof JsonSerializer<?>) {
                _concrete.put(en.getKey().getName(), (JsonSerializer<?>) value);
            } else if (value instanceof Class<?>) {
                @SuppressWarnings("unchecked")
                Class<? extends JsonSerializer<?>> cls = (Class<? extends JsonSerializer<?>>) value;
                _concreteLazy.put(en.getKey().getName(), cls);
            } else { // should never happen, but:
                throw new IllegalStateException("Internal error: unrecognized value of type "+en.getClass().getName());
            }
        }

        // Jackson-specific type(s)
        // (Q: can this ever be sub-classed?)
        _concreteLazy.put(TokenBuffer.class.getName(), TokenBufferSerializer.class);
    }

    protected final static HashMap<String, JsonSerializer<?>> _arraySerializers =
        new HashMap<String, JsonSerializer<?>>();
    static {
        // Arrays of various types (including common object types)
        _arraySerializers.put(boolean[].class.getName(), new StdArraySerializers.BooleanArraySerializer());
        _arraySerializers.put(byte[].class.getName(), new StdArraySerializers.ByteArraySerializer());
        _arraySerializers.put(char[].class.getName(), new StdArraySerializers.CharArraySerializer());
        _arraySerializers.put(short[].class.getName(), new StdArraySerializers.ShortArraySerializer());
        _arraySerializers.put(int[].class.getName(), new StdArraySerializers.IntArraySerializer());
        _arraySerializers.put(long[].class.getName(), new StdArraySerializers.LongArraySerializer());
        _arraySerializers.put(float[].class.getName(), new StdArraySerializers.FloatArraySerializer());
        _arraySerializers.put(double[].class.getName(), new StdArraySerializers.DoubleArraySerializer());
    }
    
    /**
     * Helper object used to deal with serializers for optional JDK types (like ones
     * omitted from GAE, Android)
     */
    protected OptionalHandlerFactory optionalHandlers = OptionalHandlerFactory.instance;

    /*
    /**********************************************************
    /* Life cycle
    /**********************************************************
     */

    /**
     * We will provide default constructor to allow sub-classing,
     * but make it protected so that no non-singleton instances of
     * the class will be instantiated.
     */
    protected BasicSerializerFactory() { }

    /*
    /**********************************************************
    /* SerializerFactory impl
    /**********************************************************
     */

    // Implemented by sub-classes
    @Override
    public abstract JsonSerializer<Object> createSerializer(SerializationConfig config, JavaType type,
            BeanProperty property)
        throws JsonMappingException;

    /**
     * Method called to construct a type serializer for values with given declared
     * base type. This is called for values other than those of bean property
     * types.
     */
    @Override
    public TypeSerializer createTypeSerializer(SerializationConfig config, JavaType baseType,
            BeanProperty property)
    {
        BasicBeanDescription bean = config.introspectClassAnnotations(baseType.getRawClass());
        AnnotatedClass ac = bean.getClassInfo();
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);
        /* Ok: if there is no explicit type info handler, we may want to
         * use a default. If so, config object knows what to use.
         */
        Collection<NamedType> subtypes = null;
        if (b == null) {
            b = config.getDefaultTyper(baseType);
        } else {
            subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(ac, config, ai);
        }
        return (b == null) ? null : b.buildTypeSerializer(config, baseType, subtypes, property);
    }

    
    /*
    /**********************************************************
    /* Additional API for other core classes
    /**********************************************************
     */

    public final JsonSerializer<?> getNullSerializer() {
        return NullSerializer.instance;
    }    

    protected abstract Iterable<Serializers> customSerializers();
    
    /*
    /**********************************************************
    /* Overridable secondary serializer accessor methods
    /**********************************************************
     */
    
    /**
     * Method that will use fast lookup (and identity comparison) methods to
     * see if we know serializer to use for given type.
     */
    public final JsonSerializer<?> findSerializerByLookup(JavaType type, SerializationConfig config,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping)
    {
        Class<?> raw = type.getRawClass();
        String clsName = raw.getName();
        JsonSerializer<?> ser = _concrete.get(clsName);
        if (ser != null) {
            return ser;
        }
        Class<? extends JsonSerializer<?>> serClass = _concreteLazy.get(clsName);
        if (serClass != null) {
            try {
                return serClass.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate standard serializer (of type "+serClass.getName()+"): "
                        +e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Method for checking if we can determine serializer to use based on set of
     * known primary types, checking for set of known base types (exact matches
     * having been compared against with <code>findSerializerByLookup</code>).
     * This does not include "secondary" interfaces, but
     * mostly concrete or abstract base classes.
     */
    @SuppressWarnings("deprecation")
    public final JsonSerializer<?> findSerializerByPrimaryType(JavaType type, SerializationConfig config,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping)
        throws JsonMappingException
    {
        Class<?> raw = type.getRawClass();
        // First: JsonSerializable and related
        if (JsonSerializable.class.isAssignableFrom(raw)) {
            if (JsonSerializableWithType.class.isAssignableFrom(raw)) {
                return SerializableWithTypeSerializer.instance;
            }
            return SerializableSerializer.instance;
        }
        // Second: as per [JACKSON-193] consider @JsonValue for any types:
        AnnotatedMethod valueMethod = beanDesc.findJsonValueMethod();
        if (valueMethod != null) {
            // [JACKSON-586]: need to ensure accessibility of method
            Method m = valueMethod.getAnnotated();
            if (config.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                ClassUtil.checkAndFixAccess(m);
            }
            JsonSerializer<Object> ser = findSerializerFromAnnotation(config, valueMethod, property);
            return new JsonValueSerializer(m, ser, property);
        }
        
        // One unfortunate special case, as per [JACKSON-484]
        if (InetAddress.class.isAssignableFrom(raw)) {
            return InetAddressSerializer.instance;
        }
        // ... and another one, [JACKSON-522], for TimeZone
        if (TimeZone.class.isAssignableFrom(raw)) {
            return TimeZoneSerializer.instance;
        }
        // and yet one more [JACKSON-789]
        if (java.nio.charset.Charset.class.isAssignableFrom(raw)) {
            return ToStringSerializer.instance;
        }
        
        // Then check for optional/external serializers [JACKSON-386]
        JsonSerializer<?> ser = optionalHandlers.findSerializer(config, type);
        if (ser != null) {
            return ser;
        }
        
        if (Number.class.isAssignableFrom(raw)) {
            return StdSerializers.NumberSerializer.instance;
        }
        if (Enum.class.isAssignableFrom(raw)) {
            @SuppressWarnings("unchecked")
            Class<Enum<?>> enumClass = (Class<Enum<?>>) raw;
            return EnumSerializer.construct(enumClass, config, beanDesc);
        }
        if (Calendar.class.isAssignableFrom(raw)) {
            return CalendarSerializer.instance;
        }
        if (java.util.Date.class.isAssignableFrom(raw)) {
            return DateSerializer.instance;
        }
        return null;
    }
        
    /**
     * Reflection-based serialized find method, which checks if
     * given class implements one of recognized "add-on" interfaces.
     * Add-on here means a role that is usually or can be a secondary
     * trait: for example,
     * bean classes may implement {@link Iterable}, but their main
     * function is usually something else. The reason for
     */
    public final JsonSerializer<?> findSerializerByAddonType(SerializationConfig config, JavaType javaType,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping)
        throws JsonMappingException
    {
        Class<?> type = javaType.getRawClass();

        // These need to be in decreasing order of specificity...
        if (Iterator.class.isAssignableFrom(type)) {
            return buildIteratorSerializer(config, javaType, beanDesc, property, staticTyping);
        }
        if (Iterable.class.isAssignableFrom(type)) {
            return buildIterableSerializer(config, javaType, beanDesc, property, staticTyping);
        }
        if (CharSequence.class.isAssignableFrom(type)) {
            return ToStringSerializer.instance;
        }
        return null;
    }
    
    /**
     * Helper method called to check if a class or method
     * has an annotation
     * (@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.JsonSerialize#using)
     * that tells the class to use for serialization.
     * Returns null if no such annotation found.
     */
    @SuppressWarnings("unchecked")
    protected JsonSerializer<Object> findSerializerFromAnnotation(SerializationConfig config, Annotated a,
            BeanProperty property)
        throws JsonMappingException
    {
        Object serDef = config.getAnnotationIntrospector().findSerializer(a);
        if (serDef == null) {
            return null;
        }
        if (serDef instanceof JsonSerializer) {
            JsonSerializer<Object> ser = (JsonSerializer<Object>) serDef;
            if (ser instanceof ContextualSerializer<?>) {
                return ((ContextualSerializer<Object>) ser).createContextual(config, property);
            }
            return ser;
        }
        /* Alas, there's no way to force return type of "either class
         * X or Y" -- need to throw an exception after the fact
         */
        if (!(serDef instanceof Class)) {
            throw new IllegalStateException("AnnotationIntrospector returned value of type "+serDef.getClass().getName()+"; expected type JsonSerializer or Class<JsonSerializer> instead");
        }
        Class<?> cls = (Class<?>) serDef;
        if (!JsonSerializer.class.isAssignableFrom(cls)) {
            throw new IllegalStateException("AnnotationIntrospector returned Class "+cls.getName()+"; expected Class<JsonSerializer>");
        }
        JsonSerializer<Object> ser = config.serializerInstance(a, (Class<? extends JsonSerializer<?>>) cls);
        if (ser instanceof ContextualSerializer<?>) {
            return ((ContextualSerializer<Object>) ser).createContextual(config, property);
        }
        return ser;
    }

    /*
    /**********************************************************
    /* Factory methods, container types:
    /**********************************************************
     */
    
    public JsonSerializer<?> buildContainerSerializer(SerializationConfig config, JavaType type,
            BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping)
    {
        // Let's see what we can learn about element/content/value type, type serializer for it:
        JavaType elementType = type.getContentType();
        TypeSerializer elementTypeSerializer = createTypeSerializer(config, elementType, property);
        
        // if elements have type serializer, can not force static typing:
        if (elementTypeSerializer != null) {
            staticTyping = false;
        } else if (!staticTyping) {
            staticTyping = usesStaticTyping(config, beanDesc, elementTypeSerializer, property);
        }
        JsonSerializer<Object> elementValueSerializer = findContentSerializer(config,
                beanDesc.getClassInfo(), property);
        
        if (type.isMapLikeType()) { // implements java.util.Map
            MapLikeType mlt = (MapLikeType) type;
            JsonSerializer<Object> keySerializer = findKeySerializer(config, beanDesc.getClassInfo(), property);
            if (mlt.isTrueMapType()) {
                return buildMapSerializer(config, (MapType) mlt, beanDesc, property, staticTyping,
                        keySerializer, elementTypeSerializer, elementValueSerializer);
            }
            return buildMapLikeSerializer(config, mlt, beanDesc, property, staticTyping,
                    keySerializer, elementTypeSerializer, elementValueSerializer);
        }
        if (type.isCollectionLikeType()) {
            CollectionLikeType clt = (CollectionLikeType) type;
            if (clt.isTrueCollectionType()) {
                return buildCollectionSerializer(config, (CollectionType) clt, beanDesc, property, staticTyping,
                        elementTypeSerializer, elementValueSerializer);
            }
            return buildCollectionLikeSerializer(config, clt, beanDesc, property, staticTyping,
                    elementTypeSerializer, elementValueSerializer);
        }
        if (type.isArrayType()) {
            return buildArraySerializer(config, (ArrayType) type, beanDesc, property, staticTyping,
                    elementTypeSerializer, elementValueSerializer);
        }
        return null;
    }
    
    /**
     * Helper method that handles configuration details when constructing serializers for
     * Collection and Collection-like types.
     * 
     * @since 1.8
     */
    protected JsonSerializer<?> buildCollectionLikeSerializer(SerializationConfig config,
            CollectionLikeType type,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) 
    {
        for (Serializers serializers : customSerializers()) {
            JsonSerializer<?> ser = serializers.findCollectionLikeSerializer(config, type, beanDesc, property,
                    elementTypeSerializer, elementValueSerializer);
            if (ser != null) {
                return ser;
            }
        }
        return null;
    }

    /**
     * Helper method that handles configuration details when constructing serializers for
     * {@link java.util.List} types that support efficient by-index access
     *<p> 
     * Note: signature changed in 1.8, to take 'staticTyping' argument
     */
    protected JsonSerializer<?> buildCollectionSerializer(SerializationConfig config,
            CollectionType type,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) 
    {
        // Module-provided custom collection serializers?
        for (Serializers serializers : customSerializers()) {
            JsonSerializer<?> ser = serializers.findCollectionSerializer(config, type, beanDesc, property,
                    elementTypeSerializer, elementValueSerializer);
            if (ser != null) {
                return ser;
            }
        }
        Class<?> raw = type.getRawClass();
        if (EnumSet.class.isAssignableFrom(raw)) {
            return buildEnumSetSerializer(config, type, beanDesc, property, staticTyping,
                    elementTypeSerializer, elementValueSerializer);
        }
        Class<?> elementRaw = type.getContentType().getRawClass();
        if (isIndexedList(raw)) {
            if (elementRaw == String.class) {
                return new IndexedStringListSerializer(property);
            }
            return StdContainerSerializers.indexedListSerializer(type.getContentType(), staticTyping,
                    elementTypeSerializer, property, elementValueSerializer);
        }
        if (elementRaw == String.class) {
            return new StringCollectionSerializer(property);
        }
        return StdContainerSerializers.collectionSerializer(type.getContentType(), staticTyping,
                elementTypeSerializer, property, elementValueSerializer);
    }

    protected JsonSerializer<?> buildEnumSetSerializer(SerializationConfig config, JavaType type,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) 
    {
        // this may or may not be available (Class doesn't; type of field/method does)
        JavaType enumType = type.getContentType();
        // and even if nominally there is something, only use if it really is enum
        if (!enumType.isEnumType()) {
            enumType = null;
        }
        return StdContainerSerializers.enumSetSerializer(enumType, property);
    }
    
    /**
     * @since 1.8
     */
    protected boolean isIndexedList(Class<?> cls)
    {
        return RandomAccess.class.isAssignableFrom(cls);
    }
    
    /*
    /**********************************************************
    /* Factory methods, for Maps
    /**********************************************************
     */
    
    /**
     * Helper method that handles configuration details when constructing serializers for
     * all "Map-like" types; both ones that implement {@link java.util.Map} and
     * ones that do not (but that have been indicated to behave like Maps).
     * 
     * @since 1.8
     */
    protected JsonSerializer<?> buildMapLikeSerializer(SerializationConfig config, MapLikeType type,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping,
            JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
    {
        for (Serializers serializers : customSerializers()) {
            JsonSerializer<?> ser = serializers.findMapLikeSerializer(config, type, beanDesc, property,
                    keySerializer, elementTypeSerializer, elementValueSerializer);
            if (ser != null) {
                return ser;
            }
        }
        return null;
    }
    
    /**
     * Helper method that handles configuration details when constructing serializers for
     * {@link java.util.Map} types.
     *<p> 
     * Note: signature changed in 1.8, to take 'staticTyping' argument
     */
    protected JsonSerializer<?> buildMapSerializer(SerializationConfig config, MapType type,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping,
            JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
    {
        for (Serializers serializers : customSerializers()) {
            JsonSerializer<?> ser = serializers.findMapSerializer(config, type, beanDesc, property,
                    keySerializer, elementTypeSerializer, elementValueSerializer);
            if (ser != null) {
                return ser;
            }
        }
        if (EnumMap.class.isAssignableFrom(type.getRawClass())) {
            return buildEnumMapSerializer(config, type, beanDesc, property, staticTyping,
                    elementTypeSerializer, elementValueSerializer);
        }
        return MapSerializer.construct(config.getAnnotationIntrospector().findPropertiesToIgnore(beanDesc.getClassInfo()),
                type, staticTyping, elementTypeSerializer, property,
                keySerializer, elementValueSerializer);
    }
    
    /**
     * Helper method that handles configuration details when constructing serializers for
     * {@link java.util.EnumMap} types.
     *<p> 
     * Note: signature changed in 1.8, to take 'staticTyping' argument
     */
    protected JsonSerializer<?> buildEnumMapSerializer(SerializationConfig config, JavaType type,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) 
    {
        JavaType keyType = type.getKeyType();
        // Need to find key enum values...
        EnumValues enums = null;
        if (keyType.isEnumType()) { // non-enum if we got it as type erased class (from instance)
            @SuppressWarnings("unchecked")
            Class<Enum<?>> enumClass = (Class<Enum<?>>) keyType.getRawClass();
            enums = EnumValues.construct(enumClass, config.getAnnotationIntrospector());
        }
        return new EnumMapSerializer(type.getContentType(), staticTyping, enums,
            elementTypeSerializer, property, elementValueSerializer);
    }

    /*
    /**********************************************************
    /* Factory methods, for Arrays
    /**********************************************************
     */
    
    /**
     * Helper method that handles configuration details when constructing serializers for
     * <code>Object[]</code> (and subtypes, except for String).
     */
    protected JsonSerializer<?> buildArraySerializer(SerializationConfig config, ArrayType type,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
    {
        Class<?> raw = type.getRawClass();
        if (String[].class == raw) {
            return new StdArraySerializers.StringArraySerializer(property);
        }
        // other standard types?
        JsonSerializer<?> ser = _arraySerializers.get(raw.getName());
        if (ser != null) {
            return ser;
        }
        return new ObjectArraySerializer(type.getContentType(), staticTyping, elementTypeSerializer,
                property, elementValueSerializer);
    }

    /*
    /**********************************************************
    /* Factory methods, for non-container types
    /**********************************************************
     */

    protected JsonSerializer<?> buildIteratorSerializer(SerializationConfig config, JavaType type,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping)
    {
        // if there's generic type, it'll be the first contained type
        JavaType valueType = type.containedType(0);
        if (valueType == null) {
            valueType = TypeFactory.unknownType();
        }
        TypeSerializer vts = createTypeSerializer(config, valueType, property);
        return StdContainerSerializers.iteratorSerializer(valueType,
                usesStaticTyping(config, beanDesc, vts, property), vts, property);
    }
    
    protected JsonSerializer<?> buildIterableSerializer(SerializationConfig config, JavaType type,
            BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping)
    {
        // if there's generic type, it'll be the first contained type
        JavaType valueType = type.containedType(0);
        if (valueType == null) {
            valueType = TypeFactory.unknownType();
        }
        TypeSerializer vts = createTypeSerializer(config, valueType, property);
        return StdContainerSerializers.iterableSerializer(valueType,
                usesStaticTyping(config, beanDesc, vts, property), vts, property);
    }
    
    /*
    /**********************************************************
    /* Other helper methods
    /**********************************************************
     */
    
    /**
     * Helper method used to encapsulate details of annotation-based type
     * coercion
     * 
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    protected <T extends JavaType> T modifyTypeByAnnotation(SerializationConfig config, Annotated a, T type)
    {
        // first: let's check class for the instance itself:
        Class<?> superclass = config.getAnnotationIntrospector().findSerializationType(a);
        if (superclass != null) {
            try {
                type = (T) type.widenBy(superclass);
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("Failed to widen type "+type+" with concrete-type annotation (value "+superclass.getName()+"), method '"+a.getName()+"': "+iae.getMessage());
            }
        }
        return modifySecondaryTypesByAnnotation(config, a, type);
    }

    /**
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    protected static <T extends JavaType> T modifySecondaryTypesByAnnotation(SerializationConfig config, Annotated a, T type)
    {
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        // then key class
        if (type.isContainerType()) {
            Class<?> keyClass = intr.findSerializationKeyType(a, type.getKeyType());
            if (keyClass != null) {
                // illegal to use on non-Maps
                if (!(type instanceof MapType)) {
                    throw new IllegalArgumentException("Illegal key-type annotation: type "+type+" is not a Map type");
                }
                try {
                    type = (T) ((MapType) type).widenKey(keyClass);
                } catch (IllegalArgumentException iae) {
                    throw new IllegalArgumentException("Failed to narrow key type "+type+" with key-type annotation ("+keyClass.getName()+"): "+iae.getMessage());
                }
            }
            
            // and finally content class; only applicable to structured types
            Class<?> cc = intr.findSerializationContentType(a, type.getContentType());
            if (cc != null) {
                try {
                    type = (T) type.widenContentsBy(cc);
                } catch (IllegalArgumentException iae) {
                    throw new IllegalArgumentException("Failed to narrow content type "+type+" with content-type annotation ("+cc.getName()+"): "+iae.getMessage());
                }
            }
        }
        return type;
    }

    protected static JsonSerializer<Object> findKeySerializer(SerializationConfig config,
            Annotated a, BeanProperty property)
    {
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        Class<? extends JsonSerializer<?>> serClass = intr.findKeySerializer(a);
        if (serClass == null || serClass == JsonSerializer.None.class) {
            if (property != null) {
                serClass = intr.findKeySerializer(property.getMember());
            }
        }
        if (serClass != null && serClass != JsonSerializer.None.class) {
            return config.serializerInstance(a, serClass);
        }
        return null;
    }

    protected static JsonSerializer<Object> findContentSerializer(SerializationConfig config,
            Annotated a, BeanProperty property)
    {
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        Class<? extends JsonSerializer<?>> serClass = intr.findContentSerializer(a);
        if (serClass == null || serClass == JsonSerializer.None.class) {
            if (property != null) {
                serClass = intr.findContentSerializer(property.getMember());
            }
        }
        if (serClass != null && serClass != JsonSerializer.None.class) {
            return config.serializerInstance(a, serClass);
        }
        return null;
    }
    
    /**
     * Helper method to check whether global settings and/or class
     * annotations for the bean class indicate that static typing
     * (declared types)  should be used for properties.
     * (instead of dynamic runtime types).
     */
    protected boolean usesStaticTyping(SerializationConfig config, BasicBeanDescription beanDesc,
            TypeSerializer typeSer, BeanProperty property)
    {
        /* 16-Aug-2010, tatu: If there is a (value) type serializer, we can not force
         *    static typing; that would make it impossible to handle expected subtypes
         */
        if (typeSer != null) {
            return false;
        }
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        JsonSerialize.Typing t = intr.findSerializationTyping(beanDesc.getClassInfo());
        if (t != null) {
            if (t == JsonSerialize.Typing.STATIC) {
                return true;
            }
        } else {
            if (config.isEnabled(SerializationConfig.Feature.USE_STATIC_TYPING)) {
                return true;
            }
        }
        /* 11-Mar-2011, tatu: Ok. This is bit hacky, but we really need to be able to find cases
         *    where key and/or value serializers were specified, to force use of static typing
         */
        if (property != null) {
            JavaType type = property.getType();
            if (type.isContainerType()) {
                if (intr.findSerializationContentType(property.getMember(), property.getType()) != null) {
                    return true;
                }
                if (type instanceof MapType) {
                    if (intr.findSerializationKeyType(property.getMember(), property.getType()) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
