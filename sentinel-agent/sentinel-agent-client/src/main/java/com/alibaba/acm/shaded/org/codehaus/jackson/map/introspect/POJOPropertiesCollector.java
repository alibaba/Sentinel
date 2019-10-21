package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.BeanUtil;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Helper class used for aggregating information about all possible
 * properties of a POJO.
 * 
 * @since 1.9
 */
public class POJOPropertiesCollector
{
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */
    
    /**
     * Configuration settings
     */
    protected final MapperConfig<?> _config;

    /**
     * True if introspection is done for serialization (giving
     *   precedence for serialization annotations), or not (false, deserialization)
     */
    protected final boolean _forSerialization;
    
    /**
     * Type of POJO for which properties are being collected.
     */
    protected final JavaType _type;

    /**
     * Low-level introspected class information (methods, fields etc)
     */
    protected final AnnotatedClass _classDef;

    protected final VisibilityChecker<?> _visibilityChecker;

    protected final AnnotationIntrospector _annotationIntrospector;
    
    /*
    /**********************************************************
    /* Collected information
    /**********************************************************
     */

    /**
     * Set of logical property information collected so far
     */
    protected final LinkedHashMap<String, POJOPropertyBuilder> _properties
        = new LinkedHashMap<String, POJOPropertyBuilder>();

    protected LinkedList<POJOPropertyBuilder> _creatorProperties = null;
    
    protected LinkedList<AnnotatedMethod> _anyGetters = null;

    protected LinkedList<AnnotatedMethod> _anySetters = null;

    /**
     * Method(s) marked with 'JsonValue' annotation
     */
    protected LinkedList<AnnotatedMethod> _jsonValueGetters = null;

    /**
     * Lazily collected list of properties that can be implicitly
     * ignored during serialization; only updated when collecting
     * information for deserialization purposes
     */
    protected Set<String> _ignoredPropertyNames;

    /**
     * Alternate set of property names that have been marked for
     * explicit ignoral for deserialization: needed to deal with
     * 1.x definition of ignorable with respect to "any setter"
     * (see [JACKSON-313], [JACKSON-383])
     * 
     * @since 1.9.4
     */
    protected Set<String> _ignoredPropertyNamesForDeser;
    
    /**
     * Lazily collected list of members that were annotated to
     * indicate that they represent mutators for deserializer
     * value injection.
     */
    protected LinkedHashMap<Object, AnnotatedMember> _injectables;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */
    
    protected POJOPropertiesCollector(MapperConfig<?> config, boolean forSerialization,
            JavaType type, AnnotatedClass classDef)
    {
        _config = config;
        _forSerialization = forSerialization;
        _type = type;
        _classDef = classDef;
        _annotationIntrospector = config.isAnnotationProcessingEnabled() ?
                _config.getAnnotationIntrospector() : null;
        if (_annotationIntrospector == null) {
            _visibilityChecker = _config.getDefaultVisibilityChecker();
        } else {
            _visibilityChecker = _annotationIntrospector.findAutoDetectVisibility(classDef,
                    _config.getDefaultVisibilityChecker());
        }
    }
    
    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    public MapperConfig<?> getConfig() {
        return _config;
    }

    public JavaType getType() {
        return _type;
    }
    
    public AnnotatedClass getClassDef() {
        return _classDef;
    }

    public AnnotationIntrospector getAnnotationIntrospector() {
        return _annotationIntrospector;
    }
    
    public List<BeanPropertyDefinition> getProperties() {
        // make sure we return a copy, so caller can remove entries if need be:
        return new ArrayList<BeanPropertyDefinition>(_properties.values());
    }

    public Map<Object, AnnotatedMember> getInjectables() {
        return _injectables;
    }
    
    public AnnotatedMethod getJsonValueMethod()
    {
        // If @JsonValue defined, must have a single one
        if (_jsonValueGetters != null) {
            if (_jsonValueGetters.size() > 1) {
                reportProblem("Multiple value properties defined ("+_jsonValueGetters.get(0)+" vs "
                        +_jsonValueGetters.get(1)+")");
            }
            // otherwise we won't greatly care
            return _jsonValueGetters.get(0);
        }
        return null;
    }

