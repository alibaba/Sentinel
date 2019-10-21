package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import java.text.DateFormat;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.Base64Variant;
import com.alibaba.acm.shaded.org.codehaus.jackson.Base64Variants;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.Annotated;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedClass;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.NopAnnotationIntrospector;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.VisibilityChecker;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.SubtypeResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.ClassKey;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.LinkedNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.node.JsonNodeFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Object that contains baseline configuration for deserialization
 * process. An instance is owned by {@link ObjectMapper}, which makes
 * a copy that is passed during serialization process to
 * {@link DeserializerProvider} and {@link DeserializerFactory}.
 *<p>
 * Note: although configuration settings can be changed at any time
 * (for factories and instances), they are not guaranteed to have
 * effect if called after constructing relevant mapper or deserializer
 * instance. This because some objects may be configured, constructed and
 * cached first time they are needed.
 *<p>
 * As of version 1.9, the goal is to make this class eventually immutable.
 * Because of this, existing methods that allow changing state of this
 * instance are deprecated in favor of methods that create new instances
 * with different configuration ("fluent factories")
 */
public class DeserializationConfig
    extends MapperConfig.Impl<DeserializationConfig.Feature, DeserializationConfig>
{
    /**
     * Enumeration that defines togglable features that guide
     * the serialization feature.
     */
    public enum Feature implements MapperConfig.ConfigFeature
    {
        /*
        /******************************************************
         *  Introspection features
        /******************************************************
         */

        /**
         * Feature that determines whether annotation introspection
         * is used for configuration; if enabled, configured
         * {@link AnnotationIntrospector} will be used: if disabled,
         * no annotations are considered.
         *<P>
         * Feature is enabled by default.
         *
         * @since 1.2
         */
        USE_ANNOTATIONS(true),

        /**
         * Feature that determines whether "setter" methods are
         * automatically detected based on standard Bean naming convention
         * or not. If yes, then all public one-argument methods that
         * start with prefix "set"
         * are considered setters. If disabled, only methods explicitly
         * annotated are considered setters.
         *<p>
         * Note that this feature has lower precedence than per-class
         * annotations, and is only used if there isn't more granular
         * configuration available.
         *<P>
         * Feature is enabled by default.
         */
        AUTO_DETECT_SETTERS(true),

        /**
         * Feature that determines whether "creator" methods are
         * automatically detected by consider public constructors,
         * and static single argument methods with name "valueOf".
         * If disabled, only methods explicitly annotated are considered
         * creator methods (except for the no-arg default constructor which
         * is always considered a factory method).
         *<p>
         * Note that this feature has lower precedence than per-class
         * annotations, and is only used if there isn't more granular
         * configuration available.
         *<P>
         * Feature is enabled by default.
         */
        AUTO_DETECT_CREATORS(true),

        /**
         * Feature that determines whether non-static fields are recognized as
         * properties.
         * If yes, then all public member fields
         * are considered as properties. If disabled, only fields explicitly
         * annotated are considered property fields.
         *<p>
         * Note that this feature has lower precedence than per-class
         * annotations, and is only used if there isn't more granular
         * configuration available.
         *<P>
         * Feature is enabled by default.
         *
         * @since 1.1
         */
        AUTO_DETECT_FIELDS(true),

        /**
         * Feature that determines whether otherwise regular "getter"
         * methods (but only ones that handle Collections and Maps,
         * not getters of other type)
         * can be used for purpose of getting a reference to a Collection
         * and Map to modify the property, without requiring a setter
         * method.
         * This is similar to how JAXB framework sets Collections and
         * Maps: no setter is involved, just setter.
         *<p>
         * Note that such getters-as-setters methods have lower
         * precedence than setters, so they are only used if no
         * setter is found for the Map/Collection property.
         *<p>
         * Feature is enabled by default.
         */
        USE_GETTERS_AS_SETTERS(true),

        /**
         * Feature that determines whether method and field access
         * modifier settings can be overridden when accessing
         * properties. If enabled, method
         * {@link java.lang.reflect.AccessibleObject#setAccessible}
         * may be called to enable access to otherwise unaccessible
         * objects.
         */
        CAN_OVERRIDE_ACCESS_MODIFIERS(true),

        /*
        /******************************************************
        /* Type conversion features
        /******************************************************
         */

        /**
         * Feature that determines whether Json floating point numbers
         * are to be deserialized into {@link java.math.BigDecimal}s
         * if only generic type description (either {@link Object} or
         * {@link Number}, or within untyped {@link java.util.Map}
         * or {@link java.util.Collection} context) is available.
         * If enabled such values will be deserialized as {@link java.math.BigDecimal}s;
         * if disabled, will be deserialized as {@link Double}s.
         * <p>
         * Feature is disabled by default, meaning that "untyped" floating
         * point numbers will by default be deserialized as {@link Double}s
         * (choice is for performance reason -- BigDecimals are slower than
         * Doubles)
         */
        USE_BIG_DECIMAL_FOR_FLOATS(false),

        /**
         * Feature that determines whether Json integral (non-floating-point)
         * numbers are to be deserialized into {@link java.math.BigInteger}s
         * if only generic type description (either {@link Object} or
         * {@link Number}, or within untyped {@link java.util.Map}
         * or {@link java.util.Collection} context) is available.
         * If enabled such values will be deserialized as
         * {@link java.math.BigInteger}s;
         * if disabled, will be deserialized as "smallest" available type,
         * which is either {@link Integer}, {@link Long} or
         * {@link java.math.BigInteger}, depending on number of digits.
         * <p>
         * Feature is disabled by default, meaning that "untyped" floating
         * point numbers will by default be deserialized using whatever
         * is the most compact integral type, to optimize efficiency.
         */
        USE_BIG_INTEGER_FOR_INTS(false),

        // [JACKSON-652]
        /**
         * Feature that determines whether JSON Array is mapped to
         * <code>Object[]</code> or <code>List&lt;Object></code> when binding
         * "untyped" objects (ones with nominal type of <code>java.lang.Object</code>).
         * If true, binds as <code>Object[]</code>; if false, as <code>List&lt;Object></code>.
         *<p>
         * Feature is disabled by default, meaning that JSON arrays are bound as
         * {@link java.util.List}s.
         * 
         * @since 1.9
         */
        USE_JAVA_ARRAY_FOR_JSON_ARRAY(false),
        
        /**
         * Feature that determines standard deserialization mechanism used for
         * Enum values: if enabled, Enums are assumed to have been serialized  using
         * return value of <code>Enum.toString()</code>;
         * if disabled, return value of <code>Enum.name()</code> is assumed to have been used.
         * Since pre-1.6 method was to use Enum name, this is the default.
         *<p>
         * Note: this feature should usually have same value
         * as {@link SerializationConfig.Feature#WRITE_ENUMS_USING_TO_STRING}.
         *<p>
         * For further details, check out [JACKSON-212]
         * 
         * @since 1.6
         */
        READ_ENUMS_USING_TO_STRING(false),
        
        /*
        /******************************************************
         *  Error handling features
        /******************************************************
         */

        /**
         * Feature that determines whether encountering of unknown
         * properties (ones that do not map to a property, and there is
         * no "any setter" or handler that can handle it)
         * should result in a failure (by throwing a
         * {@link JsonMappingException}) or not.
         * This setting only takes effect after all other handling
         * methods for unknown properties have been tried, and
         * property remains unhandled.
         *<p>
         * Feature is enabled by default, meaning that 
         * {@link JsonMappingException} is thrown if an unknown property
         * is encountered. This is the implicit default prior to
         * introduction of the feature.
         *
         * @since 1.2
         */
        FAIL_ON_UNKNOWN_PROPERTIES(true),

        /**
         * Feature that determines whether encountering of JSON null
         * is an error when deserializing into Java primitive types
         * (like 'int' or 'double'). If it is, a JsonProcessingException
         * is thrown to indicate this; if not, default value is used
         * (0 for 'int', 0.0 for double, same defaulting as what JVM uses).
         *<p>
         * Feature is disabled by default (to be consistent with behavior
         * of Jackson 1.6),
         * i.e. to allow use of nulls for primitive properties.
         * 
         * @since 1.7
         */
        FAIL_ON_NULL_FOR_PRIMITIVES(false),

        /**
         * Feature that determines whether JSON integer numbers are valid
         * values to be used for deserializing Java enum values.
         * If set to 'false' numbers are acceptable and are used to map to
         * ordinal() of matching enumeration value; if 'true', numbers are
         * not allowed and a {@link JsonMappingException} will be thrown.
         * Latter behavior makes sense if there is concern that accidental
         * mapping from integer values to enums might happen (and when enums
         * are always serialized as JSON Strings)
         *<p>
         * Feature is disabled by default (to be consistent with behavior
         * of Jackson 1.6), 
         * i.e. to allow use of JSON integers for Java enums.
         * 
         * @since 1.7
         */
        FAIL_ON_NUMBERS_FOR_ENUMS(false),

        /**
         * Feature that determines whether Jackson code should catch
         * and wrap {@link Exception}s (but never {@link Error}s!)
         * to add additional information about
         * location (within input) of problem or not. If enabled,
         * most exceptions will be caught and re-thrown (exception
         * specifically being that {@link java.io.IOException}s may be passed
         * as is, since they are declared as throwable); this can be
         * convenient both in that all exceptions will be checked and
         * declared, and so there is more contextual information.
         * However, sometimes calling application may just want "raw"
         * unchecked exceptions passed as is.
         *<p>
         * Feature is enabled by default, and is similar in behavior
         * to default prior to 1.7.
         * 
         * @since 1.7
         */
        WRAP_EXCEPTIONS(true),
        
        /*
        /******************************************************
         *  Structural conversion features
        /******************************************************
         */

        /**
         * Feature that determines whether it is acceptable to coerce non-array
         * (in JSON) values to work with Java collection (arrays, java.util.Collection)
         * types. If enabled, collection deserializers will try to handle non-array
         * values as if they had "implicit" surrounding JSON array.
         * This feature is meant to be used for compatibility/interoperability reasons,
         * to work with packages (such as XML-to-JSON converters) that leave out JSON
         * array in cases where there is just a single element in array.
         * 
         * @since 1.8
         */
        ACCEPT_SINGLE_VALUE_AS_ARRAY(false),
        
        /**
         * Feature to allow "unwrapping" root-level JSON value, to match setting of
         * {@link SerializationConfig.Feature#WRAP_ROOT_VALUE} used for serialization.
         * Will verify that the root JSON value is a JSON Object, and that it has
         * a single property with expected root name. If not, a
         * {@link JsonMappingException} is thrown; otherwise value of the wrapped property
         * will be deserialized as if it was the root value.
         * 
         * @since 1.9
         */
        UNWRAP_ROOT_VALUE(false),

        /*
        /******************************************************
         *  Value conversion features
        /******************************************************
         */
        
        /**
         * Feature that can be enabled to allow JSON empty String
         * value ("") to be bound to POJOs as null.
         * If disabled, standard POJOs can only be bound from JSON null or
         * JSON Object (standard meaning that no custom deserializers or
         * constructors are defined; both of which can add support for other
         * kinds of JSON values); if enable, empty JSON String can be taken
         * to be equivalent of JSON null.
         * 
         * @since 1.8
         */
        ACCEPT_EMPTY_STRING_AS_NULL_OBJECT(false)
        
        ;

        final boolean _defaultState;
	        
        private Feature(boolean defaultState) {
            _defaultState = defaultState;
        }

        @Override
        public boolean enabledByDefault() { return _defaultState; }
    
        @Override
        public int getMask() { return (1 << ordinal()); }
    }

    /*
    /**********************************************************
    /* Configuration settings for deserialization
    /**********************************************************
     */

    /**
     * Linked list that contains all registered problem handlers.
     * Implementation as front-added linked list allows for sharing
     * of the list (tail) without copying the list.
     */
    protected LinkedNode<DeserializationProblemHandler> _problemHandlers;
    
    /**
     * Factory used for constructing {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode} instances.
     *
     * @since 1.6
     */
    protected final JsonNodeFactory _nodeFactory;

    /**
     * Feature flag from {@link SerializationConfig} which is needed to
     * know if serializer will by default sort properties in
     * alphabetic order.
     *<p>
     * Note that although this property is not marked as final,
     * it is handled like it was, except for the fact that it is
     * assigned with a call to {@link #passSerializationFeatures}
     * instead of constructor.
     * 
     * @since 1.9
     */
    protected boolean _sortPropertiesAlphabetically;
    
    /*
    /**********************************************************
    /* Life-cycle, constructors
    /**********************************************************
     */

    /**
     * Constructor used by ObjectMapper to create default configuration object instance.
     */
    public DeserializationConfig(ClassIntrospector<? extends BeanDescription> intr,
            AnnotationIntrospector annIntr, VisibilityChecker<?> vc,
            SubtypeResolver subtypeResolver, PropertyNamingStrategy propertyNamingStrategy,
            TypeFactory typeFactory, HandlerInstantiator handlerInstantiator)
    {
        super(intr, annIntr, vc, subtypeResolver, propertyNamingStrategy, typeFactory, handlerInstantiator,
                collectFeatureDefaults(DeserializationConfig.Feature.class));
        _nodeFactory = JsonNodeFactory.instance;
    }
    
    /**
     * @since 1.8
     */
    protected DeserializationConfig(DeserializationConfig src) {
        this(src, src._base);
    }

    /**
     * Copy constructor used to create a non-shared instance with given mix-in
     * annotation definitions and subtype resolver.
     * 
     * @since 1.8
     */
    private DeserializationConfig(DeserializationConfig src,
            HashMap<ClassKey,Class<?>> mixins, SubtypeResolver str)
    {
        this(src, src._base);
        _mixInAnnotations = mixins;
        _subtypeResolver = str;
    }
    
    /**
     * @since 1.8
     */
    protected DeserializationConfig(DeserializationConfig src, MapperConfig.Base base)
    {
        super(src, base, src._subtypeResolver);
        _problemHandlers = src._problemHandlers;
        _nodeFactory = src._nodeFactory;
        _sortPropertiesAlphabetically = src._sortPropertiesAlphabetically;
    }
    
    /**
     * @since 1.8
     */
    protected DeserializationConfig(DeserializationConfig src, JsonNodeFactory f)
    {
        super(src);
        _problemHandlers = src._problemHandlers;
        _nodeFactory = f;
        _sortPropertiesAlphabetically = src._sortPropertiesAlphabetically;
    }

    /**
     * @since 1.9
     */
    protected DeserializationConfig(DeserializationConfig src, int featureFlags)
    {
        super(src, featureFlags);
        _problemHandlers = src._problemHandlers;
        _nodeFactory = src._nodeFactory;
        _sortPropertiesAlphabetically = src._sortPropertiesAlphabetically;
    }
    
    /**
     * Helper method to be called right after creating a non-shared
     * instance, needed to pass state of feature(s) shared with
     * SerializationConfig.
     * 
     * Since 1.9
     */
    protected DeserializationConfig passSerializationFeatures(int serializationFeatureFlags)
    {
        _sortPropertiesAlphabetically = (serializationFeatureFlags
                & SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY.getMask()) != 0;
        return this;
    }
    
    /*
    /**********************************************************
    /* Life-cycle, factory methods from MapperConfig
    /**********************************************************
     */

    @Override
    public DeserializationConfig withClassIntrospector(ClassIntrospector<? extends BeanDescription> ci) {
        return new DeserializationConfig(this, _base.withClassIntrospector(ci));
    }

    @Override
    public DeserializationConfig withAnnotationIntrospector(AnnotationIntrospector ai) {
        return new DeserializationConfig(this, _base.withAnnotationIntrospector(ai));
    }

    @Override
    public DeserializationConfig withVisibilityChecker(VisibilityChecker<?> vc) {
        return new DeserializationConfig(this, _base.withVisibilityChecker(vc));
    }

    @Override
    public DeserializationConfig withVisibility(JsonMethod forMethod, JsonAutoDetect.Visibility visibility) {
        return new DeserializationConfig(this, _base.withVisibility(forMethod, visibility));
    }
    
    @Override
    public DeserializationConfig withTypeResolverBuilder(TypeResolverBuilder<?> trb) {
        return new DeserializationConfig(this, _base.withTypeResolverBuilder(trb));
    }

    @Override
    public DeserializationConfig withSubtypeResolver(SubtypeResolver str)
    {
        DeserializationConfig cfg = new DeserializationConfig(this);
        cfg._subtypeResolver = str;
        return cfg;
    }
    
    @Override
    public DeserializationConfig withPropertyNamingStrategy(PropertyNamingStrategy pns) {
        return new DeserializationConfig(this, _base.withPropertyNamingStrategy(pns));
    }
    
    @Override
    public DeserializationConfig withTypeFactory(TypeFactory tf) {
        return (tf == _base.getTypeFactory()) ? this : new DeserializationConfig(this, _base.withTypeFactory(tf));
    }

    @Override
    public DeserializationConfig withDateFormat(DateFormat df) {
        return (df == _base.getDateFormat()) ? this : new DeserializationConfig(this, _base.withDateFormat(df));
    }
    
    @Override
    public DeserializationConfig withHandlerInstantiator(HandlerInstantiator hi) {
        return (hi == _base.getHandlerInstantiator()) ? this : new DeserializationConfig(this, _base.withHandlerInstantiator(hi));
    }

    @Override
    public DeserializationConfig withInsertedAnnotationIntrospector(AnnotationIntrospector ai) {
        return new DeserializationConfig(this, _base.withInsertedAnnotationIntrospector(ai));
    }

    @Override
    public DeserializationConfig withAppendedAnnotationIntrospector(AnnotationIntrospector ai) {
        return new DeserializationConfig(this, _base.withAppendedAnnotationIntrospector(ai));
    }
    
    /*
    /**********************************************************
    /* Life-cycle, deserialization-specific factory methods
    /**********************************************************
     */

    /**
     * Fluent factory method that will construct a new instance with
     * specified {@link JsonNodeFactory}
     * 
     * @since 1.8
     */
    public DeserializationConfig withNodeFactory(JsonNodeFactory f) {
        return new DeserializationConfig(this, f);
    }

    /**
     * Fluent factory method that will construct and return a new configuration
     * object instance with specified features enabled.
     * 
     * @since 1.9
     */
    @Override
    public DeserializationConfig with(DeserializationConfig.Feature... features)
    {
        int flags = _featureFlags;
        for (Feature f : features) {
            flags |= f.getMask();
        }
        return new DeserializationConfig(this, flags);
    }

    /**
     * Fluent factory method that will construct and return a new configuration
     * object instance with specified features disabled.
     * 
     * @since 1.9
     */
    @Override
    public DeserializationConfig without(DeserializationConfig.Feature... features)
    {
        int flags = _featureFlags;
        for (Feature f : features) {
            flags &= ~f.getMask();
        }
        return new DeserializationConfig(this, flags);
    }
    
    /*
    /**********************************************************
    /* MapperConfig implementation
    /**********************************************************
     */
    
    /**
     * Method that checks class annotations that the argument Object has,
     * and modifies settings of this configuration object accordingly,
     * similar to how those annotations would affect actual value classes
     * annotated with them, but with global scope. Note that not all
     * annotations have global significance, and thus only subset of
     * Jackson annotations will have any effect.
     *<p>
     * Ones that are known to have effect are:
     *<ul>
     * <li>{@link JsonAutoDetect}</li>
     *</ul>
     * 
     * @param cls Class of which class annotations to use
     *   for changing configuration settings
     *   
     * @deprecated Since 1.9, it is preferably to explicitly configure
     *   instances; this method also modifies existing instance which is
     *   against immutable design goals of this class.
     */
    @Deprecated
    @Override
    public void fromAnnotations(Class<?> cls)
    {
    	/* no class annotation for:
         *
         * - CAN_OVERRIDE_ACCESS_MODIFIERS
         * - USE_BIG_DECIMAL_FOR_FLOATS
         * - USE_BIG_INTEGER_FOR_INTS
         * - USE_GETTERS_AS_SETTERS
         */

        /* 10-Jul-2009, tatu: Should be able to just pass null as
         *    'MixInResolver'; no mix-ins set at this point
         */
        AnnotationIntrospector ai = getAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(cls, ai, null);
        // visibility checks handled via separate checker object...
        VisibilityChecker<?> prevVc = getDefaultVisibilityChecker();
        _base = _base.withVisibilityChecker(ai.findAutoDetectVisibility(ac, prevVc));
    }

    /**
     * Method that is called to create a non-shared copy of the configuration
     * to be used for a deserialization operation.
     * Note that if sub-classing
     * and sub-class has additional instance methods,
     * this method <b>must</b> be overridden to produce proper sub-class
     * instance.
     */
    @Override
    public DeserializationConfig createUnshared(SubtypeResolver subtypeResolver)
    {
        HashMap<ClassKey,Class<?>> mixins = _mixInAnnotations;
        // ensure that we assume sharing at this point:
        _mixInAnnotationsShared = true;
        return new DeserializationConfig(this, mixins, subtypeResolver);
    }

    /**
     * Method for getting {@link AnnotationIntrospector} configured
     * to introspect annotation values used for configuration.
     */
    @Override
    public AnnotationIntrospector getAnnotationIntrospector()
    {
        /* 29-Jul-2009, tatu: it's now possible to disable use of
         *   annotations; can be done using "no-op" introspector
         */
        if (isEnabled(Feature.USE_ANNOTATIONS)) {
            return super.getAnnotationIntrospector();
        }
        return NopAnnotationIntrospector.instance;
    }
    
    /**
     * Accessor for getting bean description that only contains class
     * annotations: useful if no getter/setter/creator information is needed.
     *<p>
     * Note: part of {@link MapperConfig} since 1.7
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BeanDescription> T introspectClassAnnotations(JavaType type) {
        return (T) getClassIntrospector().forClassAnnotations(this, type, this);
    }

    /**
     * Accessor for getting bean description that only contains immediate class
     * annotations: ones from the class, and its direct mix-in, if any, but
     * not from super types.
     *<p>
     * Note: part of {@link MapperConfig} since 1.7
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BeanDescription> T introspectDirectClassAnnotations(JavaType type) {
        return (T) getClassIntrospector().forDirectClassAnnotations(this, type, this);
    }
    
    @Override
    public boolean isAnnotationProcessingEnabled() {
        return isEnabled(Feature.USE_ANNOTATIONS);
    }

    @Override
    public boolean canOverrideAccessModifiers() {
        return isEnabled(Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
    }

    @Override
    public boolean shouldSortPropertiesAlphabetically() {
        return _sortPropertiesAlphabetically;
    }

    @Override
    public VisibilityChecker<?> getDefaultVisibilityChecker()
    {
        VisibilityChecker<?> vchecker = super.getDefaultVisibilityChecker();
        if (!isEnabled(DeserializationConfig.Feature.AUTO_DETECT_SETTERS)) {
            vchecker = vchecker.withSetterVisibility(Visibility.NONE);
        }
        if (!isEnabled(DeserializationConfig.Feature.AUTO_DETECT_CREATORS)) {
            vchecker = vchecker.withCreatorVisibility(Visibility.NONE);
        }
        if (!isEnabled(DeserializationConfig.Feature.AUTO_DETECT_FIELDS)) {
            vchecker = vchecker.withFieldVisibility(Visibility.NONE);
        }
        return vchecker;
    }

    /*
    /**********************************************************
    /* MapperConfig overrides for 1.8 backwards compatibility
    /**********************************************************
     */

    /* NOTE: these are overloads we MUST have, but that were missing
     * from 1.9.0 and 1.9.1. Type erasure can bite in the ass...
     *<p>
     * NOTE: will remove either these variants, or base class one, in 2.0.
     */
    
    /**
     * An overload for {@link MapperConfig#isEnabled(MapperConfig.ConfigFeature)},
     * needed for backwards-compatibility.
     *<p>
     * NOTE: will remove either this variant, or base class one, in 2.0./
     * 
     * @since 1.0 However, note that version 1.9.0 and 1.9.1 accidentally missed
     *    this overloaded variant
     */
    public boolean isEnabled(DeserializationConfig.Feature f) {
        return (_featureFlags & f.getMask()) != 0;
    }

    /**
     * @deprecated Since 1.9, it is preferable to use {@link #with} instead;
     *    this method is deprecated as it modifies current instance instead of
     *    creating a new one (as the goal is to make this class immutable)
     */
    @Deprecated
    @Override
    public void enable(DeserializationConfig.Feature f) {
        super.enable(f);
    }

    /** 
     * @deprecated Since 1.9, it is preferable to use {@link #without} instead;
     *    this method is deprecated as it modifies current instance instead of
     *    creating a new one (as the goal is to make this class immutable)
     */
    @Deprecated
    @Override
    public void disable(DeserializationConfig.Feature f) {
        super.disable(f);
    }

    /** 
     * @deprecated Since 1.9, it is preferable to use {@link #without} and {@link #with} instead;
     *    this method is deprecated as it modifies current instance instead of
     *    creating a new one (as the goal is to make this class immutable)
     */
    @Deprecated
    @Override
    public void set(DeserializationConfig.Feature f, boolean state) {
        super.set(f, state);
    }
    
    /*
    /**********************************************************
    /* Problem handlers
    /**********************************************************
     */

    /**
     * Method for getting head of the problem handler chain. May be null,
     * if no handlers have been added.
     */
    public LinkedNode<DeserializationProblemHandler> getProblemHandlers()
    {
        return _problemHandlers;
    }
    
    /**
     * Method that can be used to add a handler that can (try to)
     * resolve non-fatal deserialization problems.
     */
    public void addHandler(DeserializationProblemHandler h)
    {
        /* Sanity check: let's prevent adding same handler multiple
         * times
         */
        if (!LinkedNode.contains(_problemHandlers, h)) {
            _problemHandlers = new LinkedNode<DeserializationProblemHandler>(h, _problemHandlers);
        }
    }

    /**
     * Method for removing all configured problem handlers; usually done to replace
     * existing handler(s) with different one(s)
     *
     * @since 1.1
     */
    public void clearHandlers()
    {
        _problemHandlers = null;
    }

    /*
    /**********************************************************
    /* Other configuration
    /**********************************************************
     */

    /**
     * Method called during deserialization if Base64 encoded content
     * needs to be decoded. Default version just returns default Jackson
     * uses, which is modified-mime which does not add linefeeds (because
     * those would have to be escaped in JSON strings).
     */
    public Base64Variant getBase64Variant() {
        return Base64Variants.getDefaultVariant();
    }

    /**
     * @since 1.6
     */
    public final JsonNodeFactory getNodeFactory() {
        return _nodeFactory;
    }
    
    /*
    /**********************************************************
    /* Introspection methods
    /**********************************************************
     */

    /**
     * Method that will introspect full bean properties for the purpose
     * of building a bean deserializer
     *
     * @param type Type of class to be introspected
     */
    @SuppressWarnings("unchecked")
    public <T extends BeanDescription> T introspect(JavaType type) {
        return (T) getClassIntrospector().forDeserialization(this, type, this);
    }

    /**
     * Method that will introspect subset of bean properties needed to
     * construct bean instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends BeanDescription> T introspectForCreation(JavaType type) {
        return (T) getClassIntrospector().forCreation(this, type, this);
    }
    
    /*
    /**********************************************************
    /* Extended API: handler instantiation
    /**********************************************************
     */

    @SuppressWarnings("unchecked")
    public JsonDeserializer<Object> deserializerInstance(Annotated annotated,
            Class<? extends JsonDeserializer<?>> deserClass)
    {
        HandlerInstantiator hi = getHandlerInstantiator();
        if (hi != null) {
            JsonDeserializer<?> deser = hi.deserializerInstance(this, annotated, deserClass);
            if (deser != null) {
                return (JsonDeserializer<Object>) deser;
            }
        }
        return (JsonDeserializer<Object>) ClassUtil.createInstance(deserClass, canOverrideAccessModifiers());
    }

    public KeyDeserializer keyDeserializerInstance(Annotated annotated,
            Class<? extends KeyDeserializer> keyDeserClass)
    {
        HandlerInstantiator hi = getHandlerInstantiator();
        if (hi != null) {
            KeyDeserializer keyDeser = hi.keyDeserializerInstance(this, annotated, keyDeserClass);
            if (keyDeser != null) {
                return (KeyDeserializer) keyDeser;
            }
        }
        return (KeyDeserializer) ClassUtil.createInstance(keyDeserClass, canOverrideAccessModifiers());
    }

    public ValueInstantiator valueInstantiatorInstance(Annotated annotated,
            Class<? extends ValueInstantiator> instClass)
    {
        HandlerInstantiator hi = getHandlerInstantiator();
        if (hi != null) {
            ValueInstantiator inst = hi.valueInstantiatorInstance(this, annotated, instClass);
            if (inst != null) {
                return (ValueInstantiator) inst;
            }
        }
        return (ValueInstantiator) ClassUtil.createInstance(instClass, canOverrideAccessModifiers());
    }
}
