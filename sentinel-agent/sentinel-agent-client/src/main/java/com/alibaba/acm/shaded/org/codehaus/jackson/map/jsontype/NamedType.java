package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype;

/**
 * Simple container class for types with optional logical name, used
 * as external identifier
 * 
 * @author tatu
 * @since 1.5
 */
public final class NamedType
{
    protected final Class<?> _class;
    protected final int _hashCode;

    protected String _name;
    
    public NamedType(Class<?> c) { this(c, null); }
    
    public NamedType(Class<?> c, String name)
    {
        _class = c;
        _hashCode = c.getName().hashCode();
        setName(name);
    }

    public Class<?> getType() { return _class; }
    public String getName() { return _name; }
    public void setName(String name) {
        _name = (name == null || name.length() == 0) ? null : name;
    }

    public boolean hasName() { return _name != null; }
    
    /**
     * Equality is defined based on class only, not on name
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        return _class == ((NamedType) o)._class;
    }

    @Override
    public int hashCode() { return _hashCode; }

    @Override
    public String toString() {
    	return "[NamedType, class "+_class.getName()+", name: "+(_name == null ? "null" :("'"+_name+"'"))+"]";
    }
}