    public AnnotatedMethod getAnyGetterMethod()
    {
        if (_anyGetters != null) {
            if (_anyGetters.size() > 1) {
                reportProblem("Multiple 'any-getters' defined ("+_anyGetters.get(0)+" vs "
                        +_anyGetters.get(1)+")");
            }
            return _anyGetters.getFirst();
        }        
        return null;
    }

    public AnnotatedMethod getAnySetterMethod()
    {
        if (_anySetters != null) {
            if (_anySetters.size() > 1) {
                reportProblem("Multiple 'any-setters' defined ("+_anySetters.get(0)+" vs "
                        +_anySetters.get(1)+")");
            }
            return _anySetters.getFirst();
        }
        return null;
    }

    public Set<String> getIgnoredPropertyNames() {
        return _ignoredPropertyNames;
    }

    /**
     * @since 1.9.4
     */
    public Set<String> getIgnoredPropertyNamesForDeser() {
        return _ignoredPropertyNamesForDeser;
    }
    
    // for unit tests:
    protected Map<String, POJOPropertyBuilder> getPropertyMap() {
        return _properties;
    }

    /*
    /**********************************************************
    /* Public API: main-level collection
    /**********************************************************
     */

    /**
     * Method that orchestrates collection activities, and needs to be called
     * after creating the instance.
     */
    public POJOPropertiesCollector collect()
    {
        _properties.clear();
        
        // First: gather basic data
        _addFields();
        _addMethods();
        _addCreators();
        _addInjectables();

        // Remove ignored properties, individual entries
        _removeUnwantedProperties();

        // Rename remaining properties
        _renameProperties();
        // And use custom naming strategy, if applicable...
        PropertyNamingStrategy naming = _config.getPropertyNamingStrategy();
        if (naming != null) {
            _renameUsing(naming);
        }

        /* Sort by visibility (explicit over implicit); drop all but first
         * of member type (getter, setter etc) if there is visibility
         * difference
         */
        for (POJOPropertyBuilder property : _properties.values()) {
            property.trimByVisibility();
        }

        // and then the final step, "merge" annotations
        for (POJOPropertyBuilder property : _properties.values()) {
            property.mergeAnnotations(_forSerialization);
        }

        // well, almost last: there's still ordering...
        _sortProperties();
        return this;
    }

    /*
    /**********************************************************
    /* Overridable internal methods, sorting
    /**********************************************************
     */
    
    /* First, order by [JACKSON-90] (explicit ordering and/or alphabetic)
     * and then for [JACKSON-170] (implicitly order creator properties before others)
     */
    protected void _sortProperties()
    {
        // Then how about explicit ordering?
        AnnotationIntrospector intr = _config.getAnnotationIntrospector();
        boolean sort;
        Boolean alpha = intr.findSerializationSortAlphabetically(_classDef);
        
        if (alpha == null) {
            sort = _config.shouldSortPropertiesAlphabetically();
        } else {
            sort = alpha.booleanValue();
        }
        String[] propertyOrder = intr.findSerializationPropertyOrder(_classDef);
        
        // no sorting? no need to shuffle, then
        if (!sort && (_creatorProperties == null) && (propertyOrder == null)) {
            return;
        }
        int size = _properties.size();
        Map<String, POJOPropertyBuilder> all;
        // Need to (re)sort alphabetically?
        if (sort) {
            all = new TreeMap<String,POJOPropertyBuilder>();
        } else {
            all = new LinkedHashMap<String,POJOPropertyBuilder>(size+size);
        }

        for (POJOPropertyBuilder prop : _properties.values()) {
            all.put(prop.getName(), prop);
        }
        Map<String,POJOPropertyBuilder> ordered = new LinkedHashMap<String,POJOPropertyBuilder>(size+size);
        // Ok: primarily by explicit order
        if (propertyOrder != null) {
            for (String name : propertyOrder) {
                POJOPropertyBuilder w = all.get(name);
                if (w == null) { // also, as per [JACKSON-268], we will allow use of "implicit" names
                    for (POJOPropertyBuilder prop : _properties.values()) {
                        if (name.equals(prop.getInternalName())) {
                            w = prop;
                            // plus re-map to external name, to avoid dups:
                            name = prop.getName();
                            break;
                        }
                    }
                }
                if (w != null) {
                    ordered.put(name, w);
                }
            }
        }
        // And secondly by sorting Creator properties before other unordered properties
        if (_creatorProperties != null) {
            for (POJOPropertyBuilder prop : _creatorProperties) {
                ordered.put(prop.getName(), prop);
            }
        }
        // And finally whatever is left (trying to put again will not change ordering)
        ordered.putAll(all);
        
        _properties.clear();
        _properties.putAll(ordered);
    }        

