package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;

/**
 * {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver} implementation
 * that converts between fully-qualified
 * Java class names and (JSON) Strings.
 */
public class ClassNameIdResolver
    extends TypeIdResolverBase
{
    public ClassNameIdResolver(JavaType baseType, TypeFactory typeFactory) {
        super(baseType, typeFactory);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CLASS; }

    public void registerSubtype(Class<?> type, String name) {
        // not used with class name - based resolvers
    }
    
    @Override
    public String idFromValue(Object value)
    {
        return _idFrom(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> type)
    {
        return _idFrom(value, type);
    }

    @Override
    public JavaType typeFromId(String id)
    {
        /* 30-Jan-2010, tatu: Most ids are basic class names; so let's first
         *    check if any generics info is added; and only then ask factory
         *    to do translation when necessary
         */
        if (id.indexOf('<') > 0) {
            JavaType t = TypeFactory.fromCanonical(id);
            // note: may want to try combining with specialization (esp for EnumMap)
            return t;
        }
        try {
            Class<?> cls =  ClassUtil.findClass(id);
            return _typeFactory.constructSpecializedType(_baseType, cls);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Invalid type id '"+id+"' (for id type 'Id.class'): no such class found");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid type id '"+id+"' (for id type 'Id.class'): "+e.getMessage(), e);
        }
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */
    
    protected final String _idFrom(Object value, Class<?> cls)
    {
        // [JACKSON-380] Need to ensure that "enum subtypes" work too
        if (Enum.class.isAssignableFrom(cls)) {
            if (!cls.isEnum()) { // means that it's sub-class of base enum, so:
                cls = cls.getSuperclass();
            }
        }
        String str = cls.getName();
        if (str.startsWith("java.util")) {
            /* 25-Jan-2009, tatu: There are some internal classes that
             *   we can not access as is. We need better mechanism; for
             *   now this has to do...
             */
            /* Enum sets and maps are problematic since we MUST know
             * type of contained enums, to be able to deserialize.
             * In addition, EnumSet is not a concrete type either
             */
            if (value instanceof EnumSet<?>) { // Regular- and JumboEnumSet...
                Class<?> enumClass = ClassUtil.findEnumType((EnumSet<?>) value);
                // not optimal: but EnumSet is not a customizable type so this is sort of ok
                str = TypeFactory.defaultInstance().constructCollectionType(EnumSet.class, enumClass).toCanonical();
            } else if (value instanceof EnumMap<?,?>) {
                Class<?> enumClass = ClassUtil.findEnumType((EnumMap<?,?>) value);
                Class<?> valueClass = Object.class;
                // not optimal: but EnumMap is not a customizable type so this is sort of ok
                str = TypeFactory.defaultInstance().constructMapType(EnumMap.class, enumClass, valueClass).toCanonical();
            } else {
                String end = str.substring(9);
                if ((end.startsWith(".Arrays$") || end.startsWith(".Collections$"))
                       && str.indexOf("List") >= 0) {
                    /* 17-Feb-2010, tatus: Another such case: result of
                     *    Arrays.asList() is named like so in Sun JDK...
                     *   Let's just plain old ArrayList in its place
                     * NOTE: chances are there are plenty of similar cases
                     * for other wrappers... (immutable, singleton, synced etc)
                     */
                    str = "java.util.ArrayList";
                }
            }
        } else if (str.indexOf('$') >= 0) {
            /* Other special handling may be needed for inner classes, [JACKSON-584].
             * The best way to handle would be to find 'hidden' constructor; pass parent
             * value etc (which is actually done for non-anonymous static classes!),
             * but that is just not possible due to various things. So, we will instead
             * try to generalize type into something we will be more likely to be able
             * construct.
             */
            Class<?> outer = ClassUtil.getOuterClass(cls);
            if (outer != null) {
                /* one more check: let's actually not worry if the declared
                 * static type is non-static as well; if so, deserializer does
                 * have a chance at figuring it all out.
                 */
                Class<?> staticType = _baseType.getRawClass();
                if (ClassUtil.getOuterClass(staticType) == null) {
                    // Is this always correct? Seems like it should be...
                    cls = _baseType.getRawClass();
                    str = cls.getName();
                }
            }
        }
        return str;
    }
}
