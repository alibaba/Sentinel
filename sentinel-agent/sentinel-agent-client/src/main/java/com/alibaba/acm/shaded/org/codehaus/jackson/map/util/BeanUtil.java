package com.alibaba.acm.shaded.org.codehaus.jackson.map.util;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedMethod;

/**
 * Helper class that contains functionality needed by both serialization
 * and deserialization side.
 *
 * @since 1.9
 */
public class BeanUtil
{
    /*
     * Helper method to use for sorting bean properties, based on
     * ordering rules indicated by annotations, config features.
     * 
     * @param config Serialization/Deserialization configuration in effect
     * @param beanDesc Bean description
     * @param props Properties to sort if/as necessary
     * @param defaultSortByAlpha Whether properties should be (re)sorted alphabetically
     *   by default (unless overridden by type)
     */
    /*
    public static <T extends Named> List<T> sortProperties(MapperConfig<?> config,
            BasicBeanDescription beanDesc, List<T> props,
            boolean defaultSortByAlpha)
    {
        // First, order by [JACKSON-90] (explicit ordering and/or alphabetic)
        // and then for [JACKSON-170] (implicitly order creator properties before others)
        List<String> creatorProps = beanDesc.findCreatorPropertyNames();
        // Then how about explicit ordering?
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        AnnotatedClass ac = beanDesc.getClassInfo();
        String[] propertyOrder = intr.findSerializationPropertyOrder(ac);
        Boolean alpha = intr.findSerializationSortAlphabetically(ac);
        boolean sort;
        
        if (alpha == null) {
            sort = defaultSortByAlpha;
        } else {
            sort = alpha.booleanValue();
        }
        // no sorting? no need to shuffle, then
        if (!sort && creatorProps.isEmpty() && propertyOrder == null) {
            return props;
        }
        int size = props.size();
        Map<String,T> all;
        // Need to (re)sort alphabetically?
        if (sort) {
            all = new TreeMap<String,T>();
        } else {
            all = new LinkedHashMap<String,T>(size+size);
        }

        for (T w : props) {
            all.put(w.getName(), w);
        }
        Map<String,T> ordered = new LinkedHashMap<String,T>(size+size);
        // Ok: primarily by explicit order
        if (propertyOrder != null) {
            for (String name : propertyOrder) {
                T w = all.get(name);
                if (w != null) {
                    ordered.put(name, w);
                }
            }
        }
        // And secondly by sorting Creator properties before other unordered properties
        for (String name : creatorProps) {
            T w = all.get(name);
            if (w != null) {
                ordered.put(name, w);
            }
        }
        // And finally whatever is left (trying to put again will not change ordering)
        ordered.putAll(all);
        return new ArrayList<T>(ordered.values());
    }
    */

    /*
    /**********************************************************
    /* Handling "getter" names
    /**********************************************************
     */

    public static String okNameForGetter(AnnotatedMethod am)
    {
        String name = am.getName();
        String str = okNameForIsGetter(am, name);
        if (str == null) {
            str = okNameForRegularGetter(am, name);
        }
        return str;
    }

    public static String okNameForRegularGetter(AnnotatedMethod am, String name)
    {
        if (name.startsWith("get")) {
            /* 16-Feb-2009, tatu: To handle [JACKSON-53], need to block
             *   CGLib-provided method "getCallbacks". Not sure of exact
             *   safe criteria to get decent coverage without false matches;
             *   but for now let's assume there's no reason to use any 
             *   such getter from CGLib.
             *   But let's try this approach...
             */
            if ("getCallbacks".equals(name)) {
                if (isCglibGetCallbacks(am)) {
                    return null;
                }
            } else if ("getMetaClass".equals(name)) {
                /* 30-Apr-2009, tatu: [JACKSON-103], need to suppress
                 *    serialization of a cyclic (and useless) reference
                 */
                if (isGroovyMetaClassGetter(am)) {
                    return null;
                }
            }
            return manglePropertyName(name.substring(3));
        }
        return null;
    }

