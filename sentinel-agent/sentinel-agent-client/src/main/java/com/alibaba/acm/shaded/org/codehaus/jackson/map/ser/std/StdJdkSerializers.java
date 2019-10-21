package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.BasicSerializerFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.Provider;

/**
 * Class that providers access to serializers user for non-structured JDK types that
 * are serializer as scalars; some using basic {@link ToStringSerializer},
 * others explicit serializers.
 */
public class StdJdkSerializers
    implements Provider<Map.Entry<Class<?>,Object>>
{
    /**
     * Method called by {@link BasicSerializerFactory} to access
     * all serializers this class provides.
     */
    @Override
    public Collection<Map.Entry<Class<?>, Object>> provide()
    {
        HashMap<Class<?>,Object> sers = new HashMap<Class<?>,Object>();

        // First things that 'toString()' can handle
        final ToStringSerializer sls = ToStringSerializer.instance;

        sers.put(java.net.URL.class, sls);
        sers.put(java.net.URI.class, sls);

        sers.put(Currency.class, sls);
        sers.put(UUID.class, sls);
        sers.put(java.util.regex.Pattern.class, sls);
        sers.put(Locale.class, sls);

        // starting with 1.7, use compact String for Locale
        sers.put(Locale.class, sls);
        
        // then atomic types
        sers.put(AtomicReference.class, AtomicReferenceSerializer.class);
        sers.put(AtomicBoolean.class, AtomicBooleanSerializer.class);
        sers.put(AtomicInteger.class, AtomicIntegerSerializer.class);
        sers.put(AtomicLong.class, AtomicLongSerializer.class);
        
        // then types that need specialized serializers
        sers.put(File.class, FileSerializer.class);
        sers.put(Class.class, ClassSerializer.class);

        // And then some stranger types... not 100% they are needed but:
        sers.put(Void.TYPE, NullSerializer.class);
        
        return sers.entrySet();
    }

    /*
     ********************************************************
     * Serializers for atomic types
     ********************************************************
     */

    public final static class AtomicBooleanSerializer
        extends ScalarSerializerBase<AtomicBoolean>
    {
        public AtomicBooleanSerializer() { super(AtomicBoolean.class, false); }
    
        @Override
        public void serialize(AtomicBoolean value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeBoolean(value.get());
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("boolean", true);
        }
    }
    
    public final static class AtomicIntegerSerializer
        extends ScalarSerializerBase<AtomicInteger>
    {
        public AtomicIntegerSerializer() { super(AtomicInteger.class, false); }
    
        @Override
        public void serialize(AtomicInteger value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeNumber(value.get());
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("integer", true);
        }
    }

    public final static class AtomicLongSerializer
        extends ScalarSerializerBase<AtomicLong>
    {
        public AtomicLongSerializer() { super(AtomicLong.class, false); }
    
        @Override
        public void serialize(AtomicLong value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeNumber(value.get());
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("integer", true);
        }
    }
    
    public final static class AtomicReferenceSerializer
        extends SerializerBase<AtomicReference<?>>
    {
        public AtomicReferenceSerializer() { super(AtomicReference.class, false); }

        @Override
        public void serialize(AtomicReference<?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            provider.defaultSerializeValue(value.get(), jgen);
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("any", true);
        }
    }
    
    /*
    /**********************************************************
    /* Specialized serializers, referential types
    /**********************************************************
     */

    /**
     * For now, File objects get serialized by just outputting
     * absolute (but not canonical) name as String value
     */
    public final static class FileSerializer
        extends ScalarSerializerBase<File>
    {
        public FileSerializer() { super(File.class); }

        @Override
        public void serialize(File value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeString(value.getAbsolutePath());
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("string", true);
        }
    }

    /**
     * Also: default bean access will not do much good with Class.class. But
     * we can just serialize the class name and that should be enough.
     */
    public final static class ClassSerializer
        extends ScalarSerializerBase<Class<?>>
    {
        public ClassSerializer() { super(Class.class, false); }

        @Override
        public void serialize(Class<?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeString(value.getName());
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("string", true);
        }
    }
}
