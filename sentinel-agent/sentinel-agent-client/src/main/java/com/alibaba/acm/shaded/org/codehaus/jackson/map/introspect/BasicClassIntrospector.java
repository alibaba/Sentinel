package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.AnnotationIntrospector;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ClassIntrospector;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.MapperConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializationConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.SimpleType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

public class BasicClassIntrospector
    extends ClassIntrospector<BasicBeanDescription>
{
    /* We keep a small set of pre-constructed descriptions to use for
     * common non-structured values, such as Numbers and Strings.
     * This is strictly performance optimization to reduce what is
     * usually one-time cost, but seems useful for some cases considering
     * simplicity.
     */
    
    protected final static BasicBeanDescription STRING_DESC;
    static {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, null, null);
        STRING_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(String.class), ac);
    }
    protected final static BasicBeanDescription BOOLEAN_DESC;
    static {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Boolean.TYPE, null, null);
        BOOLEAN_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Boolean.TYPE), ac);
    }
    protected final static BasicBeanDescription INT_DESC;
    static {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Integer.TYPE, null, null);
        INT_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Integer.TYPE), ac);
    }
    protected final static BasicBeanDescription LONG_DESC;
    static {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Long.TYPE, null, null);
        LONG_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Long.TYPE), ac);
    }

    
    // // // Then static filter singletons
    
    /**
     * @since 1.8
     * @deprecated Since 1.9 just don't use
     */
    @Deprecated
    public final static GetterMethodFilter DEFAULT_GETTER_FILTER = new GetterMethodFilter();

    /**
     * @since 1.8
     * @deprecated Since 1.9 just don't use
     */
    @Deprecated
    public final static SetterMethodFilter DEFAULT_SETTER_FILTER = new SetterMethodFilter();

    /**
     * @since 1.8
     * @deprecated Since 1.9 just don't use
     */
    @Deprecated
    public final static SetterAndGetterMethodFilter DEFAULT_SETTER_AND_GETTER_FILTER = new SetterAndGetterMethodFilter();

    protected final static MethodFilter MINIMAL_FILTER = new MinimalMethodFilter();
    
    /*
    /**********************************************************
    /* Life cycle
    /**********************************************************
     */

    public final static BasicClassIntrospector instance = new BasicClassIntrospector();

    public BasicClassIntrospector() { }
    
    /*
    /**********************************************************
    /* Factory method impls
    /**********************************************************
     */

    @Override
    public BasicBeanDescription forSerialization(SerializationConfig cfg,
            JavaType type, MixInResolver r)
    {
        // minor optimization: for JDK types do minimal introspection
        BasicBeanDescription desc = _findCachedDesc(type);
        if (desc == null) {
            desc = BasicBeanDescription.forSerialization(collectProperties(cfg, type, r, true));
        }
        return desc;
    }

    @Override
    public BasicBeanDescription forDeserialization(DeserializationConfig cfg,
            JavaType type, MixInResolver r)
    {
        // minor optimization: for JDK types do minimal introspection
        BasicBeanDescription desc = _findCachedDesc(type);
        if (desc == null) {
            desc = BasicBeanDescription.forDeserialization(collectProperties(cfg, type, r, false));
        }
        return desc;
    }

    @Override
    public BasicBeanDescription forCreation(DeserializationConfig cfg,
            JavaType type, MixInResolver r)
    {
        BasicBeanDescription desc = _findCachedDesc(type);
        if (desc == null) {
            desc = BasicBeanDescription.forDeserialization(collectProperties(cfg, type, r, false));
        }
        return desc;
    }

    @Override
    public BasicBeanDescription forClassAnnotations(MapperConfig<?> cfg,
            JavaType type, MixInResolver r)
    {
        boolean useAnnotations = cfg.isAnnotationProcessingEnabled();
        AnnotationIntrospector ai =  cfg.getAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(type.getRawClass(), (useAnnotations ? ai : null), r);
        return BasicBeanDescription.forOtherUse(cfg, type, ac);
    }

    @Override
    public BasicBeanDescription forDirectClassAnnotations(MapperConfig<?> cfg,
            JavaType type, MixInResolver r)
    {
        boolean useAnnotations = cfg.isAnnotationProcessingEnabled();
        AnnotationIntrospector ai =  cfg.getAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(type.getRawClass(),
                (useAnnotations ? ai : null), r);
        return BasicBeanDescription.forOtherUse(cfg, type, ac);
    }
    
    /*
    /**********************************************************
    /* Overridable helper methods
    /**********************************************************
     */

    /**
     * @since 1.9
     */
    public POJOPropertiesCollector collectProperties(MapperConfig<?> config,
            JavaType type, MixInResolver r, boolean forSerialization)
    {
        AnnotatedClass ac = classWithCreators(config, type, r);
        ac.resolveMemberMethods(MINIMAL_FILTER);
        ac.resolveFields();
        return constructPropertyCollector(config, ac, type, forSerialization).collect();
    }

    /**
     * Overridable method called for creating {@link POJOPropertiesCollector} instance
     * to use; override is needed if a custom sub-class is to be used.
     * 
     * @since 1.9
     */
    protected POJOPropertiesCollector constructPropertyCollector(MapperConfig<?> config,
            AnnotatedClass ac, JavaType type,
            boolean forSerialization)
    {
        return new POJOPropertiesCollector(config, forSerialization, type, ac);
    }
    
    /**
     * @since 1.9
     */
    public AnnotatedClass classWithCreators(MapperConfig<?> config,
            JavaType type, MixInResolver r)
    {
        boolean useAnnotations = config.isAnnotationProcessingEnabled();
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(type.getRawClass(), (useAnnotations ? ai : null), r);
        ac.resolveMemberMethods(MINIMAL_FILTER);
        // true -> include all creators, not just default constructor
        ac.resolveCreators(true);
        return ac;
    }
    
    /**
     * Method called to see if type is one of core JDK types
     * that we have cached for efficiency.
     * 
     * @since 1.9
     */
    protected BasicBeanDescription _findCachedDesc(JavaType type)
    {
        Class<?> cls = type.getRawClass();
        if (cls == String.class) {
            return STRING_DESC;
        }
        if (cls == Boolean.TYPE) {
            return BOOLEAN_DESC;
        }
        if (cls == Integer.TYPE) {
            return INT_DESC;
        }
        if (cls == Long.TYPE) {
            return LONG_DESC;
        }
        return null;
    }
    
    /**
     * Helper method for getting access to filter that only guarantees
     * that methods used for serialization are to be included.
     * 
     * @deprecated Since 1.9 just don't use
     */
    @Deprecated
    protected MethodFilter getSerializationMethodFilter(SerializationConfig cfg)
    {
    	return DEFAULT_GETTER_FILTER;
    }

    /**
     * Helper method for getting access to filter that only guarantees
     * that methods used for deserialization are to be included.
     * 
     * @deprecated Since 1.9 just don't use
     */
    @Deprecated
    protected MethodFilter getDeserializationMethodFilter(DeserializationConfig cfg)
    {
        /* [JACKSON-88]: may also need to include getters (at least for
         * Collection and Map types)
         */
        if (cfg.isEnabled(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS)) {
            return DEFAULT_SETTER_AND_GETTER_FILTER;
            
        }
    	return DEFAULT_SETTER_FILTER;
    }

    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * Going forward, we will only do very minimal filtering;
     * mostly just gets rid of static methods really.
     * 
     * @since 1.9
     */
    private static class MinimalMethodFilter
        implements MethodFilter
    {
        @Override
        public boolean includeMethod(Method m)
        {
            if (Modifier.isStatic(m.getModifiers())) {
                return false;
            }
            int pcount = m.getParameterTypes().length;
            return (pcount <= 2);
        }
    }
    
    /**
     * Filter used to only include methods that have signature that is
     * compatible with "getters": take no arguments, are non-static,
     * and return something.
     * 
     * @deprecated Since 1.9 just don't use
     */
    @Deprecated
    public static class GetterMethodFilter
        implements MethodFilter
    {
        private GetterMethodFilter() { }
    
        @Override
        public boolean includeMethod(Method m)
        {
            return ClassUtil.hasGetterSignature(m);
        }
    }

    /**
     * Filter used to only include methods that have signature that is
     * compatible with "setters": take one and only argument and
     * are non-static.
     *<p>
     * Actually, also need to include 2-arg  methods to support
     * "any setters"; as well as 0-arg getters as long as they
     * return Collection or Map type.
     * 
     * @deprecated Since 1.9 just don't use
     */
    @Deprecated
    public static class SetterMethodFilter
        implements MethodFilter
    {
        @Override
        public boolean includeMethod(Method m)
        {
            // First: we can't use static methods
            if (Modifier.isStatic(m.getModifiers())) {
                return false;
            }
            int pcount = m.getParameterTypes().length;
            // Ok; multiple acceptable parameter counts:
            switch (pcount) {
            case 1:
                // Regular setters take just one param, so include:
                return true;
            case 2:
                /* 2-arg version only for "AnySetters"; they are not
                 * auto-detected, and need to have an annotation.
                 * However, due to annotation inheritance we do, we
                 * don't yet know if sub-classes might have annotations...
                 * so shouldn't leave out any methods quite yet.
                 */
                //if (m.getAnnotation(JsonAnySetter.class) != null) { ... }

                return true;
            }
            return false;
        }
    }
    
    /**
     * Filter used if some getters (namely, once needed for "setterless
     * collection injection") are also needed, not just setters.
     * 
     * @deprecated Since 1.9 just don't use
     */
    @Deprecated
    public final static class SetterAndGetterMethodFilter
        extends SetterMethodFilter
    {
        @SuppressWarnings("deprecation")
        @Override
        public boolean includeMethod(Method m)
        {
            if (super.includeMethod(m)) {
                return true;
            }
            if (!ClassUtil.hasGetterSignature(m)) {
                return false;
            }
            // but furthermore, only accept Collections & Maps, for now
            Class<?> rt = m.getReturnType();
            if (Collection.class.isAssignableFrom(rt)
                || Map.class.isAssignableFrom(rt)) {
                return true; 
            }
            return false;
        }
    }
}
