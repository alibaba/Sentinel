package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl;

import java.lang.reflect.Member;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StdValueInstantiator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeBindings;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Container class for storing information on creators (based on annotations,
 * visibility), to be able to build actual instantiator later on.
 */
public class CreatorCollector
{
    /// Type of bean being created
    final BasicBeanDescription _beanDesc;

    final boolean _canFixAccess;

    protected AnnotatedConstructor _defaultConstructor;
    
    protected AnnotatedWithParams _stringCreator, _intCreator, _longCreator;
    protected AnnotatedWithParams _doubleCreator, _booleanCreator;

    protected AnnotatedWithParams _delegateCreator;
    
    protected AnnotatedWithParams _propertyBasedCreator;
    protected CreatorProperty[] _propertyBasedArgs = null;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */
    
    public CreatorCollector(BasicBeanDescription beanDesc, boolean canFixAccess)
    {
        _beanDesc = beanDesc;
        _canFixAccess = canFixAccess;
    }

    /**
     * @since 1.9.0
     */
    public ValueInstantiator constructValueInstantiator(DeserializationConfig config)
    {
        StdValueInstantiator inst = new StdValueInstantiator(config, _beanDesc.getType());

        JavaType delegateType;

        if (_delegateCreator == null) {
            delegateType = null;
        } else {
            TypeBindings bindings = _beanDesc.bindingsForBeanType();
            delegateType = bindings.resolveType(_delegateCreator.getParameterType(0));
        }
        
        inst.configureFromObjectSettings(_defaultConstructor,
                _delegateCreator, delegateType,
                _propertyBasedCreator, _propertyBasedArgs);
        inst.configureFromStringCreator(_stringCreator);
        inst.configureFromIntCreator(_intCreator);
        inst.configureFromLongCreator(_longCreator);
        inst.configureFromDoubleCreator(_doubleCreator);
        inst.configureFromBooleanCreator(_booleanCreator);
        return inst;
    }
    
    /*
    /**********************************************************
    /* Setters
    /**********************************************************
     */

    public void setDefaultConstructor(AnnotatedConstructor ctor) {
        _defaultConstructor = ctor;
    }
    
    public void addStringCreator(AnnotatedWithParams creator) {
        _stringCreator = verifyNonDup(creator, _stringCreator, "String");
    }
    public void addIntCreator(AnnotatedWithParams creator) {
        _intCreator = verifyNonDup(creator, _intCreator, "int");
    }
    public void addLongCreator(AnnotatedWithParams creator) {
        _longCreator = verifyNonDup(creator, _longCreator, "long");
    }
    public void addDoubleCreator(AnnotatedWithParams creator) {
        _doubleCreator = verifyNonDup(creator, _doubleCreator, "double");
    }
    public void addBooleanCreator(AnnotatedWithParams creator) {
        _booleanCreator = verifyNonDup(creator, _booleanCreator, "boolean");
    }

    public void addDelegatingCreator(AnnotatedWithParams creator) {
        _delegateCreator = verifyNonDup(creator, _delegateCreator, "delegate");
    }
    
    public void addPropertyCreator(AnnotatedWithParams creator, CreatorProperty[] properties)
    {
        _propertyBasedCreator = verifyNonDup(creator, _propertyBasedCreator, "property-based");
        // [JACKSON-470] Better ensure we have no duplicate names either...
        if (properties.length > 1) {
            HashMap<String,Integer> names = new HashMap<String,Integer>();
            for (int i = 0, len = properties.length; i < len; ++i) {
                String name = properties[i].getName();
                Integer old = names.put(name, Integer.valueOf(i));
                if (old != null) {
                    throw new IllegalArgumentException("Duplicate creator property \""+name+"\" (index "+old+" vs "+i+")");
                }
            }
        }
        _propertyBasedArgs = properties;
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */

    protected AnnotatedWithParams verifyNonDup(AnnotatedWithParams newOne, AnnotatedWithParams oldOne,
            String type)
    {
        if (oldOne != null) {
            // important: ok to override factory with constructor; but not within same type, so:
            if (oldOne.getClass() == newOne.getClass()) {
                throw new IllegalArgumentException("Conflicting "+type+" creators: already had "+oldOne+", encountered "+newOne);
            }
        }
        if (_canFixAccess) {
            ClassUtil.checkAndFixAccess((Member) newOne.getAnnotated());
        }
        return newOne;
    }
}