    /*
    /**********************************************************
    /* Overridable internal methods, adding members
    /**********************************************************
     */
    
    /**
     * Method for collecting basic information on all fields found
     */
    protected void _addFields()
    {
        final AnnotationIntrospector ai = _annotationIntrospector;
        
        for (AnnotatedField f : _classDef.fields()) {
            String implName = f.getName();

            String explName;
            if (ai == null) {
                explName = null;
            } else if (_forSerialization) {
                /* 18-Aug-2011, tatu: As per existing unit tests, we should only
                 *   use serialization annotation (@JsonSerializer) when serializing
                 *   fields, and similarly for deserialize-only annotations... so
                 *   no fallbacks in this particular case.
                 */
                explName = ai.findSerializablePropertyName(f);
            } else {
                explName = ai.findDeserializablePropertyName(f);
            }
            if ("".equals(explName)) { // empty String meaning "use default name", here just means "same as field name"
                explName = implName;
            }
            // having explicit name means that field is visible; otherwise need to check the rules
            boolean visible = (explName != null);
            if (!visible) {
                visible = _visibilityChecker.isFieldVisible(f);
            }
            // and finally, may also have explicit ignoral
            boolean ignored = (ai != null) && ai.hasIgnoreMarker(f);
            _property(implName).addField(f, explName, visible, ignored);
        }
    }

    /**
     * Method for collecting basic information on constructor(s) found
     */
    protected void _addCreators()
    {
        final AnnotationIntrospector ai = _annotationIntrospector;
        // can be null if annotation processing is disabled...
        if (ai == null) {
            return;
        }
        for (AnnotatedConstructor ctor : _classDef.getConstructors()) {
            if (_creatorProperties == null) {
                _creatorProperties = new LinkedList<POJOPropertyBuilder>();
            }
            for (int i = 0, len = ctor.getParameterCount(); i < len; ++i) {
                AnnotatedParameter param = ctor.getParameter(i);
                String name = ai.findPropertyNameForParam(param);
                // is it legal not to have name?
                if (name != null) {
                    // shouldn't need to worry about @JsonIgnore (no real point, so)
                    POJOPropertyBuilder prop = _property(name);
                    prop.addCtor(param, name, true, false);
                    _creatorProperties.add(prop);
                }
            }
        }
        for (AnnotatedMethod factory : _classDef.getStaticMethods()) {
            if (_creatorProperties == null) {
                _creatorProperties = new LinkedList<POJOPropertyBuilder>();
            }
            for (int i = 0, len = factory.getParameterCount(); i < len; ++i) {
                AnnotatedParameter param = factory.getParameter(i);
                String name = ai.findPropertyNameForParam(param);
                // is it legal not to have name?
                if (name != null) {
                    // shouldn't need to worry about @JsonIgnore (no real point, so)
                    POJOPropertyBuilder prop = _property(name);
                    prop.addCtor(param, name, true, false);
                    _creatorProperties.add(prop);
                }
            }
        }
    }

