package com.alibaba.acm.shaded.org.codehaus.jackson.map.ext;

import java.util.Collection;
import java.util.Map;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StdDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.Provider;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Helper class used for isolating details of handling optional+external types (Joda datetime,
 * javax.xml classes) from standard factories that offer them.
 * 
 * @author tatu
 *
 * @since 1.6.1
 */
public class OptionalHandlerFactory
{
    /* 1.6.1+ To make 2 main "optional" handler groups (javax.xml.stream, Joda date/time)
     * more dynamic, we better only figure out handlers completely dynamically, if and
     * when they are needed. To do this we need to assume package prefixes.
     */

    private final static String PACKAGE_PREFIX_JODA_DATETIME = "org.joda.time.";
    private final static String PACKAGE_PREFIX_JAVAX_XML = "javax.xml.";

    private final static String SERIALIZERS_FOR_JODA_DATETIME = "com.alibaba.acm.shaded.org.codehaus.jackson.map.ext.JodaSerializers";
    private final static String SERIALIZERS_FOR_JAVAX_XML = "com.alibaba.acm.shaded.org.codehaus.jackson.map.ext.CoreXMLSerializers";
    private final static String DESERIALIZERS_FOR_JODA_DATETIME = "com.alibaba.acm.shaded.org.codehaus.jackson.map.ext.JodaDeserializers";
    private final static String DESERIALIZERS_FOR_JAVAX_XML = "com.alibaba.acm.shaded.org.codehaus.jackson.map.ext.CoreXMLDeserializers";

    // Plus we also have a single serializer for DOM Node:
    private final static String CLASS_NAME_DOM_NODE = "org.w3c.dom.Node";
    private final static String CLASS_NAME_DOM_DOCUMENT = "org.w3c.dom.Node";
    private final static String SERIALIZER_FOR_DOM_NODE = "com.alibaba.acm.shaded.org.codehaus.jackson.map.ext.DOMSerializer";
    private final static String DESERIALIZER_FOR_DOM_DOCUMENT = "com.alibaba.acm.shaded.org.codehaus.jackson.map.ext.DOMDeserializer$DocumentDeserializer";
    private final static String DESERIALIZER_FOR_DOM_NODE = "com.alibaba.acm.shaded.org.codehaus.jackson.map.ext.DOMDeserializer$NodeDeserializer";
    
    public final static OptionalHandlerFactory instance = new OptionalHandlerFactory();
    
