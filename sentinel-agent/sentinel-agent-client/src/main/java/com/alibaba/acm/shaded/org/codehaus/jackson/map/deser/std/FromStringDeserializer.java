package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonToken;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;

/**
 * Base class for simple deserializer which only accept JSON String
 * values as the source.
 * 
 * @since 1.9 (moved from higher-level package)
 */
public abstract class FromStringDeserializer<T>
    extends StdScalarDeserializer<T>
{
    protected FromStringDeserializer(Class<?> vc) {
        super(vc);
    }

    public static Iterable<FromStringDeserializer<?>>all()
    {
        ArrayList<FromStringDeserializer<?>> all = new ArrayList<FromStringDeserializer<?>>();

        all.add(new UUIDDeserializer());
        all.add(new URLDeserializer());
        all.add(new URIDeserializer());
        all.add(new CurrencyDeserializer());
        all.add(new PatternDeserializer());
        // since 1.7:
        all.add(new LocaleDeserializer());
        // 1.8:
        all.add(new InetAddressDeserializer());
        all.add(new TimeZoneDeserializer());
        // 1.9
        all.add(new CharsetDeserializer());

        return all;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final T deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            // 15-Oct-2010, tatu: Empty String usually means null, so
            if (text.length() == 0) {
                return null;
            }
            try {
                T result = _deserialize(text, ctxt);
                if (result != null) {
                    return result;
                }
            } catch (IllegalArgumentException iae) {
                // nothing to do here, yet? We'll fail anyway
            }
            throw ctxt.weirdStringException(_valueClass, "not a valid textual representation");
        }
        if (jp.getCurrentToken() == JsonToken.VALUE_EMBEDDED_OBJECT) {
            // Trivial cases; null to null, instance of type itself returned as is
            Object ob = jp.getEmbeddedObject();
            if (ob == null) {
                return null;
            }
            if (_valueClass.isAssignableFrom(ob.getClass())) {
                return (T) ob;
            }
            return _deserializeEmbedded(ob, ctxt);
        }
        throw ctxt.mappingException(_valueClass);
    }
        
    protected abstract T _deserialize(String value, DeserializationContext ctxt)
        throws IOException, JsonProcessingException;

    protected T _deserializeEmbedded(Object ob, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // default impl: error out
        throw ctxt.mappingException("Don't know how to convert embedded Object of type "
                +ob.getClass().getName()+" into "+_valueClass.getName());
    }
    
    /*
    /**********************************************************
    /* Then concrete implementations
    /**********************************************************
     */

    public static class UUIDDeserializer
        extends FromStringDeserializer<UUID>
    {
        public UUIDDeserializer() { super(UUID.class); }

        @Override
        protected UUID _deserialize(String value, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return UUID.fromString(value);
        }

        @Override
        protected UUID _deserializeEmbedded(Object ob, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            if (ob instanceof byte[]) {
                byte[] bytes = (byte[]) ob;
                if (bytes.length != 16) {
                    ctxt.mappingException("Can only construct UUIDs from 16 byte arrays; got "+bytes.length+" bytes");
                }
                // clumsy, but should work for now...
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
                long l1 = in.readLong();
                long l2 = in.readLong();
                return new UUID(l1, l2);
            }
            super._deserializeEmbedded(ob, ctxt);
            return null; // never gets here
        }
    }

    public static class URLDeserializer
        extends FromStringDeserializer<URL>
    {
        public URLDeserializer() { super(URL.class); }
        
        @Override
        protected URL _deserialize(String value, DeserializationContext ctxt)
            throws IOException
        {
            return new URL(value);
        }
    }

    public static class URIDeserializer
        extends FromStringDeserializer<URI>
    {
        public URIDeserializer() { super(URI.class); }

        @Override
        protected URI _deserialize(String value, DeserializationContext ctxt)
            throws IllegalArgumentException
        {
            return URI.create(value);
        }
    }

    public static class CurrencyDeserializer
        extends FromStringDeserializer<Currency>
    {
        public CurrencyDeserializer() { super(Currency.class); }
        
        @Override
        protected Currency _deserialize(String value, DeserializationContext ctxt)
            throws IllegalArgumentException
        {
            // will throw IAE if unknown:
            return Currency.getInstance(value);
        }
    }

    public static class PatternDeserializer
        extends FromStringDeserializer<Pattern>
    {
        public PatternDeserializer() { super(Pattern.class); }
        
        @Override
        protected Pattern _deserialize(String value, DeserializationContext ctxt)
            throws IllegalArgumentException
        {
            // will throw IAE (or its subclass) if malformed
            return Pattern.compile(value);
        }
    }

    /**
     * Kept protected as it's not meant to be extensible at this point
     * 
     * @since 1.7
     */
    protected static class LocaleDeserializer
        extends FromStringDeserializer<Locale>
    {
        public LocaleDeserializer() { super(Locale.class); }
        
        @Override
        protected Locale _deserialize(String value, DeserializationContext ctxt)
            throws IOException
        {
            int ix = value.indexOf('_');
            if (ix < 0) { // single argument
                return new Locale(value);
            }
            String first = value.substring(0, ix);
            value = value.substring(ix+1);
            ix = value.indexOf('_');
            if (ix < 0) { // two pieces
                return new Locale(first, value);
            }
            String second = value.substring(0, ix);
            return new Locale(first, second, value.substring(ix+1));
        }
    }

    /**
     * As per [JACKSON-484], also need special handling for InetAddress...
     * 
     * @since 1.7.4
     */
    protected static class InetAddressDeserializer
        extends FromStringDeserializer<InetAddress>
    {
        public InetAddressDeserializer() { super(InetAddress.class); }

        @Override
        protected InetAddress _deserialize(String value, DeserializationContext ctxt)
            throws IOException
        {
            return InetAddress.getByName(value);
        }
    }

    // [JACKSON-789] (since 1.9.5)
    protected static class CharsetDeserializer
        extends FromStringDeserializer<Charset>
    {
        public CharsetDeserializer() { super(Charset.class); }
    
        @Override
	    protected Charset _deserialize(String value, DeserializationContext ctxt)
            throws IOException
	    {
		return Charset.forName(value);
	    }
    }

    /**
     * As per [JACKSON-522], also need special handling for InetAddress...
     * 
     * @since 1.7.4
     */
    protected static class TimeZoneDeserializer
        extends FromStringDeserializer<TimeZone>
    {
        public TimeZoneDeserializer() { super(TimeZone.class); }

        @Override
        protected TimeZone _deserialize(String value, DeserializationContext ctxt)
            throws IOException
        {
            return TimeZone.getTimeZone(value);
        }
    }
}
