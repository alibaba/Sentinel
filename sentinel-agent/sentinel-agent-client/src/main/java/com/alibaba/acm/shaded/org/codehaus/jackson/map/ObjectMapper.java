package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.DateFormat;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonAutoDetect;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonMethod;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.SegmentedStringWriter;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.SerializedString;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JsonSerialize;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicClassIntrospector;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.VisibilityChecker;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.NamedType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.SubtypeResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl.StdSubtypeResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl.StdTypeResolverBuilder;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.SimpleType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeModifier;
import com.alibaba.acm.shaded.org.codehaus.jackson.node.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.schema.JsonSchema;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.TypeReference;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.*;

/**
 * This mapper (or, data binder, or codec) provides functionality for
 * converting between Java objects (instances of JDK provided core classes,
 * beans), and matching JSON constructs.
 * It will use instances of {@link JsonParser} and {@link JsonGenerator}
 * for implementing actual reading/writing of JSON.
 *<p>
 * The main conversion API is defined in {@link ObjectCodec}, so that
 * implementation details of this class need not be exposed to
 * streaming parser and generator classes.
 *<p>
 * Note on caching: root-level deserializers are always cached, and accessed
 * using full (generics-aware) type information. This is different from
 * caching of referenced types, which is more limited and is done only
 * for a subset of all deserializer types. The main reason for difference
 * is that at root-level there is no incoming reference (and hence no
 * referencing property, no referral information or annotations to
 * produce differing deserializers), and that the performance impact
 * greatest at root level (since it'll essentially cache the full
 * graph of deserializers involved).
 */
