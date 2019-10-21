package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl.CreatorCollector;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl.CreatorProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StdKeyDeserializers;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.ThrowableDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ArrayBuilders;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.EnumResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Concrete deserializer factory class that adds full Bean deserializer
 * construction logic using class introspection.
 *<p>
 * Since there is no caching, this factory is stateless and a globally
 * shared singleton instance ({@link #instance}) can be  used by
 * {@link DeserializerProvider}s).
 */
public class BeanDeserializerFactory
    extends BasicDeserializerFactory
{
    /**
     * Signature of <b>Throwable.initCause</b> method.
     */
    private final static Class<?>[] INIT_CAUSE_PARAMS = new Class<?>[] { Throwable.class };

    /*
    /**********************************************************
    /* Config class implementation
    /**********************************************************
     */
    
    /**
     * Standard configuration settings container class implementation.
     * 
     * @since 1.7
     */
    public static class ConfigImpl extends Config
    {
        protected final static KeyDeserializers[] NO_KEY_DESERIALIZERS = new KeyDeserializers[0];
        protected final static BeanDeserializerModifier[] NO_MODIFIERS = new BeanDeserializerModifier[0];
        protected final static AbstractTypeResolver[] NO_ABSTRACT_TYPE_RESOLVERS = new AbstractTypeResolver[0];
        protected final static ValueInstantiators[] NO_VALUE_INSTANTIATORS = new ValueInstantiators[0];
        
        /**
         * List of providers for additional deserializers, checked before considering default
         * basic or bean deserializers.
         * 
         * @since 1.7
         */
        protected final Deserializers[] _additionalDeserializers;

        /**
         * List of providers for additional key deserializers, checked before considering
         * standard key deserializers.
         * 
         * @since 1.7
         */
        protected final KeyDeserializers[] _additionalKeyDeserializers;
        
        /**
         * List of modifiers that can change the way {@link BeanDeserializer} instances
         * are configured and constructed.
         */
        protected final BeanDeserializerModifier[] _modifiers;

        /**
         * List of objects that may be able to resolve abstract types to
         * concrete types. Used by functionality like "mr Bean" to materialize
         * types as needed.
         * 
         * @since 1.8
         */
        protected final AbstractTypeResolver[] _abstractTypeResolvers;

        /**
         * List of objects that know how to create instances of POJO types;
         * possibly using custom construction (non-annoted constructors; factory
         * methods external to value type etc).
         * Used to support objects that are created using non-standard methods;
         * or to support post-constructor functionality.
         * 
         * @since 1.9
         */
        protected final ValueInstantiators[] _valueInstantiators;
        
        /**
         * Constructor for creating basic configuration with no additional
         * handlers.
         */
        public ConfigImpl() {
            this(null, null, null, null, null);
        }

        /**
         * Copy-constructor that will create an instance that contains defined
         * set of additional deserializer providers.
         */
        protected ConfigImpl(Deserializers[] allAdditionalDeserializers,
                KeyDeserializers[] allAdditionalKeyDeserializers,
                BeanDeserializerModifier[] modifiers,
                AbstractTypeResolver[] atr,
                ValueInstantiators[] vi)
        {
            _additionalDeserializers = (allAdditionalDeserializers == null) ?
                    NO_DESERIALIZERS : allAdditionalDeserializers;
            _additionalKeyDeserializers = (allAdditionalKeyDeserializers == null) ?
                    NO_KEY_DESERIALIZERS : allAdditionalKeyDeserializers;
            _modifiers = (modifiers == null) ? NO_MODIFIERS : modifiers;
            _abstractTypeResolvers = (atr == null) ? NO_ABSTRACT_TYPE_RESOLVERS : atr;
            _valueInstantiators = (vi == null) ? NO_VALUE_INSTANTIATORS : vi;
        }

        @Override
        public Config withAdditionalDeserializers(Deserializers additional)
        {
            if (additional == null) {
                throw new IllegalArgumentException("Can not pass null Deserializers");
            }
            Deserializers[] all = ArrayBuilders.insertInListNoDup(_additionalDeserializers, additional);
            return new ConfigImpl(all, _additionalKeyDeserializers, _modifiers,
                    _abstractTypeResolvers, _valueInstantiators);
        }

        @Override
        public Config withAdditionalKeyDeserializers(KeyDeserializers additional)
        {
            if (additional == null) {
                throw new IllegalArgumentException("Can not pass null KeyDeserializers");
            }
            KeyDeserializers[] all = ArrayBuilders.insertInListNoDup(_additionalKeyDeserializers, additional);
            return new ConfigImpl(_additionalDeserializers, all, _modifiers,
                    _abstractTypeResolvers, _valueInstantiators);
        }
        
        @Override
        public Config withDeserializerModifier(BeanDeserializerModifier modifier)
        {
            if (modifier == null) {
                throw new IllegalArgumentException("Can not pass null modifier");
            }
            BeanDeserializerModifier[] all = ArrayBuilders.insertInListNoDup(_modifiers, modifier);
            return new ConfigImpl(_additionalDeserializers, _additionalKeyDeserializers, all,
                    _abstractTypeResolvers, _valueInstantiators);
        }

        @Override
        public Config withAbstractTypeResolver(AbstractTypeResolver resolver)
        {
            if (resolver == null) {
                throw new IllegalArgumentException("Can not pass null resolver");
            }
            AbstractTypeResolver[] all = ArrayBuilders.insertInListNoDup(_abstractTypeResolvers, resolver);
            return new ConfigImpl(_additionalDeserializers, _additionalKeyDeserializers, _modifiers,
                    all, _valueInstantiators);
        }

        @Override
        public Config withValueInstantiators(ValueInstantiators instantiators) 
        {
            if (instantiators == null) {
                throw new IllegalArgumentException("Can not pass null resolver");
            }
            ValueInstantiators[] all = ArrayBuilders.insertInListNoDup(_valueInstantiators, instantiators);
            return new ConfigImpl(_additionalDeserializers, _additionalKeyDeserializers, _modifiers,
                    _abstractTypeResolvers, all);
        }
        
        @Override
        public boolean hasDeserializers() { return _additionalDeserializers.length > 0; }

        @Override
        public boolean hasKeyDeserializers() { return _additionalKeyDeserializers.length > 0; }
        
        @Override
        public boolean hasDeserializerModifiers() { return _modifiers.length > 0; }

        @Override
        public boolean hasAbstractTypeResolvers() { return _abstractTypeResolvers.length > 0; }

        @Override
        public boolean hasValueInstantiators() { return _valueInstantiators.length > 0; }
        
        @Override
        public Iterable<Deserializers> deserializers() {
            return ArrayBuilders.arrayAsIterable(_additionalDeserializers);
        }

        @Override
        public Iterable<KeyDeserializers> keyDeserializers() {
            return ArrayBuilders.arrayAsIterable(_additionalKeyDeserializers);
        }
        
        @Override
        public Iterable<BeanDeserializerModifier> deserializerModifiers() {
            return ArrayBuilders.arrayAsIterable(_modifiers);
        }

        @Override
        public Iterable<AbstractTypeResolver> abstractTypeResolvers() {
            return ArrayBuilders.arrayAsIterable(_abstractTypeResolvers);
        }

        @Override
        public Iterable<ValueInstantiators> valueInstantiators() {
            return ArrayBuilders.arrayAsIterable(_valueInstantiators);
        }
    }
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */
    
    /**
     * Globally shareable thread-safe instance which has no additional custom deserializers
     * registered
     */
    public final static BeanDeserializerFactory instance = new BeanDeserializerFactory(null);

    /**
     * Configuration settings for this factory; immutable instance (just like this
     * factory), new version created via copy-constructor (fluent-style)
     * 
     * @since 1.7
     */
    protected final Config _factoryConfig;

    @Deprecated
    public BeanDeserializerFactory() {
        this(null);
    }

    /**
     * @since 1.7
     */
    public BeanDeserializerFactory(DeserializerFactory.Config config) {
        if (config == null) {
            config = new ConfigImpl();
        }
        _factoryConfig = config;
    }

    @Override
    public final Config getConfig() {
        return _factoryConfig;
    }
    
    /**
     * Method used by module registration functionality, to construct a new bean
     * deserializer factory
     * with different configuration settings.
     * 
     * @since 1.7
     */
    @Override
    public DeserializerFactory withConfig(DeserializerFactory.Config config)
    {
        if (_factoryConfig == config) {
            return this;
        }

        /* 22-Nov-2010, tatu: Handling of subtypes is tricky if we do immutable-with-copy-ctor;
         *    and we pretty much have to here either choose between losing subtype instance
         *    when registering additional deserializers, or losing deserializers.
         *    Instead, let's actually just throw an error if this method is called when subtype
         *    has not properly overridden this method; this to indicate problem as soon as possible.
         */
        if (getClass() != BeanDeserializerFactory.class) {
            throw new IllegalStateException("Subtype of BeanDeserializerFactory ("+getClass().getName()
                    +") has not properly overridden method 'withAdditionalDeserializers': can not instantiate subtype with "
                    +"additional deserializer definitions");
        }
        return new BeanDeserializerFactory(config);
    }
    
    /*
    /**********************************************************
    /* Overrides for super-class methods used for finding
    /* custom deserializers
    /**********************************************************
     */

    @Override
    public KeyDeserializer createKeyDeserializer(DeserializationConfig config, JavaType type,
            BeanProperty property)
        throws JsonMappingException
    {
        // First: possible custom deserializers
        if (_factoryConfig.hasKeyDeserializers()) {
            BasicBeanDescription beanDesc = config.introspectClassAnnotations(type.getRawClass());
            for (KeyDeserializers d  : _factoryConfig.keyDeserializers()) {
                KeyDeserializer deser = d.findKeyDeserializer(type, config, beanDesc, property);
                if (deser != null) {
                    return deser;
                }
            }
        }
        // and if none found, standard ones:
        // No serializer needed if it's plain old String, or Object/untyped
        Class<?> raw = type.getRawClass();
        if (raw == String.class || raw == Object.class) {
            return StdKeyDeserializers.constructStringKeyDeserializer(config, type);
        }
        // Most other keys are of limited number of static types
        KeyDeserializer kdes = _keyDeserializers.get(type);
        if (kdes != null) {
            return kdes;
        }
        // And then other one-offs; first, Enum:
        if (type.isEnumType()) {
            return _createEnumKeyDeserializer(config, type, property);
        }
        // One more thing: can we find ctor(String) or valueOf(String)?
        kdes = StdKeyDeserializers.findStringBasedKeyDeserializer(config, type);
        return kdes;
    }

    private KeyDeserializer _createEnumKeyDeserializer(DeserializationConfig config, JavaType type,
            BeanProperty property)
        throws JsonMappingException
    {
        BasicBeanDescription beanDesc = config.introspect(type);
        Class<?> enumClass = type.getRawClass();
        EnumResolver<?> enumRes = constructEnumResolver(enumClass, config);
        // [JACKSON-193] May have @JsonCreator for static factory method:
        for (AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            if (config.getAnnotationIntrospector().hasCreatorAnnotation(factory)) {
                int argCount = factory.getParameterCount();
                if (argCount == 1) {
                    Class<?> returnType = factory.getRawType();
                    // usually should be class, but may be just plain Enum<?> (for Enum.valueOf()?)
                    if (returnType.isAssignableFrom(enumClass)) {
                        // note: mostly copied from 'EnumDeserializer.deserializerForCreator(...)'
                        if (factory.getParameterType(0) != String.class) {
                            throw new IllegalArgumentException("Parameter #0 type for factory method ("+factory+") not suitable, must be java.lang.String");
                        }
                        if (config.canOverrideAccessModifiers()) {
                            ClassUtil.checkAndFixAccess(factory.getMember());
                        }
                        return StdKeyDeserializers.constructEnumKeyDeserializer(enumRes, factory);
                    }
                }
                throw new IllegalArgumentException("Unsuitable method ("+factory+") decorated with @JsonCreator (for Enum type "
                        +enumClass.getName()+")");
            }
        }
        // [JACKSON-749] Also, need to consider @JsonValue, if one found
        return StdKeyDeserializers.constructEnumKeyDeserializer(enumRes);
    }
    
    @Override
    protected JsonDeserializer<?> _findCustomArrayDeserializer(ArrayType type, DeserializationConfig config,
            DeserializerProvider provider,
            BeanProperty property,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        for (Deserializers d  : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findArrayDeserializer(type, config, provider, property,
                        elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    @Override
    protected JsonDeserializer<?> _findCustomCollectionDeserializer(CollectionType type, DeserializationConfig config,
            DeserializerProvider provider, BasicBeanDescription beanDesc,
            BeanProperty property,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        for (Deserializers d  : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findCollectionDeserializer(type, config, provider, beanDesc, property,
                    elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    @Override
    protected JsonDeserializer<?> _findCustomCollectionLikeDeserializer(CollectionLikeType type, DeserializationConfig config,
            DeserializerProvider provider, BasicBeanDescription beanDesc,
            BeanProperty property,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        for (Deserializers d  : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findCollectionLikeDeserializer(type, config, provider, beanDesc, property,
                    elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }
    
    @Override
    protected JsonDeserializer<?> _findCustomEnumDeserializer(Class<?> type, DeserializationConfig config,
            BasicBeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException
    {
        for (Deserializers d  : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findEnumDeserializer(type, config, beanDesc, property);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    @Override
    protected JsonDeserializer<?> _findCustomMapDeserializer(MapType type,
            DeserializationConfig config,
            DeserializerProvider provider, BasicBeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        for (Deserializers d  : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findMapDeserializer(type, config, provider, beanDesc, property,
                    keyDeserializer, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    @Override
    protected JsonDeserializer<?> _findCustomMapLikeDeserializer(MapLikeType type,
            DeserializationConfig config,
            DeserializerProvider provider, BasicBeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        for (Deserializers d  : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findMapLikeDeserializer(type, config, provider, beanDesc, property,
                    keyDeserializer, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }
    
    @Override
    protected JsonDeserializer<?> _findCustomTreeNodeDeserializer(Class<? extends JsonNode> type,
            DeserializationConfig config, BeanProperty property)
        throws JsonMappingException
    {
        for (Deserializers d  : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findTreeNodeDeserializer(type, config, property);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    // Note: NOT overriding, superclass has no matching method
    @SuppressWarnings("unchecked")
    protected JsonDeserializer<Object> _findCustomBeanDeserializer(JavaType type, DeserializationConfig config,
            DeserializerProvider provider, BasicBeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException
    {
        for (Deserializers d  : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findBeanDeserializer(type, config, provider, beanDesc, property);
            if (deser != null) {
                return (JsonDeserializer<Object>) deser;
            }
        }
        return null;
    }
    
    /*
    /**********************************************************
    /* DeserializerFactory API implementation
    /**********************************************************
     */

    /**
     * Method that will find complete abstract type mapping for specified type, doing as
     * many resolution steps as necessary.
     */
    @Override
    public JavaType mapAbstractType(DeserializationConfig config, JavaType type)
        throws JsonMappingException
    {
        while (true) {
            JavaType next = _mapAbstractType2(config, type);
            if (next == null) {
                return type;
            }
            /* Should not have to worry about cycles; but better verify since they will invariably
             * occur... :-)
             * (also: guard against invalid resolution to a non-related type)
             */
            Class<?> prevCls = type.getRawClass();
            Class<?> nextCls = next.getRawClass();
            if ((prevCls == nextCls) || !prevCls.isAssignableFrom(nextCls)) {
                throw new IllegalArgumentException("Invalid abstract type resolution from "+type+" to "+next+": latter is not a subtype of former");
            }
            type = next;
        }
    }
    
    /**
     * Value instantiator is created both based on creator annotations,
     * and on optional externally provided instantiators (registered through
     * module interface).
     */
    @Override
    public ValueInstantiator findValueInstantiator(DeserializationConfig config,
            BasicBeanDescription beanDesc)
        throws JsonMappingException
    {
        ValueInstantiator instantiator;
        // [JACKSON-633] Check @JsonValueInstantiator before anything else
        AnnotatedClass ac = beanDesc.getClassInfo();
        Object instDef = config.getAnnotationIntrospector().findValueInstantiator(ac);
        if (instDef != null) {
            if (instDef instanceof ValueInstantiator) {
                instantiator = (ValueInstantiator) instDef;
            } else {
                if (!(instDef instanceof Class<?>)) { // sanity check
                    throw new IllegalStateException("Invalid value instantiator returned for type "+beanDesc+": neither a Class nor ValueInstantiator");
                }
                Class<?> cls = (Class<?>) instDef;
                if (!ValueInstantiator.class.isAssignableFrom(cls)) {
                    throw new IllegalStateException("Invalid instantiator Class<?> returned for type "+beanDesc+": "
                            +cls.getName()+" not a ValueInstantiator");
                }
                @SuppressWarnings("unchecked")
                Class<? extends ValueInstantiator> instClass = (Class<? extends ValueInstantiator>) cls;
                instantiator = config.valueInstantiatorInstance(ac, (Class<? extends ValueInstantiator>)instClass);
            }
        } else {
            instantiator = constructDefaultValueInstantiator(config, beanDesc);
        }
        
        // finally: anyone want to modify ValueInstantiator?
        if (_factoryConfig.hasValueInstantiators()) {
            for (ValueInstantiators insts : _factoryConfig.valueInstantiators()) {
                instantiator = insts.findValueInstantiator(config, beanDesc, instantiator);
                // let's do sanity check; easier to spot buggy handlers
                if (instantiator == null) {
                    throw new JsonMappingException("Broken registered ValueInstantiators (of type "
                            +insts.getClass().getName()+"): returned null ValueInstantiator");
                }
            }
        }
        
        return instantiator;
    }

    /**
     * Method that {@link DeserializerProvider}s call to create a new
     * deserializer for types other than Collections, Maps, arrays and
     * enums.
     */
    @Override
    public JsonDeserializer<Object> createBeanDeserializer(DeserializationConfig config,
            DeserializerProvider p, JavaType type, BeanProperty property)
        throws JsonMappingException
    {
        // First things first: abstract types may use defaulting:
        if (type.isAbstract()) {
            type = mapAbstractType(config, type);
        }
        
        // First things first: maybe explicit definition via annotations?
        BasicBeanDescription beanDesc = config.introspect(type);
        JsonDeserializer<Object> ad = findDeserializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (ad != null) {
            return ad;
        }
        // Or value annotation that indicates more specific type to use:
        JavaType newType =  modifyTypeByAnnotation(config, beanDesc.getClassInfo(), type, null);
        if (newType.getRawClass() != type.getRawClass()) {
            type = newType;
            beanDesc = config.introspect(type);
        }
        // We may also have custom overrides:
        JsonDeserializer<Object> custom = _findCustomBeanDeserializer(type, config, p, beanDesc, property);
        if (custom != null) {
            return custom;
        }
        /* One more thing to check: do we have an exception type
         * (Throwable or its sub-classes)? If so, need slightly
         * different handling.
         */
        if (type.isThrowable()) {
            return buildThrowableDeserializer(config, type, beanDesc, property);
        }
        /* Or, for abstract types, may have alternate means for resolution
         * (defaulting, materialization)
         */
        if (type.isAbstract()) {
            // [JACKSON-41] (v1.6): Let's make it possible to materialize abstract types.
            JavaType concreteType = materializeAbstractType(config, beanDesc);
            if (concreteType != null) {
                /* important: introspect actual implementation (abstract class or
                 * interface doesn't have constructors, for one)
                 */
                beanDesc = config.introspect(concreteType);
                return buildBeanDeserializer(config, concreteType, beanDesc, property);
            }
        }

        // Otherwise, may want to check handlers for standard types, from superclass:
        JsonDeserializer<Object> deser = findStdBeanDeserializer(config, p, type, property);
        if (deser != null) {
            return deser;
        }

        // Otherwise: could the class be a Bean class? If not, bail out
        if (!isPotentialBeanType(type.getRawClass())) {
            return null;
        }
        // Use generic bean introspection to build deserializer
        return buildBeanDeserializer(config, type, beanDesc, property);
    }

    /**
     * Method that will find abstract type mapping for specified type, doing a single
     * lookup through registered abstract type resolvers; will not do recursive lookups.
     */
    protected JavaType _mapAbstractType2(DeserializationConfig config, JavaType type)
        throws JsonMappingException
    {
        Class<?> currClass = type.getRawClass();
        if (_factoryConfig.hasAbstractTypeResolvers()) {
            for (AbstractTypeResolver resolver : _factoryConfig.abstractTypeResolvers()) {
                JavaType concrete = resolver.findTypeMapping(config, type);
                if (concrete != null && concrete.getRawClass() != currClass) {
                    return concrete;
                }
            }
        }
        return null;
    }
    
    protected JavaType materializeAbstractType(DeserializationConfig config,
            BasicBeanDescription beanDesc)
        throws JsonMappingException
    {
        final JavaType abstractType = beanDesc.getType();
        
        /* [JACKSON-502] (1.8): Now it is possible to have multiple resolvers too,
         *   as they are registered via module interface.
         */
        for (AbstractTypeResolver r : _factoryConfig.abstractTypeResolvers()) {
            JavaType concrete = r.resolveAbstractType(config, abstractType);
            if (concrete != null) {
                return concrete;
            }
        }
        return null;
    }
    
    /*
    /**********************************************************
    /* Public construction method beyond DeserializerFactory API:
    /* can be called from outside as well as overridden by
    /* sub-classes
    /**********************************************************
     */

    /**
     * Method that is to actually build a bean deserializer instance.
     * All basic sanity checks have been done to know that what we have
     * may be a valid bean type, and that there are no default simple
     * deserializers.
     */
    @SuppressWarnings("unchecked")
    public JsonDeserializer<Object> buildBeanDeserializer(DeserializationConfig config,
            JavaType type, BasicBeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException
    {
        // First: check what creators we can use, if any
        ValueInstantiator valueInstantiator = findValueInstantiator(config, beanDesc);
        // ... since often we have nothing to go on, if we have abstract type:
        if (type.isAbstract()) {
            if (!valueInstantiator.canInstantiate()) {
                // and if so, need placeholder deserializer
                return new AbstractDeserializer(type);
            }
        }
        BeanDeserializerBuilder builder = constructBeanDeserializerBuilder(beanDesc);
        builder.setValueInstantiator(valueInstantiator);
         // And then setters for deserializing from JSON Object
        addBeanProps(config, beanDesc, builder);
        // managed/back reference fields/setters need special handling... first part
        addReferenceProperties(config, beanDesc, builder);
        addInjectables(config, beanDesc, builder);

        // [JACKSON-440]: update builder now that all information is in?
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                builder = mod.updateBuilder(config, beanDesc, builder);
            }
        }
        JsonDeserializer<?> deserializer = builder.build(property);

        // [JACKSON-440]: may have modifier(s) that wants to modify or replace serializer we just built:
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deserializer = mod.modifyDeserializer(config, beanDesc, deserializer);
            }
        }
        return (JsonDeserializer<Object>) deserializer;
        
    }

    @SuppressWarnings("unchecked")
    public JsonDeserializer<Object> buildThrowableDeserializer(DeserializationConfig config,
            JavaType type, BasicBeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException
    {
        // first: construct like a regular bean deserializer...
        BeanDeserializerBuilder builder = constructBeanDeserializerBuilder(beanDesc);
        builder.setValueInstantiator(findValueInstantiator(config, beanDesc));

        addBeanProps(config, beanDesc, builder);
        // (and assume there won't be any back references)

        // But then let's decorate things a bit
        /* To resolve [JACKSON-95], need to add "initCause" as setter
         * for exceptions (sub-classes of Throwable).
         */
        AnnotatedMethod am = beanDesc.findMethod("initCause", INIT_CAUSE_PARAMS);
        if (am != null) { // should never be null
            SettableBeanProperty prop = constructSettableProperty(config, beanDesc, "cause", am);
            if (prop != null) {
                /* 21-Aug-2011, tatus: We may actually have found 'cause' property
                 *   to set (with new 1.9 code)... but let's replace it just in case,
                 *   otherwise can end up with odd errors.
                 */
                builder.addOrReplaceProperty(prop, true);
            }
        }

        // And also need to ignore "localizedMessage"
        builder.addIgnorable("localizedMessage");
        /* As well as "message": it will be passed via constructor,
         * as there's no 'setMessage()' method
        */
        builder.addIgnorable("message");
        // [JACKSON-794]: JDK 7 also added "getSuppressed", skip if we have such data:
        builder.addIgnorable("suppressed");

        // [JACKSON-440]: update builder now that all information is in?
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                builder = mod.updateBuilder(config, beanDesc, builder);
            }
        }
        JsonDeserializer<?> deserializer = builder.build(property);
        
        /* At this point it ought to be a BeanDeserializer; if not, must assume
         * it's some other thing that can handle deserialization ok...
         */
        if (deserializer instanceof BeanDeserializer) {
            deserializer = new ThrowableDeserializer((BeanDeserializer) deserializer);
        }

        // [JACKSON-440]: may have modifier(s) that wants to modify or replace serializer we just built:
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deserializer = mod.modifyDeserializer(config, beanDesc, deserializer);
            }
        }
        return (JsonDeserializer<Object>) deserializer;
    }

    /*
    /**********************************************************
    /* Helper methods for Bean deserializer construction,
    /* overridable by sub-classes
    /**********************************************************
     */

    /**
     * Overridable method that constructs a {@link BeanDeserializerBuilder}
     * which is used to accumulate information needed to create deserializer
     * instance.
     * 
     * @since 1.7
     */
    protected BeanDeserializerBuilder constructBeanDeserializerBuilder(BasicBeanDescription beanDesc) {
        return new BeanDeserializerBuilder(beanDesc);
    }

    /**
     * Method that will construct standard default {@link ValueInstantiator}
     * using annotations (like @JsonCreator) and visibility rules
     * 
     * @since 1.9
     */
    protected ValueInstantiator constructDefaultValueInstantiator(DeserializationConfig config,
            BasicBeanDescription beanDesc)
        throws JsonMappingException
    {
        boolean fixAccess = config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        CreatorCollector creators =  new CreatorCollector(beanDesc, fixAccess);
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        
        // First, let's figure out constructor/factory-based instantiation
        // 23-Jan-2010, tatus: but only for concrete types
        if (beanDesc.getType().isConcrete()) {
            AnnotatedConstructor defaultCtor = beanDesc.findDefaultConstructor();
            if (defaultCtor != null) {
                if (fixAccess) {
                    ClassUtil.checkAndFixAccess(defaultCtor.getAnnotated());
                }
                creators.setDefaultConstructor(defaultCtor);
            }
        }

        // need to construct suitable visibility checker:
        VisibilityChecker<?> vchecker = config.getDefaultVisibilityChecker();
        vchecker = config.getAnnotationIntrospector().findAutoDetectVisibility(beanDesc.getClassInfo(), vchecker);

        /* Important: first add factory methods; then constructors, so
         * latter can override former!
         */
        _addDeserializerFactoryMethods(config, beanDesc, vchecker, intr, creators);
        _addDeserializerConstructors(config, beanDesc, vchecker, intr, creators);

        return creators.constructValueInstantiator(config);
    }

    protected void _addDeserializerConstructors
        (DeserializationConfig config, BasicBeanDescription beanDesc, VisibilityChecker<?> vchecker,
         AnnotationIntrospector intr, CreatorCollector creators)
        throws JsonMappingException
    {
        for (AnnotatedConstructor ctor : beanDesc.getConstructors()) {
            int argCount = ctor.getParameterCount();
            if (argCount < 1) {
                continue;
            }
            boolean isCreator = intr.hasCreatorAnnotation(ctor);
            boolean isVisible =  vchecker.isCreatorVisible(ctor);
            // some single-arg constructors (String, number) are auto-detected
            if (argCount == 1) {
                _handleSingleArgumentConstructor(config, beanDesc, vchecker, intr, creators,
                        ctor, isCreator, isVisible);
                continue;
            }
            if (!isCreator && !isVisible) {
            	continue;
            }
            // [JACKSON-541] improved handling a bit so:
            // 2 or more args; all params must have name annotations
            // ... or @JacksonInject (or equivalent)
            // But if it was auto-detected and there's no annotations, keep silent (was not meant to be a creator?)
            boolean annotationFound = false;
            /* [JACKSON-712] One more possibility; can have 1 or more injectables, and
             * exactly one non-annotated parameter: if so, it's still delegating.
             */
            AnnotatedParameter nonAnnotatedParam = null;
            int namedCount = 0;
            int injectCount = 0;
            CreatorProperty[] properties = new CreatorProperty[argCount];
            for (int i = 0; i < argCount; ++i) {
                AnnotatedParameter param = ctor.getParameter(i);
                String name = (param == null) ? null : intr.findPropertyNameForParam(param);
                Object injectId = intr.findInjectableValueId(param);
                if (name != null && name.length() > 0) {
                    ++namedCount;
                    properties[i] = constructCreatorProperty(config, beanDesc, name, i, param, injectId);
                } else if (injectId != null) {
                    ++injectCount;
                    properties[i] = constructCreatorProperty(config, beanDesc, name, i, param, injectId);
                } else if (nonAnnotatedParam == null) {
                    nonAnnotatedParam = param;
                }
            }

            // Ok: if named or injectable, we have more work to do
            if (isCreator || namedCount > 0 || injectCount > 0) {
                // simple case; everything covered:
                if ((namedCount + injectCount) == argCount) {
                    creators.addPropertyCreator(ctor, properties);
                } else if ((namedCount == 0) && ((injectCount + 1) == argCount)) {
                    // secondary: all but one injectable, one un-annotated (un-named)
                    // [JACKSON-712] SHOULD support; but we won't yet (tricky to do, not impossible)
                    throw new IllegalArgumentException("Delegated constructor with Injectables not yet supported (see [JACKSON-712]) for "
                            +ctor);
                } else { // otherwise, epic fail
                    throw new IllegalArgumentException("Argument #"+nonAnnotatedParam.getIndex()+" of constructor "+ctor+" has no property name annotation; must have name when multiple-paramater constructor annotated as Creator");
                }
            }
            if (annotationFound) {
            	creators.addPropertyCreator(ctor, properties);
            }
        }
    }

    protected boolean _handleSingleArgumentConstructor(DeserializationConfig config,
            BasicBeanDescription beanDesc, VisibilityChecker<?> vchecker,
            AnnotationIntrospector intr, CreatorCollector creators,
            AnnotatedConstructor ctor, boolean isCreator, boolean isVisible)
        throws JsonMappingException
    {
        // note: if we do have parameter name, it'll be "property constructor":
        AnnotatedParameter param = ctor.getParameter(0);
        String name = intr.findPropertyNameForParam(param);
        Object injectId = intr.findInjectableValueId(param);
    
        if ((injectId != null) || (name != null && name.length() > 0)) { // property-based
            // We know there's a name and it's only 1 parameter.
            CreatorProperty[] properties = new CreatorProperty[1];
            properties[0] = constructCreatorProperty(config, beanDesc, name, 0, param, injectId);
            creators.addPropertyCreator(ctor, properties);
            return true;
        }
    
        // otherwise either 'simple' number, String, or general delegate:
        Class<?> type = ctor.getParameterClass(0);
        if (type == String.class) {
            if (isCreator || isVisible) {
                creators.addStringCreator(ctor);
            }
            return true;
        }
        if (type == int.class || type == Integer.class) {
            if (isCreator || isVisible) {
                creators.addIntCreator(ctor);
            }
            return true;
        }
        if (type == long.class || type == Long.class) {
            if (isCreator || isVisible) {
                creators.addLongCreator(ctor);
            }
            return true;
        }
        if (type == double.class || type == Double.class) {
            if (isCreator || isVisible) {
                creators.addDoubleCreator(ctor);
            }
            return true;
        }
    
        // Delegating Creator ok iff it has @JsonCreator (etc)
        if (isCreator) {
            creators.addDelegatingCreator(ctor);
            return true;
        }
        return false;
    }
    
    protected void _addDeserializerFactoryMethods
        (DeserializationConfig config, BasicBeanDescription beanDesc, VisibilityChecker<?> vchecker,
         AnnotationIntrospector intr, CreatorCollector creators)
        throws JsonMappingException
    {

        for (AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            int argCount = factory.getParameterCount();
            if (argCount < 1) {
                continue;
            }
            boolean isCreator = intr.hasCreatorAnnotation(factory);
            // some single-arg factory methods (String, number) are auto-detected
            if (argCount == 1) {
                AnnotatedParameter param = factory.getParameter(0);
                String name = intr.findPropertyNameForParam(param);
                Object injectId = intr.findInjectableValueId(param);

                if ((injectId == null) && (name == null || name.length() == 0)) { // not property based
                    _handleSingleArgumentFactory(config, beanDesc, vchecker, intr, creators,
                            factory, isCreator);
                    // otherwise just ignored
                    continue;
                }
                // fall through if there's name
            } else {
                // more than 2 args, must be @JsonCreator
                if (!intr.hasCreatorAnnotation(factory)) {
                    continue;
                }
            }
            // 1 or more args; all params must have name annotations
            CreatorProperty[] properties = new CreatorProperty[argCount];
            for (int i = 0; i < argCount; ++i) {
                AnnotatedParameter param = factory.getParameter(i);
                String name = intr.findPropertyNameForParam(param);
                Object injectableId = intr.findInjectableValueId(param);
                // At this point, name annotation is NOT optional
                if ((name == null || name.length() == 0) && (injectableId == null)) {
                    throw new IllegalArgumentException("Argument #"+i+" of factory method "+factory+" has no property name annotation; must have when multiple-paramater static method annotated as Creator");
                }
                properties[i] = constructCreatorProperty(config, beanDesc, name, i, param, injectableId);
            }
            creators.addPropertyCreator(factory, properties);
        }
    }

    protected boolean _handleSingleArgumentFactory(DeserializationConfig config,
            BasicBeanDescription beanDesc, VisibilityChecker<?> vchecker,
            AnnotationIntrospector intr, CreatorCollector creators,
            AnnotatedMethod factory, boolean isCreator)
        throws JsonMappingException
    {
        Class<?> type = factory.getParameterClass(0);
        
        if (type == String.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addStringCreator(factory);
            }
            return true;
        }
        if (type == int.class || type == Integer.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addIntCreator(factory);
            }
            return true;
        }
        if (type == long.class || type == Long.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addLongCreator(factory);
            }
            return true;
        }
        if (type == double.class || type == Double.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addDoubleCreator(factory);
            }
            return true;
        }
        if (type == boolean.class || type == Boolean.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addBooleanCreator(factory);
            }
            return true;
        }
        if (intr.hasCreatorAnnotation(factory)) {
            creators.addDelegatingCreator(factory);
            return true;
        }
        return false;
    }
    
    /**
     * Method that will construct a property object that represents
     * a logical property passed via Creator (constructor or static
     * factory method)
     */
    protected CreatorProperty constructCreatorProperty(DeserializationConfig config,
            BasicBeanDescription beanDesc, String name, int index,
            AnnotatedParameter param,
            Object injectableValueId)
        throws JsonMappingException
    {
        JavaType t0 = config.getTypeFactory().constructType(param.getParameterType(), beanDesc.bindingsForBeanType());
        BeanProperty.Std property = new BeanProperty.Std(name, t0, beanDesc.getClassAnnotations(), param);
        JavaType type = resolveType(config, beanDesc, t0, param, property);
        if (type != t0) {
            property = property.withType(type);
        }
        // Is there an annotation that specifies exact deserializer?
        JsonDeserializer<Object> deser = findDeserializerFromAnnotation(config, param, property);
        // If yes, we are mostly done:
        type = modifyTypeByAnnotation(config, param, type, name);

        // Type deserializer: either comes from property (and already resolved)
        TypeDeserializer typeDeser = (TypeDeserializer) type.getTypeHandler();
        // or if not, based on type being referenced:
        if (typeDeser == null) {
            typeDeser = findTypeDeserializer(config, type, property);
        }
        CreatorProperty prop = new CreatorProperty(name, type, typeDeser,
                beanDesc.getClassAnnotations(), param, index, injectableValueId);
        if (deser != null) {
            prop = prop.withValueDeserializer(deser);
        }
        return prop;
    }
    
    /**
     * Method called to figure out settable properties for the
     * bean deserializer to use.
     *<p>
     * Note: designed to be overridable, and effort is made to keep interface
     * similar between versions.
     */
    protected void addBeanProps(DeserializationConfig config,
            BasicBeanDescription beanDesc, BeanDeserializerBuilder builder)
        throws JsonMappingException
    {
        List<BeanPropertyDefinition> props = beanDesc.findProperties();
        // Things specified as "ok to ignore"? [JACKSON-77]
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        boolean ignoreAny = false;
        {
            Boolean B = intr.findIgnoreUnknownProperties(beanDesc.getClassInfo());
            if (B != null) {
                ignoreAny = B.booleanValue();
                builder.setIgnoreUnknownProperties(ignoreAny);
            }
        }
        // Or explicit/implicit definitions?
        Set<String> ignored = ArrayBuilders.arrayToSet(intr.findPropertiesToIgnore(beanDesc.getClassInfo()));        
        for (String propName : ignored) {
            builder.addIgnorable(propName);
        }
        AnnotatedMethod anySetter = beanDesc.findAnySetter();
        // Implicit ones via @JsonIgnore and equivalent?
        /* 26-Dec-2011, tatu: As per [JACKSON-744], it probably does NOT make
         *   sense to consider ignorable, esp. now that both getters and setters
         *   can induce ignoral.
         */
        {
            Collection<String> ignored2 = (anySetter == null) ?
                    beanDesc.getIgnoredPropertyNames() : beanDesc.getIgnoredPropertyNamesForDeser();
                    if (ignored2 != null) {
                for (String propName : ignored2) {
                    // allow ignoral of similarly named JSON property, but do not force;
                    // latter means NOT adding this to 'ignored':
                    builder.addIgnorable(propName);
                }
            }
        }
        HashMap<Class<?>,Boolean> ignoredTypes = new HashMap<Class<?>,Boolean>();
        
        // These are all valid setters, but we do need to introspect bit more
        for (BeanPropertyDefinition property : props) {
            String name = property.getName();
            if (ignored.contains(name)) { // explicit ignoral using @JsonIgnoreProperties needs to block entries
                continue;
            }
            /* [JACKSON-700] If property as passed via constructor parameter, we must
             *   handle things in special way. Not sure what is the most optimal way...
             *   for now, let's just call a (new) method in builder, which does nothing.
             */
            if (property.hasConstructorParameter()) {
                // but let's call a method just to allow custom builders to be aware...
                builder.addCreatorProperty(property);
                continue;
            }
            // primary: have a setter?
            if (property.hasSetter()) {
                AnnotatedMethod setter = property.getSetter();
                // [JACKSON-429] Some types are declared as ignorable as well
                Class<?> type = setter.getParameterClass(0);
                if (isIgnorableType(config, beanDesc, type, ignoredTypes)) {
                    // important: make ignorable, to avoid errors if value is actually seen
                    builder.addIgnorable(name);
                    continue;
                }
                SettableBeanProperty prop = constructSettableProperty(config, beanDesc, name, setter);
                if (prop != null) {
                    builder.addProperty(prop);
                }
                continue;
            }
            if (property.hasField()) {
                AnnotatedField field = property.getField();
                // [JACKSON-429] Some types are declared as ignorable as well
                Class<?> type = field.getRawType();
                if (isIgnorableType(config, beanDesc, type, ignoredTypes)) {
                    // important: make ignorable, to avoid errors if value is actually seen
                    builder.addIgnorable(name);
                    continue;
                }
                SettableBeanProperty prop = constructSettableProperty(config, beanDesc, name, field);
                if (prop != null) {
                    builder.addProperty(prop);
                }
            }
        }
        // Also, do we have a fallback "any" setter?
        if (anySetter != null) {
            builder.setAnySetter(constructAnySetter(config, beanDesc, anySetter));
        }

        /* As per [JACKSON-88], may also need to consider getters
         * for Map/Collection properties
         */
        /* also, as per [JACKSON-328], should not override fields (or actual setters),
         * thus these are added AFTER adding fields
         */
        if (config.isEnabled(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS)) {
            /* Hmmh. We have to assume that 'use getters as setters' also
             * implies 'yes, do auto-detect these getters'? (if not, we'd
             * need to add AUTO_DETECT_GETTERS to deser config too, not
             * just ser config)
             */
            for (BeanPropertyDefinition property : props) {
                if (property.hasGetter()) {
                    String name = property.getName();
                    if (builder.hasProperty(name) || ignored.contains(name)) {
                        continue;
                    }
                    AnnotatedMethod getter = property.getGetter();
                    // should only consider Collections and Maps, for now?
                    Class<?> rt = getter.getRawType();
                    if (Collection.class.isAssignableFrom(rt) || Map.class.isAssignableFrom(rt)) {
                        if (!ignored.contains(name) && !builder.hasProperty(name)) {
                            builder.addProperty(constructSetterlessProperty(config, beanDesc, name, getter));
                        }
                    }
                }
            }
        }
    }

    /**
     * Method that will find if bean has any managed- or back-reference properties,
     * and if so add them to bean, to be linked during resolution phase.
     * 
     * @since 1.6
     */
    protected void addReferenceProperties(DeserializationConfig config,
            BasicBeanDescription beanDesc, BeanDeserializerBuilder builder)
        throws JsonMappingException
    {
        // and then back references, not necessarily found as regular properties
        Map<String,AnnotatedMember> refs = beanDesc.findBackReferenceProperties();
        if (refs != null) {
            for (Map.Entry<String, AnnotatedMember> en : refs.entrySet()) {
                String name = en.getKey();
                AnnotatedMember m = en.getValue();
                if (m instanceof AnnotatedMethod) {
                    builder.addBackReferenceProperty(name, constructSettableProperty(
                            config, beanDesc, m.getName(), (AnnotatedMethod) m));
                } else {
                    builder.addBackReferenceProperty(name, constructSettableProperty(
                            config, beanDesc, m.getName(), (AnnotatedField) m));
                }
            }
        }
    }
    
    /**
     * Method called locate all members used for value injection (if any),
     * constructor {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl.ValueInjector} instances, and add them to builder.
     * 
     * @since 1.9
     */
    protected void addInjectables(DeserializationConfig config,
            BasicBeanDescription beanDesc, BeanDeserializerBuilder builder)
        throws JsonMappingException
    {
        Map<Object, AnnotatedMember> raw = beanDesc.findInjectables();
        if (raw != null) {
            boolean fixAccess = config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
            for (Map.Entry<Object, AnnotatedMember> entry : raw.entrySet()) {
                AnnotatedMember m = entry.getValue();
                if (fixAccess) {
                    m.fixAccess(); // to ensure we can call it
                }
                builder.addInjectable(m.getName(), beanDesc.resolveType(m.getGenericType()),
                        beanDesc.getClassAnnotations(), m, entry.getKey());
            }
        }
    }

    /**
     * Method called to construct fallback {@link SettableAnyProperty}
     * for handling unknown bean properties, given a method that
     * has been designated as such setter.
     */
    protected SettableAnyProperty constructAnySetter(DeserializationConfig config,
            BasicBeanDescription beanDesc, AnnotatedMethod setter)
        throws JsonMappingException
    {
        if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            setter.fixAccess(); // to ensure we can call it
        }
        // we know it's a 2-arg method, second arg is the value
        JavaType type = beanDesc.bindingsForBeanType().resolveType(setter.getParameterType(1));
        BeanProperty.Std property = new BeanProperty.Std(setter.getName(), type, beanDesc.getClassAnnotations(), setter);
        type = resolveType(config, beanDesc, type, setter, property);

        /* AnySetter can be annotated with @JsonClass (etc) just like a
         * regular setter... so let's see if those are used.
         * Returns null if no annotations, in which case binding will
         * be done at a later point.
         */
        JsonDeserializer<Object> deser = findDeserializerFromAnnotation(config, setter, property);
        if (deser != null) {
            return new SettableAnyProperty(property, setter, type, deser);
        }
        /* Otherwise, method may specify more specific (sub-)class for
         * value (no need to check if explicit deser was specified):
         */
        type = modifyTypeByAnnotation(config, setter, type, property.getName());
        return new SettableAnyProperty(property, setter, type, null);
    }

    /**
     * Method that will construct a regular bean property setter using
     * the given setter method.
     *
     * @param setter Method to use to set property value; or null if none.
     *    Null only for "setterless" properties
     *
     * @return Property constructed, if any; or null to indicate that
     *   there should be no property based on given definitions.
     */
    protected SettableBeanProperty constructSettableProperty(DeserializationConfig config,
            BasicBeanDescription beanDesc, String name,
            AnnotatedMethod setter)
        throws JsonMappingException
    {
        // need to ensure method is callable (for non-public)
        if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            setter.fixAccess();
        }

        // note: this works since we know there's exactly one argument for methods
        JavaType t0 = beanDesc.bindingsForBeanType().resolveType(setter.getParameterType(0));
        BeanProperty.Std property = new BeanProperty.Std(name, t0, beanDesc.getClassAnnotations(), setter);
        JavaType type = resolveType(config, beanDesc, t0, setter, property);
        // did type change?
        if (type != t0) {
            property = property.withType(type);
        }
        
        /* First: does the Method specify the deserializer to use?
         * If so, let's use it.
         */
        JsonDeserializer<Object> propDeser = findDeserializerFromAnnotation(config, setter, property);
        type = modifyTypeByAnnotation(config, setter, type, name);
        TypeDeserializer typeDeser = type.getTypeHandler();
        SettableBeanProperty prop = new SettableBeanProperty.MethodProperty(name, type, typeDeser,
                beanDesc.getClassAnnotations(), setter);
        if (propDeser != null) {
            prop = prop.withValueDeserializer(propDeser);
        }
        // [JACKSON-235]: need to retain name of managed forward references:
        AnnotationIntrospector.ReferenceProperty ref = config.getAnnotationIntrospector().findReferenceType(setter);
        if (ref != null && ref.isManagedReference()) {
            prop.setManagedReferenceName(ref.getName());
        }
        return prop;
    }

    protected SettableBeanProperty constructSettableProperty(DeserializationConfig config,
            BasicBeanDescription beanDesc, String name, AnnotatedField field)
        throws JsonMappingException
    {
        // need to ensure method is callable (for non-public)
        if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            field.fixAccess();
        }
        JavaType t0 = beanDesc.bindingsForBeanType().resolveType(field.getGenericType());
        BeanProperty.Std property = new BeanProperty.Std(name, t0, beanDesc.getClassAnnotations(), field);
        JavaType type = resolveType(config, beanDesc, t0, field, property);
        // did type change?
        if (type != t0) {
            property = property.withType(type);
        }
        /* First: does the Method specify the deserializer to use?
         * If so, let's use it.
         */
        JsonDeserializer<Object> propDeser = findDeserializerFromAnnotation(config, field, property);
        type = modifyTypeByAnnotation(config, field, type, name);
        TypeDeserializer typeDeser = type.getTypeHandler();
        SettableBeanProperty prop = new SettableBeanProperty.FieldProperty(name, type, typeDeser,
                beanDesc.getClassAnnotations(), field);
        if (propDeser != null) {
            prop = prop.withValueDeserializer(propDeser);
        }
        // [JACKSON-235]: need to retain name of managed forward references:
        AnnotationIntrospector.ReferenceProperty ref = config.getAnnotationIntrospector().findReferenceType(field);
        if (ref != null && ref.isManagedReference()) {
            prop.setManagedReferenceName(ref.getName());
        }
        return prop;
    }

    /**
     * Method that will construct a regular bean property setter using
     * the given setter method.
     *
     * @param getter Method to use to get property value to modify, null if
     *    none. Non-null for "setterless" properties.
     */
    protected SettableBeanProperty constructSetterlessProperty(DeserializationConfig config,
            BasicBeanDescription beanDesc, String name, AnnotatedMethod getter)
        throws JsonMappingException
    {
        // need to ensure it is callable now:
        if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            getter.fixAccess();
        }

        JavaType type = getter.getType(beanDesc.bindingsForBeanType());
        /* First: does the Method specify the deserializer to use?
         * If so, let's use it.
         */
        BeanProperty.Std property = new BeanProperty.Std(name, type, beanDesc.getClassAnnotations(), getter);
        // @TODO: create BeanProperty to pass?
        JsonDeserializer<Object> propDeser = findDeserializerFromAnnotation(config, getter, property);
        type = modifyTypeByAnnotation(config, getter, type, name);
        TypeDeserializer typeDeser = type.getTypeHandler();
        SettableBeanProperty prop = new SettableBeanProperty.SetterlessProperty(name, type, typeDeser,
                beanDesc.getClassAnnotations(), getter);
        if (propDeser != null) {
            prop = prop.withValueDeserializer(propDeser);
        }
        return prop;
    }

    /*
    /**********************************************************
    /* Helper methods for Bean deserializer, other
    /**********************************************************
     */

    /**
     * Helper method used to skip processing for types that we know
     * can not be (i.e. are never consider to be) beans: 
     * things like primitives, Arrays, Enums, and proxy types.
     *<p>
     * Note that usually we shouldn't really be getting these sort of
     * types anyway; but better safe than sorry.
     */
    protected boolean isPotentialBeanType(Class<?> type)
    {
        String typeStr = ClassUtil.canBeABeanType(type);
        if (typeStr != null) {
            throw new IllegalArgumentException("Can not deserialize Class "+type.getName()+" (of type "+typeStr+") as a Bean");
        }
        if (ClassUtil.isProxyType(type)) {
            throw new IllegalArgumentException("Can not deserialize Proxy class "+type.getName()+" as a Bean");
        }
        /* also: can't deserialize some local classes: static are ok; in-method not;
         * and with [JACKSON-594], other non-static inner classes are ok
         */
        typeStr = ClassUtil.isLocalType(type, true);
        if (typeStr != null) {
            throw new IllegalArgumentException("Can not deserialize Class "+type.getName()+" (of type "+typeStr+") as a Bean");
        }
    	return true;
    }

    /**
     * Helper method that will check whether given raw type is marked as always ignorable
     * (for purpose of ignoring properties with type)
     */
    protected boolean isIgnorableType(DeserializationConfig config, BasicBeanDescription beanDesc,
            Class<?> type, Map<Class<?>,Boolean> ignoredTypes)
    {
        Boolean status = ignoredTypes.get(type);
        if (status == null) {
            BasicBeanDescription desc = config.introspectClassAnnotations(type);
            status = config.getAnnotationIntrospector().isIgnorableType(desc.getClassInfo());
            // We default to 'false', ie. not ignorable
            if (status == null) {
                status = Boolean.FALSE;
            }
        }
        return status;
    }
}