    public static String okNameForIsGetter(AnnotatedMethod am, String name)
    {
        if (name.startsWith("is")) {
            // plus, must return boolean...
            Class<?> rt = am.getRawType();
            if (rt != Boolean.class && rt != Boolean.TYPE) {
                return null;
            }
            return manglePropertyName(name.substring(2));
        }
        // no, not a match by name
        return null;
    }

    public static String okNameForSetter(AnnotatedMethod am)
    {
        String name = am.getName();
        if (name.startsWith("set")) {
            name = manglePropertyName(name.substring(3));
            if (name == null) { // plain old "set" is no good...
                return null;
            }
            if ("metaClass".equals(name)) {
                // 26-Nov-2009 [JACSON-103], need to suppress this internal groovy method
                if (isGroovyMetaClassSetter(am)) {
                    return null;
                }
            }
            return name;
        }
        return null;
    }

    /*
    /**********************************************************
    /* Helper methods for bean property name handling
    /**********************************************************
     */

    /**
     * This method was added to address [JACKSON-53]: need to weed out
     * CGLib-injected "getCallbacks". 
     * At this point caller has detected a potential getter method
     * with name "getCallbacks" and we need to determine if it is
     * indeed injectect by Cglib. We do this by verifying that the
     * result type is "net.sf.cglib.proxy.Callback[]"
     *<p>
     * Also, see [JACKSON-177]; Hibernate may repackage cglib
     * it uses, so we better catch that too
     */
    protected static boolean isCglibGetCallbacks(AnnotatedMethod am)
    {
        Class<?> rt = am.getRawType();
        // Ok, first: must return an array type
        if (rt == null || !rt.isArray()) {
            return false;
        }
        /* And that type needs to be "net.sf.cglib.proxy.Callback".
         * Theoretically could just be a type that implements it, but
         * for now let's keep things simple, fix if need be.
         */
        Class<?> compType = rt.getComponentType();
        // Actually, let's just verify it's a "net.sf.cglib.*" class/interface
        Package pkg = compType.getPackage();
        if (pkg != null) {
            String pname = pkg.getName();
            if (pname.startsWith("net.sf.cglib")
                // also, as per [JACKSON-177]
                || pname.startsWith("org.hibernate.repackage.cglib")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Similar to {@link #isCglibGetCallbacks}, need to suppress
     * a cyclic reference to resolve [JACKSON-103]
     */
    protected static boolean isGroovyMetaClassSetter(AnnotatedMethod am)
    {
        Class<?> argType = am.getParameterClass(0);
        Package pkg = argType.getPackage();
        if (pkg != null && pkg.getName().startsWith("groovy.lang")) {
            return true;
        }
        return false;
    }

    /**
     * Another helper method to deal with rest of [JACKSON-103]
     */
    protected static boolean isGroovyMetaClassGetter(AnnotatedMethod am)
    {
        Class<?> rt = am.getRawType();
        if (rt == null || rt.isArray()) {
            return false;
        }
        Package pkg = rt.getPackage();
        if (pkg != null && pkg.getName().startsWith("groovy.lang")) {
            return true;
        }
        return false;
    }

    /**
     * Method called to figure out name of the property, given 
     * corresponding suggested name based on a method or field name.
     *
     * @param basename Name of accessor/mutator method, not including prefix
     *  ("get"/"is"/"set")
     */
    protected static String manglePropertyName(String basename)
    {
        int len = basename.length();

        // First things first: empty basename is no good
        if (len == 0) {
            return null;
        }
        // otherwise, lower case initial chars
        StringBuilder sb = null;
        for (int i = 0; i < len; ++i) {
            char upper = basename.charAt(i);
            char lower = Character.toLowerCase(upper);
            if (upper == lower) {
                break;
            }
            if (sb == null) {
                sb = new StringBuilder(basename);
            }
            sb.setCharAt(i, lower);
        }
        return (sb == null) ? basename : sb.toString();
    }
}
