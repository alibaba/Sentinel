package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl.BeanPropertyMap;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl.ValueInjector;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedMember;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.Annotations;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Builder class used for aggregating deserialization information about
 * a POJO, in order to build a {@link JsonDeserializer} for deserializing
 * intances.
 * 
 * @since 1.7
 */
public class BeanDeserializerBuilder
{
    /*
    /**********************************************************
    /* General information about POJO
    /**********************************************************
     */

    final protected BasicBeanDescription _beanDesc;
    
    /*
    /**********************************************************
    /* Accumulated information about properties
    /**********************************************************
     */
    
    /**
     * Properties to deserialize collected so far.
     *<p>
     * Note: since 1.9.1, LinkedHashMap has been used, since preservation
     * of order is actually important for some use cases.
     */
    final protected HashMap<String, SettableBeanProperty> _properties = new LinkedHashMap<String, SettableBeanProperty>();

    /**
     * Value injectors for deserialization
     * 
     * @since 1.9
     */
    protected List<ValueInjector> _injectables;
    
    /**
     * Back-reference properties this bean contains (if any)
     */
    protected HashMap<String, SettableBeanProperty> _backRefProperties;

    /**
     * Set of names of properties that are recognized but are to be ignored for deserialization
     * purposes (meaning no exception is thrown, value is just skipped).
     */
    protected HashSet<String> _ignorableProps;
    
    /**
     * Object that will handle value instantiation for the bean type.
     * 
     * @since 1.9
     */
    protected ValueInstantiator _valueInstantiator;

    /**
     * Fallback setter used for handling any properties that are not
     * mapped to regular setters. If setter is not null, it will be
     * called once for each such property.
     */
    protected SettableAnyProperty _anySetter;

    /**
     * Flag that can be set to ignore and skip unknown properties.
     * If set, will not throw an exception for unknown properties.
     */
    protected boolean _ignoreAllUnknown;
    
    /*
    /**********************************************************
    /* Life-cycle: construction
    /**********************************************************
     */
    
    public BeanDeserializerBuilder(BasicBeanDescription beanDesc)
    { 
        _beanDesc = beanDesc;
    }

    /**
     * Copy constructor for sub-classes to use, when constructing
     * custom builder instances
     * 
     * @since 1.9
     */
    protected BeanDeserializerBuilder(BeanDeserializerBuilder src)
    {
        _beanDesc = src._beanDesc;
        _anySetter = src._anySetter;
        _ignoreAllUnknown = src._ignoreAllUnknown;

        // let's make copy of properties
        _properties.putAll(src._properties);
        _backRefProperties = _copy(src._backRefProperties);
        // Hmmh. Should we create defensive copies here? For now, not yet
        _ignorableProps = src._ignorableProps;
        _valueInstantiator = src._valueInstantiator;
    }

    private static HashMap<String, SettableBeanProperty> _copy(HashMap<String, SettableBeanProperty> src)
    {
        if (src == null) {
            return null;
        }
        return new HashMap<String, SettableBeanProperty>(src);
    }
    
    /*
    /**********************************************************
    /* Life-cycle: state modification (adders, setters)
    /**********************************************************
     */

    /**
     * Method for adding a new property or replacing a property.
     */
    public void addOrReplaceProperty(SettableBeanProperty prop, boolean allowOverride)
    {
        _properties.put(prop.getName(), prop);
    }

    /**
     * Method to add a property setter. Will ensure that there is no
     * unexpected override; if one is found will throw a
     * {@link IllegalArgumentException}.
     */
    public void addProperty(SettableBeanProperty prop)
    {
        SettableBeanProperty old =  _properties.put(prop.getName(), prop);
        if (old != null && old != prop) { // should never occur...
            throw new IllegalArgumentException("Duplicate property '"+prop.getName()+"' for "+_beanDesc.getType());
        }
    }

    /**
     * Method called to add a property that represents so-called back reference;
     * reference that "points back" to object that has forward reference to
     * currently built bean.
     */
    public void  addBackReferenceProperty(String referenceName, SettableBeanProperty prop)
    {
        if (_backRefProperties == null) {
            _backRefProperties = new HashMap<String, SettableBeanProperty>(4);
        }
        _backRefProperties.put(referenceName, prop);
        // also: if we had property with same name, actually remove it
        if (_properties != null) {
            _properties.remove(prop.getName());
        }
    }

    /**
     * @since 1.9
     */
    public void addInjectable(String propertyName, JavaType propertyType,
            Annotations contextAnnotations, AnnotatedMember member,
            Object valueId)
    {
        if (_injectables == null) {
            _injectables = new ArrayList<ValueInjector>();
        }
        _injectables.add(new ValueInjector(propertyName, propertyType,
                contextAnnotations, member, valueId));
    }
    
    /**
     * Method that will add property name as one of properties that can
     * be ignored if not recognized.
     */
    public void addIgnorable(String propName)
    {
        if (_ignorableProps == null) {
            _ignorableProps = new HashSet<String>();
        }
        _ignorableProps.add(propName);
    }

    /**
     * Method called by deserializer factory, when a "creator property"
     * (something that is passed via constructor- or factory method argument;
     * instead of setter or field).
     *<p>
     * Default implementation does not do anything; we may need to revisit this
     * decision if these properties need to be available through accessors.
     * For now, however, we just have to ensure that we don't try to resolve
     * types that masked setter/field has (see [JACKSON-700] for details).
     * 
     * @since 1.9.2
     */
    public void addCreatorProperty(BeanPropertyDefinition propDef)
    {
        // do nothing
    }
    
    public void setAnySetter(SettableAnyProperty s)
    {
        if (_anySetter != null && s != null) {
            throw new IllegalStateException("_anySetter already set to non-null");
        }
        _anySetter = s;
    }

    public void setIgnoreUnknownProperties(boolean ignore) {
        _ignoreAllUnknown = ignore;
    }

    /**
     * @since 1.9
     */
    public void setValueInstantiator(ValueInstantiator inst) {
        _valueInstantiator = inst;
    }
    
    /*
    /**********************************************************
    /* Public accessors
    /**********************************************************
     */
    
    /**
     * Method that allows accessing all properties that this
     * builder currently contains.
     *<p>
     * Note that properties are returned in order that properties
     * are ordered (explictly, or by rule), which is the serialization
     * order.
     * 
     * @since 1.8.3
     */
    public Iterator<SettableBeanProperty> getProperties() {
        return _properties.values().iterator();
    }
    
    public boolean hasProperty(String propertyName) {
        return _properties.containsKey(propertyName);
    }
    
    public SettableBeanProperty removeProperty(String name)
    {
        return _properties.remove(name);
    }

    /**
     * @since 1.9
     */
    public ValueInstantiator getValueInstantiator() {
        return _valueInstantiator;
    }
    
    /*
    /**********************************************************
    /* Build method(s)
    /**********************************************************
     */

    public JsonDeserializer<?> build(BeanProperty forProperty)
    {
        BeanPropertyMap propertyMap = new BeanPropertyMap(_properties.values());
        propertyMap.assignIndexes();
        return new BeanDeserializer(_beanDesc, forProperty,
                _valueInstantiator, propertyMap, _backRefProperties, _ignorableProps, _ignoreAllUnknown,
                _anySetter, _injectables);
    }
}
