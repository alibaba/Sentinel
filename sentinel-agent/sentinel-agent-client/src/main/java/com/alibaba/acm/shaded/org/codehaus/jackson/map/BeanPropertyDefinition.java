package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedField;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedMember;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedMethod;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedParameter;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.Named;

/**
 * Simple value classes that contain definitions of properties,
 * used during introspection of properties to use for
 * serialization and deserialization purposes.
 * These instances are created before actual {@link BeanProperty}
 * instances are created, i.e. they are used earlier in the process
 * flow.
 *
 * @since 1.9
 */
public abstract class BeanPropertyDefinition
    implements Named
{
    /**
     * Accessor for name used for external representation (in JSON).
     */
    @Override // from Named
    public abstract String getName();

    /**
     * Accessor that can be used to determine implicit name from underlying
     * element(s) before possible renaming. This is the "internal"
     * name derived from accessor ("x" from "getX"), and is not based on
     * annotations or naming strategy.
     */
    public abstract String getInternalName();

    /**
     * Accessor that can be called to check whether property was included
     * due to an explicit marker (usually annotation), or just by naming
     * convention.
     * 
     * @return True if property was explicitly included (usually by having
     *   one of components being annotated); false if inclusion was purely
     *   due to naming or visibility definitions (that is, implicit)
     *
     * @since 1.9.6
     */
    public abstract boolean isExplicitlyIncluded();
    
    public abstract boolean hasGetter();
    public abstract boolean hasSetter();
    public abstract boolean hasField();
    public abstract boolean hasConstructorParameter();

    public boolean couldDeserialize() {
        return getMutator() != null;
    }
    public boolean couldSerialize() {
        return getAccessor() != null;
    }

    public abstract AnnotatedMethod getGetter();
    public abstract AnnotatedMethod getSetter();
    public abstract AnnotatedField getField();
    public abstract AnnotatedParameter getConstructorParameter();

    /**
     * Method used to find accessor (getter, field to access) to use for accessing
     * value of the property.
     * Null if no such member exists.
     */
    public abstract AnnotatedMember getAccessor();

    /**
     * Method used to find mutator (constructor parameter, setter, field) to use for
     * changing value of the property.
     * Null if no such member exists.
     */
    public abstract AnnotatedMember getMutator();
}
