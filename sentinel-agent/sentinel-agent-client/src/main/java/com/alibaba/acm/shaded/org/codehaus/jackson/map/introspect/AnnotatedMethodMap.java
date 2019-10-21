package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Simple helper class used to keep track of collection of
 * {@link AnnotatedMethod}s, accessible by lookup. Lookup
 * is usually needed for augmenting and overriding annotations.
 */
public final class AnnotatedMethodMap
    implements Iterable<AnnotatedMethod>
{
    protected LinkedHashMap<MemberKey,AnnotatedMethod> _methods;

    public AnnotatedMethodMap() { }

    /**
     * Method called to add specified annotated method in the Map.
     */
    public void add(AnnotatedMethod am)
    {
        if (_methods == null) {
            _methods = new LinkedHashMap<MemberKey,AnnotatedMethod>();
        }
        _methods.put(new MemberKey(am.getAnnotated()), am);
    }

    /**
     * Method called to remove specified method, assuming
     * it exists in the Map
     */
    public AnnotatedMethod remove(AnnotatedMethod am)
    {
        return remove(am.getAnnotated());
    }

    public AnnotatedMethod remove(Method m)
    {
        if (_methods != null) {
            return _methods.remove(new MemberKey(m));
        }
        return null;
    }

    public boolean isEmpty() {
        return (_methods == null || _methods.size() == 0);
    }

    public int size() {
        return (_methods == null) ? 0 : _methods.size();
    }

    public AnnotatedMethod find(String name, Class<?>[] paramTypes)
    {
        if (_methods == null) {
            return null;
        }
        return _methods.get(new MemberKey(name, paramTypes));
    }

    public AnnotatedMethod find(Method m)
    {
        if (_methods == null) {
            return null;
        }
        return _methods.get(new MemberKey(m));
    }

    /*
    /**********************************************************
    /* Iterable implementation (for iterating over values)
    /**********************************************************
     */

    @Override
    public Iterator<AnnotatedMethod> iterator()
    {
        if (_methods != null) {
            return _methods.values().iterator();
        }
        List<AnnotatedMethod> empty = Collections.emptyList();
        return empty.iterator();
    }
}
