package com.alibaba.acm.shaded.org.codehaus.jackson.annotate;

/**
 * Enumeration used to define kinds of methods that annotations like
 * {@link JsonAutoDetect} apply to.
 *<p>
 * In addition to actual method types (GETTER, SETTER, CREATOR; and
 * sort-of-method, FIELD), 2 pseudo-types
 * are defined for convenience: <code>ALWAYS</code> and <code>NONE</code>. These
 * can be used to indicate, all or none of available method types (respectively),
 * for use by annotations that takes <code>JsonMethod</code> argument.
 */
public enum JsonMethod
{
    /**
     * Getters are methods used to get a POJO field value for serialization,
     * or, under certain conditions also for de-serialization. Latter
     * can be used for effectively setting Collection or Map values
     * in absence of setters, iff returned value is not a copy but
     * actual value of the logical property.
     *<p>
     * Since version 1.3, this does <b>NOT</b> include "is getters" (methods
     * that return boolean and named 'isXxx' for property 'xxx'); instead,
     * {@link #IS_GETTER} is used}.
     */
    GETTER,

    /**
     * Setters are methods used to set a POJO value for deserialization.
     */
    SETTER,

        /**
         * Creators are constructors and (static) factory methods used to
         * construct POJO instances for deserialization
         */
        CREATOR,

        /**
         * Field refers to fields of regular Java objects. Although
         * they are not really methods, addition of optional field-discovery
         * in version 1.1 meant that there was need to enable/disable
         * their auto-detection, and this is the place to add it in.
         *
         * @since 1.1
         */
        FIELD,

        /**
         * "Is getters" are getter-like methods that are named "isXxx"
         * (instead of "getXxx" for getters) and return boolean value
         * (either primitive, or {@link java.lang.Boolean}).
         *
         * @since 1.3
         */
        IS_GETTER,

        /**
         * This pseudo-type indicates that none of real types is included
         */
        NONE,

        /**
         * This pseudo-type indicates that all of real types are included
         */
        ALL
        ;

    private JsonMethod() { }

    public boolean creatorEnabled() {
        return (this == CREATOR) || (this == ALL);
    }

    public boolean getterEnabled() {
        return (this == GETTER) || (this == ALL);
    }

    public boolean isGetterEnabled() {
        return (this == IS_GETTER) || (this == ALL);
    }

    public boolean setterEnabled() {
        return (this == SETTER) || (this == ALL);
    }

    public boolean fieldEnabled() {
        return (this == FIELD) || (this == ALL);
    }
}
