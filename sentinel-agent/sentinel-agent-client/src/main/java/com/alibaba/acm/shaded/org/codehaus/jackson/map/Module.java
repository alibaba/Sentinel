package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.Version;
import com.alibaba.acm.shaded.org.codehaus.jackson.Versioned;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.BeanDeserializerModifier;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiators;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.BeanSerializerModifier;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeModifier;

/**
 * Simple interface for extensions that can be registered with {@link ObjectMapper}
 * to provide a well-defined set of extensions to default functionality; such as
 * support for new data types.
 *
 * @since 1.7
 */
public abstract class Module
    implements Versioned
{
    /*
    /**********************************************************
    /* Simple accessors
    /**********************************************************
     */
    
    /**
     * Method that returns identifier for module; this can be used by Jackson
     * for informational purposes, as well as in associating extensions with
     * module that provides them.
     */
    public abstract String getModuleName();

    /**
     * Method that returns version of this module. Can be used by Jackson for
     * informational purposes.
     */
    @Override
    public abstract Version version();

    /*
    /**********************************************************
    /* Life-cycle: registration
    /**********************************************************
     */
    
    /**
     * Method called by {@link ObjectMapper} when module is registered.
     * It is called to let module register functionality it provides,
     * using callback methods passed-in context object exposes.
     */
    public abstract void setupModule(SetupContext context);
    
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */

    /**
     * Interface Jackson exposes to modules for purpose of registering
     * extended functionality.
     */
    public interface SetupContext
    {
        /*
        /**********************************************************
        /* Simple accessors
        /**********************************************************
         */
        
        /**
         * Method that returns version information about {@link ObjectMapper} 
         * that implements this context. Modules can use this to choose
         * different settings or initialization order; or even decide to fail
         * set up completely if version is compatible with module.
         */
        public Version getMapperVersion();

        /**
         * Method that returns current deserialization configuration
         * settings. Since modules may be interested in these settings,
         * caller should make sure to make changes to settings before
         * module registrations.
         */
        public DeserializationConfig getDeserializationConfig();

        /**
         * Method that returns current serialization configuration
         * settings. Since modules may be interested in these settings,
         * caller should make sure to make changes to settings before
         * module registrations.
         * 
         * @since 1.7.1 (1.7.0 unfortunately had a typo in method name!)
         */
        public SerializationConfig getSerializationConfig();

        /**
         * @since 1.9.0
         */
        public boolean isEnabled(DeserializationConfig.Feature f);

        /**
         * @since 1.9.0
         */
        public boolean isEnabled(SerializationConfig.Feature f);

        /**
         * @since 1.9.0
         */
        public boolean isEnabled(JsonParser.Feature f);

        /**
         * @since 1.9.0
         */
        public boolean isEnabled(JsonGenerator.Feature f);
        
        /*
        /**********************************************************
        /* Handler registration; serializers/deserializers
        /**********************************************************
         */
        
        /**
         * Method that module can use to register additional deserializers to use for
         * handling types.
         * 
         * @param d Object that can be called to find deserializer for types supported
         *   by module (null returned for non-supported types)
         */
        public void addDeserializers(Deserializers d);

        /**
         * Method that module can use to register additional deserializers to use for
         * handling Map key values (which are separate from value deserializers because
         * they are always serialized from String values)
         *
         * @since 1.8
         */
        public void addKeyDeserializers(KeyDeserializers s);
        
        /**
         * Method that module can use to register additional serializers to use for
         * handling types.
         * 
         * @param s Object that can be called to find serializer for types supported
         *   by module (null returned for non-supported types)
         */
        public void addSerializers(Serializers s);

        /**
         * Method that module can use to register additional serializers to use for
         * handling Map key values (which are separate from value serializers because
         * they must write <code>JsonToken.FIELD_NAME</code> instead of String value).
         *
         * @since 1.8
         */
        public void addKeySerializers(Serializers s);

        /*
        /**********************************************************
        /* Handler registration; other
        /**********************************************************
         */
        
        /**
         * Method that module can use to register additional modifier objects to
         * customize configuration and construction of bean deserializers.
         * 
         * @param mod Modifier to register
         */
        public void addBeanDeserializerModifier(BeanDeserializerModifier mod);

        /**
         * Method that module can use to register additional modifier objects to
         * customize configuration and construction of bean serializers.
         * 
         * @param mod Modifier to register
         */
        public void addBeanSerializerModifier(BeanSerializerModifier mod);

        /**
         * Method that module can use to register additional
         * {@link AbstractTypeResolver} instance, to handle resolution of
         * abstract to concrete types (either by defaulting, or by materializing).
         * 
         * @param resolver Resolver to add.
         * 
         * @since 1.8
         */
        public void addAbstractTypeResolver(AbstractTypeResolver resolver);

        /**
         * Method that module can use to register additional
         * {@link TypeModifier} instance, which can augment {@link com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType}
         * instances constructed by {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeFactory}.
         * 
         * @param modifier to add
         * 
         * @since 1.8
         */
        public void addTypeModifier(TypeModifier modifier);

        /**
         * Method that module can use to register additional {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator}s,
         * by adding {@link ValueInstantiators} object that gets called when 
         * instantatiator is needed by a deserializer.
         * 
         * @param instantiators Object that can provide {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator}s for
         *    constructing POJO values during deserialization
         * 
         * @since 1.9
         */
        public void addValueInstantiators(ValueInstantiators instantiators);
        
        /**
         * Method for registering specified {@link AnnotationIntrospector} as the highest
         * priority introspector (will be chained with existing introspector(s) which
         * will be used as fallbacks for cases this introspector does not handle)
         * 
         * @param ai Annotation introspector to register.
         */
        public void insertAnnotationIntrospector(AnnotationIntrospector ai);

        /**
         * Method for registering specified {@link AnnotationIntrospector} as the lowest
         * priority introspector, chained with existing introspector(s) and called
         * as fallback for cases not otherwise handled.
         * 
         * @param ai Annotation introspector to register.
         */
        public void appendAnnotationIntrospector(AnnotationIntrospector ai);

        /**
         * Method used for defining mix-in annotations to use for augmenting
         * specified class or interface.
         * All annotations from
         * <code>mixinSource</code> are taken to override annotations
         * that <code>target</code> (or its supertypes) has.
         *<p>
         * Note: mix-ins are registered both for serialization and deserialization
         * (which can be different internally).
         *<p>
         * Note: currently only one set of mix-in annotations can be defined for
         * a single class; so if multiple modules register mix-ins, highest
         * priority one (last one registered) will have priority over other modules.
         *
         * @param target Class (or interface) whose annotations to effectively override
         * @param mixinSource Class (or interface) whose annotations are to
         *   be "added" to target's annotations, overriding as necessary
         */
        public void setMixInAnnotations(Class<?> target, Class<?> mixinSource);
    }
}
