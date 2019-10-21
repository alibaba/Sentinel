package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Helper class needed to be able to efficiently access class
 * member functions ({@link Method}s and {@link Constructor}s)
 * in {@link java.util.Map}s.
 */
public final class MemberKey
{
    final static Class<?>[] NO_CLASSES = new Class<?>[0];

    final String _name;
    final Class<?>[] _argTypes;

    public MemberKey(Method m)
    {
        this(m.getName(), m.getParameterTypes());
    }

    public MemberKey(Constructor<?> ctor)
    {
        this("", ctor.getParameterTypes());
    }

    public MemberKey(String name, Class<?>[] argTypes)
    {
        _name = name;
        _argTypes = (argTypes == null) ? NO_CLASSES : argTypes;
    }

    @Override
    public String toString() {
        return _name + "(" + _argTypes.length+"-args)";
    }

    @Override
    public int hashCode()
    {
        return _name.hashCode() + _argTypes.length;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) {
            return false;
        }
        MemberKey other = (MemberKey) o;
        if (!_name.equals(other._name)) {
            return false;
        }
        Class<?>[] otherArgs = other._argTypes;
        int len = _argTypes.length;
        if (otherArgs.length != len) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            Class<?> type1 = otherArgs[i];
            Class<?> type2 = _argTypes[i];
            if (type1 == type2) {
                continue;
            }
            /* 23-Feb-2009, tatu: Are there any cases where we would have to
             *   consider some narrowing conversions or such? For now let's
             *   assume exact type match is enough
             */
            /* 07-Apr-2009, tatu: Indeed there are (see [JACKSON-97]).
             *    This happens with generics when a bound is specified.
             *    I hope this works; check here must be transitive
             */
            if (type1.isAssignableFrom(type2) || type2.isAssignableFrom(type1)) {
                continue;
            }
            return false;
        }
        return true;
    }
}
