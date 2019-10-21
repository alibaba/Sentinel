package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.AtomicReferenceDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.CollectionDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.EnumDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.EnumMapDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.EnumSetDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.JsonNodeDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.MapDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.ObjectArrayDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.PrimitiveArrayDeserializers;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StdKeyDeserializers;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StringCollectionDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ext.OptionalHandlerFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.NamedType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.EnumResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Abstract factory base class that can provide deserializers for standard
 * JDK classes, including collection classes and simple heuristics for
 * "upcasting" commmon collection interface types
 * (such as {@link java.util.Collection}).
 *<p>
 * Since all simple deserializers are eagerly instantiated, and there is
 * no additional introspection or customizability of these types,
 * this factory is stateless.
 */
public abstract class BasicDeserializerFactory
    extends DeserializerFactory
{
    /**
     * We will pre-create serializers for common non-structured
     * (that is things other than Collection, Map or array)
     * types. These need not go through factory.
     */
    final static HashMap<ClassKey, JsonDeserializer<Object>> _simpleDeserializers = StdDeserializers.constructAll();

    /**
     * Set of available key deserializers is currently limited
     * to standard types; and all known instances are storing
     * in this map.
     */
    final static HashMap<JavaType, KeyDeserializer> _keyDeserializers = StdKeyDeserializers.constructAll();
    
    /* We do some defaulting for abstract Map classes and
     * interfaces, to avoid having to use exact types or annotations in
     * cases where the most common concrete Maps will do.
     */
    @SuppressWarnings("rawtypes")
    final static HashMap<String, Class<? extends Map>> _mapFallbacks =
        new HashMap<String, Class<? extends Map>>();
    static {
        _mapFallbacks.put(Map.class.getName(), LinkedHashMap.class);
        _mapFallbacks.put(ConcurrentMap.class.getName(), ConcurrentHashMap.class);
        _mapFallbacks.put(SortedMap.class.getName(), TreeMap.class);

        /* 11-Jan-2009, tatu: Let's see if we can still add support for
         *    JDK 1.6 interfaces, even if we run on 1.5. Just need to be
         *    more careful with typos, since compiler won't notice any
         *    problems...
         */
        _mapFallbacks.put("java.util.NavigableMap", TreeMap.class);
        try {
            Class<?> key = Class.forName("java.util.ConcurrentNavigableMap");
            Class<?> value = Class.forName("java.util.ConcurrentSkipListMap");
            @SuppressWarnings("unchecked")
                Class<? extends Map<?,?>> mapValue = (Class<? extends Map<?,?>>) value;
            _mapFallbacks.put(key.getName(), mapValue);
        } catch (ClassNotFoundException cnfe) { // occurs on 1.5
        }
    }

    /* We do some defaulting for abstract Collection classes and
     * interfaces, to avoid having to use exact types or annotations in
     * cases where the most common concrete Collection will do.
     */
    @SuppressWarnings("rawtypes")
    final static HashMap<String, Class<? extends Collection>> _collectionFallbacks =
        new HashMap<String, Class<? extends Collection>>();
    static {
        _collectionFallbacks.put(Collection.class.getName(), ArrayList.class);
        _collectionFallbacks.put(List.class.getName(), ArrayList.class);
        _collectionFallbacks.put(Set.class.getName(), HashSet.class);
        _collectionFallbacks.put(SortedSet.class.getName(), TreeSet.class);
        _collectionFallbacks.put(Queue.class.getName(), LinkedList.class);

        /* 11-Jan-2009, tatu: Let's see if we can still add support for
         *    JDK 1.6 interfaces, even if we run on 1.5. Just need to be
         *    more careful with typos, since compiler won't notice any
         *    problems...
         */
        _collectionFallbacks.put("java.util.Deque", LinkedList.class);
        _collectionFallbacks.put("java.util.NavigableSet", TreeSet.class);
    }

    /**
     * And finally, we have special array deserializers for primitive
     * array types
     */
    protected final static HashMap<JavaType,JsonDeserializer<Object>> _arrayDeserializers
        = PrimitiveArrayDeserializers.getAll();

    /**
     * To support external/optional deserializers, we'll use this helper class
     * (as per [JACKSON-386])
     */
    protected OptionalHandlerFactory optionalHandlers = OptionalHandlerFactory.instance;
    
    /*
    /**********************************************************
    /* Life cycle
    /**********************************************************
     */

    protected BasicDeserializerFactory() { }

    // can't be implemented quite here
    @Override
    public abstract DeserializerFactory withConfig(DeserializerFactory.Config config);
    
    /*
    /**********************************************************
    /* Methods for sub-classes to override to provide
    /* custom deserializers (since 1.7)
    /**********************************************************
     */
    
    protected abstract JsonDeserializer<?> _findCustomArrayDeserializer(ArrayType type, DeserializationConfig config,
            DeserializerProvider p, BeanProperty property,
            TypeDeserializer elementTypeDeser, JsonDeserializer<?> elementDeser)
        throws JsonMappingException;
    
    protected abstract JsonDeserializer<?> _findCustomCollectionDeserializer(
            CollectionType type, DeserializationConfig config,
            DeserializerProvider p, BasicBeanDescription beanDesc, BeanProperty property,
            TypeDeserializer elementTypeDeser, JsonDeserializer<?> elementDeser)
        throws JsonMappingException;

    protected abstract JsonDeserializer<?> _findCustomCollectionLikeDeserializer(
            CollectionLikeType type, DeserializationConfig config,
            DeserializerProvider p, BasicBeanDescription beanDesc, BeanProperty property,
            TypeDeserializer elementTypeDeser, JsonDeserializer<?> elementDeser)
        throws JsonMappingException;
    
    protected abstract JsonDeserializer<?> _findCustomEnumDeserializer(Class<?> type,
            DeserializationConfig config, BasicBeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException;

    protected abstract JsonDeserializer<?> _findCustomMapDeserializer(MapType type,
            DeserializationConfig config,
            DeserializerProvider p, BasicBeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeser,
            TypeDeserializer elementTypeDeser, JsonDeserializer<?> elementDeser)
        throws JsonMappingException;

    protected abstract JsonDeserializer<?> _findCustomMapLikeDeserializer(MapLikeType type,
            DeserializationConfig config,
            DeserializerProvider p, BasicBeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeser,
            TypeDeserializer elementTypeDeser, JsonDeserializer<?> elementDeser)
        throws JsonMappingException;
    
    protected abstract JsonDeserializer<?> _findCustomTreeNodeDeserializer(Class<? extends JsonNode> type,
            DeserializationConfig config, BeanProperty property)
        throws JsonMappingException;
    
    /*
    /**********************************************************
    /* JsonDeserializerFactory impl (partial)
    /**********************************************************
     */

    @Override
    public abstract ValueInstantiator findValueInstantiator(DeserializationConfig config,
            BasicBeanDescription beanDesc)
        throws JsonMappingException;

    @Override
    public abstract JavaType mapAbstractType(DeserializationConfig config, JavaType type)
            throws JsonMappingException;
    
    @Override
    public JsonDeserializer<?> createArrayDeserializer(DeserializationConfig config, DeserializerProvider p,
            ArrayType type, BeanProperty property)
        throws JsonMappingException
    {
        JavaType elemType = type.getContentType();
        
        // Very first thing: is deserializer hard-coded for elements?
        JsonDeserializer<Object> contentDeser = elemType.getValueHandler();
        if (contentDeser == null) {
            // Maybe special array type, such as "primitive" arrays (int[] etc)
            JsonDeserializer<?> deser = _arrayDeserializers.get(elemType);
            if (deser != null) {
                /* 23-Nov-2010, tatu: Although not commonly needed, ability to override
                 *   deserializers for all types (including primitive arrays) is useful
                 *   so let's allow this
                 */
                JsonDeserializer<?> custom = _findCustomArrayDeserializer(type, config, p, property, null, null);
                if (custom != null) {
                    return custom;
                }
                return deser;
            }
            // If not, generic one:
            if (elemType.isPrimitive()) { // sanity check
                throw new IllegalArgumentException("Internal error: primitive type ("+type+") passed, no array deserializer found");
            }
        }
        // Then optional type info (1.5): if type has been resolved, we may already know type deserializer:
        TypeDeserializer elemTypeDeser = elemType.getTypeHandler();
        // but if not, may still be possible to find:
        if (elemTypeDeser == null) {
            elemTypeDeser = findTypeDeserializer(config, elemType, property);
        }
        // 23-Nov-2010, tatu: Custom array deserializer?
        JsonDeserializer<?> custom = _findCustomArrayDeserializer(type, config, p, property, elemTypeDeser, contentDeser);
        if (custom != null) {
            return custom;
        }
        
        if (contentDeser == null) {
            // 'null' -> arrays have no referring fields
            contentDeser = p.findValueDeserializer(config, elemType, property);
        }
        return new ObjectArrayDeserializer(type, contentDeser, elemTypeDeser);
    }
    
    @Override
    public JsonDeserializer<?> createCollectionDeserializer(DeserializationConfig config, DeserializerProvider p,
            CollectionType type, BeanProperty property)
        throws JsonMappingException
    {
        // First: global defaulting:
        type = (CollectionType) mapAbstractType(config, type);

        Class<?> collectionClass = type.getRawClass();
        BasicBeanDescription beanDesc = config.introspectForCreation(type);
        // Explicit deserializer to use? (@JsonDeserialize.using)
        JsonDeserializer<Object> deser = findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (deser != null) {
            return deser;
        }
        // If not, any type modifiers? (@JsonDeserialize.as)
        type = modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, null);

        JavaType contentType = type.getContentType();
        // Very first thing: is deserializer hard-coded for elements?
        JsonDeserializer<Object> contentDeser = contentType.getValueHandler();

        // Then optional type info (1.5): if type has been resolved, we may already know type deserializer:
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        // but if not, may still be possible to find:
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(config, contentType, property);
        }

        // 23-Nov-2010, tatu: Custom deserializer?
        JsonDeserializer<?> custom = _findCustomCollectionDeserializer(type, config, p, beanDesc, property,
                contentTypeDeser, contentDeser);
        if (custom != null) {
            return custom;
        }
        
        if (contentDeser == null) { // not defined by annotation
            // One special type: EnumSet:
            if (EnumSet.class.isAssignableFrom(collectionClass)) {
                return new EnumSetDeserializer(contentType.getRawClass(),
                        createEnumDeserializer(config, p, contentType, property));
            }
            // But otherwise we can just use a generic value deserializer:
            // 'null' -> collections have no referring fields
            contentDeser = p.findValueDeserializer(config, contentType, property);            
        }
        
        /* One twist: if we are being asked to instantiate an interface or
         * abstract Collection, we need to either find something that implements
         * the thing, or give up.
         *
         * Note that we do NOT try to guess based on secondary interfaces
         * here; that would probably not work correctly since casts would
         * fail later on (as the primary type is not the interface we'd
         * be implementing)
         */
        if (type.isInterface() || type.isAbstract()) {
            @SuppressWarnings({ "rawtypes" })
            Class<? extends Collection> fallback = _collectionFallbacks.get(collectionClass.getName());
            if (fallback == null) {
                throw new IllegalArgumentException("Can not find a deserializer for non-concrete Collection type "+type);
            }
            collectionClass = fallback;
            type = (CollectionType) config.constructSpecializedType(type, collectionClass);
            // But if so, also need to re-check creators...
            beanDesc = config.introspectForCreation(type);
        }
        ValueInstantiator inst = findValueInstantiator(config, beanDesc);
        // 13-Dec-2010, tatu: Can use more optimal deserializer if content type is String, so:
        if (contentType.getRawClass() == String.class) {
            // no value type deserializer because Strings are one of natural/native types:
            return new StringCollectionDeserializer(type, contentDeser, inst);
        }
        return new CollectionDeserializer(type, contentDeser, contentTypeDeser, inst);
    }

    // Copied almost verbatim from "createCollectionDeserializer" -- should try to share more code
    @Override
    public JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationConfig config,
            DeserializerProvider p, CollectionLikeType type, BeanProperty property)
        throws JsonMappingException
    {
        // First: global defaulting:
        type = (CollectionLikeType) mapAbstractType(config, type);

        Class<?> collectionClass = type.getRawClass();
        BasicBeanDescription beanDesc = config.introspectClassAnnotations(collectionClass);
        // Explicit deserializer to use? (@JsonDeserialize.using)
        JsonDeserializer<Object> deser = findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (deser != null) {
            return deser;
        }
        // If not, any type modifiers? (@JsonDeserialize.as)
        type = modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, null);

        JavaType contentType = type.getContentType();
        // Very first thing: is deserializer hard-coded for elements?
        JsonDeserializer<Object> contentDeser = contentType.getValueHandler();

        // Then optional type info (1.5): if type has been resolved, we may already know type deserializer:
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        // but if not, may still be possible to find:
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(config, contentType, property);
        }
        return _findCustomCollectionLikeDeserializer(type, config, p, beanDesc, property,
                contentTypeDeser, contentDeser);
    }
    
    @Override
    public JsonDeserializer<?> createMapDeserializer(DeserializationConfig config, DeserializerProvider p,
            MapType type, BeanProperty property)
        throws JsonMappingException
    {
        // First: global defaulting:
        type = (MapType) mapAbstractType(config, type);

        BasicBeanDescription beanDesc = config.introspectForCreation(type);
        // Explicit deserializer to use? (@JsonDeserialize.using)
        JsonDeserializer<Object> deser = findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (deser != null) {
            return deser;
        }
        // If not, any type modifiers? (@JsonDeserialize.as)
        type = modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, null);        
        JavaType keyType = type.getKeyType();
        JavaType contentType = type.getContentType();
        
        // First: is there annotation-specified deserializer for values?
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> contentDeser = (JsonDeserializer<Object>) contentType.getValueHandler();
        
        // Ok: need a key deserializer (null indicates 'default' here)
        KeyDeserializer keyDes = (KeyDeserializer) keyType.getValueHandler();
        if (keyDes == null) {
            keyDes = p.findKeyDeserializer(config, keyType, property);
        }
        // Then optional type info (1.5); either attached to type, or resolve separately:
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        // but if not, may still be possible to find:
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(config, contentType, property);
        }

        // 23-Nov-2010, tatu: Custom deserializer?
        JsonDeserializer<?> custom = _findCustomMapDeserializer(type, config, p, beanDesc, property,
                keyDes, contentTypeDeser, contentDeser);

        if (custom != null) {
            return custom;
        }

        if (contentDeser == null) { // nope...
            // 'null' -> maps have no referring fields
            contentDeser = p.findValueDeserializer(config, contentType, property);
        }
        /* Value handling is identical for all,
         * but EnumMap requires special handling for keys
         */
        Class<?> mapClass = type.getRawClass();
        if (EnumMap.class.isAssignableFrom(mapClass)) {
            Class<?> kt = keyType.getRawClass();
            if (kt == null || !kt.isEnum()) {
                throw new IllegalArgumentException("Can not construct EnumMap; generic (key) type not available");
            }
            return new EnumMapDeserializer(keyType.getRawClass(),
                    createEnumDeserializer(config, p, keyType, property),
                    contentDeser);
        }

        // Otherwise, generic handler works ok.

        /* But there is one more twist: if we are being asked to instantiate
         * an interface or abstract Map, we need to either find something
         * that implements the thing, or give up.
         *
         * Note that we do NOT try to guess based on secondary interfaces
         * here; that would probably not work correctly since casts would
         * fail later on (as the primary type is not the interface we'd
         * be implementing)
         */
        if (type.isInterface() || type.isAbstract()) {
            @SuppressWarnings("rawtypes")
            Class<? extends Map> fallback = _mapFallbacks.get(mapClass.getName());
            if (fallback == null) {
                throw new IllegalArgumentException("Can not find a deserializer for non-concrete Map type "+type);
            }
            mapClass = fallback;
            type = (MapType) config.constructSpecializedType(type, mapClass);
            // But if so, also need to re-check creators...
            beanDesc = config.introspectForCreation(type);
        }
        ValueInstantiator inst = findValueInstantiator(config, beanDesc);
        MapDeserializer md = new MapDeserializer(type, inst, keyDes, contentDeser, contentTypeDeser);
        md.setIgnorableProperties(config.getAnnotationIntrospector().findPropertiesToIgnore(beanDesc.getClassInfo()));
        return md;
    }

    // Copied almost verbatim from "createMapDeserializer" -- should try to share more code
    @Override
    public JsonDeserializer<?> createMapLikeDeserializer(DeserializationConfig config,
            DeserializerProvider p, MapLikeType type, BeanProperty property)
        throws JsonMappingException
    {
        // First: global defaulting:
        type = (MapLikeType) mapAbstractType(config, type);
        BasicBeanDescription beanDesc = config.introspectForCreation(type);
        // Explicit deserializer to use? (@JsonDeserialize.using)
        JsonDeserializer<Object> deser = findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (deser != null) {
            return deser;
        }
        // If not, any type modifiers? (@JsonDeserialize.as)
        type = modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, null);        
        JavaType keyType = type.getKeyType();
        JavaType contentType = type.getContentType();
        
        // First: is there annotation-specified deserializer for values?
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> contentDeser = (JsonDeserializer<Object>) contentType.getValueHandler();
        
        // Ok: need a key deserializer (null indicates 'default' here)
        KeyDeserializer keyDes = (KeyDeserializer) keyType.getValueHandler();
        if (keyDes == null) {
            keyDes = p.findKeyDeserializer(config, keyType, property);
        }
        // Then optional type info (1.5); either attached to type, or resolve separately:
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        // but if not, may still be possible to find:
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(config, contentType, property);
        }
        return _findCustomMapLikeDeserializer(type, config, p, beanDesc, property,
                keyDes, contentTypeDeser, contentDeser);
    }
    
    /**
     * Factory method for constructing serializers of {@link Enum} types.
     */
    @Override
    public JsonDeserializer<?> createEnumDeserializer(DeserializationConfig config, DeserializerProvider p,
            JavaType type, BeanProperty property)
        throws JsonMappingException
    {
        /* 18-Feb-2009, tatu: Must first check if we have a class annotation
         *    that should override default deserializer
         */
        BasicBeanDescription beanDesc = config.introspectForCreation(type);
        JsonDeserializer<?> des = findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (des != null) {
            return des;
        }
        Class<?> enumClass = type.getRawClass();
        // 23-Nov-2010, tatu: Custom deserializer?
        JsonDeserializer<?> custom = _findCustomEnumDeserializer(enumClass, config, beanDesc, property);
        if (custom != null) {
            return custom;
        }

        // [JACKSON-193] May have @JsonCreator for static factory method:
        for (AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            if (config.getAnnotationIntrospector().hasCreatorAnnotation(factory)) {
                int argCount = factory.getParameterCount();
                if (argCount == 1) {
                    Class<?> returnType = factory.getRawType();
                    // usually should be class, but may be just plain Enum<?> (for Enum.valueOf()?)
                    if (returnType.isAssignableFrom(enumClass)) {
                        return EnumDeserializer.deserializerForCreator(config, enumClass, factory);
                    }
                }
                throw new IllegalArgumentException("Unsuitable method ("+factory+") decorated with @JsonCreator (for Enum type "
                        +enumClass.getName()+")");
            }
        }
        return new EnumDeserializer(constructEnumResolver(enumClass, config));
    }

    @Override
    public JsonDeserializer<?> createTreeDeserializer(DeserializationConfig config, DeserializerProvider p,
            JavaType nodeType, BeanProperty property)
        throws JsonMappingException
    {
        @SuppressWarnings("unchecked")
        Class<? extends JsonNode> nodeClass = (Class<? extends JsonNode>) nodeType.getRawClass();
        // 23-Nov-2010, tatu: Custom deserializer?
        JsonDeserializer<?> custom = _findCustomTreeNodeDeserializer(nodeClass, config, property);
        if (custom != null) {
            return custom;
        }
        return JsonNodeDeserializer.getDeserializer(nodeClass);
    }

    /**
     * Method called by {@link BeanDeserializerFactory} to see if there might be a standard
     * deserializer registered for given type.
     * 
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    protected JsonDeserializer<Object> findStdBeanDeserializer(DeserializationConfig config,
            DeserializerProvider p, JavaType type, BeanProperty property)
        throws JsonMappingException
    {
        Class<?> cls = type.getRawClass();
        // note: we do NOT check for custom deserializers here; that's for sub-class to do
        JsonDeserializer<Object> deser = _simpleDeserializers.get(new ClassKey(cls));
        if (deser != null) {
            return deser;
        }
        
        // [JACKSON-283]: AtomicReference is a rather special type...
        if (AtomicReference.class.isAssignableFrom(cls)) {
            // Must find parameterization
            TypeFactory tf = config.getTypeFactory();
            JavaType[] params = tf.findTypeParameters(type, AtomicReference.class);
            JavaType referencedType;
            if (params == null || params.length < 1) { // untyped (raw)
                referencedType = TypeFactory.unknownType();
            } else {
                referencedType = params[0];
            }
            
            JsonDeserializer<?> d2 = new AtomicReferenceDeserializer(referencedType, property);
            return (JsonDeserializer<Object>)d2;
        }
        // [JACKSON-386]: External/optional type handlers are handled somewhat differently
        JsonDeserializer<?> d = optionalHandlers.findDeserializer(type, config, p);
        if (d != null) {
            return (JsonDeserializer<Object>)d;
        }
        return null;
    }
    
    @Override
    public TypeDeserializer findTypeDeserializer(DeserializationConfig config, JavaType baseType,
            BeanProperty property)
        throws JsonMappingException
    {
        Class<?> cls = baseType.getRawClass();
        BasicBeanDescription bean = config.introspectClassAnnotations(cls);
        AnnotatedClass ac = bean.getClassInfo();
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);

        /* Ok: if there is no explicit type info handler, we may want to
         * use a default. If so, config object knows what to use.
         */
        Collection<NamedType> subtypes = null;
        if (b == null) {
            b = config.getDefaultTyper(baseType);
            if (b == null) {
                return null;
            }
        } else {
            subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(ac, config, ai);
        }
        // [JACKSON-505]: May need to figure out default implementation, if none found yet
        // (note: check for abstract type is not 100% mandatory, more of an optimization)
        if ((b.getDefaultImpl() == null) && baseType.isAbstract()) {
            JavaType defaultType = mapAbstractType(config, baseType);
            if (defaultType != null && defaultType.getRawClass() != baseType.getRawClass()) {
                b = b.defaultImpl(defaultType.getRawClass());
            }
        }
        return b.buildTypeDeserializer(config, baseType, subtypes, property);
    }
    
    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    /**
     * Method called to create a type information deserializer for values of
     * given non-container property, if one is needed.
     * If not needed (no polymorphic handling configured for property), should return null.
     *<p>
     * Note that this method is only called for non-container bean properties,
     * and not for values in container types or root values (or container properties)
     *
     * @param baseType Declared base type of the value to deserializer (actual
     *    deserializer type will be this type or its subtype)
     * 
     * @return Type deserializer to use for given base type, if one is needed; null if not.
     * 
     * @since 1.5
     */
    public TypeDeserializer findPropertyTypeDeserializer(DeserializationConfig config, JavaType baseType,
           AnnotatedMember annotated, BeanProperty property)
        throws JsonMappingException
    {
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findPropertyTypeResolver(config, annotated, baseType);        
        // Defaulting: if no annotations on member, check value class
        if (b == null) {
            return findTypeDeserializer(config, baseType, property);
        }
        // but if annotations found, may need to resolve subtypes:
        Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(annotated, config, ai);
        return b.buildTypeDeserializer(config, baseType, subtypes, property);
    }
    
    /**
     * Method called to find and create a type information deserializer for values of
     * given container (list, array, map) property, if one is needed.
     * If not needed (no polymorphic handling configured for property), should return null.
     *<p>
     * Note that this method is only called for container bean properties,
     * and not for values in container types or root values (or non-container properties)
     * 
     * @param containerType Type of property; must be a container type
     * @param propertyEntity Field or method that contains container property
     * 
     * @since 1.5
     */    
    public TypeDeserializer findPropertyContentTypeDeserializer(DeserializationConfig config, JavaType containerType,
            AnnotatedMember propertyEntity, BeanProperty property)
        throws JsonMappingException
    {
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findPropertyContentTypeResolver(config, propertyEntity, containerType);        
        JavaType contentType = containerType.getContentType();
        // Defaulting: if no annotations on member, check class
        if (b == null) {
            return findTypeDeserializer(config, contentType, property);
        }
        // but if annotations found, may need to resolve subtypes:
        Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(propertyEntity, config, ai);
        return b.buildTypeDeserializer(config, contentType, subtypes, property);
    }
    
    /*
    /**********************************************************
    /* Helper methods, value/content/key type introspection
    /**********************************************************
     */
    
    /**
     * Helper method called to check if a class or method
     * has annotation that tells which class to use for deserialization.
     * Returns null if no such annotation found.
     */
    protected JsonDeserializer<Object> findDeserializerFromAnnotation(DeserializationConfig config,
            Annotated ann, BeanProperty property)
        throws JsonMappingException
    {
        Object deserDef = config.getAnnotationIntrospector().findDeserializer(ann);
        if (deserDef != null) {
            return _constructDeserializer(config, ann, property, deserDef);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    JsonDeserializer<Object> _constructDeserializer(DeserializationConfig config, Annotated ann, BeanProperty property,
            Object deserDef)
        throws JsonMappingException
    {
        if (deserDef instanceof JsonDeserializer) {
            JsonDeserializer<Object> deser = (JsonDeserializer<Object>) deserDef;
            // related to [JACKSON-569], need contextualization:
            if (deser instanceof ContextualDeserializer<?>) {
                deser = (JsonDeserializer<Object>)((ContextualDeserializer<?>) deser).createContextual(config, property);
            }
            return deser;
        }
        /* Alas, there's no way to force return type of "either class
         * X or Y" -- need to throw an exception after the fact
         */
        if (!(deserDef instanceof Class)) {
            throw new IllegalStateException("AnnotationIntrospector returned deserializer definition of type "+deserDef.getClass().getName()+"; expected type JsonDeserializer or Class<JsonDeserializer> instead");
        }
        Class<? extends JsonDeserializer<?>> deserClass = (Class<? extends JsonDeserializer<?>>) deserDef;
        if (!JsonDeserializer.class.isAssignableFrom(deserClass)) {
            throw new IllegalStateException("AnnotationIntrospector returned Class "+deserClass.getName()+"; expected Class<JsonDeserializer>");
        }
        JsonDeserializer<Object> deser = config.deserializerInstance(ann, deserClass);
        // related to [JACKSON-569], need contextualization:
        if (deser instanceof ContextualDeserializer<?>) {
            deser = (JsonDeserializer<Object>)((ContextualDeserializer<?>) deser).createContextual(config, property);
        }
        return deser;
    }

    /**
     * Method called to see if given method has annotations that indicate
     * a more specific type than what the argument specifies.
     * If annotations are present, they must specify compatible Class;
     * instance of which can be assigned using the method. This means
     * that the Class has to be raw class of type, or its sub-class
     * (or, implementing class if original Class instance is an interface).
     *
     * @param a Method or field that the type is associated with
     * @param type Type derived from the setter argument
     * @param propName Name of property that refers to type, if any; null
     *   if no property information available (when modify type declaration
     *   of a class, for example)
     *
     * @return Original type if no annotations are present; or a more
     *   specific type derived from it if type annotation(s) was found
     *
     * @throws JsonMappingException if invalid annotation is found
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    protected <T extends JavaType> T modifyTypeByAnnotation(DeserializationConfig config,
            Annotated a, T type, String propName)
        throws JsonMappingException
    {
        // first: let's check class for the instance itself:
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        Class<?> subclass = intr.findDeserializationType(a, type, propName);
        if (subclass != null) {
            try {
                type = (T) type.narrowBy(subclass);
            } catch (IllegalArgumentException iae) {
                throw new JsonMappingException("Failed to narrow type "+type+" with concrete-type annotation (value "+subclass.getName()+"), method '"+a.getName()+"': "+iae.getMessage(), null, iae);
            }
        }

        // then key class
        if (type.isContainerType()) {
            Class<?> keyClass = intr.findDeserializationKeyType(a, type.getKeyType(), propName);
            if (keyClass != null) {
                // illegal to use on non-Maps
                if (!(type instanceof MapLikeType)) {
                    throw new JsonMappingException("Illegal key-type annotation: type "+type+" is not a Map(-like) type");
                }
                try {
                    type = (T) ((MapLikeType) type).narrowKey(keyClass);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException("Failed to narrow key type "+type+" with key-type annotation ("+keyClass.getName()+"): "+iae.getMessage(), null, iae);
                }
            }
            JavaType keyType = type.getKeyType();
            /* 21-Mar-2011, tatu: ... and associated deserializer too (unless already assigned)
             *   (not 100% why or how, but this does seem to get called more than once, which
             *   is not good: for now, let's just avoid errors)
             */
            if (keyType != null && keyType.getValueHandler() == null) {
                Class<? extends KeyDeserializer> kdClass = intr.findKeyDeserializer(a);
                if (kdClass != null && kdClass != KeyDeserializer.None.class) {
                    KeyDeserializer kd = config.keyDeserializerInstance(a, kdClass);
                    // !!! TODO: For 2.0, change to use this instead:
                    /*
                    type = (T) ((MapLikeType) type).withKeyValueHandler(kd);
                    keyType = type.getKeyType(); // just in case it's used below
                    */
                    keyType.setValueHandler(kd);
                }
            }            
            
            // and finally content class; only applicable to structured types
            Class<?> cc = intr.findDeserializationContentType(a, type.getContentType(), propName);
            if (cc != null) {
                try {
                    type = (T) type.narrowContentsBy(cc);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException("Failed to narrow content type "+type+" with content-type annotation ("+cc.getName()+"): "+iae.getMessage(), null, iae);
                }
            }
            // ... as well as deserializer for contents:
            JavaType contentType = type.getContentType();
            if (contentType.getValueHandler() == null) { // as with above, avoid resetting (which would trigger exception)
                Class<? extends JsonDeserializer<?>> cdClass = intr.findContentDeserializer(a);
                if (cdClass != null && cdClass != JsonDeserializer.None.class) {
                    JsonDeserializer<Object> cd = config.deserializerInstance(a, cdClass);
                    // !!! TODO: For 2.0, change to use this instead:
                    /*
                    type = (T) type.withContentValueHandler(cd);
                    */
                    type.getContentType().setValueHandler(cd);
                }
            }
        }
        return type;
    }

    /**
     * Helper method used to resolve method return types and field
     * types. The main trick here is that the containing bean may
     * have type variable binding information (when deserializing
     * using generic type passed as type reference), which is
     * needed in some cases.
     *<p>
     * Starting with version 1.3, this method will also resolve instances
     * of key and content deserializers if defined by annotations.
     */
    @SuppressWarnings("deprecation")
    protected JavaType resolveType(DeserializationConfig config,
            BasicBeanDescription beanDesc, JavaType type, AnnotatedMember member,
            BeanProperty property)                    
        throws JsonMappingException
    {
        // [JACKSON-154]: Also need to handle keyUsing, contentUsing
        if (type.isContainerType()) {
            AnnotationIntrospector intr = config.getAnnotationIntrospector();
            JavaType keyType = type.getKeyType();
            if (keyType != null) {
                Class<? extends KeyDeserializer> kdClass = intr.findKeyDeserializer(member);
                if (kdClass != null && kdClass != KeyDeserializer.None.class) {
                    KeyDeserializer kd = config.keyDeserializerInstance(member, kdClass);
                    // !!! TODO: For 2.0, change to use this instead:
                    /*
                    type = ((MapLikeType) type).withKeyValueHandler(kd);
                    keyType = type.getKeyType(); // just in case it's used below
                    */
                    keyType.setValueHandler(kd);
                }
            }
            // and all container types have content types...
            Class<? extends JsonDeserializer<?>> cdClass = intr.findContentDeserializer(member);
            if (cdClass != null && cdClass != JsonDeserializer.None.class) {
                JsonDeserializer<Object> cd = config.deserializerInstance(member, cdClass);
                // !!! TODO: For 2.0, change to use this instead:
                /*
                type = type.withContentValueHandler(cd);
                */
                type.getContentType().setValueHandler(cd);
            }
            /* 04-Feb-2010, tatu: Need to figure out JAXB annotations that indicate type
             *    information to use for polymorphic members; and specifically types for
             *    collection values (contents).
             *    ... but only applies to members (fields, methods), not classes
             */
            if (member instanceof AnnotatedMember) {
            	TypeDeserializer contentTypeDeser = findPropertyContentTypeDeserializer(config, type,
            	       (AnnotatedMember) member, property);            	
            	if (contentTypeDeser != null) {
            	    type = type.withContentTypeHandler(contentTypeDeser);
            	}
            }
        }
    	TypeDeserializer valueTypeDeser;

        if (member instanceof AnnotatedMember) { // JAXB allows per-property annotations
            valueTypeDeser = findPropertyTypeDeserializer(config, type, (AnnotatedMember) member, property);
        } else { // classes just have Jackson annotations
            // probably only occurs if 'property' is null anyway
            valueTypeDeser = findTypeDeserializer(config, type, null);
        }
    	if (valueTypeDeser != null) {
            type = type.withTypeHandler(valueTypeDeser);
    	}
    	return type;
    }
    
    protected EnumResolver<?> constructEnumResolver(Class<?> enumClass, DeserializationConfig config)
    {
        // [JACKSON-212]: may need to use Enum.toString()
        if (config.isEnabled(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING)) {
            return EnumResolver.constructUnsafeUsingToString(enumClass);
        }
        return EnumResolver.constructUnsafe(enumClass, config.getAnnotationIntrospector());
    }
}