    /**
     * Method for collecting basic information on all fields found
     */
    protected void _addMethods()
    {
        final AnnotationIntrospector ai = _annotationIntrospector;
        
        for (AnnotatedMethod m : _classDef.memberMethods()) {
            String explName; // from annotation(s)
            String implName; // from naming convention
            
            /* For methods, handling differs between getters and setters; and
             * we will also only consider entries that either follow the bean
             * naming convention or are explicitly marked: just being visible
             * is not enough (unlike with fields)
             */
            int argCount = m.getParameterCount();
            boolean visible;
            
            if (argCount == 0) { // getters (including 'any getter')
                // any getter?
                if (ai != null) {
                    if (ai.hasAnyGetterAnnotation(m)) {
                        if (_anyGetters == null) {
                            _anyGetters = new LinkedList<AnnotatedMethod>();
                        }
                        _anyGetters.add(m);
                        continue;
                    }
                    // @JsonValue?
                    if (ai.hasAsValueAnnotation(m)) {
                        if (_jsonValueGetters == null) {
                            _jsonValueGetters = new LinkedList<AnnotatedMethod>();
                        }
                        _jsonValueGetters.add(m);
                        continue;
                    }
                }
                
                explName = (ai == null) ? null : ai.findGettablePropertyName(m);
                if (explName == null) { // no explicit name; must follow naming convention
                    implName = BeanUtil.okNameForRegularGetter(m, m.getName());
                    if (implName == null) { // if not, must skip
                        implName = BeanUtil.okNameForIsGetter(m, m.getName());
                        if (implName == null) {
                            continue;
                        }
                        visible = _visibilityChecker.isIsGetterVisible(m);
                    } else {
                        visible = _visibilityChecker.isGetterVisible(m);
                    }
                } else { // explicit indication of inclusion, but may be empty
                    // we still need implicit name to link with other pieces
                    implName = BeanUtil.okNameForGetter(m);
                    // if not regular getter name, use method name as is
                    if (implName == null) {
                        implName = m.getName();
                    }
                    if (explName.length() == 0) {
                        explName = implName;
                    }
                    visible = true;
                }
                boolean ignore = (ai == null) ? false : ai.hasIgnoreMarker(m);
                _property(implName).addGetter(m, explName, visible, ignore);
            } else if (argCount == 1) { // setters
                explName = (ai == null) ? null : ai.findSettablePropertyName(m);
                if (explName == null) { // no explicit name; must follow naming convention
                    implName = BeanUtil.okNameForSetter(m);
                    if (implName == null) { // if not, must skip
                        continue;
                    }
                    visible = _visibilityChecker.isSetterVisible(m);
                } else { // explicit indication of inclusion, but may be empty
                    // we still need implicit name to link with other pieces
                    implName = BeanUtil.okNameForSetter(m);
                    // if not regular getter name, use method name as is
                    if (implName == null) {
                        implName = m.getName();
                    }
                    if (explName.length() == 0) { 
                        explName = implName;
                    }
                    visible = true;
                }
                boolean ignore = (ai == null) ? false : ai.hasIgnoreMarker(m);
                _property(implName).addSetter(m, explName, visible, ignore);

            } else if (argCount == 2) { // any getter?
                if (ai != null  && ai.hasAnySetterAnnotation(m)) {
                    if (_anySetters == null) {
                        _anySetters = new LinkedList<AnnotatedMethod>();
                    }
                    _anySetters.add(m);
                }
            }
        }
    }

    protected void _addInjectables()
    {
        final AnnotationIntrospector ai = _annotationIntrospector;
        if (ai == null) {
            return;
        }
        
        // first fields, then methods
        for (AnnotatedField f : _classDef.fields()) {
            _doAddInjectable(ai.findInjectableValueId(f), f);
        }
        
        for (AnnotatedMethod m : _classDef.memberMethods()) {
            /* for now, only allow injection of a single arg
             * (to be changed in future)
             */
            if (m.getParameterCount() != 1) {
                continue;
            }
            _doAddInjectable(ai.findInjectableValueId(m), m);
        }
    }

    protected void _doAddInjectable(Object id, AnnotatedMember m)
    {
        if (id == null) {
            return;
        }
        if (_injectables == null) {
            _injectables = new LinkedHashMap<Object, AnnotatedMember>();
        }
        AnnotatedMember prev = _injectables.put(id, m);
        if (prev != null) {
            String type = (id == null) ? "[null]" : id.getClass().getName();
            throw new IllegalArgumentException("Duplicate injectable value with id '"
                    +String.valueOf(id)+"' (of type "+type+")");
        }
    }
    
    /*
    /**********************************************************
    /* Internal methods; removing ignored properties
    /**********************************************************
     */

