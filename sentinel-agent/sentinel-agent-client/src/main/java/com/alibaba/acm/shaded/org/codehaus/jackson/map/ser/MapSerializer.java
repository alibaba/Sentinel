package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import java.util.HashSet;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * @deprecated Since 1.9 use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.MapSerializer}
 */
@Deprecated
@JacksonStdImpl
public class MapSerializer
    extends com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.MapSerializer
{
    protected MapSerializer() {
        this((HashSet<String>)null, null, null, false, null, null, null, null);
    }

    /**
     * Legacy constructor (as of 1.7)
     * 
     * @deprecated Use variant that takes Key type and property information
     */
    @Deprecated
    protected MapSerializer(HashSet<String> ignoredEntries,
            JavaType valueType, boolean valueTypeIsStatic,
            TypeSerializer vts)
    {
        super(ignoredEntries, UNSPECIFIED_TYPE, valueType, valueTypeIsStatic, vts, null, null, null);
    }

    /**
     * Legacy constructor (as of 1.8)
     * 
     * @deprecated As of 1.8, use version that takes valueSerializer
     */
    @Deprecated
    protected MapSerializer(HashSet<String> ignoredEntries,
            JavaType keyType, JavaType valueType, boolean valueTypeIsStatic,
            TypeSerializer vts, JsonSerializer<Object> keySerializer, BeanProperty property)
    {
        super(ignoredEntries, keyType, valueType, valueTypeIsStatic, vts, keySerializer, null, property);
    }
    
    protected MapSerializer(HashSet<String> ignoredEntries,
            JavaType keyType, JavaType valueType, boolean valueTypeIsStatic,
            TypeSerializer vts,
            JsonSerializer<Object> keySerializer, JsonSerializer<Object> valueSerializer, 
            BeanProperty property)
    {
        super(ignoredEntries, keyType, valueType, valueTypeIsStatic,
                vts, keySerializer, valueSerializer, property);
    }    
}
