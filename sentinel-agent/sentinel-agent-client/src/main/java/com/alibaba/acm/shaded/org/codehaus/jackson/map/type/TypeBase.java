package com.alibaba.acm.shaded.org.codehaus.jackson.map.type;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializableWithType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

public abstract class TypeBase
    extends JavaType
    implements JsonSerializableWithType
{
    /**
     * Lazily initialized external representation of the type
     */
    volatile String _canonicalName;

    @Deprecated // since 1.9; to remove from 2.0
    protected TypeBase(Class<?> raw, int hash) {
        super(raw, hash);
    }

    /**
     * Main constructor to use by extending classes.
     * 
     * @since 1.9
     */
    protected TypeBase(Class<?> raw, int hash,
            Object valueHandler, Object typeHandler)
    {
        super(raw, hash);
        // @TODO: !!! 16-Aug-2011, tatu: With 2.0, we will move value and type
        //   handles higher in type hierarchy, make final

        // and then comment out these:
        _valueHandler = valueHandler;
        _typeHandler = typeHandler;
    }

    @Override
    public String toCanonical()
    {
    	String str = _canonicalName;
    	if (str == null) {
            str = buildCanonicalName();
    	}
    	return str;
    }
    
    protected abstract String buildCanonicalName();

    @Override
    public abstract StringBuilder getGenericSignature(StringBuilder sb);

    @Override
    public abstract StringBuilder getErasedSignature(StringBuilder sb);

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValueHandler() { return (T) _valueHandler; }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getTypeHandler() { return (T) _typeHandler; }
    
    /*
    /**********************************************************
    /* JsonSerializableWithType base implementation
    /**********************************************************
     */

    @Override
    public void serializeWithType(JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonProcessingException
    {
        typeSer.writeTypePrefixForScalar(this, jgen);
        this.serialize(jgen, provider);
        typeSer.writeTypeSuffixForScalar(this, jgen);
    }

    @Override
    public void serialize(JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        jgen.writeString(toCanonical());
    } 
    
    /*
    /**********************************************************
    /* Methods for sub-classes to use
    /**********************************************************
     */

    /**
     * @param trailingSemicolon Whether to add trailing semicolon for non-primitive
     *   (reference) types or not
     */
    protected static StringBuilder _classSignature(Class<?> cls, StringBuilder sb,
           boolean trailingSemicolon)
    {
        if (cls.isPrimitive()) {
            if (cls == Boolean.TYPE) {                
                sb.append('Z');
            } else if (cls == Byte.TYPE) {
                sb.append('B');
            }
            else if (cls == Short.TYPE) {
                sb.append('S');
            }
            else if (cls == Character.TYPE) {
                sb.append('C');
            }
            else if (cls == Integer.TYPE) {
                sb.append('I');
            }
            else if (cls == Long.TYPE) {
                sb.append('J');
            }
            else if (cls == Float.TYPE) {
                sb.append('F');
            }
            else if (cls == Double.TYPE) {
                sb.append('D');
            }
            else if (cls == Void.TYPE) {
                sb.append('V');
            } else {
                throw new IllegalStateException("Unrecognized primitive type: "+cls.getName());
            }
        } else {
            sb.append('L');
            String name = cls.getName();
            for (int i = 0, len = name.length(); i < len; ++i) {
                char c = name.charAt(i);
                if (c == '.') c = '/';
                sb.append(c);
            }
            if (trailingSemicolon) {
                sb.append(';');
            }
        }
        return sb;
    }
}
