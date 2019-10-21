package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanPropertyDefinition;

/**
 * Helper class used for aggregating information about a single
 * potential POJO property.
 * 
 * @since 1.9
 */
public class POJOPropertyBuilder
    extends BeanPropertyDefinition
    implements Comparable<POJOPropertyBuilder>
{
    /**
     * External name of logical property; may change with
     * renaming (by new instance being constructed using
     * a new name)
     */
    protected final String _name;

    /**
     * Original internal name, derived from accessor, of this
     * property. Will not be changed by renaming.
     */
    protected final String _internalName;

    protected Node<AnnotatedField> _fields;
    
    protected Node<AnnotatedParameter> _ctorParameters;
    
    protected Node<AnnotatedMethod> _getters;

    protected Node<AnnotatedMethod> _setters;

    public POJOPropertyBuilder(String internalName)
    {
        _internalName = internalName;
        _name = internalName;
    }

    public POJOPropertyBuilder(POJOPropertyBuilder src, String newName)
    {
        _internalName = src._internalName;
        _name = newName;
        _fields = src._fields;
        _ctorParameters = src._ctorParameters;
        _getters = src._getters;
        _setters = src._setters;
    }

    /**
     * Method for constructing a renamed instance
     */
    public POJOPropertyBuilder withName(String newName) {
        return new POJOPropertyBuilder(this, newName);
    }
    
    /*
    /**********************************************************
    /* Comparable implementation: sort alphabetically, except
    /* that properties with constructor parameters sorted
    /* before other properties
    /**********************************************************
     */

    @Override
    public int compareTo(POJOPropertyBuilder other)
    {
        // first, if one has ctor params, that should come first:
        if (_ctorParameters != null) {
            if (other._ctorParameters == null) {
                return -1;
            }
        } else if (other._ctorParameters != null) {
            return 1;
        }
        /* otherwise sort by external name (including sorting of
         * ctor parameters)
         */
        return getName().compareTo(other.getName());
    }
    
    /*
    /**********************************************************
    /* BeanPropertyDefinition implementation
    /**********************************************************
     */

    @Override
    public String getName() { return _name; }

    @Override
    public String getInternalName() { return _internalName; }

    @Override
    public boolean isExplicitlyIncluded() {
        return anyExplicitNames();
    }
    
    @Override
    public boolean hasGetter() { return _getters != null; }

    @Override
    public boolean hasSetter() { return _setters != null; }

    @Override
    public boolean hasField() { return _fields != null; }

    @Override
    public boolean hasConstructorParameter() { return _ctorParameters != null; }

    @Override
    public AnnotatedMember getAccessor()
    {
        AnnotatedMember m = getGetter();
        if (m == null) {
            m = getField();
        }
        return m;
    }

    @Override
    public AnnotatedMember getMutator()
    {
        AnnotatedMember m = getConstructorParameter();
        if (m == null) {
            m = getSetter();
            if (m == null) {
                m = getField();
            }
        }
        return m;
    }

    @Override
    public boolean couldSerialize() {
        return (_getters != null) || (_fields != null);
    }

    @Override
    public AnnotatedMethod getGetter()
    {
        if (_getters == null) {
            return null;
        }
        // If multiple, verify that they do not conflict...
        AnnotatedMethod getter = _getters.value;
        Node<AnnotatedMethod> next = _getters.next;
        for (; next != null; next = next.next) {
            /* [JACKSON-255] Allow masking, i.e. report exception only if
             *   declarations in same class, or there's no inheritance relationship
             *   (sibling interfaces etc)
             */
            AnnotatedMethod nextGetter = next.value;
            Class<?> getterClass = getter.getDeclaringClass();
            Class<?> nextClass = nextGetter.getDeclaringClass();
            if (getterClass != nextClass) {
                if (getterClass.isAssignableFrom(nextClass)) { // next is more specific
                    getter = nextGetter;
                    continue;
                }
                if (nextClass.isAssignableFrom(getterClass)) { // getter more specific
                    continue;
                }
            }
            throw new IllegalArgumentException("Conflicting getter definitions for property \""+getName()+"\": "
                    +getter.getFullName()+" vs "+nextGetter.getFullName());
        }
        return getter;
    }

    @Override
    public AnnotatedMethod getSetter()
    {
        if (_setters == null) {
            return null;
        }
        // If multiple, verify that they do not conflict...
        AnnotatedMethod setter = _setters.value;
        Node<AnnotatedMethod> next = _setters.next;
        for (; next != null; next = next.next) {
            /* [JACKSON-255] Allow masking, i.e. report exception only if
             *   declarations in same class, or there's no inheritance relationship
             *   (sibling interfaces etc)
             */
            AnnotatedMethod nextSetter = next.value;
            Class<?> setterClass = setter.getDeclaringClass();
            Class<?> nextClass = nextSetter.getDeclaringClass();
            if (setterClass != nextClass) {
                if (setterClass.isAssignableFrom(nextClass)) { // next is more specific
                    setter = nextSetter;
                    continue;
                }
                if (nextClass.isAssignableFrom(setterClass)) { // getter more specific
                    continue;
                }
            }
            throw new IllegalArgumentException("Conflicting setter definitions for property \""+getName()+"\": "
                    +setter.getFullName()+" vs "+nextSetter.getFullName());
        }
        return setter;
    }

    @Override
    public AnnotatedField getField()
    {
        if (_fields == null) {
            return null;
        }
        // If multiple, verify that they do not conflict...
        AnnotatedField field = _fields.value;
        Node<AnnotatedField> next = _fields.next;
        for (; next != null; next = next.next) {
            AnnotatedField nextField = next.value;
            Class<?> fieldClass = field.getDeclaringClass();
            Class<?> nextClass = nextField.getDeclaringClass();
            if (fieldClass != nextClass) {
                if (fieldClass.isAssignableFrom(nextClass)) { // next is more specific
                    field = nextField;
                    continue;
                }
                if (nextClass.isAssignableFrom(fieldClass)) { // getter more specific
                    continue;
                }
            }
            throw new IllegalArgumentException("Multiple fields representing property \""+getName()+"\": "
                    +field.getFullName()+" vs "+nextField.getFullName());
        }
        return field;
    }

    @Override
    public AnnotatedParameter getConstructorParameter()
    {
        if (_ctorParameters == null) {
            return null;
        }
        /* Hmmh. Checking for constructor parameters is trickier; for one,
         * we must allow creator and factory method annotations.
         * If this is the case, constructor parameter has the precedence.
         * 
         * So, for now, just try finding the first constructor parameter;
         * if none, first factory method. And don't check for dups, if we must,
         * can start checking for them later on.
         */
        Node<AnnotatedParameter> curr = _ctorParameters;
        do {
            if (curr.value.getOwner() instanceof AnnotatedConstructor) {
                return curr.value;
            }
            curr = curr.next;
        } while (curr != null);
        return _ctorParameters.value;
    }
    
    /*
    /**********************************************************
    /* Data aggregation
    /**********************************************************
     */
    
    public void addField(AnnotatedField a, String ename, boolean visible, boolean ignored) {
        _fields = new Node<AnnotatedField>(a, _fields, ename, visible, ignored);
    }

    public void addCtor(AnnotatedParameter a, String ename, boolean visible, boolean ignored) {
        _ctorParameters = new Node<AnnotatedParameter>(a, _ctorParameters, ename, visible, ignored);
    }

    public void addGetter(AnnotatedMethod a, String ename, boolean visible, boolean ignored) {
        _getters = new Node<AnnotatedMethod>(a, _getters, ename, visible, ignored);
    }

    public void addSetter(AnnotatedMethod a, String ename, boolean visible, boolean ignored) {
        _setters = new Node<AnnotatedMethod>(a, _setters, ename, visible, ignored);
    }

    /**
     * Method for adding all property members from specified collector into
     * this collector.
     */
    public void addAll(POJOPropertyBuilder src)
    {
        _fields = merge(_fields, src._fields);
        _ctorParameters = merge(_ctorParameters, src._ctorParameters);
        _getters= merge(_getters, src._getters);
        _setters = merge(_setters, src._setters);
    }

    private static <T> Node<T> merge(Node<T> chain1, Node<T> chain2)
    {
        if (chain1 == null) {
            return chain2;
        }
        if (chain2 == null) {
            return chain1;
        }
        return chain1.append(chain2);
    }
    
    /*
    /**********************************************************
    /* Modifications
    /**********************************************************
     */

    /**
     * Method called to remove all entries that are marked as
     * ignored.
     */
    public void removeIgnored()
    {
        _fields = _removeIgnored(_fields);
        _getters = _removeIgnored(_getters);
        _setters = _removeIgnored(_setters);
        _ctorParameters = _removeIgnored(_ctorParameters);
    }

    public void removeNonVisible()
    {
        /* 21-Aug-2011, tatu: This is tricky part -- if and when allow
         *   non-visible property elements to be "pulled in" by visible
         *   counterparts?
         *   For now, we will only do this to pull in setter or field used
         *   as setter, if an explicit getter is found.
         */
        _getters = _removeNonVisible(_getters);
        _ctorParameters = _removeNonVisible(_ctorParameters);

        if (_getters == null) {
            _fields = _removeNonVisible(_fields);
            _setters = _removeNonVisible(_setters);
        }
    }

    /**
     * Method called to trim unnecessary entries, such as implicit
     * getter if there is an explict one available. This is important
     * for later stages, to avoid unnecessary conflicts.
     */
    public void trimByVisibility()
    {
        _fields = _trimByVisibility(_fields);
        _getters = _trimByVisibility(_getters);
        _setters = _trimByVisibility(_setters);
        _ctorParameters = _trimByVisibility(_ctorParameters);
    }

    @SuppressWarnings("unchecked")
    public void mergeAnnotations(boolean forSerialization)
    {
        if (forSerialization) {
            if (_getters != null) {
                AnnotationMap ann = _mergeAnnotations(0, _getters, _fields, _ctorParameters, _setters);
                _getters = _getters.withValue(_getters.value.withAnnotations(ann));
            } else if (_fields != null) {
                AnnotationMap ann = _mergeAnnotations(0, _fields, _ctorParameters, _setters);
                _fields = _fields.withValue(_fields.value.withAnnotations(ann));
            }
        } else {
            if (_ctorParameters != null) {
                AnnotationMap ann = _mergeAnnotations(0, _ctorParameters, _setters, _fields, _getters);
                _ctorParameters = _ctorParameters.withValue(_ctorParameters.value.withAnnotations(ann));
            } else if (_setters != null) {
                AnnotationMap ann = _mergeAnnotations(0, _setters, _fields, _getters);
                _setters = _setters.withValue(_setters.value.withAnnotations(ann));
            } else if (_fields != null) {
                AnnotationMap ann = _mergeAnnotations(0, _fields, _getters);
                _fields = _fields.withValue(_fields.value.withAnnotations(ann));
            }
        }
    }

    private AnnotationMap _mergeAnnotations(int index, Node<? extends AnnotatedMember>... nodes)
    {
        AnnotationMap ann = nodes[index].value.getAllAnnotations();
        ++index;
        for (; index < nodes.length; ++index) {
            if (nodes[index] != null) {
              return AnnotationMap.merge(ann, _mergeAnnotations(index, nodes));
            }
        }
        return ann;
    }
    
    private <T> Node<T> _removeIgnored(Node<T> node)
    {
        if (node == null) {
            return node;
        }
        return node.withoutIgnored();
    }

    private <T> Node<T> _removeNonVisible(Node<T> node)
    {
        if (node == null) {
            return node;
        }
        return node.withoutNonVisible();
    }

    private <T> Node<T> _trimByVisibility(Node<T> node)
    {
        if (node == null) {
            return node;
        }
        return node.trimByVisibility();
    }
        
    /*
    /**********************************************************
    /* Accessors for aggregate information
    /**********************************************************
     */

    public boolean anyExplicitNames() {
        return _anyExplicitNames(_fields)
                || _anyExplicitNames(_getters)
                || _anyExplicitNames(_setters)
                || _anyExplicitNames(_ctorParameters)
                ;
    }

    private <T> boolean _anyExplicitNames(Node<T> n)
    {
        for (; n != null; n = n.next) {
            if (n.explicitName != null && n.explicitName.length() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean anyVisible() {
        return _anyVisible(_fields)
            || _anyVisible(_getters)
            || _anyVisible(_setters)
            || _anyVisible(_ctorParameters)
        ;
    }

    private <T> boolean _anyVisible(Node<T> n)
    {
        for (; n != null; n = n.next) {
            if (n.isVisible) {
                return true;
            }
        }
        return false;
    }
    
    public boolean anyIgnorals() {
        return _anyIgnorals(_fields)
            || _anyIgnorals(_getters)
            || _anyIgnorals(_setters)
            || _anyIgnorals(_ctorParameters)
        ;
    }

    public boolean anyDeserializeIgnorals() {
        return _anyIgnorals(_fields)
            || _anyIgnorals(_setters)
            || _anyIgnorals(_ctorParameters)
        ;
    }

    public boolean anySerializeIgnorals() {
        return _anyIgnorals(_fields)
            || _anyIgnorals(_getters)
        ;
    }
    
    private <T> boolean _anyIgnorals(Node<T> n)
    {
        for (; n != null; n = n.next) {
            if (n.isMarkedIgnored) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method called to check whether property represented by this collector
     * should be renamed from the implicit name; and also verify that there
     * are no conflicting rename definitions.
     */
    public String findNewName()
    {
        Node<? extends AnnotatedMember> renamed = null;
        renamed = findRenamed(_fields, renamed);
        renamed = findRenamed(_getters, renamed);
        renamed = findRenamed(_setters, renamed);
        renamed = findRenamed(_ctorParameters, renamed);
        return (renamed == null) ? null : renamed.explicitName;
    }

    private Node<? extends AnnotatedMember> findRenamed(Node<? extends AnnotatedMember> node,
            Node<? extends AnnotatedMember> renamed)
    {
        for (; node != null; node = node.next) {
            String explName = node.explicitName;
            if (explName == null) {
                continue;
            }
            // different from default name?
            if (explName.equals(_name)) { // nope, skip
                continue;
            }
            if (renamed == null) {
                renamed = node;
            } else {
                // different from an earlier renaming? problem
                if (!explName.equals(renamed.explicitName)) {
                    throw new IllegalStateException("Conflicting property name definitions: '"
                            +renamed.explicitName+"' (for "+renamed.value+") vs '"
                            +node.explicitName+"' (for "+node.value+")");
                }
            }
        }
        return renamed;
    }

    // For trouble-shooting
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[Property '").append(_name)
          .append("'; ctors: ").append(_ctorParameters)
          .append(", field(s): ").append(_fields)
          .append(", getter(s): ").append(_getters)
          .append(", setter(s): ").append(_setters)
          ;
        sb.append("]");
        return sb.toString();
    }
    
    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * Node used for creating simple linked lists to efficiently store small sets
     * of things.
     */
    private final static class Node<T>
    {
        public final T value;
        public final Node<T> next;

        public final String explicitName;
        public final boolean isVisible;
        public final boolean isMarkedIgnored;
        
        public Node(T v, Node<T> n,
                String explName, boolean visible, boolean ignored)
        {
            value = v;
            next = n;
            // ensure that we'll never have missing names
            if (explName == null) {
                explicitName = null;
            } else {
                explicitName = (explName.length() == 0) ? null : explName;
            }
            isVisible = visible;
            isMarkedIgnored = ignored;
        }

        public Node<T> withValue(T newValue)
        {
            if (newValue == value) {
                return this;
            }
            return new Node<T>(newValue, next, explicitName, isVisible, isMarkedIgnored);
        }
        
        public Node<T> withNext(Node<T> newNext) {
            if (newNext == next) {
                return this;
            }
            return new Node<T>(value, newNext, explicitName, isVisible, isMarkedIgnored);
        }
        
        public Node<T> withoutIgnored()
        {
            if (isMarkedIgnored) {
                return (next == null) ? null : next.withoutIgnored();
            }
            if (next != null) {
                Node<T> newNext = next.withoutIgnored();
                if (newNext != next) {
                    return withNext(newNext);
                }
            }
            return this;
        }
        
        public Node<T> withoutNonVisible()
        {
            Node<T> newNext = (next == null) ? null : next.withoutNonVisible();
            return isVisible ? withNext(newNext) : newNext;
        }

        /**
         * Method called to append given node(s) at the end of this
         * node chain.
         */
        private Node<T> append(Node<T> appendable) 
        {
            if (next == null) {
                return withNext(appendable);
            }
            return withNext(next.append(appendable));
        }
        
        public Node<T> trimByVisibility()
        {
            if (next == null) {
                return this;
            }
            Node<T> newNext = next.trimByVisibility();
            if (explicitName != null) { // this already has highest; how about next one?
                if (newNext.explicitName == null) { // next one not, drop it
                    return withNext(null);
                }
                //  both have it, keep
                return withNext(newNext);
            }
            if (newNext.explicitName != null) { // next one has higher, return it...
                return newNext;
            }
            // neither has explicit name; how about visibility?
            if (isVisible == newNext.isVisible) { // same; keep both in current order
                return withNext(newNext);
            }
            return isVisible ? withNext(null) : newNext;
        }
        
        @Override
        public String toString() {
            String msg = value.toString()+"[visible="+isVisible+"]";
            if (next != null) {
                msg = msg + ", "+next.toString();
            }
            return msg;
        }
    }
}
