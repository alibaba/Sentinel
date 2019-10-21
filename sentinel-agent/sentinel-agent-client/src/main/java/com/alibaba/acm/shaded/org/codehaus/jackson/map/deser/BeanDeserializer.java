package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JsonCachable;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.ContainerDeserializerBase;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StdDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedClass;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedMember;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedWithParams;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.ClassKey;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.TokenBuffer;

/**
 * Deserializer class that can deserialize instances of
 * arbitrary bean objects, usually from JSON Object structs,
 * but possibly also from simple types like String values.
 */
@JsonCachable
/* Because of costs associated with constructing bean deserializers,
 * they usually should be cached unlike other deserializer types.
 * But more importantly, it is important to be able to cache
 * bean serializers to handle cyclic references.
 */
public class BeanDeserializer
    extends StdDeserializer<Object>
    implements ResolvableDeserializer
{
    /*
    /**********************************************************
    /* Information regarding type being deserialized
    /**********************************************************
     */

    /**
     * Class for which deserializer is built; used for accessing
     * annotations during resolution phase (see {@link #resolve}).
     */
    final protected AnnotatedClass _forClass;
    
    /**
     * Declared type of the bean this deserializer handles.
     */
    final protected JavaType _beanType;

    /**
     * Property that contains value to be deserialized using
     * deserializer; mostly needed to find contextual annotations
     * for subtypes.
     * 
     * @since 1.7
     */
    final protected BeanProperty _property;
    
    /*
    /**********************************************************
    /* Construction configuration
    /**********************************************************
     */

    /**
     * Object that handles details of constructing initial 
     * bean value (to which bind data to), unless instance
     * is passed (via updateValue())
     */
    protected final ValueInstantiator _valueInstantiator;
    
    /**
     * Deserializer that is used iff delegate-based creator is
     * to be used for deserializing from JSON Object.
     */
    protected JsonDeserializer<Object> _delegateDeserializer;
    
    /**
     * If the bean needs to be instantiated using constructor
     * or factory method
     * that takes one or more named properties as argument(s),
     * this creator is used for instantiation.
     */
    protected final PropertyBasedCreator _propertyBasedCreator;

    /**
     * Flag that is set to mark "non-standard" cases; where either
     * we use one of non-default creators, or there are unwrapped
     * values to consider.
     */
    protected boolean _nonStandardCreation;
    
    /*
    /**********************************************************
    /* Property information, setters
    /**********************************************************
     */

    /**
     * Mapping of property names to properties, built when all properties
     * to use have been successfully resolved.
     * 
     * @since 1.7
     */
    final protected BeanPropertyMap _beanProperties;

    /**
     * List of {@link ValueInjector}s, if any injectable values are
     * expected by the bean; otherwise null.
     * This includes injectors used for injecting values via setters
     * and fields, but not ones passed through constructor parameters.
     * 
     * @since 1.9
     */
    final protected ValueInjector[] _injectables;
    
    /**
     * Fallback setter used for handling any properties that are not
     * mapped to regular setters. If setter is not null, it will be
     * called once for each such property.
     */
    protected SettableAnyProperty _anySetter;

    /**
     * In addition to properties that are set, we will also keep
     * track of recognized but ignorable properties: these will
     * be skipped without errors or warnings.
     */
    final protected HashSet<String> _ignorableProps;

    /**
     * Flag that can be set to ignore and skip unknown properties.
     * If set, will not throw an exception for unknown properties.
     */
    final protected boolean _ignoreAllUnknown;

    /**
     * We may also have one or more back reference fields (usually
     * zero or one).
     */
    final protected Map<String, SettableBeanProperty> _backRefs;
    
    /*
    /**********************************************************
    /* Related handlers
    /**********************************************************
     */

    /**
     * Lazily constructed map used to contain deserializers needed
     * for polymorphic subtypes.
     */
    protected HashMap<ClassKey, JsonDeserializer<Object>> _subDeserializers;

    /**
     * If one of properties has "unwrapped" value, we need separate
     * helper object
     * 
     * @since 1.9
     */
    protected UnwrappedPropertyHandler _unwrappedPropertyHandler;

    /**
     * Handler that we need iff any of properties uses external
     * type id.
     */
    protected ExternalTypeHandler _externalTypeIdHandler;
    
    /*
    /**********************************************************
    /* Life-cycle, construction, initialization
    /**********************************************************
     */

    /**
     * @deprecated (since 1.9) Use the constructor that takes {@link ValueInstantiator} instead
     */
    @Deprecated
    public BeanDeserializer(AnnotatedClass forClass, JavaType type, BeanProperty property,
            CreatorCollector creators,
            BeanPropertyMap properties, Map<String, SettableBeanProperty> backRefs,
            HashSet<String> ignorableProps, boolean ignoreAllUnknown,
            SettableAnyProperty anySetter)
    {
        this(forClass, type, property,
                creators.constructValueInstantiator(null),
                properties, backRefs,
                ignorableProps, ignoreAllUnknown,
                anySetter, null);
    }

    /**
     * @since 1.9
     */
    public BeanDeserializer(BeanDescription beanDesc, BeanProperty property,
            ValueInstantiator valueInstantiator,
            BeanPropertyMap properties, Map<String, SettableBeanProperty> backRefs,
            HashSet<String> ignorableProps, boolean ignoreAllUnknown,
            SettableAnyProperty anySetter, List<ValueInjector> injectables)
    {
        this(beanDesc.getClassInfo(), beanDesc.getType(), property,
                valueInstantiator,
                properties, backRefs,
                ignorableProps, ignoreAllUnknown,
                anySetter, injectables);
    }
    
    /**
     * @since 1.9
     */
    protected BeanDeserializer(AnnotatedClass forClass, JavaType type, BeanProperty property,
            ValueInstantiator valueInstantiator,
            BeanPropertyMap properties, Map<String, SettableBeanProperty> backRefs,
            HashSet<String> ignorableProps, boolean ignoreAllUnknown,
            SettableAnyProperty anySetter, List<ValueInjector> injectables)
    {
        super(type);
        _forClass = forClass;
        _beanType = type;
        _property = property;

        _valueInstantiator = valueInstantiator;
        if (valueInstantiator.canCreateFromObjectWith()) {
            _propertyBasedCreator = new PropertyBasedCreator(valueInstantiator);
        } else {
            _propertyBasedCreator = null;
        }
        
        _beanProperties = properties;
        _backRefs = backRefs;
        _ignorableProps = ignorableProps;
        _ignoreAllUnknown = ignoreAllUnknown;
        _anySetter = anySetter;
        _injectables = (injectables == null || injectables.isEmpty()) ? null
                : injectables.toArray(new ValueInjector[injectables.size()]);

        _nonStandardCreation = valueInstantiator.canCreateUsingDelegate()
            || (_propertyBasedCreator != null)
            || !valueInstantiator.canCreateUsingDefault()
            || (_unwrappedPropertyHandler != null);
    }

    /**
     * Copy-constructor that can be used by sub-classes to allow
     * copy-on-write styling copying of settings of an existing instance.
     * 
     * @since 1.7
     */
    protected BeanDeserializer(BeanDeserializer src)
    {
        this(src, src._ignoreAllUnknown);
    }

    /**
     * @since 1.9
     */
    protected BeanDeserializer(BeanDeserializer src, boolean ignoreAllUnknown)
    {
        super(src._beanType);
    
        _forClass = src._forClass;
        _beanType = src._beanType;
        _property = src._property;
        
        _valueInstantiator = src._valueInstantiator;
        _delegateDeserializer = src._delegateDeserializer;
        _propertyBasedCreator = src._propertyBasedCreator;

        _beanProperties = src._beanProperties;
        _backRefs = src._backRefs;
        _ignorableProps = src._ignorableProps;
        _ignoreAllUnknown = ignoreAllUnknown;
        _anySetter = src._anySetter;
        _injectables = src._injectables;

        _nonStandardCreation = src._nonStandardCreation;
        _unwrappedPropertyHandler = src._unwrappedPropertyHandler;
    }

    @Override
    public JsonDeserializer<Object> unwrappingDeserializer()
    {
        /* bit kludgy but we don't want to accidentally change type;
         * sub-classes MUST override this method to support unwrapped
         * properties...
         */
        if (getClass() != BeanDeserializer.class) {
            return this;
        }
        /* main thing really is to just enforce ignoring of unknown
         * properties; since there may be multiple unwrapped values
         * and properties for all may be interleaved...
         */
        return new BeanDeserializer(this, true);
    }
    
    /*
    /**********************************************************
    /* Public accessors
    /**********************************************************
     */

    public boolean hasProperty(String propertyName) {
        return _beanProperties.find(propertyName) != null;
    }

    /**
     * Accessor for checking number of deserialized properties.
     * 
     * @since 1.7
     */
    public int getPropertyCount() { 
        return _beanProperties.size();
    }

    public final Class<?> getBeanClass() { return _beanType.getRawClass(); }

    @Override public JavaType getValueType() { return _beanType; }

    /**
     * 
     * @since 1.6
     */
    public Iterator<SettableBeanProperty> properties()
    {
        if (_beanProperties == null) { // since 1.7
            throw new IllegalStateException("Can only call before BeanDeserializer has been resolved");
        }
        return _beanProperties.allProperties();
    }

    /**
     * Method needed by {@link BeanDeserializerFactory} to properly link
     * managed- and back-reference pairs.
     */
    public SettableBeanProperty findBackReference(String logicalName)
    {
        if (_backRefs == null) {
            return null;
        }
        return _backRefs.get(logicalName);
    }

    /**
     * @since 1.9
     */
    public ValueInstantiator getValueInstantiator() {
        return _valueInstantiator;
    }
    
    /*
    /**********************************************************
    /* Validation, post-processing
    /**********************************************************
     */

    /**
     * Method called to finalize setup of this deserializer,
     * after deserializer itself has been registered.
     * This is needed to handle recursive and transitive dependencies.
     */
    @Override
    public void resolve(DeserializationConfig config, DeserializerProvider provider)
        throws JsonMappingException
    {
        Iterator<SettableBeanProperty> it = _beanProperties.allProperties();
        UnwrappedPropertyHandler unwrapped = null;
        ExternalTypeHandler.Builder extTypes = null;
        
        while (it.hasNext()) {
            SettableBeanProperty origProp = it.next();
            SettableBeanProperty prop = origProp;
            // May already have deserializer from annotations, if so, skip:
            if (!prop.hasValueDeserializer()) {
                prop = prop.withValueDeserializer(findDeserializer(config, provider, prop.getType(), prop));
            }
            // [JACKSON-235]: need to link managed references with matching back references
            prop = _resolveManagedReferenceProperty(config, prop);
            // [JACKSON-132]: support unwrapped values (via @JsonUnwrapped)
            SettableBeanProperty u = _resolveUnwrappedProperty(config, prop);
            if (u != null) {
                prop = u;
                if (unwrapped == null) {
                    unwrapped = new UnwrappedPropertyHandler();
                }
                unwrapped.addProperty(prop);
            }
            // [JACKSON-594]: non-static inner classes too:
            prop = _resolveInnerClassValuedProperty(config, prop);
            if (prop != origProp) {
                _beanProperties.replace(prop);
            }
            
            /* one more thing: if this property uses "external property" type inclusion
             * (see [JACKSON-453]), it needs different handling altogether
             */
            if (prop.hasValueTypeDeserializer()) {
                TypeDeserializer typeDeser = prop.getValueTypeDeserializer();
                if (typeDeser.getTypeInclusion() == JsonTypeInfo.As.EXTERNAL_PROPERTY) {
                    if (extTypes == null) {
                        extTypes = new ExternalTypeHandler.Builder();
                    }
                    extTypes.addExternal(prop, typeDeser.getPropertyName());
                    // In fact, remove from list of known properties to simplify later handling
                    _beanProperties.remove(prop);
                }
            }
        }

        // Finally, "any setter" may also need to be resolved now
        if (_anySetter != null && !_anySetter.hasValueDeserializer()) {
            _anySetter = _anySetter.withValueDeserializer(findDeserializer(config, provider, _anySetter.getType(), _anySetter.getProperty()));
        }

        // as well as delegate-based constructor:
        if (_valueInstantiator.canCreateUsingDelegate()) {
            JavaType delegateType = _valueInstantiator.getDelegateType();
            if (delegateType == null) {
                throw new IllegalArgumentException("Invalid delegate-creator definition for "+_beanType
                        +": value instantiator ("+_valueInstantiator.getClass().getName()
                        +") returned true for 'canCreateUsingDelegate()', but null for 'getDelegateType()'");
            }
            AnnotatedWithParams delegateCreator = _valueInstantiator.getDelegateCreator();
            // Need to create a temporary property to allow contextual deserializers:
            BeanProperty.Std property = new BeanProperty.Std(null,
                    delegateType, _forClass.getAnnotations(), delegateCreator);
            _delegateDeserializer = findDeserializer(config, provider, delegateType, property);
        }
        // or property-based one
        // IMPORTANT: must access properties that _propertyBasedCreator has
        if (_propertyBasedCreator != null) {
            for (SettableBeanProperty prop : _propertyBasedCreator.getCreatorProperties()) {
                if (!prop.hasValueDeserializer()) {
                    _propertyBasedCreator.assignDeserializer(prop,
                           findDeserializer(config, provider, prop.getType(), prop));
                }
            }
        }
        if (extTypes != null) {
            _externalTypeIdHandler = extTypes.build();
            // we consider this non-standard, to offline handling
            _nonStandardCreation = true;
        }
        
        _unwrappedPropertyHandler = unwrapped;
        if (unwrapped != null) { // we consider this non-standard, to offline handling
            _nonStandardCreation = true;
        }
    }

    /**
     * Helper method called to see if given property is part of 'managed' property
     * pair (managed + back reference), and if so, handle resolution details.
     * 
     * @since 1.9
     */
    protected SettableBeanProperty _resolveManagedReferenceProperty(DeserializationConfig config,
            SettableBeanProperty prop)
    {
        String refName = prop.getManagedReferenceName();
        if (refName == null) {
            return prop;
        }
        JsonDeserializer<?> valueDeser = prop.getValueDeserializer();
        SettableBeanProperty backProp = null;
        boolean isContainer = false;
        if (valueDeser instanceof BeanDeserializer) {
            backProp = ((BeanDeserializer) valueDeser).findBackReference(refName);
        } else if (valueDeser instanceof ContainerDeserializerBase<?>) {
            JsonDeserializer<?> contentDeser = ((ContainerDeserializerBase<?>) valueDeser).getContentDeserializer();
            if (!(contentDeser instanceof BeanDeserializer)) {
                throw new IllegalArgumentException("Can not handle managed/back reference '"+refName
                        +"': value deserializer is of type ContainerDeserializerBase, but content type is not handled by a BeanDeserializer "
                        +" (instead it's of type "+contentDeser.getClass().getName()+")");
            }
            backProp = ((BeanDeserializer) contentDeser).findBackReference(refName);
            isContainer = true;
        } else if (valueDeser instanceof AbstractDeserializer) { // [JACKSON-368]: not easy to fix, alas  
            throw new IllegalArgumentException("Can not handle managed/back reference for abstract types (property "+_beanType.getRawClass().getName()+"."+prop.getName()+")");
        } else {
            throw new IllegalArgumentException("Can not handle managed/back reference '"+refName
                    +"': type for value deserializer is not BeanDeserializer or ContainerDeserializerBase, but "
                    +valueDeser.getClass().getName());
        }
        if (backProp == null) {
            throw new IllegalArgumentException("Can not handle managed/back reference '"+refName+"': no back reference property found from type "
                    +prop.getType());
        }
        // also: verify that type is compatible
        JavaType referredType = _beanType;
        JavaType backRefType = backProp.getType();
        if (!backRefType.getRawClass().isAssignableFrom(referredType.getRawClass())) {
            throw new IllegalArgumentException("Can not handle managed/back reference '"+refName+"': back reference type ("
                    +backRefType.getRawClass().getName()+") not compatible with managed type ("
                    +referredType.getRawClass().getName()+")");
        }
        return new SettableBeanProperty.ManagedReferenceProperty(refName, prop, backProp,
                _forClass.getAnnotations(), isContainer);
    }

    /**
     * Helper method called to see if given property might be so-called unwrapped
     * property: these require special handling.
     * 
     * @since 1.9
     */
    protected SettableBeanProperty _resolveUnwrappedProperty(DeserializationConfig config,
            SettableBeanProperty prop)
    {
        AnnotatedMember am = prop.getMember();
        if (am != null && config.getAnnotationIntrospector().shouldUnwrapProperty(am) == Boolean.TRUE) {
            JsonDeserializer<Object> orig = prop.getValueDeserializer();
            JsonDeserializer<Object> unwrapping = orig.unwrappingDeserializer();
            if (unwrapping != orig && unwrapping != null) {
                // might be cleaner to create new instance; but difficult to do reliably, so:
                return prop.withValueDeserializer(unwrapping);
            }
        }
        return null;
    }
    
    /**
     * Helper method that will handle gruesome details of dealing with properties
     * that have non-static inner class as value...
     * 
     * @since 1.9
     */
    protected SettableBeanProperty _resolveInnerClassValuedProperty(DeserializationConfig config,
            SettableBeanProperty prop)
    {            
        /* Should we encounter a property that has non-static inner-class
         * as value, we need to add some more magic to find the "hidden" constructor...
         */
        JsonDeserializer<Object> deser = prop.getValueDeserializer();
        // ideally wouldn't rely on it being BeanDeserializer; but for now it'll have to do
        if (deser instanceof BeanDeserializer) {
            BeanDeserializer bd = (BeanDeserializer) deser;
            ValueInstantiator vi = bd.getValueInstantiator();
            if (!vi.canCreateUsingDefault()) { // no default constructor
                Class<?> valueClass = prop.getType().getRawClass();
                Class<?> enclosing = ClassUtil.getOuterClass(valueClass);
                // and is inner class of the bean class...
                if (enclosing != null && enclosing == _beanType.getRawClass()) {
                    for (Constructor<?> ctor : valueClass.getConstructors()) {
                        Class<?>[] paramTypes = ctor.getParameterTypes();
                        if (paramTypes.length == 1 && paramTypes[0] == enclosing) {
                            if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                                ClassUtil.checkAndFixAccess(ctor);
                            }
                            return new SettableBeanProperty.InnerClassProperty(prop, ctor);
                        }
                    }
                }
            }
        }
        return prop;
    }

    /*
    /**********************************************************
    /* JsonDeserializer implementation
    /**********************************************************
     */

    /**
     * Main deserialization method for bean-based objects (POJOs).
     */
    @Override
    public final Object deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        // common case first:
        if (t == JsonToken.START_OBJECT) {
            jp.nextToken();
            return deserializeFromObject(jp, ctxt);
        }
        // and then others, generally requiring use of @JsonCreator
        switch (t) {
        case VALUE_STRING:
            return deserializeFromString(jp, ctxt);
        case VALUE_NUMBER_INT:
            return deserializeFromNumber(jp, ctxt);
        case VALUE_NUMBER_FLOAT:
	    return deserializeFromDouble(jp, ctxt);
        case VALUE_EMBEDDED_OBJECT:
            return jp.getEmbeddedObject();
        case VALUE_TRUE:
        case VALUE_FALSE:
            return deserializeFromBoolean(jp, ctxt);
        case START_ARRAY:
            // these only work if there's a (delegating) creator...
            return deserializeFromArray(jp, ctxt);
        case FIELD_NAME:
        case END_OBJECT: // added to resolve [JACKSON-319], possible related issues
            return deserializeFromObject(jp, ctxt);
	}
        throw ctxt.mappingException(getBeanClass());
    }

    /**
     * Secondary deserialization method, called in cases where POJO
     * instance is created as part of deserialization, potentially
     * after collecting some or all of the properties to set.
     */
    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt, Object bean)
        throws IOException, JsonProcessingException
    {
        if (_injectables != null) {
            injectValues(ctxt, bean);
        }
        if (_unwrappedPropertyHandler != null) {
            return deserializeWithUnwrapped(jp, ctxt, bean);
        }
        if (_externalTypeIdHandler != null) {
            return deserializeWithExternalTypeId(jp, ctxt, bean);
        }
        JsonToken t = jp.getCurrentToken();
        // 23-Mar-2010, tatu: In some cases, we start with full JSON object too...
        if (t == JsonToken.START_OBJECT) {
            t = jp.nextToken();
        }
        for (; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
            String propName = jp.getCurrentName();
            // Skip field name:
            jp.nextToken();
            SettableBeanProperty prop = _beanProperties.find(propName);
            
            if (prop != null) { // normal case
                try {
                    prop.deserializeAndSet(jp, ctxt, bean);
                } catch (Exception e) {
                    wrapAndThrow(e, bean, propName, ctxt);
                }
                continue;
            }
            /* As per [JACKSON-313], things marked as ignorable should not be
             * passed to any setter
             */
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                jp.skipChildren();
            } else if (_anySetter != null) {
                _anySetter.deserializeAndSet(jp, ctxt, bean, propName);
                continue;
            } else {
                // Unknown: let's call handler method
                handleUnknownProperty(jp, ctxt, bean, propName);
            }
        }
        return bean;
    }

    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        // In future could check current token... for now this should be enough:
        return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
    }
    
    /*
    /**********************************************************
    /* Concrete deserialization methods
    /**********************************************************
     */
    
    public Object deserializeFromObject(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        if (_nonStandardCreation) {
            if (_unwrappedPropertyHandler != null) {
                return deserializeWithUnwrapped(jp, ctxt);
            }
            if (_externalTypeIdHandler != null) {
                return deserializeWithExternalTypeId(jp, ctxt);
            }
            return deserializeFromObjectUsingNonDefault(jp, ctxt);
        }

        final Object bean = _valueInstantiator.createUsingDefault();
        if (_injectables != null) {
            injectValues(ctxt, bean);
        }
        for (; jp.getCurrentToken() != JsonToken.END_OBJECT; jp.nextToken()) {
            String propName = jp.getCurrentName();
            // Skip field name:
            jp.nextToken();
            SettableBeanProperty prop = _beanProperties.find(propName);
            if (prop != null) { // normal case
                try {
                    prop.deserializeAndSet(jp, ctxt, bean);
                } catch (Exception e) {
                    wrapAndThrow(e, bean, propName, ctxt);
                }
                continue;
            }
            _handleUnknown(jp, ctxt, bean, propName);
        }
        return bean;
    }

    private final void _handleUnknown(JsonParser jp, DeserializationContext ctxt,
            Object bean, String propName)
                    throws IOException, JsonProcessingException
    {
        /* As per [JACKSON-313], things marked as ignorable should not be
         * passed to any setter
         */
        if (_ignorableProps != null && _ignorableProps.contains(propName)) {
            jp.skipChildren();
        } else if (_anySetter != null) {
            try {
                _anySetter.deserializeAndSet(jp, ctxt, bean, propName);
            } catch (Exception e) {
                wrapAndThrow(e, bean, propName, ctxt);
            }
        } else {
            // Unknown: let's call handler method
            handleUnknownProperty(jp, ctxt, bean, propName);         
        }
    }
    
    /**
     * @since 1.9
     */
    protected Object deserializeFromObjectUsingNonDefault(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {        
        if (_delegateDeserializer != null) {
            return _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
        }
        if (_propertyBasedCreator != null) {
            return _deserializeUsingPropertyBased(jp, ctxt);
        }
        // should only occur for abstract types...
        if (_beanType.isAbstract()) {
            throw JsonMappingException.from(jp, "Can not instantiate abstract type "+_beanType
                    +" (need to add/enable type information?)");
        }
        throw JsonMappingException.from(jp, "No suitable constructor found for type "
                +_beanType+": can not instantiate from JSON object (need to add/enable type information?)");
    }
    
    public Object deserializeFromString(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        /* Bit complicated if we have delegating creator; may need to use it,
         * or might not...
         */
        if (_delegateDeserializer != null) {
            if (!_valueInstantiator.canCreateFromString()) {
                Object bean = _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
                if (_injectables != null) {
                    injectValues(ctxt, bean);
                }
                return bean;
            }
        }
        return _valueInstantiator.createFromString(jp.getText());
    }

    public Object deserializeFromNumber(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        switch (jp.getNumberType()) {
        case INT:
            if (_delegateDeserializer != null) {
                if (!_valueInstantiator.canCreateFromInt()) {
                    Object bean = _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
                    if (_injectables != null) {
                        injectValues(ctxt, bean);
                    }
                    return bean;
                }
            }
            return _valueInstantiator.createFromInt(jp.getIntValue());
        case LONG:
            if (_delegateDeserializer != null) {
                if (!_valueInstantiator.canCreateFromInt()) {
                    Object bean = _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
                    if (_injectables != null) {
                        injectValues(ctxt, bean);
                    }
                    return bean;
                }
            }
            return _valueInstantiator.createFromLong(jp.getLongValue());
    	}
        // actually, could also be BigInteger, so:
        if (_delegateDeserializer != null) {
            Object bean = _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
            if (_injectables != null) {
                injectValues(ctxt, bean);
            }
            return bean;
        }
        throw ctxt.instantiationException(getBeanClass(), "no suitable creator method found to deserialize from JSON integer number");
    }

    /**
     * Method called to deserialize POJO value from a JSON floating-point
     * number.
     * 
     * @since 1.9
     */
    public Object deserializeFromDouble(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        switch (jp.getNumberType()) {
        case FLOAT: // no separate methods for taking float...
        case DOUBLE:
            if (_delegateDeserializer != null) {
                if (!_valueInstantiator.canCreateFromDouble()) {
                    Object bean = _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
                    if (_injectables != null) {
                        injectValues(ctxt, bean);
                    }
                    return bean;
                }
            }
            return _valueInstantiator.createFromDouble(jp.getDoubleValue());
        }
        // actually, could also be BigDecimal, so:
        if (_delegateDeserializer != null) {
            return _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
        }
        throw ctxt.instantiationException(getBeanClass(), "no suitable creator method found to deserialize from JSON floating-point number");
    }

    /**
     * Method called to deserialize POJO value from a JSON boolean
     * value (true, false)
     * 
     * @since 1.9
     */
    public Object deserializeFromBoolean(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        if (_delegateDeserializer != null) {
            if (!_valueInstantiator.canCreateFromBoolean()) {
                Object bean = _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
                if (_injectables != null) {
                    injectValues(ctxt, bean);
                }
                return bean;
            }
        }
        boolean value = (jp.getCurrentToken() == JsonToken.VALUE_TRUE);
        return _valueInstantiator.createFromBoolean(value);
    }

    /**
     * @since 1.9
     */
    public Object deserializeFromArray(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
    	if (_delegateDeserializer != null) {
    	    try {
    	        Object bean = _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
    	        if (_injectables != null) {
    	            injectValues(ctxt, bean);
    	        }
    	        return bean;
            } catch (Exception e) {
                wrapInstantiationProblem(e, ctxt);
            }
    	}
    	throw ctxt.mappingException(getBeanClass());
    }

    /**
     * Method called to deserialize bean using "property-based creator":
     * this means that a non-default constructor or factory method is
     * called, and then possibly other setters. The trick is that
     * values for creator method need to be buffered, first; and 
     * due to non-guaranteed ordering possibly some other properties
     * as well.
     *
     * @since 1.2
     */
    protected final Object _deserializeUsingPropertyBased(final JsonParser jp, final DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    { 
        final PropertyBasedCreator creator = _propertyBasedCreator;
        PropertyValueBuffer buffer = creator.startBuilding(jp, ctxt);

        // 04-Jan-2010, tatu: May need to collect unknown properties for polymorphic cases
        TokenBuffer unknown = null;

        JsonToken t = jp.getCurrentToken();
        for (; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
            String propName = jp.getCurrentName();
            jp.nextToken(); // to point to value
            // creator property?
            SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
            if (creatorProp != null) {
                // Last creator property to set?
                Object value = creatorProp.deserialize(jp, ctxt);
                if (buffer.assignParameter(creatorProp.getPropertyIndex(), value)) {
                    jp.nextToken(); // to move to following FIELD_NAME/END_OBJECT
                    Object bean;
                    try {
                        bean = creator.build(buffer);
                    } catch (Exception e) {
                        wrapAndThrow(e, _beanType.getRawClass(), propName, ctxt);
                        continue; // never gets here
                    }
                    //  polymorphic?
		    if (bean.getClass() != _beanType.getRawClass()) {
			return handlePolymorphic(jp, ctxt, bean, unknown);
		    }
                    if (unknown != null) { // nope, just extra unknown stuff...
                        bean = handleUnknownProperties(ctxt, bean, unknown);
		    }
		    // or just clean?
                    return deserialize(jp, ctxt, bean);
                }
                continue;
            }
            // regular property? needs buffering
            SettableBeanProperty prop = _beanProperties.find(propName);
            if (prop != null) {
                buffer.bufferProperty(prop, prop.deserialize(jp, ctxt));
                continue;
            }
            /* As per [JACKSON-313], things marked as ignorable should not be
             * passed to any setter
             */
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                jp.skipChildren();
                continue;
            }
            // "any property"?
            if (_anySetter != null) {
                buffer.bufferAnyProperty(_anySetter, propName, _anySetter.deserialize(jp, ctxt));
                continue;
            }
            // Ok then, let's collect the whole field; name and value
            if (unknown == null) {
                unknown = new TokenBuffer(jp.getCodec());
            }
            unknown.writeFieldName(propName);
            unknown.copyCurrentStructure(jp);
        }

        // We hit END_OBJECT, so:
        Object bean;
        try {
            bean =  creator.build(buffer);
        } catch (Exception e) {
            wrapInstantiationProblem(e, ctxt);
            return null; // never gets here
        }
        if (unknown != null) {
            // polymorphic?
            if (bean.getClass() != _beanType.getRawClass()) {
                return handlePolymorphic(null, ctxt, bean, unknown);
            }
            // no, just some extra unknown properties
            return handleUnknownProperties(ctxt, bean, unknown);
        }
        return bean;
    }

    /**
     * Method called in cases where we may have polymorphic deserialization
     * case: that is, type of Creator-constructed bean is not the type
     * of deserializer itself. It should be a sub-class or implementation
     * class; either way, we may have more specific deserializer to use
     * for handling it.
     *
     * @param jp (optional) If not null, parser that has more properties to handle
     *   (in addition to buffered properties); if null, all properties are passed
     *   in buffer
     */
    protected Object handlePolymorphic(JsonParser jp, DeserializationContext ctxt,                                          
            Object bean, TokenBuffer unknownTokens)
        throws IOException, JsonProcessingException
    {  
        // First things first: maybe there is a more specific deserializer available?
        JsonDeserializer<Object> subDeser = _findSubclassDeserializer(ctxt, bean, unknownTokens);
        if (subDeser != null) {
            if (unknownTokens != null) {
                // need to add END_OBJECT marker first
                unknownTokens.writeEndObject();
                JsonParser p2 = unknownTokens.asParser();
                p2.nextToken(); // to get to first data field
                bean = subDeser.deserialize(p2, ctxt, bean);
            }
            // Original parser may also have some leftovers
            if (jp != null) {
                bean = subDeser.deserialize(jp, ctxt, bean);
            }
            return bean;
        }
        // nope; need to use this deserializer. Unknowns we've seen so far?
        if (unknownTokens != null) {
            bean = handleUnknownProperties(ctxt, bean, unknownTokens);
        }
        // and/or things left to process via main parser?
        if (jp != null) {
            bean = deserialize(jp, ctxt, bean);
        }
        return bean;
    }
    
    /*
    /**********************************************************
    /* Handling for cases where we have "unwrapped" values
    /**********************************************************
     */

    /**
     * Method called when there are declared "unwrapped" properties
     * which need special handling
     */
    protected Object deserializeWithUnwrapped(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        if (_delegateDeserializer != null) {
            return _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
        }
        if (_propertyBasedCreator != null) {
            return deserializeUsingPropertyBasedWithUnwrapped(jp, ctxt);
        }
        
        TokenBuffer tokens = new TokenBuffer(jp.getCodec());
        tokens.writeStartObject();
        final Object bean = _valueInstantiator.createUsingDefault();

        if (_injectables != null) {
            injectValues(ctxt, bean);
        }
        
        for (; jp.getCurrentToken() != JsonToken.END_OBJECT; jp.nextToken()) {
            String propName = jp.getCurrentName();
            jp.nextToken();
            SettableBeanProperty prop = _beanProperties.find(propName);
            if (prop != null) { // normal case
                try {
                    prop.deserializeAndSet(jp, ctxt, bean);
                } catch (Exception e) {
                    wrapAndThrow(e, bean, propName, ctxt);
                }
                continue;
            }
            // ignorable things should be ignored
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                jp.skipChildren();
                continue;
            }
            // but... others should be passed to unwrapped property deserializers
            tokens.writeFieldName(propName);
            tokens.copyCurrentStructure(jp);
            // how about any setter? We'll get copies but...
            if (_anySetter != null) {
                try {
                    _anySetter.deserializeAndSet(jp, ctxt, bean, propName);
                } catch (Exception e) {
                    wrapAndThrow(e, bean, propName, ctxt);
                }
                continue;
            }
        }
        tokens.writeEndObject();
        _unwrappedPropertyHandler.processUnwrapped(jp, ctxt, bean, tokens);
        return bean;
    }    

    protected Object deserializeWithUnwrapped(JsonParser jp, DeserializationContext ctxt, Object bean)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = jp.nextToken();
        }
        TokenBuffer tokens = new TokenBuffer(jp.getCodec());
        tokens.writeStartObject();
        for (; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
            String propName = jp.getCurrentName();
            SettableBeanProperty prop = _beanProperties.find(propName);
            jp.nextToken();
            if (prop != null) { // normal case
                try {
                    prop.deserializeAndSet(jp, ctxt, bean);
                } catch (Exception e) {
                    wrapAndThrow(e, bean, propName, ctxt);
                }
                continue;
            }
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                jp.skipChildren();
                continue;
            }
            // but... others should be passed to unwrapped property deserializers
            tokens.writeFieldName(propName);
            tokens.copyCurrentStructure(jp);
            // how about any setter? We'll get copies but...
            if (_anySetter != null) {
                _anySetter.deserializeAndSet(jp, ctxt, bean, propName);
            }
        }
        tokens.writeEndObject();
        _unwrappedPropertyHandler.processUnwrapped(jp, ctxt, bean, tokens);
        return bean;
    }

    protected Object deserializeUsingPropertyBasedWithUnwrapped(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        final PropertyBasedCreator creator = _propertyBasedCreator;
        PropertyValueBuffer buffer = creator.startBuilding(jp, ctxt);

        TokenBuffer tokens = new TokenBuffer(jp.getCodec());
        tokens.writeStartObject();

        JsonToken t = jp.getCurrentToken();
        for (; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
            String propName = jp.getCurrentName();
            jp.nextToken(); // to point to value
            // creator property?
            SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
            if (creatorProp != null) {
                // Last creator property to set?
                Object value = creatorProp.deserialize(jp, ctxt);
                if (buffer.assignParameter(creatorProp.getPropertyIndex(), value)) {
                    t = jp.nextToken(); // to move to following FIELD_NAME/END_OBJECT
                    Object bean;
                    try {
                        bean = creator.build(buffer);
                    } catch (Exception e) {
                        wrapAndThrow(e, _beanType.getRawClass(), propName, ctxt);
                        continue; // never gets here
                    }
                    // if so, need to copy all remaining tokens into buffer
                    while (t == JsonToken.FIELD_NAME) {
                        jp.nextToken(); // to skip name
                        tokens.copyCurrentStructure(jp);
                        t = jp.nextToken();
                    }
                    tokens.writeEndObject();
                    if (bean.getClass() != _beanType.getRawClass()) {
                        // !!! 08-Jul-2011, tatu: Could probably support; but for now
                        //   it's too complicated, so bail out
                        throw ctxt.mappingException("Can not create polymorphic instances with unwrapped values");
                    }
                    return _unwrappedPropertyHandler.processUnwrapped(jp, ctxt, bean, tokens);
                }
                continue;
            }
            // regular property? needs buffering
            SettableBeanProperty prop = _beanProperties.find(propName);
            if (prop != null) {
                buffer.bufferProperty(prop, prop.deserialize(jp, ctxt));
                continue;
            }
            /* As per [JACKSON-313], things marked as ignorable should not be
             * passed to any setter
             */
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                jp.skipChildren();
                continue;
            }
            tokens.writeFieldName(propName);
            tokens.copyCurrentStructure(jp);
            // "any property"?
            if (_anySetter != null) {
                buffer.bufferAnyProperty(_anySetter, propName, _anySetter.deserialize(jp, ctxt));
            }
        }

        // We hit END_OBJECT, so:
        Object bean;
        try {
            bean =  creator.build(buffer);
        } catch (Exception e) {
            wrapInstantiationProblem(e, ctxt);
            return null; // never gets here
        }
        return _unwrappedPropertyHandler.processUnwrapped(jp, ctxt, bean, tokens);
    }

    /*
    /**********************************************************
    /* Handling for cases where we have property/-ies wth
    /* external type id
    /**********************************************************
     */
    
    protected Object deserializeWithExternalTypeId(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        if (_propertyBasedCreator != null) {
            return deserializeUsingPropertyBasedWithExternalTypeId(jp, ctxt);
        }
        return deserializeWithExternalTypeId(jp, ctxt, _valueInstantiator.createUsingDefault());
    }

    protected Object deserializeWithExternalTypeId(JsonParser jp, DeserializationContext ctxt,
            Object bean)
        throws IOException, JsonProcessingException
    {
        final ExternalTypeHandler ext = _externalTypeIdHandler.start();
        for (; jp.getCurrentToken() != JsonToken.END_OBJECT; jp.nextToken()) {
            String propName = jp.getCurrentName();
            jp.nextToken();
            SettableBeanProperty prop = _beanProperties.find(propName);
            if (prop != null) { // normal case
                try {
                    prop.deserializeAndSet(jp, ctxt, bean);
                } catch (Exception e) {
                    wrapAndThrow(e, bean, propName, ctxt);
                }
                continue;
            }
            // ignorable things should be ignored
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                jp.skipChildren();
                continue;
            }
            // but others are likely to be part of external type id thingy...
            if (ext.handleToken(jp, ctxt, propName, bean)) {
                continue;
            }
            // if not, the usual fallback handling:
            if (_anySetter != null) {
                try {
                    _anySetter.deserializeAndSet(jp, ctxt, bean, propName);
                } catch (Exception e) {
                    wrapAndThrow(e, bean, propName, ctxt);
                }
                continue;
            } else {
                // Unknown: let's call handler method
                handleUnknownProperty(jp, ctxt, bean, propName);         
            }
        }
        // and when we get this far, let's try finalizing the deal:
        return ext.complete(jp, ctxt, bean);
    }        

    protected Object deserializeUsingPropertyBasedWithExternalTypeId(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        final ExternalTypeHandler ext = _externalTypeIdHandler.start();
        final PropertyBasedCreator creator = _propertyBasedCreator;
        PropertyValueBuffer buffer = creator.startBuilding(jp, ctxt);

        TokenBuffer tokens = new TokenBuffer(jp.getCodec());
        tokens.writeStartObject();

        JsonToken t = jp.getCurrentToken();
        for (; t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
            String propName = jp.getCurrentName();
            jp.nextToken(); // to point to value
            // creator property?
            SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
            if (creatorProp != null) {
                // Last creator property to set?
                Object value = creatorProp.deserialize(jp, ctxt);
                if (buffer.assignParameter(creatorProp.getPropertyIndex(), value)) {
                    t = jp.nextToken(); // to move to following FIELD_NAME/END_OBJECT
                    Object bean;
                    try {
                        bean = creator.build(buffer);
                    } catch (Exception e) {
                        wrapAndThrow(e, _beanType.getRawClass(), propName, ctxt);
                        continue; // never gets here
                    }
                    // if so, need to copy all remaining tokens into buffer
                    while (t == JsonToken.FIELD_NAME) {
                        jp.nextToken(); // to skip name
                        tokens.copyCurrentStructure(jp);
                        t = jp.nextToken();
                    }
                    if (bean.getClass() != _beanType.getRawClass()) {
                        // !!! 08-Jul-2011, tatu: Could probably support; but for now
                        //   it's too complicated, so bail out
                        throw ctxt.mappingException("Can not create polymorphic instances with unwrapped values");
                    }
                    return ext.complete(jp, ctxt, bean);
                }
                continue;
            }
            // regular property? needs buffering
            SettableBeanProperty prop = _beanProperties.find(propName);
            if (prop != null) {
                buffer.bufferProperty(prop, prop.deserialize(jp, ctxt));
                continue;
            }
            // external type id (or property that depends on it)?
            if (ext.handleToken(jp, ctxt, propName, null)) {
                continue;
            }
            /* As per [JACKSON-313], things marked as ignorable should not be
             * passed to any setter
             */
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                jp.skipChildren();
                continue;
            }
            // "any property"?
            if (_anySetter != null) {
                buffer.bufferAnyProperty(_anySetter, propName, _anySetter.deserialize(jp, ctxt));
            }
        }

        // We hit END_OBJECT, so:
        Object bean;
        try {
            bean = creator.build(buffer);
        } catch (Exception e) {
            wrapInstantiationProblem(e, ctxt);
            return null; // never gets here
        }
        return ext.complete(jp, ctxt, bean);
    }
    
    /*
    /**********************************************************
    /* Overridable helper methods
    /**********************************************************
     */

    protected void injectValues(DeserializationContext ctxt, Object bean)
            throws IOException, JsonProcessingException
    {
        for (ValueInjector injector : _injectables) {
            injector.inject(ctxt, bean);
        }
    }
    
    /**
     * Method called when a JSON property is encountered that has not matching
     * setter, any-setter or field, and thus can not be assigned.
     */
    @Override
    protected void handleUnknownProperty(JsonParser jp, DeserializationContext ctxt, Object beanOrClass, String propName)
        throws IOException, JsonProcessingException
    {
        /* 22-Aug-2010, tatu: Caller now mostly checks for ignorable properties, so
         *    following should not be necessary. However, "handleUnknownProperties()" seems
         *    to still possibly need it so it is left for now.
         */
        // If registered as ignorable, skip
        if (_ignoreAllUnknown ||
            (_ignorableProps != null && _ignorableProps.contains(propName))) {
            jp.skipChildren();
            return;
        }
        /* Otherwise use default handling (call handler(s); if not
         * handled, throw exception or skip depending on settings)
         */
        super.handleUnknownProperty(jp, ctxt, beanOrClass, propName);
    }

    /**
     * Method called to handle set of one or more unknown properties,
     * stored in their entirety in given {@link TokenBuffer}
     * (as field entries, name and value).
     */
    protected Object handleUnknownProperties(DeserializationContext ctxt, Object bean, TokenBuffer unknownTokens)
        throws IOException, JsonProcessingException
    {
        // First: add closing END_OBJECT as marker
        unknownTokens.writeEndObject();
        
        // note: buffer does NOT have starting START_OBJECT
        JsonParser bufferParser = unknownTokens.asParser();
        while (bufferParser.nextToken() != JsonToken.END_OBJECT) {
            String propName = bufferParser.getCurrentName();
            // Unknown: let's call handler method
            bufferParser.nextToken();
            handleUnknownProperty(bufferParser, ctxt, bean, propName);
        }
        return bean;
    }
    
    /**
     * Helper method called to (try to) locate deserializer for given sub-type of
     * type that this deserializer handles.
     */
    protected JsonDeserializer<Object> _findSubclassDeserializer(DeserializationContext ctxt, Object bean, TokenBuffer unknownTokens)
        throws IOException, JsonProcessingException
    {  
        JsonDeserializer<Object> subDeser;

        // First: maybe we have already created sub-type deserializer?
        synchronized (this) {
            subDeser = (_subDeserializers == null) ? null : _subDeserializers.get(new ClassKey(bean.getClass()));
        }
        if (subDeser != null) {
            return subDeser;
        }
        // If not, maybe we can locate one. First, need provider
        DeserializerProvider deserProv = ctxt.getDeserializerProvider();
        if (deserProv != null) {
            JavaType type = ctxt.constructType(bean.getClass());
            /* 09-Dec-2010, tatu: Would be nice to know which property pointed to this
             *    bean... but, alas, no such information is retained, so:
             */
            subDeser = deserProv.findValueDeserializer(ctxt.getConfig(), type, _property);
            // Also, need to cache it
            if (subDeser != null) {
                synchronized (this) {
                    if (_subDeserializers == null) {
                        _subDeserializers = new HashMap<ClassKey,JsonDeserializer<Object>>();;
                    }
                    _subDeserializers.put(new ClassKey(bean.getClass()), subDeser);
                }            
            }
        }
        return subDeser;
    }

    /*
    /**********************************************************
    /* Helper methods for error reporting
    /**********************************************************
     */

    /**
     * Method that will modify caught exception (passed in as argument)
     * as necessary to include reference information, and to ensure it
     * is a subtype of {@link IOException}, or an unchecked exception.
     *<p>
     * Rules for wrapping and unwrapping are bit complicated; essentially:
     *<ul>
     * <li>Errors are to be passed as is (if uncovered via unwrapping)
     * <li>"Plain" IOExceptions (ones that are not of type
     *   {@link JsonMappingException} are to be passed as is
     *</ul>
     */
    public void wrapAndThrow(Throwable t, Object bean, String fieldName,
            DeserializationContext ctxt)
        throws IOException
    {
        /* 05-Mar-2009, tatu: But one nasty edge is when we get
         *   StackOverflow: usually due to infinite loop. But that
         *   usually gets hidden within an InvocationTargetException...
         */
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        // Errors and "plain" IOExceptions to be passed as is
        if (t instanceof Error) {
            throw (Error) t;
        }
        boolean wrap = (ctxt == null) || ctxt.isEnabled(DeserializationConfig.Feature.WRAP_EXCEPTIONS);
        // Ditto for IOExceptions; except we may want to wrap mapping exceptions
        if (t instanceof IOException) {
            if (!wrap || !(t instanceof JsonMappingException)) {
                throw (IOException) t;
            }
        } else if (!wrap) { // [JACKSON-407] -- allow disabling wrapping for unchecked exceptions
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
        }
        // [JACKSON-55] Need to add reference information
        throw JsonMappingException.wrapWithPath(t, bean, fieldName);
    }

    public void wrapAndThrow(Throwable t, Object bean, int index, DeserializationContext ctxt)
        throws IOException
    {
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        // Errors and "plain" IOExceptions to be passed as is
        if (t instanceof Error) {
            throw (Error) t;
        }
        boolean wrap = (ctxt == null) || ctxt.isEnabled(DeserializationConfig.Feature.WRAP_EXCEPTIONS);
        // Ditto for IOExceptions; except we may want to wrap mapping exceptions
        if (t instanceof IOException) {
            if (!wrap || !(t instanceof JsonMappingException)) {
                throw (IOException) t;
            }
        } else if (!wrap) { // [JACKSON-407] -- allow disabling wrapping for unchecked exceptions
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
        }
        // [JACKSON-55] Need to add reference information
        throw JsonMappingException.wrapWithPath(t, bean, index);
    }

    protected void wrapInstantiationProblem(Throwable t, DeserializationContext ctxt)
        throws IOException
    {
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        // Errors and "plain" IOExceptions to be passed as is
        if (t instanceof Error) {
            throw (Error) t;
        }
        boolean wrap = (ctxt == null) || ctxt.isEnabled(DeserializationConfig.Feature.WRAP_EXCEPTIONS);
        if (t instanceof IOException) {
            // Since we have no more information to add, let's not actually wrap..
            throw (IOException) t;
        } else if (!wrap) { // [JACKSON-407] -- allow disabling wrapping for unchecked exceptions
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
        }
        throw ctxt.instantiationException(_beanType.getRawClass(), t);
    }
    
    /**
     * @deprecated Since 1.7 use variant that takes {@link DeserializationContext}
     */
    @Deprecated
    public void wrapAndThrow(Throwable t, Object bean, String fieldName)
        throws IOException
    {
        wrapAndThrow(t, bean, fieldName, null);
    }
    
    /**
     * @deprecated Since 1.7 use variant that takes {@link DeserializationContext}
     */
    @Deprecated
    public void wrapAndThrow(Throwable t, Object bean, int index)
        throws IOException
    {
        wrapAndThrow(t, bean, index, null);
    }    
}