    protected OptionalHandlerFactory() { }

    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */
    
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type)
//            BasicBeanDescription beanInfo, BeanProperty property)
    {
        Class<?> rawType = type.getRawClass();
        String className = rawType.getName();
        String factoryName;
        
        if (className.startsWith(PACKAGE_PREFIX_JODA_DATETIME)) {
            factoryName = SERIALIZERS_FOR_JODA_DATETIME;
        } else if (className.startsWith(PACKAGE_PREFIX_JAVAX_XML)
                || hasSupertypeStartingWith(rawType, PACKAGE_PREFIX_JAVAX_XML)) {
            factoryName = SERIALIZERS_FOR_JAVAX_XML;
        } else if (doesImplement(rawType, CLASS_NAME_DOM_NODE)) {
            return (JsonSerializer<?>) instantiate(SERIALIZER_FOR_DOM_NODE);
        } else {
            return null;
        }

        Object ob = instantiate(factoryName);
        if (ob == null) { // could warn, if we had logging system (j.u.l?)
            return null;
        }
        @SuppressWarnings("unchecked")
        Provider<Map.Entry<Class<?>,JsonSerializer<?>>> prov = (Provider<Map.Entry<Class<?>,JsonSerializer<?>>>) ob;
        Collection<Map.Entry<Class<?>,JsonSerializer<?>>> entries = prov.provide();

        // first, check for exact match (concrete)
        for (Map.Entry<Class<?>,JsonSerializer<?>> entry : entries) {
            if (rawType == entry.getKey()) {
                return entry.getValue();
            }
        }
        // if no match, check super-type match
        for (Map.Entry<Class<?>,JsonSerializer<?>> entry : entries) {
            if (entry.getKey().isAssignableFrom(rawType)) {
                return entry.getValue();
            }
        }
        // but maybe there's just no match to be found?
        return null;
    }

    public JsonDeserializer<?> findDeserializer(JavaType type, DeserializationConfig config, DeserializerProvider p)
    {
        Class<?> rawType = type.getRawClass();
        String className = rawType.getName();
        String factoryName;
        
        if (className.startsWith(PACKAGE_PREFIX_JODA_DATETIME)) {
            factoryName = DESERIALIZERS_FOR_JODA_DATETIME;
        } else if (className.startsWith(PACKAGE_PREFIX_JAVAX_XML)
                || hasSupertypeStartingWith(rawType, PACKAGE_PREFIX_JAVAX_XML)) {
            factoryName = DESERIALIZERS_FOR_JAVAX_XML;
        } else if (doesImplement(rawType, CLASS_NAME_DOM_DOCUMENT)) {
            return (JsonDeserializer<?>) instantiate(DESERIALIZER_FOR_DOM_DOCUMENT);
        } else if (doesImplement(rawType, CLASS_NAME_DOM_NODE)) {
            return (JsonDeserializer<?>) instantiate(DESERIALIZER_FOR_DOM_NODE);
        } else {
            return null;
        }
        Object ob = instantiate(factoryName);
        if (ob == null) { // could warn, if we had logging system (j.u.l?)
            return null;
        }
        @SuppressWarnings("unchecked")
        Provider<StdDeserializer<?>> prov = (Provider<StdDeserializer<?>>) ob;
        Collection<StdDeserializer<?>> entries = prov.provide();

        // first, check for exact match (concrete)
        for (StdDeserializer<?> deser : entries) {
            if (rawType == deser.getValueClass()) {
                return deser;
            }
        }
        // if no match, check super-type match
        for (StdDeserializer<?> deser : entries) {
            if (deser.getValueClass().isAssignableFrom(rawType)) {
                return deser;
            }
        }
        // but maybe there's just no match to be found?
        return null;
    }
    
    /*
    /**********************************************************
    /* Internal helper methods
    /**********************************************************
     */

    private Object instantiate(String className)
    {
        try {
            return Class.forName(className).newInstance();
        }
        catch (LinkageError e) { }
        // too many different kinds to enumerate here:
        catch (Exception e) { }
        return null;
    }
    
    private boolean doesImplement(Class<?> actualType, String classNameToImplement)
    {
        for (Class<?> type = actualType; type != null; type = type.getSuperclass()) {
            if (type.getName().equals(classNameToImplement)) {
                return true;
            }
            // or maybe one of super-interfaces
            if (hasInterface(type, classNameToImplement)) {
                return true;
            }
        }
        return false;
    }
        
    private boolean hasInterface(Class<?> type, String interfaceToImplement)
    {
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> iface : interfaces) {
            if (iface.getName().equals(interfaceToImplement)) {
                return true;
            }
        }
        // maybe super-interface?
        for (Class<?> iface : interfaces) {
            if (hasInterface(iface, interfaceToImplement)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSupertypeStartingWith(Class<?> rawType, String prefix)
    {
        // first, superclasses
        for (Class<?> supertype = rawType.getSuperclass(); supertype != null; supertype = supertype.getSuperclass()) {
            if (supertype.getName().startsWith(prefix)) {
                return true;
            }
        }
        // then interfaces
        for (Class<?> cls = rawType; cls != null; cls = cls.getSuperclass()) {
            if (hasInterfaceStartingWith(cls, prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInterfaceStartingWith(Class<?> type, String prefix)
    {
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> iface : interfaces) {
            if (iface.getName().startsWith(prefix)) {
                return true;
            }
        }
        // maybe super-interface?
        for (Class<?> iface : interfaces) {
            if (hasInterfaceStartingWith(iface, prefix)) {
                return true;
            }
        }
        return false;
    }
    
}
