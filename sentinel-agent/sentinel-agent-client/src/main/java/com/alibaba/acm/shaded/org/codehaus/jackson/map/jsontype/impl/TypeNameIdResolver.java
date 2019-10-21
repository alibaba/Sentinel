package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.MapperConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.NamedType;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

public class TypeNameIdResolver
    extends TypeIdResolverBase
{
    /**
     * @since 1.8
     */
    protected final MapperConfig<?> _config;
    
    /**
     * Mappings from class name to type id, used for serialization
     */
    protected final HashMap<String, String> _typeToId;

    /**
     * Mappings from type id to JavaType, used for deserialization
     */
    protected final HashMap<String, JavaType> _idToType;
    
    protected TypeNameIdResolver(MapperConfig<?> config, JavaType baseType,
            HashMap<String, String> typeToId, HashMap<String, JavaType> idToType)
    {
        super(baseType, config.getTypeFactory());
        _config = config;
        _typeToId = typeToId;
        _idToType = idToType;
    }
 
    public static TypeNameIdResolver construct(MapperConfig<?> config,
            JavaType baseType,
            Collection<NamedType> subtypes, boolean forSer, boolean forDeser)
    {
        // sanity check
        if (forSer == forDeser) throw new IllegalArgumentException();
        HashMap<String, String> typeToId = null;
        HashMap<String, JavaType> idToType = null;

        if (forSer) {
            typeToId = new HashMap<String, String>();
        }
        if (forDeser) {
            idToType = new HashMap<String, JavaType>();
        }
        if (subtypes != null) {
            for (NamedType t : subtypes) {
                /* no name? Need to figure out default; for now, let's just
                 * use non-qualified class name
                 */
                Class<?> cls = t.getType();
                String id = t.hasName() ? t.getName() : _defaultTypeId(cls);
                if (forSer) {
                    typeToId.put(cls.getName(), id);
                }
                if (forDeser) {
                    /* 24-Feb-2011, tatu: [JACKSON-498] One more problem; sometimes
                     *   we have same name for multiple types; if so, use most specific
                     *   one.
                     */
                    JavaType prev = idToType.get(id);
                    if (prev != null) { // Can only override if more specific
                        if (cls.isAssignableFrom(prev.getRawClass())) { // nope, more generic (or same)
                            continue;
                        }
                    }
                    idToType.put(id, config.constructType(cls));
                }
            }
        }
        return new TypeNameIdResolver(config, baseType, typeToId, idToType);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.NAME; }

    @Override
    public String idFromValue(Object value)
    {
        Class<?> cls = value.getClass();
        final String key = cls.getName();
        String name;
        synchronized (_typeToId) {
            name = _typeToId.get(key);
            if (name == null) {
                // 24-Feb-2011, tatu: As per [JACKSON-498], may need to dynamically look up name
                // can either throw an exception, or use default name...
                if (_config.isAnnotationProcessingEnabled()) {
                    BasicBeanDescription beanDesc = _config.introspectClassAnnotations(cls);
                    name = _config.getAnnotationIntrospector().findTypeName(beanDesc.getClassInfo());
                }
                if (name == null) {
                    // And if still not found, let's choose default?
                    name = _defaultTypeId(cls);
                }
                _typeToId.put(key, name);
            }
        }
        return name;
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> type)
    {
        return idFromValue(value);
    }
    
    @Override
    public JavaType typeFromId(String id)
        throws IllegalArgumentException
    {
        JavaType t = _idToType.get(id);
        /* Now: if no type is found, should we try to locate it by
         * some other means? (specifically, if in same package as base type,
         * could just try Class.forName)
         * For now let's not add any such workarounds; can add if need be
         */
        return t;
    }    

    @Override
    public String toString()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append('[').append(getClass().getName());
    	sb.append("; id-to-type=").append(_idToType);
    	sb.append(']');
    	return sb.toString();
    }
    
    /*
    /*********************************************************
    /* Helper methods
    /*********************************************************
     */
    
    /**
     * If no name was explicitly given for a class, we will just
     * use non-qualified class name
     */
    protected static String _defaultTypeId(Class<?> cls)
    {
        String n = cls.getName();
        int ix = n.lastIndexOf('.');
        return (ix < 0) ? n : n.substring(ix+1);
    }
}