public class ObjectMapper
    extends ObjectCodec
    implements Versioned
{
    /*
    /**********************************************************
    /* Helper classes, enums
    /**********************************************************
     */

    /**
     * Enumeration used with {@link ObjectMapper#enableDefaultTyping()}
     * to specify what kind of types (classes) default typing should
     * be used for. It will only be used if no explicit type information
     * is found, but this enumeration further limits subset of those
     * types.
     * 
     * @since 1.5
     */
    public enum DefaultTyping {
        /**
         * This value means that only properties that have
         * {@link java.lang.Object} as declared type (including
         * generic types without explicit type) will use default
         * typing.
         */
        JAVA_LANG_OBJECT,
        
        /**
         * Value that means that default typing will be used for
         * properties with declared type of {@link java.lang.Object}
         * or an abstract type (abstract class or interface).
         * Note that this does <b>not</b> include array types.
         */
        OBJECT_AND_NON_CONCRETE,

        /**
         * Value that means that default typing will be used for
         * all types covered by {@link #OBJECT_AND_NON_CONCRETE}
         * plus all array types for them.
         */
        NON_CONCRETE_AND_ARRAYS,
        
        /**
         * Value that means that default typing will be used for
         * all non-final types, with exception of small number of
         * "natural" types (String, Boolean, Integer, Double), which
         * can be correctly inferred from JSON; as well as for
         * all arrays of non-final types.
         */
        NON_FINAL
    }

    /**
     * Customized {@link TypeResolverBuilder} that provides type resolver builders
     * used with so-called "default typing"
     * (see {@link ObjectMapper#enableDefaultTyping()} for details).
     *<p>
     * Type resolver construction is based on configuration: implementation takes care
     * of only providing builders in cases where type information should be applied.
     * This is important since build calls may be sent for any and all types, and
     * type information should NOT be applied to all of them.
     */
    public static class DefaultTypeResolverBuilder
        extends StdTypeResolverBuilder
    {
        /**
         * Definition of what types is this default typer valid for.
         */
        protected final DefaultTyping _appliesFor;

        public DefaultTypeResolverBuilder(DefaultTyping t) {
            _appliesFor = t;
        }

        @Override
        public TypeDeserializer buildTypeDeserializer(DeserializationConfig config,
                JavaType baseType, Collection<NamedType> subtypes, BeanProperty property)
        {
            return useForType(baseType) ? super.buildTypeDeserializer(config, baseType, subtypes, property) : null;
        }

        @Override
        public TypeSerializer buildTypeSerializer(SerializationConfig config,
                JavaType baseType, Collection<NamedType> subtypes, BeanProperty property)
        {
            return useForType(baseType) ? super.buildTypeSerializer(config, baseType, subtypes, property) : null;            
        }

        /**
         * Method called to check if the default type handler should be
         * used for given type.
         * Note: "natural types" (String, Boolean, Integer, Double) will never
         * use typing; that is both due to them being concrete and final,
         * and since actual serializers and deserializers will also ignore any
         * attempts to enforce typing.
         */
        public boolean useForType(JavaType t)
        {
            switch (_appliesFor) {
            case NON_CONCRETE_AND_ARRAYS:
                if (t.isArrayType()) {
                    t = t.getContentType();
                }
                // fall through
            case OBJECT_AND_NON_CONCRETE:
                return (t.getRawClass() == Object.class) || !t.isConcrete();
            case NON_FINAL:
                if (t.isArrayType()) {
                    t = t.getContentType();
                }
                return !t.isFinal(); // includes Object.class
            default:
            //case JAVA_LANG_OBJECT:
                return (t.getRawClass() == Object.class);
            }
        }
    }

    /*
    /**********************************************************
    /* Internal constants, singletons
    /**********************************************************
     */
    
    // Quick little shortcut, to avoid having to use global TypeFactory instance...
    private final static JavaType JSON_NODE_TYPE = SimpleType.constructUnsafe(JsonNode.class);

    /* !!! 03-Apr-2009, tatu: Should try to avoid direct reference... but not
     *   sure what'd be simple and elegant way. So until then:
     */
    protected final static ClassIntrospector<? extends BeanDescription> DEFAULT_INTROSPECTOR = BasicClassIntrospector.instance;

    // 16-May-2009, tatu: Ditto ^^^
    protected final static AnnotationIntrospector DEFAULT_ANNOTATION_INTROSPECTOR = new JacksonAnnotationIntrospector();

    /**
     * @since 1.5
     */
    protected final static VisibilityChecker<?> STD_VISIBILITY_CHECKER = VisibilityChecker.Std.defaultInstance();
    
    /*
    /**********************************************************
    /* Configuration settings, shared
    /**********************************************************
     */

    /**
     * Factory used to create {@link JsonParser} and {@link JsonGenerator}
     * instances as necessary.
     */
    protected final JsonFactory _jsonFactory;

    /**
     * Registered concrete subtypes that can be used instead of (or
     * in addition to) ones declared using annotations.
     * 
     * @since 1.6
     */
    protected SubtypeResolver _subtypeResolver;

    /**
     * Specific factory used for creating {@link JavaType} instances;
     * needed to allow modules to add more custom type handling
     * (mostly to support types of non-Java JVM languages)
     */
    protected TypeFactory _typeFactory;

    /**
     * Provider for values to inject in deserialized POJOs.
     * 
     * @since 1.9
     */
    protected InjectableValues _injectableValues;
    
    /*
    /**********************************************************
    /* Configuration settings, serialization
    /**********************************************************
     */

    /**
     * Configuration object that defines basic global
     * settings for the serialization process
     */
    protected SerializationConfig _serializationConfig;

    /**
     * Object that manages access to serializers used for serialization,
     * including caching.
     * It is configured with {@link #_serializerFactory} to allow
     * for constructing custom serializers.
     */
    protected SerializerProvider _serializerProvider;

    /**
     * Serializer factory used for constructing serializers.
     */
    protected SerializerFactory _serializerFactory;

    /*
    /**********************************************************
    /* Configuration settings, deserialization
    /**********************************************************
     */

    /**
     * Configuration object that defines basic global
     * settings for the serialization process
     */
    protected DeserializationConfig _deserializationConfig;

    /**
     * Object that manages access to deserializers used for deserializing
     * JSON content into Java objects, including possible caching
     * of the deserializers. It contains a reference to
     * {@link DeserializerFactory} to use for constructing acutal deserializers.
     */
    protected DeserializerProvider _deserializerProvider;

    /*
    /**********************************************************
    /* Caching
    /**********************************************************
     */

    /* Note: handling of serializers and deserializers is not symmetric;
     * and as a result, only root-level deserializers can be cached here.
     * This is mostly because typing and resolution for deserializers is
     * fully static; whereas it is quite dynamic for serialization.
     */

    /**
     * We will use a separate main-level Map for keeping track
     * of root-level deserializers. This is where most succesful
     * cache lookups get resolved.
     * Map will contain resolvers for all kinds of types, including
     * container types: this is different from the component cache
     * which will only cache bean deserializers.
     *<p>
     * Given that we don't expect much concurrency for additions
     * (should very quickly converge to zero after startup), let's
     * explicitly define a low concurrency setting.
     *<p>
     * Since version 1.5, these may are either "raw" deserializers (when
     * no type information is needed for base type), or type-wrapped
     * deserializers (if it is needed)
     */
    final protected ConcurrentHashMap<JavaType, JsonDeserializer<Object>> _rootDeserializers
        = new ConcurrentHashMap<JavaType, JsonDeserializer<Object>>(64, 0.6f, 2);

    /*
    /**********************************************************
    /* Life-cycle: constructing instance
    /**********************************************************
     */

    /**
     * Default constructor, which will construct the default
     * {@link JsonFactory} as necessary, use
     * {@link StdSerializerProvider} as its
     * {@link SerializerProvider}, and
     * {@link BeanSerializerFactory} as its
     * {@link SerializerFactory}.
     * This means that it
     * can serialize all standard JDK types, as well as regular
     * Java Beans (based on method names and Jackson-specific annotations),
     * but does not support JAXB annotations.
     */
    public ObjectMapper()
    {
        this(null, null, null);
    }

    /**
     * Construct mapper that uses specified {@link JsonFactory}
     * for constructing necessary {@link JsonParser}s and/or
     * {@link JsonGenerator}s.
     */
    public ObjectMapper(JsonFactory jf)
    {
        this(jf, null, null);
    }

    /**
     * Construct mapper that uses specified {@link SerializerFactory}
     * for constructing necessary serializers.
     *
     * @deprecated Use other constructors instead; note that
     *   you can just set serializer factory with {@link #setSerializerFactory}
     */
    @Deprecated
    public ObjectMapper(SerializerFactory sf)
    {
        this(null, null, null);
        setSerializerFactory(sf);
    }

    public ObjectMapper(JsonFactory jf,
                        SerializerProvider sp, DeserializerProvider dp)
    {
    	this(jf, sp, dp, null, null);
    }

    /**
     * 
     * @param jf JsonFactory to use: if null, a new {@link MappingJsonFactory} will be constructed
     * @param sp SerializerProvider to use: if null, a {@link StdSerializerProvider} will be constructed
     * @param dp DeserializerProvider to use: if null, a {@link StdDeserializerProvider} will be constructed
     * @param sconfig Serialization configuration to use; if null, basic {@link SerializationConfig}
     * 	will be constructed
     * @param dconfig Deserialization configuration to use; if null, basic {@link DeserializationConfig}
     * 	will be constructed
     */
    public ObjectMapper(JsonFactory jf,
            SerializerProvider sp, DeserializerProvider dp,
            SerializationConfig sconfig, DeserializationConfig dconfig)
    {
        /* 02-Mar-2009, tatu: Important: we MUST default to using
         *   the mapping factory, otherwise tree serialization will
         *   have problems with POJONodes.
         * 03-Jan-2010, tatu: and obviously we also must pass 'this',
         *    to create actual linking.
         */
        if (jf == null) {
            _jsonFactory = new MappingJsonFactory(this);
        } else {
            _jsonFactory = jf;
            if (jf.getCodec() == null) { // as per [JACKSON-741]
                _jsonFactory.setCodec(this);
            }
        }
        // and default type factory is shared one
        _typeFactory = TypeFactory.defaultInstance();
        _serializationConfig = (sconfig != null) ? sconfig :
            new SerializationConfig(DEFAULT_INTROSPECTOR, DEFAULT_ANNOTATION_INTROSPECTOR, STD_VISIBILITY_CHECKER,
                    null, null, _typeFactory, null);
        _deserializationConfig = (dconfig != null) ? dconfig :
            new DeserializationConfig(DEFAULT_INTROSPECTOR, DEFAULT_ANNOTATION_INTROSPECTOR, STD_VISIBILITY_CHECKER,
                    null, null, _typeFactory, null);
        _serializerProvider = (sp == null) ? new StdSerializerProvider() : sp;
        _deserializerProvider = (dp == null) ? new StdDeserializerProvider() : dp;

        // Default serializer factory is stateless, can just assign
        _serializerFactory = BeanSerializerFactory.instance;
    }

    /*
    /**********************************************************
    /* Versioned impl
    /**********************************************************
     */
    
    /**
     * Method that will return version information stored in and read from jar
     * that contains this class.
     * 
     * @since 1.6
     */
    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }
    
    /*
    /**********************************************************
    /* Module registration
    /**********************************************************
     */

    /**
     * Method for registering a module that can extend functionality
     * provided by this mapper; for example, by adding providers for
     * custom serializers and deserializers.
     * 
     * @param module Module to register
     * 
     * @since 1.7
     */
    public void registerModule(Module module)
    {
        /* Let's ensure we have access to name and version information, 
         * even if we do not have immediate use for either. This way we know
         * that they will be available from beginning
         */
        String name = module.getModuleName();
        if (name == null) {
            throw new IllegalArgumentException("Module without defined name");
        }
        Version version = module.version();
        if (version == null) {
            throw new IllegalArgumentException("Module without defined version");
        }

        final ObjectMapper mapper = this;
        
        // And then call registration
        module.setupModule(new Module.SetupContext()
        {
            // // // Accessors

            @Override
            public Version getMapperVersion() {
                return version();
            }

            @Override
            public DeserializationConfig getDeserializationConfig() {
                return mapper.getDeserializationConfig();
            }

            @Override
            public SerializationConfig getSerializationConfig() {
                return mapper.getSerializationConfig();
            }

            @Override
            public boolean isEnabled(DeserializationConfig.Feature f) {
                return mapper.isEnabled(f);
            }

            @Override
            public boolean isEnabled(SerializationConfig.Feature f) {
                return mapper.isEnabled(f);
            }

            @Override
            public boolean isEnabled(JsonParser.Feature f) {
                return mapper.isEnabled(f);
            }

            @Override
            public boolean isEnabled(JsonGenerator.Feature f) {
                return mapper.isEnabled(f);
            }
            
            // // // Methods for registering handlers: deserializers, serializers
            
            @Override
            public void addDeserializers(Deserializers d) {
                mapper._deserializerProvider = mapper._deserializerProvider.withAdditionalDeserializers(d);
            }

            @Override
            public void addKeyDeserializers(KeyDeserializers d) {
                mapper._deserializerProvider = mapper._deserializerProvider.withAdditionalKeyDeserializers(d);
            }
            
            @Override
            public void addSerializers(Serializers s) {
                mapper._serializerFactory = mapper._serializerFactory.withAdditionalSerializers(s);
            }

            @Override
            public void addKeySerializers(Serializers s) {
                mapper._serializerFactory = mapper._serializerFactory.withAdditionalKeySerializers(s);
            }
            
            @Override
            public void addBeanSerializerModifier(BeanSerializerModifier modifier) {
                mapper._serializerFactory = mapper._serializerFactory.withSerializerModifier(modifier);
            }

            @Override
            public void addBeanDeserializerModifier(BeanDeserializerModifier modifier) {
                mapper._deserializerProvider = mapper._deserializerProvider.withDeserializerModifier(modifier);
            }

            // // // Methods for registering handlers: other
            
            @Override
            public void addAbstractTypeResolver(AbstractTypeResolver resolver) {
                mapper._deserializerProvider = mapper._deserializerProvider.withAbstractTypeResolver(resolver);
            }

            @Override
            public void addTypeModifier(TypeModifier modifier) {
                TypeFactory f = mapper._typeFactory;
                f = f.withModifier(modifier);
                mapper.setTypeFactory(f);
            }

            @Override
            public void addValueInstantiators(ValueInstantiators instantiators) {
                mapper._deserializerProvider = mapper._deserializerProvider.withValueInstantiators(instantiators);
            }
            
            @Override
            public void insertAnnotationIntrospector(AnnotationIntrospector ai) {
                mapper._deserializationConfig = mapper._deserializationConfig.withInsertedAnnotationIntrospector(ai);
                mapper._serializationConfig = mapper._serializationConfig.withInsertedAnnotationIntrospector(ai);
            }
            
            @Override
            public void appendAnnotationIntrospector(AnnotationIntrospector ai) {
                mapper._deserializationConfig = mapper._deserializationConfig.withAppendedAnnotationIntrospector(ai);
                mapper._serializationConfig = mapper._serializationConfig.withAppendedAnnotationIntrospector(ai);
            }

            @Override
            public void setMixInAnnotations(Class<?> target, Class<?> mixinSource) {
                mapper._deserializationConfig.addMixInAnnotations(target, mixinSource);
                mapper._serializationConfig.addMixInAnnotations(target, mixinSource);
            }
        });
    }

    /**
     * Fluent-style alternative to {@link #registerModule}; functionally equivalent to:
     *<pre>
     *  mapper.registerModule(module);
     *  return mapper;
     *</pre>
     * NOTE: name is unfortunately misleading in suggesting that a new instance
     * might be created (as is the case with most other 'withXxx()' methods
     * for Jackson core objects) -- this is not the case; rather, this is just
     * a variant of {@link #registerModule} but one that returns 'this'
     * (like it should return, but does not for historical reasons).
     * 
     * @since 1.8
     */
    public ObjectMapper withModule(Module module)
    {
        registerModule(module);
        return this;
    }

    /*
    /**********************************************************
    /* Configuration: main config object access
    /**********************************************************
     */

    /**
     * Method that returns the shared default {@link SerializationConfig}
     * object that defines configuration settings for serialization.
     * Returned object is "live" meaning that changes will be used
     * for future serialization operations for this mapper when using
     * mapper's default configuration
     */
    public SerializationConfig getSerializationConfig() {
        return _serializationConfig;
    }

    /**
     * Method that creates a copy of
     * the shared default {@link SerializationConfig} object
     * that defines configuration settings for serialization.
     * Since it is a copy, any changes made to the configuration
     * object will NOT directly affect serialization done using
     * basic serialization methods that use the shared object (that is,
     * ones that do not take separate {@link SerializationConfig}
     * argument.
     *<p>
     * The use case is that of changing object settings of the configuration
     * (like date format being used, see {@link SerializationConfig#setDateFormat}).
     */
    public SerializationConfig copySerializationConfig() {
        return _serializationConfig.createUnshared(_subtypeResolver);
    }

    /**
     * Method for replacing the shared default serialization configuration
     * object.
     */
    public ObjectMapper setSerializationConfig(SerializationConfig cfg) {
        _serializationConfig = cfg;
        return this;
    }

    /**
     * Method that returns
     * the shared default {@link DeserializationConfig} object
     * that defines configuration settings for deserialization.
     * Returned object is "live" meaning that changes will be used
     * for future deserialization operations for this mapper when using
     * mapper's default configuration
     */
    public DeserializationConfig getDeserializationConfig() {
        return _deserializationConfig;
    }

    /**
     * Method that creates a copy of
     * the shared default {@link DeserializationConfig} object
     * that defines configuration settings for deserialization.
     * Since it is a copy, any changes made to the configuration
     * object will NOT directly affect deserialization done using
     * basic deserialization methods that use the shared object (that is,
     * ones that do not take separate {@link DeserializationConfig}
     * argument.
     *<p>
     * The use case is that of changing object settings of the configuration
     * (like deserialization problem handler,
     * see {@link DeserializationConfig#addHandler})
     */
    public DeserializationConfig copyDeserializationConfig() {
        return _deserializationConfig.createUnshared(_subtypeResolver)
                .passSerializationFeatures(_serializationConfig._featureFlags);
    }

    /**
     * Method for replacing the shared default deserialization configuration
     * object.
     */
    public ObjectMapper setDeserializationConfig(DeserializationConfig cfg) {
        _deserializationConfig = cfg;
        return this;
    }

    /*
    /**********************************************************
    /* Configuration: ser/deser factory, provider access
    /**********************************************************
     */
    
    /**
     * Method for setting specific {@link SerializerFactory} to use
     * for constructing (bean) serializers.
     */
    public ObjectMapper setSerializerFactory(SerializerFactory f) {
        _serializerFactory = f;
        return this;
    }

    /**
     * Method for setting specific {@link SerializerProvider} to use
     * for handling caching of {@link JsonSerializer} instances.
     */
    public ObjectMapper setSerializerProvider(SerializerProvider p) {
        _serializerProvider = p;
        return this;
    }

    /**
     * @since 1.4
     */   
    public SerializerProvider getSerializerProvider() {
        return _serializerProvider;
    }
    
    /**
     * Method for setting specific {@link DeserializerProvider} to use
     * for handling caching of {@link JsonDeserializer} instances.
     */
    public ObjectMapper setDeserializerProvider(DeserializerProvider p) {
        _deserializerProvider = p;
        return this;
    }

    /**
     * @since 1.4
     */   
    public DeserializerProvider getDeserializerProvider() {
        return _deserializerProvider;
    }

    /*
    /**********************************************************
    /* Configuration, introspection
    /**********************************************************
     */

    /**
     * Method for accessing currently configured visibility checker;
     * object used for determining whether given property element
     * (method, field, constructor) can be auto-detected or not.
     * 
     * @since 1.5
     */
    public VisibilityChecker<?> getVisibilityChecker() {
        return _serializationConfig.getDefaultVisibilityChecker();
    }

    /**
     * Method for setting currently configured visibility checker;
     * object used for determining whether given property element
     * (method, field, constructor) can be auto-detected or not.
     * This default checker is used if no per-class overrides
     * are defined.
     * 
     * @since 1.5
     */    
    public void setVisibilityChecker(VisibilityChecker<?> vc) {
        _deserializationConfig = _deserializationConfig.withVisibilityChecker(vc);
        _serializationConfig = _serializationConfig.withVisibilityChecker(vc);
    }

    /**
     * Convenience method that allows changing configuration for
     * underlying {@link VisibilityChecker}s, to change details of what kinds of
     * properties are auto-detected.
     * Basically short cut for doing:
     *<pre>
     *  mapper.setVisibilityChecker(
     *     mapper.getVisibilityChecker().withVisibility(forMethod, visibility)
     *  );
     *</pre>
     * one common use case would be to do:
     *<pre>
     *  mapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
     *</pre>
     * which would make all member fields serializable without further annotations,
     * instead of just public fields (default setting).
     * 
     * @param forMethod Type of property descriptor affected (field, getter/isGetter,
     *     setter, creator)
     * @param visibility Minimum visibility to require for the property descriptors of type
     * 
     * @return Modified mapper instance (that is, "this"), to allow chaining
     *    of configuration calls
     * 
     * @since 1.9
     */
    public ObjectMapper setVisibility(JsonMethod forMethod, JsonAutoDetect.Visibility visibility)
    {
        _deserializationConfig = _deserializationConfig.withVisibility(forMethod, visibility);
        _serializationConfig = _serializationConfig.withVisibility(forMethod, visibility);
        return this;
    }
    
    /**
     * Method for accessing subtype resolver in use.
     * 
     * @since 1.6
     */
    public SubtypeResolver getSubtypeResolver() {
        if (_subtypeResolver == null) {
            _subtypeResolver = new StdSubtypeResolver();
        }
        return _subtypeResolver;
    }

    /**
     * Method for setting custom subtype resolver to use.
     * 
     * @since 1.6
     */
    public void setSubtypeResolver(SubtypeResolver r) {
        _subtypeResolver = r;
    }

    /**
     * Method for changing {@link AnnotationIntrospector} used by this
     * mapper instance for both serialization and deserialization
     * 
     * @since 1.8
     */
    public ObjectMapper setAnnotationIntrospector(AnnotationIntrospector ai) {
        _serializationConfig = _serializationConfig.withAnnotationIntrospector(ai);
        _deserializationConfig = _deserializationConfig.withAnnotationIntrospector(ai);
        return this;
    }
    
    /**
     * Method for setting custom property naming strategy to use.
     * 
     * @since 1.8
     */
    public ObjectMapper setPropertyNamingStrategy(PropertyNamingStrategy s) {
        _serializationConfig = _serializationConfig.withPropertyNamingStrategy(s);
        _deserializationConfig = _deserializationConfig.withPropertyNamingStrategy(s);
        return this;
    }

    /**
     * Method for setting defalt POJO property inclusion strategy for serialization.
     * Equivalent to:
     *<pre>
     *  mapper.setSerializationConfig(mapper.getSerializationConfig().withSerializationInclusion(incl));
     *</pre>
     * 
     * @since 1.9
     */
    public ObjectMapper setSerializationInclusion(JsonSerialize.Inclusion incl) {
        _serializationConfig = _serializationConfig.withSerializationInclusion(incl);
        return this;
    }
   
    /*
    /**********************************************************
    /* Type information configuration (1.5+)
    /**********************************************************
     */

    /**
     * Convenience method that is equivalent to calling
     *<pre>
     *  enableObjectTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
     *</pre>
     */
    public ObjectMapper enableDefaultTyping() {
        return enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
    }

    /**
     * Convenience method that is equivalent to calling
     *<pre>
     *  enableObjectTyping(dti, JsonTypeInfo.As.WRAPPER_ARRAY);
     *</pre>
     */
    public ObjectMapper enableDefaultTyping(DefaultTyping dti) {
        return enableDefaultTyping(dti, JsonTypeInfo.As.WRAPPER_ARRAY);
    }

    /**
     * Method for enabling automatic inclusion of type information, needed
     * for proper deserialization of polymorphic types (unless types
     * have been annotated with {@link com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo}).
     * 
     * @param applicability Defines kinds of types for which additional type information
     *    is added; see {@link DefaultTyping} for more information.
     */
    public ObjectMapper enableDefaultTyping(DefaultTyping applicability, JsonTypeInfo.As includeAs)
    {
        TypeResolverBuilder<?> typer = new DefaultTypeResolverBuilder(applicability);
        // we'll always use full class name, when using defaulting
        typer = typer.init(JsonTypeInfo.Id.CLASS, null);
        typer = typer.inclusion(includeAs);
        return setDefaultTyping(typer);
    }

    /**
     * Method for enabling automatic inclusion of type information -- needed
     * for proper deserialization of polymorphic types (unless types
     * have been annotated with {@link com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo}) --
     * using "As.PROPERTY" inclusion mechanism and specified property name
     * to use for inclusion (default being "@class" since default type information
     * always uses class name as type identifier)
     * 
     * @since 1.7
     */
    public ObjectMapper enableDefaultTypingAsProperty(DefaultTyping applicability, String propertyName)
    {
        TypeResolverBuilder<?> typer = new DefaultTypeResolverBuilder(applicability);
        // we'll always use full class name, when using defaulting
        typer = typer.init(JsonTypeInfo.Id.CLASS, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        typer = typer.typeProperty(propertyName);
        return setDefaultTyping(typer);
    }
    
    /**
     * Method for disabling automatic inclusion of type information; if so, only
     * explicitly annotated types (ones with
     * {@link com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo}) will have
     * additional embedded type information.
     */
    public ObjectMapper disableDefaultTyping() {
        return setDefaultTyping(null);
    }

    /**
     * Method for enabling automatic inclusion of type information, using
     * specified handler object for determining which types this affects,
     * as well as details of how information is embedded.
     * 
     * @param typer Type information inclusion handler
     * 
     * 
     */
    public ObjectMapper setDefaultTyping(TypeResolverBuilder<?> typer) {
        _deserializationConfig = _deserializationConfig.withTypeResolverBuilder(typer);
        _serializationConfig = _serializationConfig.withTypeResolverBuilder(typer);
        return this;
    }

    /**
     * Method for registering specified class as a subtype, so that
     * typename-based resolution can link supertypes to subtypes
     * (as an alternative to using annotations).
     * Type for given class is determined from appropriate annotation;
     * or if missing, default name (unqualified class name)
     * 
     * @since 1.6
     */
    public void registerSubtypes(Class<?>... classes) {
        getSubtypeResolver().registerSubtypes(classes);
    }

    /**
     * Method for registering specified class as a subtype, so that
     * typename-based resolution can link supertypes to subtypes
     * (as an alternative to using annotations).
     * Name may be provided as part of argument, but if not will
     * be based on annotations or use default name (unqualified
     * class name).
     * 
     * @since 1.6
     */
    public void registerSubtypes(NamedType... types) {
        getSubtypeResolver().registerSubtypes(types);
    }

    /*
    /**********************************************************
    /* Configuration, basic type handling
    /**********************************************************
     */

    /**
     * Accessor for getting currently configured {@link TypeFactory} instance.
     * 
     * @since 1.8
     */
    public TypeFactory getTypeFactory() {
        return _typeFactory;
    }

    /**
     * Method that can be used to override {@link TypeFactory} instance
     * used by this mapper.
     *<p>
     * Note: will also set {@link TypeFactory} that deserialization and
     * serialization config objects use.
     */
    public ObjectMapper setTypeFactory(TypeFactory f)
    {
        _typeFactory = f;
        _deserializationConfig = _deserializationConfig.withTypeFactory(f);
        _serializationConfig = _serializationConfig.withTypeFactory(f);
        return this;
    }
    
    /**
     * Convenience method for constructing {@link JavaType} out of given
     * type (typically <code>java.lang.Class</code>), but without explicit
     * context.
     * 
     * @since 1.8
     */
    public JavaType constructType(Type t) {
        return _typeFactory.constructType(t);
    }
    
    /*
    /**********************************************************
    /* Configuration, deserialization
    /**********************************************************
     */
    
    /**
     * Method for specifying {@link JsonNodeFactory} to use for
     * constructing root level tree nodes (via method
     * {@link #createObjectNode}
     *
     * @since 1.2
     */
    public ObjectMapper setNodeFactory(JsonNodeFactory f) {
        _deserializationConfig = _deserializationConfig.withNodeFactory(f);
        return this;
    }

    /*
    /**********************************************************
    /* Configuration, serialization
    /**********************************************************
     */

    /**
     * Convenience method that is equivalent to:
     *<pre>
     *  mapper.setFilters(mapper.getSerializationConfig().withFilters(filterProvider));
     *</pre>
     *<p>
     * Note that usually it is better to use method {@link #filteredWriter}; however, sometimes
     * this method is more convenient. For example, some frameworks only allow configuring
     * of ObjectMapper instances and not ObjectWriters.
     * 
     * @since 1.8
     */
    public void setFilters(FilterProvider filterProvider) {
        _serializationConfig = _serializationConfig.withFilters(filterProvider);
    }
    
    /*
    /**********************************************************
    /* Configuration, other
    /**********************************************************
     */

    /**
     * Method that can be used to get hold of {@link JsonFactory} that this
     * mapper uses if it needs to construct {@link JsonParser}s
     * and/or {@link JsonGenerator}s.
     *
     * @return {@link JsonFactory} that this mapper uses when it needs to
     *   construct Json parser and generators
     */
    public JsonFactory getJsonFactory() { return _jsonFactory; }
    
    /**
     * Method for configuring the default {@link DateFormat} to use when serializing time
     * values as Strings, and deserializing from JSON Strings.
     * This is preferably to directly modifying {@link SerializationConfig} and
     * {@link DeserializationConfig} instances.
     * If you need per-request configuration, use {@link #writer(DateFormat)} to
     * create properly configured {@link ObjectWriter} and use that; this because
     * {@link ObjectWriter}s are thread-safe whereas ObjectMapper itself is only
     * thread-safe when configuring methods (such as this one) are NOT called.
     * 
     * @since 1.8
     */
    public void setDateFormat(DateFormat dateFormat)
    {
        _deserializationConfig = _deserializationConfig.withDateFormat(dateFormat);
        _serializationConfig = _serializationConfig.withDateFormat(dateFormat);
    }

    /**
     * Method for configuring {@link HandlerInstantiator} to use for creating
     * instances of handlers (such as serializers, deserializers, type and type
     * id resolvers), given a class.
     *
     * @param hi Instantiator to use; if null, use the default implementation
     */
    public void setHandlerInstantiator(HandlerInstantiator hi)
    {
        _deserializationConfig = _deserializationConfig.withHandlerInstantiator(hi);
        _serializationConfig = _serializationConfig.withHandlerInstantiator(hi);
    }
    
    /**
     * @since 1.9
     */
    public ObjectMapper setInjectableValues(InjectableValues injectableValues) {
        _injectableValues = injectableValues;
        return this;
    }
    
    /*
    /**********************************************************
    /* Configuration, simple features
    /**********************************************************
     */

    /**
     * Method for changing state of an on/off serialization feature for
     * this object mapper.
     *<p>
     * This is method is basically a shortcut method for calling
     * {@link SerializationConfig#set} on the shared {@link SerializationConfig}
     * object with given arguments.
     */
    @SuppressWarnings("deprecation")
    public ObjectMapper configure(SerializationConfig.Feature f, boolean state) {
        _serializationConfig.set(f, state);
        return this;
    }

    /**
     * Method for changing state of an on/off deserialization feature for
     * this object mapper.
     *<p>
     * This is method is basically a shortcut method for calling
     * {@link DeserializationConfig#set} on the shared {@link DeserializationConfig}
     * object with given arguments.
     */
    @SuppressWarnings("deprecation")
    public ObjectMapper configure(DeserializationConfig.Feature f, boolean state) {
        _deserializationConfig.set(f, state);
        return this;
    }

    /**
     * Method for changing state of an on/off {@link JsonParser} feature for
     * {@link JsonFactory} instance this object mapper uses.
     *<p>
     * This is method is basically a shortcut method for calling
     * {@link JsonFactory#setParserFeature} on the shared
     * {@link JsonFactory} this mapper uses (which is accessible
     * using {@link #getJsonFactory}).
     *
     * @since 1.2
     */
    public ObjectMapper configure(JsonParser.Feature f, boolean state) {
        _jsonFactory.configure(f, state);
        return this;
    }

    /**
     * Method for changing state of an on/off {@link JsonGenerator} feature for
     * {@link JsonFactory} instance this object mapper uses.
     *<p>
     * This is method is basically a shortcut method for calling
     * {@link JsonFactory#setGeneratorFeature} on the shared
     * {@link JsonFactory} this mapper uses (which is accessible
     * using {@link #getJsonFactory}).
     *
     * @since 1.2
     */
    public ObjectMapper configure(JsonGenerator.Feature f, boolean state) {
        _jsonFactory.configure(f, state);
        return this;
    }

    /**
     * Method for enabling specified {@link DeserializationConfig} features.
     * Modifies and returns this instance; no new object is created.
     * 
     * @since 1.9
     */
    public ObjectMapper enable(DeserializationConfig.Feature... f) {
        _deserializationConfig = _deserializationConfig.with(f);
        return this;
    }

    /**
     * Method for enabling specified {@link DeserializationConfig} features.
     * Modifies and returns this instance; no new object is created.
     * 
     * @since 1.9
     */
    public ObjectMapper disable(DeserializationConfig.Feature... f) {
        _deserializationConfig = _deserializationConfig.without(f);
        return this;
    }

    /**
     * Method for enabling specified {@link DeserializationConfig} features.
     * Modifies and returns this instance; no new object is created.
     * 
     * @since 1.9
     */
    public ObjectMapper enable(SerializationConfig.Feature... f) {
        _serializationConfig = _serializationConfig.with(f);
        return this;
    }

    /**
     * Method for enabling specified {@link DeserializationConfig} features.
     * Modifies and returns this instance; no new object is created.
     * 
     * @since 1.9
     */
    public ObjectMapper disable(SerializationConfig.Feature... f) {
        _serializationConfig = _serializationConfig.without(f);
        return this;
    }
    
    /**
     * Convenience method, equivalent to:
     *<pre>
     *  getSerializationConfig().isEnabled(f);
     *</pre>
     * 
     * @since 1.9
     */
    public boolean isEnabled(SerializationConfig.Feature f) {
        return _serializationConfig.isEnabled(f);
    }

    /**
     * Convenience method, equivalent to:
     *<pre>
     *  getDeserializationConfig().isEnabled(f);
     *</pre>
     * 
     * @since 1.9
     */
    public boolean isEnabled(DeserializationConfig.Feature f) {
        return _deserializationConfig.isEnabled(f);
    }

    /**
     * Convenience method, equivalent to:
     *<pre>
     *  getJsonFactory().isEnabled(f);
     *</pre>
     * 
     * @since 1.9
     */
    public boolean isEnabled(JsonParser.Feature f) {
        return _jsonFactory.isEnabled(f);
    }

    /**
     * Convenience method, equivalent to:
     *<pre>
     *  getJsonFactory().isEnabled(f);
     *</pre>
     * 
     * @since 1.9
     */
    public boolean isEnabled(JsonGenerator.Feature f) {
        return _jsonFactory.isEnabled(f);
    }
    
    /**
     * Method that can be used to get hold of {@link JsonNodeFactory}
     * that this mapper will use when directly constructing
     * root {@link JsonNode} instances for Trees.
     *<p>
     * Note: this is just a shortcut for calling
     *<pre>
     *   getDeserializationConfig().getNodeFactory()
     *</pre>
     *
     * @since 1.2
     */
    public JsonNodeFactory getNodeFactory() {
        return _deserializationConfig.getNodeFactory();
    }

    /*
    /**********************************************************
    /* Public API (from ObjectCodec): deserialization
    /* (mapping from JSON to Java types);
    /* main methods
    /**********************************************************
     */

    /**
     * Method to deserialize JSON content into a non-container
     * type (it can be an array type, however): typically a bean, array
     * or a wrapper type (like {@link java.lang.Boolean}).
     *<p>
     * Note: this method should NOT be used if the result type is a
     * container ({@link java.util.Collection} or {@link java.util.Map}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected when using this method.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T readValue(JsonParser jp, Class<T> valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
// !!! TODO
//    	_setupClassLoaderForDeserialization(valueType);
        return (T) _readValue(copyDeserializationConfig(), jp, _typeFactory.constructType(valueType));
    } 

    /**
     * Method to deserialize JSON content into a Java type, reference
     * to which is passed as argument. Type is passed using so-called
     * "super type token" (see )
     * and specifically needs to be used if the root type is a 
     * parameterized (generic) container type.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T readValue(JsonParser jp, TypeReference<?> valueTypeRef)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readValue(copyDeserializationConfig(), jp, _typeFactory.constructType(valueTypeRef));
    }

    /**
     * Method to deserialize JSON content into a Java type, reference
     * to which is passed as argument. Type is passed using 
     * Jackson specific type; instance of which can be constructed using
     * {@link TypeFactory}.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T readValue(JsonParser jp, JavaType valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readValue(copyDeserializationConfig(), jp, valueType);
    } 

    /**
     * Method to deserialize JSON content as tree expressed
     * using set of {@link JsonNode} instances. Returns
     * root of the resulting tree (where root can consist
     * of just a single node if the current event is a
     * value event, not container).
     */
    @Override
    public JsonNode readTree(JsonParser jp)
        throws IOException, JsonProcessingException
    {
        /* 02-Mar-2009, tatu: One twist; deserialization provider
         *   will map JSON null straight into Java null. But what
         *   we want to return is the "null node" instead.
         */
        /* 05-Aug-2011, tatu: Also, must check for EOF here before
         *   calling readValue(), since that'll choke on it otherwise
         */
        DeserializationConfig cfg = copyDeserializationConfig();
        JsonToken t = jp.getCurrentToken();
        if (t == null) {
            t = jp.nextToken();
            if (t == null) {
                return null;
            }
        }
        JsonNode n = (JsonNode) _readValue(cfg, jp, JSON_NODE_TYPE);
        return (n == null) ? getNodeFactory().nullNode() : n;
    }

    /**
     * Method for reading sequence of Objects from parser stream.
     *<p>
     * Note that {@link ObjectReader} has more complete set of variants.
     * 
     * @since 1.8
     */
    @Override
    public <T> MappingIterator<T> readValues(JsonParser jp, JavaType valueType)
        throws IOException, JsonProcessingException
    {
        DeserializationConfig config = copyDeserializationConfig();
        DeserializationContext ctxt = _createDeserializationContext(jp, config);
        JsonDeserializer<?> deser = _findRootDeserializer(config, valueType);
        // false -> do NOT close JsonParser (since caller passed it)
        return new MappingIterator<T>(valueType, jp, ctxt, deser,
                false, null);
    }

    /**
     * Method for reading sequence of Objects from parser stream.
     * 
     * @since 1.8
     */
    @Override
    public <T> MappingIterator<T> readValues(JsonParser jp, Class<T> valueType)
        throws IOException, JsonProcessingException
    {
        return readValues(jp, _typeFactory.constructType(valueType));
    }

    /**
     * Method for reading sequence of Objects from parser stream.
     * 
     * @since 1.8
     */
    @Override
    public <T> MappingIterator<T> readValues(JsonParser jp, TypeReference<?> valueTypeRef)
        throws IOException, JsonProcessingException
    {
        return readValues(jp, _typeFactory.constructType(valueTypeRef));
    }
    
    /*
    /**********************************************************
    /* Public API not included in ObjectCodec: deserialization
    /* (mapping from JSON to Java types)
    /**********************************************************
     */
    
    /**
     * Method to deserialize JSON content into a non-container
     * type (it can be an array type, however): typically a bean, array
     * or a wrapper type (like {@link java.lang.Boolean}).
     *<p>
     * Note: this method should NOT be used if the result type is a
     * container ({@link java.util.Collection} or {@link java.util.Map}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected when using this method.
     * @since 1.1
     *
     * @param cfg Specific deserialization configuration to use for
     *   this operation. Note that not all config settings can
     *   be changed on per-operation basis: some changeds only take effect
     *   before calling the operation for the first time (for the mapper
     *   instance)
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(JsonParser jp, Class<T> valueType, 
                           DeserializationConfig cfg)
        throws IOException, JsonParseException, JsonMappingException
    {
     // !!! TODO
//    	_setupClassLoaderForDeserialization(valueType);
        return (T) _readValue(cfg, jp, _typeFactory.constructType(valueType));
    } 

    /**
     * Method to deserialize JSON content into a Java type, reference
     * to which is passed as argument. Type is passed using so-called
     * "super type token" (see )
     * and specifically needs to be used if the root type is a 
     * parameterized (generic) container type.
     *
     * @param cfg Specific deserialization configuration to use for
     *   this operation. Note that not all config settings can
     *   be changed on per-operation basis: some changeds only take effect
     *   before calling the operation for the first time (for the mapper
     *   instance)
     *
     * @since 1.1
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(JsonParser jp, TypeReference<?> valueTypeRef,
                           DeserializationConfig cfg)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readValue(cfg, jp, _typeFactory.constructType(valueTypeRef));
    } 

    /**
     * Method to deserialize JSON content into a Java type, reference
     * to which is passed as argument. Type is passed using 
     * Jackson specific type; instance of which can be constructed using
     * {@link TypeFactory}.
     *
     * @param cfg Specific deserialization configuration to use for
     *   this operation. Note that not all config settings can
     *   be changed on per-operation basis: some changeds only take effect
     *   before calling the operation for the first time (for the mapper
     *   instance)
     *
     * @since 1.1
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(JsonParser jp, JavaType valueType,
                           DeserializationConfig cfg)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readValue(cfg, jp, valueType);
    } 

    /**
     * Method to deserialize JSON content as tree expressed
     * using set of {@link JsonNode} instances. Returns
     * root of the resulting tree (where root can consist
     * of just a single node if the current event is a
     * value event, not container).
     *
     * @param cfg Specific deserialization configuration to use for
     *   this operation. Note that not all config settings can
     *   be changed on per-operation basis: some changeds only take effect
     *   before calling the operation for the first time (for the mapper
     *   instance)
     *
     * @since 1.1
     */
    public JsonNode readTree(JsonParser jp, DeserializationConfig cfg)
        throws IOException, JsonProcessingException
    {
        JsonNode n = (JsonNode) _readValue(cfg, jp, JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }

    /**
     * Method to deserialize JSON content as tree expressed
     * using set of {@link JsonNode} instances.
     * Returns root of the resulting tree (where root can consist
     * of just a single node if the current event is a
     * value event, not container).
     *
     * @param in Input stream used to read JSON content
     *   for building the JSON tree.
     *
     * @since 1.3
     */
    public JsonNode readTree(InputStream in)
        throws IOException, JsonProcessingException
    {
        JsonNode n = (JsonNode) _readMapAndClose(_jsonFactory.createJsonParser(in), JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }

    /**
     * Method to deserialize JSON content as tree expressed
     * using set of {@link JsonNode} instances.
     * Returns root of the resulting tree (where root can consist
     * of just a single node if the current event is a
     * value event, not container).
     *
     * @param r Reader used to read JSON content
     *   for building the JSON tree.
     *
     * @since 1.3
     */
    public JsonNode readTree(Reader r)
        throws IOException, JsonProcessingException
    {
        JsonNode n = (JsonNode) _readMapAndClose(_jsonFactory.createJsonParser(r), JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }

    /**
     * Method to deserialize JSON content as tree expressed using set of {@link JsonNode} instances.
     * Returns root of the resulting tree (where root can consist of just a single node if the current
     * event is a value event, not container).
     *
     * @param content JSON content to parse to build the JSON tree.
     *
     * @since 1.3
     */
    public JsonNode readTree(String content)
        throws IOException, JsonProcessingException
    {
        JsonNode n = (JsonNode) _readMapAndClose(_jsonFactory.createJsonParser(content), JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }

    /**
     * Method to deserialize JSON content as tree expressed using set of {@link JsonNode} instances.
     * Returns root of the resulting tree (where root can consist of just a single node if the current
     * event is a value event, not container).
     *
     * @param content JSON content to parse to build the JSON tree.
     *
     * @since 1.9
     */
    public JsonNode readTree(byte[] content)
        throws IOException, JsonProcessingException
    {
        JsonNode n = (JsonNode) _readMapAndClose(_jsonFactory.createJsonParser(content), JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }
    
    /**
     * Method to deserialize JSON content as tree expressed using set of {@link JsonNode} instances.
     * Returns root of the resulting tree (where root can consist of just a single node if the current
     * event is a value event, not container).
     *
     * @param file File of which contents to parse as JSON for building a tree instance
     *
     * @since 1.9
     */
    public JsonNode readTree(File file)
        throws IOException, JsonProcessingException
    {
        JsonNode n = (JsonNode) _readMapAndClose(_jsonFactory.createJsonParser(file), JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }

    /**
     * Method to deserialize JSON content as tree expressed using set of {@link JsonNode} instances.
     * Returns root of the resulting tree (where root can consist of just a single node if the current
     * event is a value event, not container).
     *
     * @param source URL to use for fetching contents to parse as JSON for building a tree instance
     *
     * @since 1.9
     */
    public JsonNode readTree(URL source)
        throws IOException, JsonProcessingException
    {
        JsonNode n = (JsonNode) _readMapAndClose(_jsonFactory.createJsonParser(source), JSON_NODE_TYPE);
        return (n == null) ? NullNode.instance : n;
    }

    /*
    /**********************************************************
    /* Public API (from ObjectCodec): serialization
    /* (mapping from Java types to Json)
    /**********************************************************
     */

    /**
     * Method that can be used to serialize any Java value as
     * JSON output, using provided {@link JsonGenerator}.
     */
    @Override
    public void writeValue(JsonGenerator jgen, Object value)
        throws IOException, JsonGenerationException, JsonMappingException
    {
        SerializationConfig config = copySerializationConfig();
        if (config.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && (value instanceof Closeable)) {
            _writeCloseableValue(jgen, value, config);
        } else {
            _serializerProvider.serializeValue(config, jgen, value, _serializerFactory);
            if (config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
                jgen.flush();
            }
        }
    }

    /**
     * Method that can be used to serialize any Java value as
     * JSON output, using provided {@link JsonGenerator},
     * configured as per passed configuration object.
     *
     * @since 1.1
     */
    public void writeValue(JsonGenerator jgen, Object value, SerializationConfig config)
        throws IOException, JsonGenerationException, JsonMappingException
    {
        // [JACKSON-282] Consider java.util.Closeable
        if (config.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && (value instanceof Closeable)) {
            _writeCloseableValue(jgen, value, config);
        } else {
            _serializerProvider.serializeValue(config, jgen, value, _serializerFactory);
            if (config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
                jgen.flush();
            }
        }
    }

    /**
     * Method to serialize given JSON Tree, using generator
     * provided.
     */
    @Override
    public void writeTree(JsonGenerator jgen, JsonNode rootNode)
        throws IOException, JsonProcessingException
    {
        SerializationConfig config = copySerializationConfig();
        _serializerProvider.serializeValue(config, jgen, rootNode, _serializerFactory);
        if (config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
            jgen.flush();
        }
    }

    /**
     * Method to serialize given Json Tree, using generator
     * provided.
     *
     * @since 1.1
     */
    public void writeTree(JsonGenerator jgen, JsonNode rootNode,
                          SerializationConfig cfg)
        throws IOException, JsonProcessingException
    {
        _serializerProvider.serializeValue(cfg, jgen, rootNode, _serializerFactory);
        if (cfg.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
            jgen.flush();
        }
    }

    /*
    /**********************************************************
    /* Public API (from ObjectCodec): Tree Model support
    /**********************************************************
     */

    /**
     *<p>
     * Note: return type is co-variant, as basic ObjectCodec
     * abstraction can not refer to concrete node types (as it's
     * part of core package, whereas impls are part of mapper
     * package)
     *
     * @since 1.2
     */
    @Override    
    public ObjectNode createObjectNode() {
        return _deserializationConfig.getNodeFactory().objectNode();
    }

    /**
     *<p>
     * Note: return type is co-variant, as basic ObjectCodec
     * abstraction can not refer to concrete node types (as it's
     * part of core package, whereas impls are part of mapper
     * package)
     *
     * @since 1.2
     */
    @Override
    public ArrayNode createArrayNode() {
        return _deserializationConfig.getNodeFactory().arrayNode();
    }

    /**
     * Method for constructing a {@link JsonParser} out of JSON tree
     * representation.
     * 
     * @param n Root node of the tree that resulting parser will read from
     * 
     * @since 1.3
     */
    @Override
    public JsonParser treeAsTokens(JsonNode n)
    {
        return new TreeTraversingParser(n, this);
    }

    /**
     * Convenience conversion method that will bind data given JSON tree
     * contains into specific value (usually bean) type.
     *<p>
     * Equivalent to:
     *<pre>
     *   objectMapper.convertValue(n, valueClass);
     *</pre>
     */
    @Override
    public <T> T treeToValue(JsonNode n, Class<T> valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        return readValue(treeAsTokens(n), valueType);
    }

    /**
     * Reverse of {@link #treeToValue}; given a value (usually bean), will
     * construct equivalent JSON Tree representation. Functionally same
     * as if serializing value into JSON and parsing JSON as tree, but
     * more efficient.
     * 
     * @param <T> Actual node type; usually either basic {@link JsonNode} or
     *  {@link com.alibaba.acm.shaded.org.codehaus.jackson.node.ObjectNode}
     * @param fromValue Bean value to convert
     * @return Root node of the resulting JSON tree
     * 
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonNode> T valueToTree(Object fromValue)
        throws IllegalArgumentException
    {
        if (fromValue == null) return null;
        TokenBuffer buf = new TokenBuffer(this);
        JsonNode result;
        try {
            writeValue(buf, fromValue);
            JsonParser jp = buf.asParser();
            result = readTree(jp);
            jp.close();
        } catch (IOException e) { // should not occur, no real i/o...
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return (T) result;
    } 
    
    /*
    /**********************************************************
    /* Extended Public API, accessors
    /**********************************************************
     */

    /**
     * Method that can be called to check whether mapper thinks
     * it could serialize an instance of given Class.
     * Check is done
     * by checking whether a serializer can be found for the type.
     *
     * @return True if mapper can find a serializer for instances of
     *  given class (potentially serializable), false otherwise (not
     *  serializable)
     */
    public boolean canSerialize(Class<?> type)
    {
        return _serializerProvider.hasSerializerFor(copySerializationConfig(),
                type, _serializerFactory);
    }

    /**
     * Method that can be called to check whether mapper thinks
     * it could deserialize an Object of given type.
     * Check is done
     * by checking whether a deserializer can be found for the type.
     *
     * @return True if mapper can find a serializer for instances of
     *  given class (potentially serializable), false otherwise (not
     *  serializable)
     */
    public boolean canDeserialize(JavaType type)
    {
        return _deserializerProvider.hasValueDeserializerFor(copyDeserializationConfig(), type);
    }

    /*
    /**********************************************************
    /* Extended Public API, deserialization,
    /* convenience methods
    /**********************************************************
     */

    @SuppressWarnings("unchecked")
    public <T> T readValue(File src, Class<T> valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
     // !!! TODO
//    	_setupClassLoaderForDeserialization(valueType);
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), _typeFactory.constructType(valueType));
    } 

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T readValue(File src, TypeReference valueTypeRef)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), _typeFactory.constructType(valueTypeRef));
    } 

    @SuppressWarnings("unchecked")
    public <T> T readValue(File src, JavaType valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), valueType);
    } 

    @SuppressWarnings("unchecked")
    public <T> T readValue(URL src, Class<T> valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
     // !!! TODO
//    	_setupClassLoaderForDeserialization(valueType);
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), _typeFactory.constructType(valueType));
    } 

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T readValue(URL src, TypeReference valueTypeRef)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), _typeFactory.constructType(valueTypeRef));
    } 

    @SuppressWarnings("unchecked")
    public <T> T readValue(URL src, JavaType valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), valueType);
    } 

    @SuppressWarnings("unchecked")
    public <T> T readValue(String content, Class<T> valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
     // !!! TODO
//    	_setupClassLoaderForDeserialization(valueType);
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(content), _typeFactory.constructType(valueType));
    } 

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T readValue(String content, TypeReference valueTypeRef)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(content), _typeFactory.constructType(valueTypeRef));
    } 

    @SuppressWarnings("unchecked")
    public <T> T readValue(String content, JavaType valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(content), valueType);
    } 

    @SuppressWarnings("unchecked")
    public <T> T readValue(Reader src, Class<T> valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
     // !!! TODO
//    	_setupClassLoaderForDeserialization(valueType);
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), _typeFactory.constructType(valueType));
    } 

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T readValue(Reader src, TypeReference valueTypeRef)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), _typeFactory.constructType(valueTypeRef));
    } 

    @SuppressWarnings("unchecked")
    public <T> T readValue(Reader src, JavaType valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), valueType);
    } 

    @SuppressWarnings("unchecked")
    public <T> T readValue(InputStream src, Class<T> valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
     // !!! TODO
//    	_setupClassLoaderForDeserialization(valueType);
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), _typeFactory.constructType(valueType));
    } 

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T readValue(InputStream src, TypeReference valueTypeRef)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), _typeFactory.constructType(valueTypeRef));
    } 

    @SuppressWarnings("unchecked")
    public <T> T readValue(InputStream src, JavaType valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), valueType);
    } 

    /**
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(byte[] src, Class<T> valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
     // !!! TODO
//      _setupClassLoaderForDeserialization(valueType);
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), _typeFactory.constructType(valueType));
    } 
    
    @SuppressWarnings("unchecked")
    public <T> T readValue(byte[] src, int offset, int len, 
                               Class<T> valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
     // !!! TODO
//    	_setupClassLoaderForDeserialization(valueType);
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src, offset, len), _typeFactory.constructType(valueType));
    } 

    /**
     * @since 1.8
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T readValue(byte[] src, TypeReference valueTypeRef)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), _typeFactory.constructType(valueTypeRef));
    } 
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T readValue(byte[] src, int offset, int len,
                           TypeReference valueTypeRef)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src, offset, len), _typeFactory.constructType(valueTypeRef));
    } 

    /**
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(byte[] src, JavaType valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src), valueType);
    } 

    @SuppressWarnings("unchecked")
    public <T> T readValue(byte[] src, int offset, int len,
                           JavaType valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readMapAndClose(_jsonFactory.createJsonParser(src, offset, len), valueType);
    } 
    
    /**
     * Convenience method for converting results from given JSON tree into given
     * value type. Basically short-cut for:
     *<pre>
     *   mapper.readValue(mapper.treeAsTokens(root), valueType);
     *</pre>
     *
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(JsonNode root, Class<T> valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
     // !!! TODO
//    	_setupClassLoaderForDeserialization(valueType);
        return (T) _readValue(copyDeserializationConfig(), treeAsTokens(root), _typeFactory.constructType(valueType));
    } 

    /**
     * Convenience method for converting results from given JSON tree into given
     * value type. Basically short-cut for:
     *<pre>
     *   mapper.readValue(mapper.treeAsTokens(root), valueType);
     *</pre>
     *
     * @since 1.6
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T readValue(JsonNode root, TypeReference valueTypeRef)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readValue(copyDeserializationConfig(), treeAsTokens(root), _typeFactory.constructType(valueTypeRef));
    } 
    
    /**
     * Convenience method for converting results from given JSON tree into given
     * value type. Basically short-cut for:
     *<pre>
     *   mapper.readValue(mapper.treeAsTokens(root), valueType);
     *</pre>
     *
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(JsonNode root, JavaType valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        return (T) _readValue(copyDeserializationConfig(), treeAsTokens(root), valueType);
    } 
    
    /*
    /**********************************************************
    /* Extended Public API: serialization
    /* (mapping from Java types to Json)
    /**********************************************************
     */

    /**
     * Method that can be used to serialize any Java value as
     * JSON output, written to File provided.
     */
    public void writeValue(File resultFile, Object value)
        throws IOException, JsonGenerationException, JsonMappingException
    {
        _configAndWriteValue(_jsonFactory.createJsonGenerator(resultFile, JsonEncoding.UTF8), value);
    }

    /**
     * Method that can be used to serialize any Java value as
     * JSON output, using output stream provided (using encoding
     * {@link JsonEncoding#UTF8}).
     *<p>
     * Note: method does not close the underlying stream explicitly
     * here; however, {@link JsonFactory} this mapper uses may choose
     * to close the stream depending on its settings (by default,
     * it will try to close it when {@link JsonGenerator} we construct
     * is closed).
     */
    public void writeValue(OutputStream out, Object value)
        throws IOException, JsonGenerationException, JsonMappingException
    {
        _configAndWriteValue(_jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8), value);
    }

    /**
     * Method that can be used to serialize any Java value as
     * JSON output, using Writer provided.
     *<p>
     * Note: method does not close the underlying stream explicitly
     * here; however, {@link JsonFactory} this mapper uses may choose
     * to close the stream depending on its settings (by default,
     * it will try to close it when {@link JsonGenerator} we construct
     * is closed).
     */
    public void writeValue(Writer w, Object value)
        throws IOException, JsonGenerationException, JsonMappingException
    {
        _configAndWriteValue(_jsonFactory.createJsonGenerator(w), value);
    }

    /**
     * Method that can be used to serialize any Java value as
     * a String. Functionally equivalent to calling
     * {@link #writeValue(Writer,Object)} with {@link java.io.StringWriter}
     * and constructing String, but more efficient.
     *
     * @since 1.3
     */
    public String writeValueAsString(Object value)
        throws IOException, JsonGenerationException, JsonMappingException
    {        
        // alas, we have to pull the recycler directly here...
        SegmentedStringWriter sw = new SegmentedStringWriter(_jsonFactory._getBufferRecycler());
        _configAndWriteValue(_jsonFactory.createJsonGenerator(sw), value);
        return sw.getAndClear();
    }
    
    /**
     * Method that can be used to serialize any Java value as
     * a byte array. Functionally equivalent to calling
     * {@link #writeValue(Writer,Object)} with {@link java.io.ByteArrayOutputStream}
     * and getting bytes, but more efficient.
     * Encoding used will be UTF-8.
     *
     * @since 1.5
     */
    public byte[] writeValueAsBytes(Object value)
        throws IOException, JsonGenerationException, JsonMappingException
    {        
        ByteArrayBuilder bb = new ByteArrayBuilder(_jsonFactory._getBufferRecycler());
        _configAndWriteValue(_jsonFactory.createJsonGenerator(bb, JsonEncoding.UTF8), value);
        byte[] result = bb.toByteArray();
        bb.release();
        return result;
    }

    /*
    /**********************************************************
    /* Extended Public API: constructing ObjectWriters
    /* for more advanced configuration
    /**********************************************************
     */

    /**
     * Convenience method for constructing {@link ObjectWriter}
     * with default settings.
     * 
     * @since 1.6
     */
    public ObjectWriter writer() {
        return new ObjectWriter(this, copySerializationConfig());
    }

    /**
     * Factory method for constructing {@link ObjectWriter} that will
     * serialize objects using specified {@link DateFormat}; or, if
     * null passed, using timestamp (64-bit number.
     * 
     * @since 1.9
     */
    public ObjectWriter writer(DateFormat df) {
        return new ObjectWriter(this,
                copySerializationConfig().withDateFormat(df));
    }
    
    /**
     * Factory method for constructing {@link ObjectWriter} that will
     * serialize objects using specified JSON View (filter).
     * 
     * @since 1.9
     */
    public ObjectWriter writerWithView(Class<?> serializationView) {
        return new ObjectWriter(this, copySerializationConfig().withView(serializationView));
    }
    
    /**
     * Factory method for constructing {@link ObjectWriter} that will
     * serialize objects using specified root type, instead of actual
     * runtime type of value. Type must be a super-type of runtime
     * type.
     *
     * @since 1.9
     */
    public ObjectWriter writerWithType(Class<?> rootType) {
        JavaType t = (rootType == null) ? null : _typeFactory.constructType(rootType);
        return new ObjectWriter(this, copySerializationConfig(), t, /*PrettyPrinter*/null);
    }

    /**
     * Factory method for constructing {@link ObjectWriter} that will
     * serialize objects using specified root type, instead of actual
     * runtime type of value. Type must be a super-type of runtime type.
     * 
     * @since 1.9
     */
    public ObjectWriter writerWithType(JavaType rootType) {
        return new ObjectWriter(this, copySerializationConfig(), rootType, /*PrettyPrinter*/null);
    }

    /**
     * Factory method for constructing {@link ObjectWriter} that will
     * serialize objects using specified root type, instead of actual
     * runtime type of value. Type must be a super-type of runtime type.
     * 
     * @since 1.9
     */
    public ObjectWriter writerWithType(TypeReference<?> rootType) {
        JavaType t = (rootType == null) ? null : _typeFactory.constructType(rootType);
        return new ObjectWriter(this, copySerializationConfig(), t, /*PrettyPrinter*/null);
    }
    
    /**
     * Factory method for constructing {@link ObjectWriter} that will
     * serialize objects using specified pretty printer for indentation
     * (or if null, no pretty printer)
     * 
     * @since 1.9
     */
    public ObjectWriter writer(PrettyPrinter pp) {
        if (pp == null) { // need to use a marker to indicate explicit disabling of pp
            pp = ObjectWriter.NULL_PRETTY_PRINTER;
        }
        return new ObjectWriter(this, copySerializationConfig(), /*root type*/ null, pp);
    }
    
    /**
     * Factory method for constructing {@link ObjectWriter} that will
     * serialize objects using the default pretty printer for indentation
     * 
     * @since 1.9
     */
    public ObjectWriter writerWithDefaultPrettyPrinter() {
        return new ObjectWriter(this, copySerializationConfig(),
                /*root type*/ null, _defaultPrettyPrinter());
    }
    
    /**
     * Factory method for constructing {@link ObjectWriter} that will
     * serialize objects using specified filter provider.
     * 
     * @since 1.9
     */
    public ObjectWriter writer(FilterProvider filterProvider) {
        return new ObjectWriter(this,
                copySerializationConfig().withFilters(filterProvider));
    }
    
    /**
     * Factory method for constructing {@link ObjectWriter} that will
     * pass specific schema object to {@link JsonGenerator} used for
     * writing content.
     * 
     * @param schema Schema to pass to generator
     * 
     * @since 1.9
     */
    public ObjectWriter writer(FormatSchema schema) {
        return new ObjectWriter(this, copySerializationConfig(), schema);
    }
    
    /*
    /**********************************************************
    /* Deprecated ObjectWriter creator methods
    /**********************************************************
     */

    /**
     * @deprecated Since 1.9, use {@link #writerWithType(Class)} instead.
     */
    @Deprecated
    public ObjectWriter typedWriter(Class<?> rootType) {
        return writerWithType(rootType);
    }

    /**
     * @deprecated Since 1.9, use {@link #writerWithType(JavaType)} instead.
     */
    @Deprecated
    public ObjectWriter typedWriter(JavaType rootType) {
        return writerWithType(rootType);
    }

    /**
     * @deprecated Since 1.9, use {@link #writerWithType(TypeReference)} instead.
     */
    @Deprecated
    public ObjectWriter typedWriter(TypeReference<?> rootType) {
        return writerWithType(rootType);
    }
    
    /**
     * @deprecated Since 1.9, use {@link #writerWithView(Class)} instead.
     */
    @Deprecated
    public ObjectWriter viewWriter(Class<?> serializationView) {
        return writerWithView(serializationView);
    }
    
    /**
     * @deprecated Since 1.9, use {@link #writer(FilterProvider)} instead.
     */
    @Deprecated
    public ObjectWriter prettyPrintingWriter(PrettyPrinter pp) {
        return writer(pp);
    }

    /**
     * @deprecated Since 1.9, use {@link #writerWithDefaultPrettyPrinter} instead.
     */
    @Deprecated
    public ObjectWriter defaultPrettyPrintingWriter() {
        return writerWithDefaultPrettyPrinter();
    }
    
    /**
     * @deprecated Since 1.9, use {@link #writer(FilterProvider)} instead.
     */
    @Deprecated
    public ObjectWriter filteredWriter(FilterProvider filterProvider) {
        return writer(filterProvider);
    }
    
    /**
     * @deprecated Since 1.9, use {@link #writer(FilterProvider)} instead.
     */
    @Deprecated
    public ObjectWriter schemaBasedWriter(FormatSchema schema) {
        return writer(schema);
    }
    
    /*
    /**********************************************************
    /* Extended Public API: constructing ObjectReaders
    /* for more advanced configuration
    /**********************************************************
     */

    /**
     * Factory method for constructing {@link ObjectReader} with
     * default settings. Note that the resulting instance is NOT usable as is,
     * without defining expected value type.
     * 
     * @since 1.6
     */
    public ObjectReader reader() {
        return new ObjectReader(this, copyDeserializationConfig())
            .withInjectableValues(_injectableValues);
    }
    
    /**
     * Factory method for constructing {@link ObjectReader} that will
     * update given Object (usually Bean, but can be a Collection or Map
     * as well, but NOT an array) with JSON data. Deserialization occurs
     * normally except that the root-level value in JSON is not used for
     * instantiating a new object; instead give updateable object is used
     * as root.
     * Runtime type of value object is used for locating deserializer,
     * unless overridden by other factory methods of {@link ObjectReader}
     * 
     * @since 1.9
     */
    public ObjectReader readerForUpdating(Object valueToUpdate)
    {
        JavaType t = _typeFactory.constructType(valueToUpdate.getClass());
        return new ObjectReader(this, copyDeserializationConfig(), t, valueToUpdate,
                null, _injectableValues);
    }

    /**
     * Factory method for constructing {@link ObjectReader} that will
     * read or update instances of specified type
     * 
     * @since 1.6
     */
    public ObjectReader reader(JavaType type)
    {
        return new ObjectReader(this, copyDeserializationConfig(), type, null,
                null, _injectableValues);
    }

    /**
     * Factory method for constructing {@link ObjectReader} that will
     * read or update instances of specified type
     * 
     * @since 1.6
     */
    public ObjectReader reader(Class<?> type)
    {
        return reader(_typeFactory.constructType(type));
    }

    /**
     * Factory method for constructing {@link ObjectReader} that will
     * read or update instances of specified type
     * 
     * @since 1.6
     */
    public ObjectReader reader(TypeReference<?> type)
    {
        return reader(_typeFactory.constructType(type));
    }

    /**
     * Factory method for constructing {@link ObjectReader} that will
     * use specified {@link JsonNodeFactory} for constructing JSON trees.
     * 
     * @since 1.6
     */
    public ObjectReader reader(JsonNodeFactory f)
    {
        return new ObjectReader(this, copyDeserializationConfig()).withNodeFactory(f);
    }

    /**
     * Factory method for constructing {@link ObjectReader} that will
     * pass specific schema object to {@link JsonParser} used for
     * reading content.
     * 
     * @param schema Schema to pass to parser
     * 
     * @since 1.8
     */
    public ObjectReader reader(FormatSchema schema) {
        return new ObjectReader(this, copyDeserializationConfig(), null, null,
                schema, _injectableValues);
    }

    /**
     * Factory method for constructing {@link ObjectReader} that will
     * use specified injectable values.
     * 
     * @param injectableValues Injectable values to use
     * 
     * @since 1.9
     */
    public ObjectReader reader(InjectableValues injectableValues) {
        return new ObjectReader(this, copyDeserializationConfig(), null, null,
                null, injectableValues);
    }
    
    /*
    /**********************************************************
    /* Deprecated ObjectReader creator methods
    /**********************************************************
     */
    
    /**
     * @deprecated Since 1.9, use {@link #readerForUpdating} instead.
     */
    @Deprecated
    public ObjectReader updatingReader(Object valueToUpdate) {
        return readerForUpdating(valueToUpdate);
    }
    
    /**
     * @deprecated Since 1.9, use {@link #reader(FormatSchema)} instead.
     */
    @Deprecated
    public ObjectReader schemaBasedReader(FormatSchema schema) {
        return reader(schema);
    }
    
    /*
    /**********************************************************
    /* Extended Public API: convenience type conversion
    /**********************************************************
     */
   
    /**
     * Convenience method for doing two-step conversion from given value, into
     * instance of given value type. This is functionality equivalent to first
     * serializing given value into JSON, then binding JSON data into value
     * of given type, but may be executed without fully serializing into
     * JSON. Same converters (serializers, deserializers) will be used as for
     * data binding, meaning same object mapper configuration works.
     *      
     * @throws IllegalArgumentException If conversion fails due to incompatible type;
     *    if so, root cause will contain underlying checked exception data binding
     *    functionality threw
     */
    @SuppressWarnings("unchecked")
    public <T> T convertValue(Object fromValue, Class<T> toValueType)
        throws IllegalArgumentException
    {
        return (T) _convert(fromValue, _typeFactory.constructType(toValueType));
    } 

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T convertValue(Object fromValue, TypeReference toValueTypeRef)
        throws IllegalArgumentException
    {
        return (T) _convert(fromValue, _typeFactory.constructType(toValueTypeRef));
    } 

    @SuppressWarnings("unchecked")
    public <T> T convertValue(Object fromValue, JavaType toValueType)
        throws IllegalArgumentException
    {
        return (T) _convert(fromValue, toValueType);
    } 

    protected Object _convert(Object fromValue, JavaType toValueType)
        throws IllegalArgumentException
    {
        // sanity check for null first:
        if (fromValue == null) return null;
        /* Then use TokenBuffer, which is a JsonGenerator:
         * (see [JACKSON-175])
         */
        TokenBuffer buf = new TokenBuffer(this);
        try {
            writeValue(buf, fromValue);
            // and provide as with a JsonParser for contents as well!
            JsonParser jp = buf.asParser();
            Object result = readValue(jp, toValueType);
            jp.close();
            return result;
        } catch (IOException e) { // should not occur, no real i/o...
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
    
    /*
    /**********************************************************
    /* Extended Public API: JSON Schema generation
    /**********************************************************
     */

    /**
     * Generate <a href="http://json-schema.org/">Json-schema</a>
     * instance for specified class.
     *
     * @param t The class to generate schema for
     * @return Constructed JSON schema.
     */
    public JsonSchema generateJsonSchema(Class<?> t)
            throws JsonMappingException
    {
        return generateJsonSchema(t, copySerializationConfig());
    }

    /**
     * Generate <a href="http://json-schema.org/">Json-schema</a>
     * instance for specified class, using specific
     * serialization configuration
     *
     * @param t The class to generate schema for
     * @return Constructed JSON schema.
     */
    public JsonSchema generateJsonSchema(Class<?> t, SerializationConfig cfg)
            throws JsonMappingException
    {
        return _serializerProvider.generateJsonSchema(t, cfg, _serializerFactory);
    }

    /*
    /**********************************************************
    /* Internal methods for serialization, overridable
    /**********************************************************
     */

    /**
     * Helper method that should return default pretty-printer to
     * use for generators constructed by this mapper, when instructed
     * to use default pretty printer.
     * 
     * @since 1.7
     */
    protected PrettyPrinter _defaultPrettyPrinter() {
        return new DefaultPrettyPrinter();
    }
    
    /**
     * Method called to configure the generator as necessary and then
     * call write functionality
     */
    protected final void _configAndWriteValue(JsonGenerator jgen, Object value)
        throws IOException, JsonGenerationException, JsonMappingException
    {
        SerializationConfig cfg = copySerializationConfig();
        // [JACKSON-96]: allow enabling pretty printing for ObjectMapper directly
        if (cfg.isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
            jgen.useDefaultPrettyPrinter();
        }
        // [JACKSON-282]: consider Closeable
        if (cfg.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && (value instanceof Closeable)) {
            _configAndWriteCloseable(jgen, value, cfg);
            return;
        }
        boolean closed = false;
        try {
            _serializerProvider.serializeValue(cfg, jgen, value, _serializerFactory);
            closed = true;
            jgen.close();
        } finally {
            /* won't try to close twice; also, must catch exception (so it 
             * will not mask exception that is pending)
             */
            if (!closed) {
                try {
                    jgen.close();
                } catch (IOException ioe) { }
            }
        }
    }

    protected final void _configAndWriteValue(JsonGenerator jgen, Object value, Class<?> viewClass)
        throws IOException, JsonGenerationException, JsonMappingException
    {
        SerializationConfig cfg = copySerializationConfig().withView(viewClass);
        if (cfg.isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
            jgen.useDefaultPrettyPrinter();
        }
        // [JACKSON-282]: consider Closeable
        if (cfg.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && (value instanceof Closeable)) {
            _configAndWriteCloseable(jgen, value, cfg);
            return;
        }
        boolean closed = false;
        try {
            _serializerProvider.serializeValue(cfg, jgen, value, _serializerFactory);
            closed = true;
            jgen.close();
        } finally {
            if (!closed) {
                try {
                    jgen.close();
                } catch (IOException ioe) { }
            }
        }
    }

    /**
     * Helper method used when value to serialize is {@link Closeable} and its <code>close()</code>
     * method is to be called right after serialization has been called
     */
    private final void _configAndWriteCloseable(JsonGenerator jgen, Object value, SerializationConfig cfg)
        throws IOException, JsonGenerationException, JsonMappingException
    {
        Closeable toClose = (Closeable) value;
        try {
            _serializerProvider.serializeValue(cfg, jgen, value, _serializerFactory);
            JsonGenerator tmpJgen = jgen;
            jgen = null;
            tmpJgen.close();
            Closeable tmpToClose = toClose;
            toClose = null;
            tmpToClose.close();
        } finally {
            /* Need to close both generator and value, as long as they haven't yet
             * been closed
             */
            if (jgen != null) {
                try {
                    jgen.close();
                } catch (IOException ioe) { }
            }
            if (toClose != null) {
                try {
                    toClose.close();
                } catch (IOException ioe) { }
            }
        }
    }
    
    /**
     * Helper method used when value to serialize is {@link Closeable} and its <code>close()</code>
     * method is to be called right after serialization has been called
     */
    private final void _writeCloseableValue(JsonGenerator jgen, Object value, SerializationConfig cfg)
        throws IOException, JsonGenerationException, JsonMappingException
    {
        Closeable toClose = (Closeable) value;
        try {
            _serializerProvider.serializeValue(cfg, jgen, value, _serializerFactory);
            if (cfg.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
                jgen.flush();
            }
            Closeable tmpToClose = toClose;
            toClose = null;
            tmpToClose.close();
        } finally {
            if (toClose != null) {
                try {
                    toClose.close();
                } catch (IOException ioe) { }
            }
        }
    }

    /*
    /**********************************************************
    /* Internal methods for deserialization, overridable
    /**********************************************************
     */
    
    /**
     * Actual implementation of value reading+binding operation.
     */
    protected Object _readValue(DeserializationConfig cfg, JsonParser jp, JavaType valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        /* First: may need to read the next token, to initialize
         * state (either before first read from parser, or after
         * previous token has been cleared)
         */
        Object result;
        JsonToken t = _initForReading(jp);
        if (t == JsonToken.VALUE_NULL) {
            // [JACKSON-643]: Ask JsonDeserializer what 'null value' to use:
            result = _findRootDeserializer(cfg, valueType).getNullValue();
        } else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
            result = null;
        } else { // pointing to event other than null
            DeserializationContext ctxt = _createDeserializationContext(jp, cfg);
            JsonDeserializer<Object> deser = _findRootDeserializer(cfg, valueType);
            // ok, let's get the value
            if (cfg.isEnabled(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE)) {
                result = _unwrapAndDeserialize(jp, valueType, ctxt, deser);
            } else {
                result = deser.deserialize(jp, ctxt);
            }
        }
        // Need to consume the token too
        jp.clearCurrentToken();
        return result;
    }

    
    protected Object _readMapAndClose(JsonParser jp, JavaType valueType)
        throws IOException, JsonParseException, JsonMappingException
    {
        try {
            Object result;
            JsonToken t = _initForReading(jp);
            if (t == JsonToken.VALUE_NULL) {
                // [JACKSON-643]: Ask JsonDeserializer what 'null value' to use:
                // (note: probably no need to make a copy of config for just this access)
                result = _findRootDeserializer(this._deserializationConfig, valueType).getNullValue();
            } else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
                result = null;
            } else {
                DeserializationConfig cfg = copyDeserializationConfig();
                DeserializationContext ctxt = _createDeserializationContext(jp, cfg);
                JsonDeserializer<Object> deser = _findRootDeserializer(cfg, valueType);
                if (cfg.isEnabled(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE)) {
                    result = _unwrapAndDeserialize(jp, valueType, ctxt, deser);
                } else {
                    result = deser.deserialize(jp, ctxt);
                }
            }
            // Need to consume the token too
            jp.clearCurrentToken();
            return result;
        } finally {
            try {
                jp.close();
            } catch (IOException ioe) { }
        }
    }
    
    /**
     * Method called to ensure that given parser is ready for reading
     * content for data binding.
     *
     * @return First token to be used for data binding after this call:
     *  can never be null as exception will be thrown if parser can not
     *  provide more tokens.
     *
     * @throws IOException if the underlying input source has problems during
     *   parsing
     * @throws JsonParseException if parser has problems parsing content
     * @throws JsonMappingException if the parser does not have any more
     *   content to map (note: Json "null" value is considered content;
     *   enf-of-stream not)
     */
    protected JsonToken _initForReading(JsonParser jp)
        throws IOException, JsonParseException, JsonMappingException
    {
        /* First: must point to a token; if not pointing to one, advance.
         * This occurs before first read from JsonParser, as well as
         * after clearing of current token.
         */
        JsonToken t = jp.getCurrentToken();
        if (t == null) {
            // and then we must get something...
            t = jp.nextToken();
            if (t == null) {
                /* [JACKSON-99] Should throw EOFException, closest thing
                 *   semantically
                 */
                throw new EOFException("No content to map to Object due to end of input");
            }
        }
        return t;
    }

    protected Object _unwrapAndDeserialize(JsonParser jp, JavaType rootType,
            DeserializationContext ctxt, JsonDeserializer<Object> deser)
        throws IOException, JsonParseException, JsonMappingException
    {
        SerializedString rootName = _deserializerProvider.findExpectedRootName(ctxt.getConfig(), rootType);
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw JsonMappingException.from(jp, "Current token not START_OBJECT (needed to unwrap root name '"
                    +rootName+"'), but "+jp.getCurrentToken());
        }
        if (jp.nextToken() != JsonToken.FIELD_NAME) {
            throw JsonMappingException.from(jp, "Current token not FIELD_NAME (to contain expected root name '"
                    +rootName+"'), but "+jp.getCurrentToken());
        }
        String actualName = jp.getCurrentName();
        if (!rootName.getValue().equals(actualName)) {
            throw JsonMappingException.from(jp, "Root name '"+actualName+"' does not match expected ('"+rootName
                    +"') for type "+rootType);
        }
        // ok, then move to value itself....
        jp.nextToken();
        
        Object result = deser.deserialize(jp, ctxt);
        // and last, verify that we now get matching END_OBJECT
        if (jp.nextToken() != JsonToken.END_OBJECT) {
            throw JsonMappingException.from(jp, "Current token not END_OBJECT (to match wrapper object with root name '"
                    +rootName+"'), but "+jp.getCurrentToken());
        }
        return result;
    }
    
    /*
    /**********************************************************
    /* Internal methods, other
    /**********************************************************
     */

    /**
     * Method called to locate deserializer for the passed root-level value.
     */
    protected JsonDeserializer<Object> _findRootDeserializer(DeserializationConfig cfg, JavaType valueType)
        throws JsonMappingException
    {
        // First: have we already seen it?
        JsonDeserializer<Object> deser = _rootDeserializers.get(valueType);
        if (deser != null) {
            return deser;
        }
        // Nope: need to ask provider to resolve it
        deser = _deserializerProvider.findTypedValueDeserializer(cfg, valueType, null);
        if (deser == null) { // can this happen?
            throw new JsonMappingException("Can not find a deserializer for type "+valueType);
        }
        _rootDeserializers.put(valueType, deser);
        return deser;
    }
    
    protected DeserializationContext _createDeserializationContext(JsonParser jp, DeserializationConfig cfg)
    {
        return new StdDeserializationContext(cfg, jp, _deserializerProvider,
                _injectableValues);
    }
    
    //Allows use of the correct classloader (primarily for OSGi), separating framework from application
    //should be safe to use in all contexts
    /*
    protected <T> void _setupClassLoaderForDeserialization(Class<T> valueType)
    {
        ClassLoader loader = (valueType.getClassLoader() == null) ? Thread.currentThread().getContextClassLoader() : valueType.getClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
    }
    */
}
