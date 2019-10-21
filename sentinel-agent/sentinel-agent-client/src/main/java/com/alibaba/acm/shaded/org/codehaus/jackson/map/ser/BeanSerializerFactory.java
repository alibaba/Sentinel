package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.NamedType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.MapSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ArrayBuilders;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
 
/**
 * Factory class that can provide serializers for any regular Java beans
 * (as defined by "having at least one get method recognizable as bean
 * accessor" -- where {@link Object#getClass} does not count);
 * as well as for "standard" JDK types. Latter is achieved
 * by delegating calls to {@link BasicSerializerFactory} 
 * to find serializers both for "standard" JDK types (and in some cases,
 * sub-classes as is the case for collection classes like
 * {@link java.util.List}s and {@link java.util.Map}s) and bean (value)
 * classes.
 *<p>
 * Note about delegating calls to {@link BasicSerializerFactory}:
 * although it would be nicer to use linear delegation
 * for construction (to essentially dispatch all calls first to the
 * underlying {@link BasicSerializerFactory}; or alternatively after
 * failing to provide bean-based serializer}, there is a problem:
 * priority levels for detecting standard types are mixed. That is,
 * we want to check if a type is a bean after some of "standard" JDK
 * types, but before the rest.
 * As a result, "mixed" delegation used, and calls are NOT done using
 * regular {@link SerializerFactory} interface but rather via
 * direct calls to {@link BasicSerializerFactory}.
 *<p>
 * Finally, since all caching is handled by the serializer provider
 * (not factory) and there is no configurability, this
 * factory is stateless.
 * This means that a global singleton instance can be used.
 *<p>
 * Notes for version 1.7 (and above): the new module registration system
 * required addition of {@link #withConfig}, which has to
 * be redefined by sub-classes so that they can work properly with
 * pluggable additional serializer providing components.
 */
