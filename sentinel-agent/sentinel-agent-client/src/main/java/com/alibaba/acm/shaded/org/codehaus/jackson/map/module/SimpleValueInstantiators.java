package com.alibaba.acm.shaded.org.codehaus.jackson.map.module;

import java.util.HashMap;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanDescription;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiators;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.ClassKey;

public class SimpleValueInstantiators
    extends ValueInstantiators.Base
{
    /**
     * Mappings from raw (type-erased, i.e. non-generic) types
     * to matching {@link ValueInstantiator} instances.
     */
    protected HashMap<ClassKey,ValueInstantiator> _classMappings;

    /*
    /**********************************************************
    /* Life-cycle, construction and configuring
    /**********************************************************
     */

    public SimpleValueInstantiators()
    {
        _classMappings = new HashMap<ClassKey,ValueInstantiator>();        
    }
    
    public SimpleValueInstantiators addValueInstantiator(Class<?> forType,
            ValueInstantiator inst)
    {
        _classMappings.put(new ClassKey(forType), inst);
        return this;
    }
    
    @Override
    public ValueInstantiator findValueInstantiator(DeserializationConfig config,
            BeanDescription beanDesc, ValueInstantiator defaultInstantiator)
    {
        ValueInstantiator inst = _classMappings.get(new ClassKey(beanDesc.getBeanClass()));
        return (inst == null) ? defaultInstantiator : inst;
    }
}
