package com.alibaba.acm.shaded.org.codehaus.jackson.sym;

/**
 * Base class for tokenized names (key strings in objects) that have
 * been tokenized from byte-based input sources (like
 * {@link java.io.InputStream}.
 *
 * @author Tatu Saloranta
 */
public abstract class Name
{
    protected final String _name;

    protected final int _hashCode;

    protected Name(String name, int hashCode) {
        _name = name;
        _hashCode = hashCode;
    }

    public String getName() { return _name; }

    /*
    /**********************************************************
    /* Methods for package/core parser
    /**********************************************************
     */

    public abstract boolean equals(int quad1);

    public abstract boolean equals(int quad1, int quad2);

    public abstract boolean equals(int[] quads, int qlen);

    /*
    /**********************************************************
    /* Overridden standard methods
    /**********************************************************
     */

    @Override public String toString() { return _name; }

    @Override public final int hashCode() { return _hashCode; }

    @Override public boolean equals(Object o)
    {
        // Canonical instances, can usually just do identity comparison
        return (o == this);
    }
}