public class BeanSerializerFactory
    extends BasicSerializerFactory
{
    /**
     * Like {@link BasicSerializerFactory}, this factory is stateless, and
     * thus a single shared global (== singleton) instance can be used
     * without thread-safety issues.
     */
    public final static BeanSerializerFactory instance = new BeanSerializerFactory(null);

    /**
     * Configuration settings for this factory; immutable instance (just like this
     * factory), new version created via copy-constructor (fluent-style)
     * 
     * @since 1.7
     */
    protected final Config _factoryConfig;

    /*
    /**********************************************************
    /* Config class implementation
    /**********************************************************
     */
    
    /**
     * Configuration settings container class for bean serializer factory
     * 
     * @since 1.7
     */
    public static class ConfigImpl extends Config
    {
        /**
         * Constant for empty <code>Serializers</code> array (which by definition
         * is stateless and reusable)
         */
        protected final static Serializers[] NO_SERIALIZERS = new Serializers[0];

        protected final static BeanSerializerModifier[] NO_MODIFIERS = new BeanSerializerModifier[0];
        
        /**
         * List of providers for additional serializers, checked before considering default
         * basic or bean serialializers.
         * 
         * @since 1.7
         */
        protected final Serializers[] _additionalSerializers;

        /**
         * @since 1.8
         */
        protected final Serializers[] _additionalKeySerializers;
        
        /**
         * List of modifiers that can change the way {@link BeanSerializer} instances
         * are configured and constructed.
         */
        protected final BeanSerializerModifier[] _modifiers;
        
        public ConfigImpl() {
            this(null, null, null);
        }

        protected ConfigImpl(Serializers[] allAdditionalSerializers,
                Serializers[] allAdditionalKeySerializers,
                BeanSerializerModifier[] modifiers)
        {
            _additionalSerializers = (allAdditionalSerializers == null) ?
                    NO_SERIALIZERS : allAdditionalSerializers;
            _additionalKeySerializers = (allAdditionalKeySerializers == null) ?
                    NO_SERIALIZERS : allAdditionalKeySerializers;
            _modifiers = (modifiers == null) ? NO_MODIFIERS : modifiers;
        }

        @Override
        public Config withAdditionalSerializers(Serializers additional)
        {
            if (additional == null) {
                throw new IllegalArgumentException("Can not pass null Serializers");
            }
            Serializers[] all = ArrayBuilders.insertInListNoDup(_additionalSerializers, additional);
            return new ConfigImpl(all, _additionalKeySerializers, _modifiers);
        }

        @Override
        public Config withAdditionalKeySerializers(Serializers additional)
        {
            if (additional == null) {
                throw new IllegalArgumentException("Can not pass null Serializers");
            }
            Serializers[] all = ArrayBuilders.insertInListNoDup(_additionalKeySerializers, additional);
            return new ConfigImpl(_additionalSerializers, all, _modifiers);
        }
        
        @Override
        public Config withSerializerModifier(BeanSerializerModifier modifier)
        {
            if (modifier == null) {
                throw new IllegalArgumentException("Can not pass null modifier");
            }
            BeanSerializerModifier[] modifiers = ArrayBuilders.insertInListNoDup(_modifiers, modifier);
            return new ConfigImpl(_additionalSerializers, _additionalKeySerializers, modifiers);
        }

        @Override
        public boolean hasSerializers() { return _additionalSerializers.length > 0; }

        @Override
        public boolean hasKeySerializers() { return _additionalKeySerializers.length > 0; }
        
        @Override
        public boolean hasSerializerModifiers() { return _modifiers.length > 0; }
        
        @Override
        public Iterable<Serializers> serializers() {
            return ArrayBuilders.arrayAsIterable(_additionalSerializers);
        }

        @Override
        public Iterable<Serializers> keySerializers() {
            return ArrayBuilders.arrayAsIterable(_additionalKeySerializers);
        }
        
        @Override
        public Iterable<BeanSerializerModifier> serializerModifiers() {
            return ArrayBuilders.arrayAsIterable(_modifiers);
        }
    }

    /*
    /**********************************************************
    /* Life-cycle: creation, configuration
    /**********************************************************
     */

    /**
     * Constructor for creating instances with specified configuration.
     */
    protected BeanSerializerFactory(Config config)
    {
        if (config == null) {
            config = new ConfigImpl();
        }
        _factoryConfig = config;
    }

    @Override public Config getConfig() { return _factoryConfig; }
    
    /**
     * Method used by module registration functionality, to attach additional
     * serializer providers into this serializer factory. This is typically
     * handled by constructing a new instance with additional serializers,
     * to ensure thread-safe access.
     * 
     * @since 1.7
     */
    @Override
    public SerializerFactory withConfig(Config config)
    {
        if (_factoryConfig == config) {
            return this;
        }
        /* 22-Nov-2010, tatu: Handling of subtypes is tricky if we do immutable-with-copy-ctor;
         *    and we pretty much have to here either choose between losing subtype instance
         *    when registering additional serializers, or losing serializers.
         *    Instead, let's actually just throw an error if this method is called when subtype
         *    has not properly overridden this method; this to indicate problem as soon as possible.
         */
        if (getClass() != BeanSerializerFactory.class) {
            throw new IllegalStateException("Subtype of BeanSerializerFactory ("+getClass().getName()
                    +") has not properly overridden method 'withAdditionalSerializers': can not instantiate subtype with "
                    +"additional serializer definitions");
        }
        return new BeanSerializerFactory(config);
    }

    @Override
    protected Iterable<Serializers> customSerializers() {
        return _factoryConfig.serializers();
    }
    
    /*
    /**********************************************************
    /* SerializerFactory impl
    /**********************************************************
     */

    /**
     * Main serializer constructor method. We will have to be careful
     * with respect to ordering of various method calls: essentially
     * we want to reliably figure out which classes are standard types,
     * and which are beans. The problem is that some bean Classes may
     * implement standard interfaces (say, {@link java.lang.Iterable}.
     *<p>
     * Note: sub-classes may choose to complete replace implementation,
     * if they want to alter priority of serializer lookups.
     */
    @Override
    @SuppressWarnings("unchecked")
    public JsonSerializer<Object> createSerializer(SerializationConfig config, JavaType origType,
            BeanProperty property)
        throws JsonMappingException
    {
        // Very first thing, let's check if there is explicit serializer annotation:
        BasicBeanDescription beanDesc = config.introspect(origType);
        JsonSerializer<?> ser = findSerializerFromAnnotation(config, beanDesc.getClassInfo(), property);
        if (ser != null) {
            return (JsonSerializer<Object>) ser;
        }

        // Next: we may have annotations that further define types to use...
        JavaType type = modifyTypeByAnnotation(config, beanDesc.getClassInfo(), origType);
        // and if so, we consider it implicit "force static typing" instruction
        boolean staticTyping = (type != origType);
	if (type != origType && type.getRawClass() != origType.getRawClass()) {
	    // [JACKSON-799]: need to re-introspect
	    beanDesc = config.introspect(type);
	}
        
        // Container types differ from non-container types:
        if (origType.isContainerType()) {
            return (JsonSerializer<Object>) buildContainerSerializer(config, type, beanDesc, property, staticTyping);
        }

        // Modules may provide serializers of all types:
        for (Serializers serializers : _factoryConfig.serializers()) {
            ser = serializers.findSerializer(config, type, beanDesc, property);
            if (ser != null) {
                return (JsonSerializer<Object>) ser;
            }
        }

        /* Otherwise, we will check "primary types"; both marker types that
         * indicate specific handling (JsonSerializable), or main types that have
         * precedence over container types
         */
        ser = findSerializerByLookup(type, config, beanDesc, property, staticTyping);
        if (ser == null) {
            ser = findSerializerByPrimaryType(type, config, beanDesc, property, staticTyping);
            if (ser == null) {
                /* And this is where this class comes in: if type is not a
                 * known "primary JDK type", perhaps it's a bean? We can still
                 * get a null, if we can't find a single suitable bean property.
                 */
                ser = findBeanSerializer(config, type, beanDesc, property);
                /* Finally: maybe we can still deal with it as an
                 * implementation of some basic JDK interface?
                 */
                if (ser == null) {
                    ser = findSerializerByAddonType(config, type, beanDesc, property, staticTyping);
                }
            }
        }
        return (JsonSerializer<Object>) ser;
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonSerializer<Object> createKeySerializer(SerializationConfig config, JavaType type,
            BeanProperty property)
    {
        // Minor optimization: to avoid constructing beanDesc, bail out if none registered
        if (!_factoryConfig.hasKeySerializers()) {
            return null;
        }
        
        // We should not need any member method info; at most class annotations for Map type
        BasicBeanDescription beanDesc = config.introspectClassAnnotations(type.getRawClass());
        JsonSerializer<?> ser = null;
        
        // Only thing we have here are module-provided key serializers:
        for (Serializers serializers : _factoryConfig.keySerializers()) {
            ser = serializers.findSerializer(config, type, beanDesc, property);
            if (ser != null) {
                break;
            }
        }
        return (JsonSerializer<Object>) ser;
    }
    
    /*
    /**********************************************************
    /* Other public methods that are not part of
    /* JsonSerializerFactory API
    /**********************************************************
     */

    /**
     * Method that will try to construct a {@link BeanSerializer} for
     * given class. Returns null if no properties are found.
     */
    @SuppressWarnings("unchecked")
    public JsonSerializer<Object> findBeanSerializer(SerializationConfig config, JavaType type,
            BasicBeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException
    {
        // First things first: we know some types are not beans...
        if (!isPotentialBeanType(type.getRawClass())) {
            return null;
        }
        JsonSerializer<Object> serializer = constructBeanSerializer(config, beanDesc, property);
        // [JACKSON-440] Need to allow overriding actual serializer, as well...
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                serializer = (JsonSerializer<Object>)mod.modifySerializer(config, beanDesc, serializer);
            }
        }
        return serializer;
    }

    /**
     * Method called to create a type information serializer for values of given
     * non-container property
     * if one is needed. If not needed (no polymorphic handling configured), should
     * return null.
     *
     * @param baseType Declared type to use as the base type for type information serializer
     * 
     * @return Type serializer to use for property values, if one is needed; null if not.
     * 
     * @since 1.5
     */
    public TypeSerializer findPropertyTypeSerializer(JavaType baseType, SerializationConfig config,
            AnnotatedMember accessor, BeanProperty property)
        throws JsonMappingException
    {
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findPropertyTypeResolver(config, accessor, baseType);        
        // Defaulting: if no annotations on member, check value class
        if (b == null) {
            return createTypeSerializer(config, baseType, property);
        }
        Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(accessor, config, ai);
        return b.buildTypeSerializer(config, baseType, subtypes, property);
    }

    /**
     * Method called to create a type information serializer for values of given
     * container property
     * if one is needed. If not needed (no polymorphic handling configured), should
     * return null.
     *
     * @param containerType Declared type of the container to use as the base type for type information serializer
     * 
     * @return Type serializer to use for property value contents, if one is needed; null if not.
     * 
     * @since 1.5
     */    
    public TypeSerializer findPropertyContentTypeSerializer(JavaType containerType, SerializationConfig config,
            AnnotatedMember accessor, BeanProperty property)
        throws JsonMappingException
    {
        JavaType contentType = containerType.getContentType();
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findPropertyContentTypeResolver(config, accessor, containerType);        
        // Defaulting: if no annotations on member, check value class
        if (b == null) {
            return createTypeSerializer(config, contentType, property);
        }
        Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(accessor, config, ai);
        return b.buildTypeSerializer(config, contentType, subtypes, property);
    }
    
    /*
    /**********************************************************
    /* Overridable non-public factory methods
    /**********************************************************
     */

    /**
     * Method called to construct serializer for serializing specified bean type.
     * 
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
    protected JsonSerializer<Object> constructBeanSerializer(SerializationConfig config,
            BasicBeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException
    {
        // 13-Oct-2010, tatu: quick sanity check: never try to create bean serializer for plain Object
        if (beanDesc.getBeanClass() == Object.class) {
            throw new IllegalArgumentException("Can not create bean serializer for Object.class");
        }
        
        BeanSerializerBuilder builder = constructBeanSerializerBuilder(beanDesc);
        
        // First: any detectable (auto-detect, annotations) properties to serialize?
        List<BeanPropertyWriter> props = findBeanProperties(config, beanDesc);

        if (props == null) {
            props = new ArrayList<BeanPropertyWriter>();
        }
        // [JACKSON-440] Need to allow modification bean properties to serialize:
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                props = mod.changeProperties(config, beanDesc, props);
            }
        }
        
        // Any properties to suppress?
        props = filterBeanProperties(config, beanDesc, props);
        // Do they need to be sorted in some special way?
        props = sortBeanProperties(config, beanDesc, props);
        
        // [JACKSON-440] Need to allow reordering of properties to serialize
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                props = mod.orderProperties(config, beanDesc, props);
            }
        }
        
        builder.setProperties(props);
        builder.setFilterId(findFilterId(config, beanDesc));
        
        AnnotatedMethod anyGetter = beanDesc.findAnyGetter();
        if (anyGetter != null) { // since 1.6
            if (config.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                anyGetter.fixAccess();
            }
            JavaType type = anyGetter.getType(beanDesc.bindingsForBeanType());
            // copied from BasicSerializerFactory.buildMapSerializer():
            boolean staticTyping = config.isEnabled(SerializationConfig.Feature.USE_STATIC_TYPING);
            JavaType valueType = type.getContentType();
            TypeSerializer typeSer = createTypeSerializer(config, valueType, property);
            // last 2 nulls; don't know key, value serializers (yet)
            MapSerializer mapSer = MapSerializer.construct(/* ignored props*/ null, type, staticTyping,
                    typeSer, property, null, null);
            builder.setAnyGetter(new AnyGetterWriter(anyGetter, mapSer));
        }
        // One more thing: need to gather view information, if any:
        processViews(config, builder);
        // And maybe let interested parties mess with the result bit more...
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                builder = mod.updateBuilder(config, beanDesc, builder);
            }
        }
        JsonSerializer<Object> ser = (JsonSerializer<Object>) builder.build();

        /* However, after all modifications: no properties, no serializer
         * (note; as per [JACKSON-670], check was moved later on from an earlier location)
         */
        if (ser == null) {
            /* 27-Nov-2009, tatu: Except that as per [JACKSON-201], we are
             *   ok with that as long as it has a recognized class annotation
             *  (which may come from a mix-in too)
             */
            if (beanDesc.hasKnownClassAnnotations()) {
                return builder.createDummy();
            }
        }
        return ser;
    }

    /**
     * Method called to construct a filtered writer, for given view
     * definitions. Default implementation constructs filter that checks
     * active view type to views property is to be included in.
     */
    protected BeanPropertyWriter constructFilteredBeanWriter(BeanPropertyWriter writer, Class<?>[] inViews)
    {
        return FilteredBeanPropertyWriter.constructViewBased(writer, inViews);
    }
    
    protected PropertyBuilder constructPropertyBuilder(SerializationConfig config,
                                                       BasicBeanDescription beanDesc)
    {
        return new PropertyBuilder(config, beanDesc);
    }

    protected BeanSerializerBuilder constructBeanSerializerBuilder(BasicBeanDescription beanDesc) {
        return new BeanSerializerBuilder(beanDesc);
    }

    /**
     * Method called to find filter that is configured to be used with bean
     * serializer being built, if any.
     * 
     * @since 1.7
     */
    protected Object findFilterId(SerializationConfig config, BasicBeanDescription beanDesc)
    {
        return config.getAnnotationIntrospector().findFilterId(beanDesc.getClassInfo());
    }
    
    /*
    /**********************************************************
    /* Overridable non-public introspection methods
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
        return (ClassUtil.canBeABeanType(type) == null) && !ClassUtil.isProxyType(type);
    }

    /**
     * Method used to collect all actual serializable properties.
     * Can be overridden to implement custom detection schemes.
     */
    protected List<BeanPropertyWriter> findBeanProperties(SerializationConfig config, BasicBeanDescription beanDesc)
        throws JsonMappingException
    {
        List<BeanPropertyDefinition> properties = beanDesc.findProperties();
        AnnotationIntrospector intr = config.getAnnotationIntrospector();

        // [JACKSON-429]: ignore specified types
        removeIgnorableTypes(config, beanDesc, properties);
        
        // and possibly remove ones without matching mutator...
        if (config.isEnabled(SerializationConfig.Feature.REQUIRE_SETTERS_FOR_GETTERS)) {
            removeSetterlessGetters(config, beanDesc, properties);
        }
        
        // nothing? can't proceed (caller may or may not throw an exception)
        if (properties.isEmpty()) {
            return null;
        }
        
        // null is for value type serializer, which we don't have access to from here (ditto for bean prop)
        boolean staticTyping = usesStaticTyping(config, beanDesc, null, null);
        PropertyBuilder pb = constructPropertyBuilder(config, beanDesc);

        ArrayList<BeanPropertyWriter> result = new ArrayList<BeanPropertyWriter>(properties.size());
        TypeBindings typeBind = beanDesc.bindingsForBeanType();
        // [JACKSON-98]: start with field properties, if any
        for (BeanPropertyDefinition property : properties) {
            AnnotatedMember accessor = property.getAccessor();
            // [JACKSON-235]: suppress writing of back references
            AnnotationIntrospector.ReferenceProperty prop = intr.findReferenceType(accessor);
            if (prop != null && prop.isBackReference()) {
                continue;
            }
            String name = property.getName();
            if (accessor instanceof AnnotatedMethod) {
                result.add(_constructWriter(config, typeBind, pb, staticTyping, name, (AnnotatedMethod) accessor));
            } else {
                result.add(_constructWriter(config, typeBind, pb, staticTyping, name, (AnnotatedField) accessor));
            }
        }
        return result;
    }

    /*
    /**********************************************************
    /* Overridable non-public methods for manipulating bean properties
    /**********************************************************
     */
    
    /**
     * Overridable method that can filter out properties. Default implementation
     * checks annotations class may have.
     */
    protected List<BeanPropertyWriter> filterBeanProperties(SerializationConfig config,
            BasicBeanDescription beanDesc, List<BeanPropertyWriter> props)
    {
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        AnnotatedClass ac = beanDesc.getClassInfo();
        String[] ignored = intr.findPropertiesToIgnore(ac);
        if (ignored != null && ignored.length > 0) {
            HashSet<String> ignoredSet = ArrayBuilders.arrayToSet(ignored);
            Iterator<BeanPropertyWriter> it = props.iterator();
            while (it.hasNext()) {
                if (ignoredSet.contains(it.next().getName())) {
                    it.remove();
                }
            }
        }
        return props;
    }

    /**
     * Overridable method that will impose given partial ordering on
     * list of discovered propertied. Method can be overridden to
     * provide custom ordering of properties, beyond configurability
     * offered by annotations (whic allow alphabetic ordering, as
     * well as explicit ordering by providing array of property names).
     *<p>
     * By default Creator properties will be ordered before other
     * properties. Explicit custom ordering will override this implicit
     * default ordering.
     */
    /**
     * Method that used to be called (pre-1.9) to impose configured
     * ordering on list of discovered properties.
     * With 1.9 it is not needed any more as ordering is done earlier.
     * 
     * @deprecated Since 1.9 this method does nothing, so there is no
     *    benefit from overriding it; it will be removed from 2.0.
     */
    @Deprecated
    protected List<BeanPropertyWriter> sortBeanProperties(SerializationConfig config,
            BasicBeanDescription beanDesc, List<BeanPropertyWriter> props)
    {
        return props;
    }

    /**
     * Method called to handle view information for constructed serializer,
     * based on bean property writers.
     *<p>
     * Note that this method is designed to be overridden by sub-classes
     * if they want to provide custom view handling. As such it is not
     * considered an internal implementation detail, and will be supported
     * as part of API going forward.
     *<p>
     * NOTE: signature of this method changed in 1.7, due to other significant
     * changes (esp. use of builder for serializer construction).
     */
    protected void processViews(SerializationConfig config, BeanSerializerBuilder builder)
    {
        // [JACKSON-232]: whether non-annotated fields are included by default or not is configurable
        List<BeanPropertyWriter> props = builder.getProperties();
        boolean includeByDefault = config.isEnabled(SerializationConfig.Feature.DEFAULT_VIEW_INCLUSION);
        final int propCount = props.size();
        int viewsFound = 0;
        BeanPropertyWriter[] filtered = new BeanPropertyWriter[propCount];
        // Simple: view information is stored within individual writers, need to combine:
        for (int i = 0; i < propCount; ++i) {
            BeanPropertyWriter bpw = props.get(i);
            Class<?>[] views = bpw.getViews();
            if (views == null) { // no view info? include or exclude by default?
                if (includeByDefault) {
                    filtered[i] = bpw;
                }
            } else {
                ++viewsFound;
                filtered[i] = constructFilteredBeanWriter(bpw, views);
            }
        }
        // minor optimization: if no view info, include-by-default, can leave out filtering info altogether:
        if (includeByDefault && viewsFound == 0) {
            return;
        }
        builder.setFilteredProperties(filtered);
    }

    /**
     * Method that will apply by-type limitations (as per [JACKSON-429]);
     * by default this is based on {@link com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonIgnoreType} annotation but
     * can be supplied by module-provided introspectors too.
     */
    protected void removeIgnorableTypes(SerializationConfig config, BasicBeanDescription beanDesc,
            List<BeanPropertyDefinition> properties)
    {
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        HashMap<Class<?>,Boolean> ignores = new HashMap<Class<?>,Boolean>();
        Iterator<BeanPropertyDefinition> it = properties.iterator();
        while (it.hasNext()) {
            BeanPropertyDefinition property = it.next();
            AnnotatedMember accessor = property.getAccessor();
            if (accessor == null) {
                it.remove();
                continue;
            }
            Class<?> type = accessor.getRawType();
            Boolean result = ignores.get(type);
            if (result == null) {
                BasicBeanDescription desc = config.introspectClassAnnotations(type);
                AnnotatedClass ac = desc.getClassInfo();
                result = intr.isIgnorableType(ac);
                // default to false, non-ignorable
                if (result == null) {
                    result = Boolean.FALSE;
                }
                ignores.put(type, result);
            }
            // lotsa work, and yes, it is ignorable type, so:
            if (result.booleanValue()) {
                it.remove();
            }
        }
    }

    /**
     * Helper method that will remove all properties that do not have a
     * mutator.
     * 
     * @since 1.9
     */
    protected void removeSetterlessGetters(SerializationConfig config, BasicBeanDescription beanDesc,
            List<BeanPropertyDefinition> properties)
    {
        Iterator<BeanPropertyDefinition> it = properties.iterator();
        while (it.hasNext()) {
            BeanPropertyDefinition property = it.next();
            // one caveat: as per [JACKSON-806], only remove implicit properties;
            // explicitly annotated ones should remain
            if (!property.couldDeserialize() && !property.isExplicitlyIncluded()) {
                it.remove();
            }
        }
    }
    
    /*
    /**********************************************************
    /* Internal helper methods
    /**********************************************************
     */

    /**
     * Secondary helper method for constructing {@link BeanPropertyWriter} for
     * given member (field or method).
     */
    protected BeanPropertyWriter _constructWriter(SerializationConfig config, TypeBindings typeContext,
            PropertyBuilder pb, boolean staticTyping, String name, AnnotatedMember accessor)
        throws JsonMappingException
    {
        if (config.isEnabled(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            accessor.fixAccess();
        }
        JavaType type = accessor.getType(typeContext);
        BeanProperty.Std property = new BeanProperty.Std(name, type, pb.getClassAnnotations(), accessor);

        // Does member specify a serializer? If so, let's use it.
        JsonSerializer<Object> annotatedSerializer = findSerializerFromAnnotation(config, accessor, property);
        // And how about polymorphic typing? First special to cover JAXB per-field settings:
        TypeSerializer contentTypeSer = null;
        if (ClassUtil.isCollectionMapOrArray(type.getRawClass())) {
            contentTypeSer = findPropertyContentTypeSerializer(type, config, accessor, property);
        }

        // and if not JAXB collection/array with annotations, maybe regular type info?
        TypeSerializer typeSer = findPropertyTypeSerializer(type, config, accessor, property);
        BeanPropertyWriter pbw = pb.buildWriter(name, type, annotatedSerializer,
                        typeSer, contentTypeSer, accessor, staticTyping);
        // how about views? (1.4+)
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        pbw.setViews(intr.findSerializationViews(accessor));
        return pbw;
    }
}