    /**
     * Method called to get rid of candidate properties that are marked
     * as ignored, or that are not visible.
     */
    protected void _removeUnwantedProperties()
    {
        Iterator<Map.Entry<String,POJOPropertyBuilder>> it = _properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, POJOPropertyBuilder> entry = it.next();
            POJOPropertyBuilder prop = entry.getValue();

            // First: if nothing visible, just remove altogether
            if (!prop.anyVisible()) {
                it.remove();
                continue;
            }
            // Otherwise, check ignorals
            if (prop.anyIgnorals()) {
                _addIgnored(prop);
                // first: if one or more ignorals, and no explicit markers, remove the whole thing
                if (!prop.anyExplicitNames()) {
                    it.remove();
                    continue;
                }
                // otherwise just remove explicitly ignored (and retain others)
                prop.removeIgnored();
            }
            // and finally, handle removal of individual non-visible elements
            prop.removeNonVisible();
        }
    }
    
    private void _addIgnored(POJOPropertyBuilder prop)
    {
        // not used in any way for serialization side:
        if (_forSerialization) {
            return;
        }
        /* and with deserialization, two aspects: whether it's ok to see
         * property ('ignore for failure reporting') and whether we forcifully
         * ignore it even if there was "any setter" available.
         */
        // but do not add unless ignoral was for field, setter or ctor param
        String name = prop.getName();
        _ignoredPropertyNames = addToSet(_ignoredPropertyNames, name);
        if (prop.anyDeserializeIgnorals()) {
            _ignoredPropertyNamesForDeser = addToSet(_ignoredPropertyNamesForDeser, name);
        }
    }

    /*
    /**********************************************************
    /* Internal methods; renaming properties
    /**********************************************************
     */

    protected void _renameProperties()
    {
        // With renaming need to do in phases: first, find properties to rename
        Iterator<Map.Entry<String,POJOPropertyBuilder>> it = _properties.entrySet().iterator();
        LinkedList<POJOPropertyBuilder> renamed = null;
        while (it.hasNext()) {
            Map.Entry<String, POJOPropertyBuilder> entry = it.next();
            POJOPropertyBuilder prop = entry.getValue();
            String newName = prop.findNewName();
            if (newName != null) {
                if (renamed == null) {
                    renamed = new LinkedList<POJOPropertyBuilder>();
                }
                prop = prop.withName(newName);
                renamed.add(prop);
                it.remove();
            }
        }
        
        // and if any were renamed, merge back in...
        if (renamed != null) {
            for (POJOPropertyBuilder prop : renamed) {
                String name = prop.getName();
                POJOPropertyBuilder old = _properties.get(name);
                if (old == null) {
                    _properties.put(name, prop);
                } else {
                    old.addAll(prop);
                }
            }
        }
    }

    protected void _renameUsing(PropertyNamingStrategy naming)
    {
        POJOPropertyBuilder[] props = _properties.values().toArray(new POJOPropertyBuilder[_properties.size()]);
        _properties.clear();
        for (POJOPropertyBuilder prop : props) {
            String name = prop.getName();
            if (_forSerialization) {
                if (prop.hasGetter()) {
                    name = naming.nameForGetterMethod(_config, prop.getGetter(), name);
                } else if (prop.hasField()) {
                    name = naming.nameForField(_config, prop.getField(), name);
                }
            } else {
                if (prop.hasSetter()) {
                    name = naming.nameForSetterMethod(_config, prop.getSetter(), name);
                } else if (prop.hasConstructorParameter()) {
                    name = naming.nameForConstructorParameter(_config, prop.getConstructorParameter(), name);
                } else if (prop.hasField()) {
                    name = naming.nameForField(_config, prop.getField(), name);
                } else if (prop.hasGetter()) {
                    /* Plus, when getter-as-setter is used, need to convert that too..
                     * (should we verify that's enabled? For now, assume it's ok always)
                     */
                    name = naming.nameForGetterMethod(_config, prop.getGetter(), name);
                }
            }
            if (!name.equals(prop.getName())) {
                prop = prop.withName(name);
            }
            /* As per [JACKSON-687], need to consider case where there may already be
             * something in there...
             */
            POJOPropertyBuilder old = _properties.get(name);
            if (old == null) {
                _properties.put(name, prop);
            } else {
                old.addAll(prop);
            }
        }
    }
    
    /*
    /**********************************************************
    /* Internal methods; helpers
    /**********************************************************
     */

    protected void reportProblem(String msg) {
        throw new IllegalArgumentException("Problem with definition of "+_classDef+": "+msg);
    }
    
    protected POJOPropertyBuilder _property(String implName)
    {
        POJOPropertyBuilder prop = _properties.get(implName);
        if (prop == null) {
            prop = new POJOPropertyBuilder(implName);
            _properties.put(implName, prop);
        }
        return prop;
    }

    private Set<String> addToSet(Set<String> set, String str)
    {
        if (set == null) {
            set = new HashSet<String>();
        }
        set.add(str);
        return set;
    }        
}
