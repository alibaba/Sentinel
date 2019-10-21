package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.AnnotationIntrospector;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.MapperConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.NamedType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.SubtypeResolver;

/**
 * @since 1.5
 */
public class StdSubtypeResolver extends SubtypeResolver
{
    protected LinkedHashSet<NamedType> _registeredSubtypes;

    public StdSubtypeResolver() { }
    
    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    @Override    
    public void registerSubtypes(NamedType... types)
    {
        if (_registeredSubtypes == null) {
            _registeredSubtypes = new LinkedHashSet<NamedType>();
        }
        for (NamedType type : types) {
            _registeredSubtypes.add(type);
        }
    }

    @Override
    public void registerSubtypes(Class<?>... classes)
    {
        NamedType[] types = new NamedType[classes.length];
        for (int i = 0, len = classes.length; i < len; ++i) {
            types[i] = new NamedType(classes[i]);
        }
        registerSubtypes(types);
    }
    
    /**
     * 
     * @param property Base member to use for type resolution: either annotated type (class),
     *    or property (field, getter/setter)
     */
    @Override
    public Collection<NamedType> collectAndResolveSubtypes(AnnotatedMember property,
        MapperConfig<?> config, AnnotationIntrospector ai)
    {
        HashMap<NamedType, NamedType> collected = new HashMap<NamedType, NamedType>();
        // start with registered subtypes (which have precedence)
        if (_registeredSubtypes != null) {
            Class<?> rawBase = property.getRawType();
            for (NamedType subtype : _registeredSubtypes) {
                // is it a subtype of root type?
                if (rawBase.isAssignableFrom(subtype.getType())) { // yes
                    AnnotatedClass curr = AnnotatedClass.constructWithoutSuperTypes(subtype.getType(), ai, config);
                    _collectAndResolve(curr, subtype, config, ai, collected);
                }
            }
        }

        // then annotated types for property itself
        Collection<NamedType> st = ai.findSubtypes(property);
        if (st != null) {
            for (NamedType nt : st) {
                AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(nt.getType(), ai, config);
                _collectAndResolve(ac, nt, config, ai, collected);
            }            
        }
        NamedType rootType = new NamedType(property.getRawType(), null);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(property.getRawType(), ai, config);
            
        // and finally subtypes via annotations from base type (recursively)
        _collectAndResolve(ac, rootType, config, ai, collected);
        return new ArrayList<NamedType>(collected.values());
    }

    @Override
    public Collection<NamedType> collectAndResolveSubtypes(AnnotatedClass type,
            MapperConfig<?> config, AnnotationIntrospector ai)
    {
        HashMap<NamedType, NamedType> subtypes = new HashMap<NamedType, NamedType>();
        // [JACKSON-257] then consider registered subtypes (which have precedence over annotations)
        if (_registeredSubtypes != null) {
            Class<?> rawBase = type.getRawType();
            for (NamedType subtype : _registeredSubtypes) {
                // is it a subtype of root type?
                if (rawBase.isAssignableFrom(subtype.getType())) { // yes
                    AnnotatedClass curr = AnnotatedClass.constructWithoutSuperTypes(subtype.getType(), ai, config);
                    _collectAndResolve(curr, subtype, config, ai, subtypes);
                }
            }
        }
        // and then check subtypes via annotations from base type (recursively)
        NamedType rootType = new NamedType(type.getRawType(), null);
        _collectAndResolve(type, rootType, config, ai, subtypes);
        return new ArrayList<NamedType>(subtypes.values());
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */
    
    /**
     * Method called to find subtypes for a specific type (class)
     */
    protected void _collectAndResolve(AnnotatedClass annotatedType, NamedType namedType,
            MapperConfig<?> config, AnnotationIntrospector ai, HashMap<NamedType, NamedType> collectedSubtypes)
    {
        if (!namedType.hasName()) {
            String name = ai.findTypeName(annotatedType);
            if (name != null) {
                namedType = new NamedType(namedType.getType(), name);
            }
        }

        // First things first: is base type itself included?
        if (collectedSubtypes.containsKey(namedType)) {
            // if so, no recursion; however, may need to update name?
            if (namedType.hasName()) {
                NamedType prev = collectedSubtypes.get(namedType);
                if (!prev.hasName()) {
                    collectedSubtypes.put(namedType, namedType);
                }
            }
            return;
        }
        // if it wasn't, add and check subtypes recursively
        collectedSubtypes.put(namedType, namedType);
        Collection<NamedType> st = ai.findSubtypes(annotatedType);
        if (st != null && !st.isEmpty()) {
            for (NamedType subtype : st) {
                AnnotatedClass subtypeClass = AnnotatedClass.constructWithoutSuperTypes(subtype.getType(), ai, config);
                // One more thing: name may be either in reference, or in subtype:
                if (!subtype.hasName()) {
                    subtype = new NamedType(subtype.getType(), ai.findTypeName(subtypeClass));
                }
                _collectAndResolve(subtypeClass, subtype, config, ai, collectedSubtypes);
            }
        }
    }
}
