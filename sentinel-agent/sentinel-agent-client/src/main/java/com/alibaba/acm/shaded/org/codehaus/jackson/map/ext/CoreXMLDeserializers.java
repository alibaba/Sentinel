package com.alibaba.acm.shaded.org.codehaus.jackson.map.ext;

import java.io.IOException;
import java.util.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StdDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.FromStringDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StdScalarDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.Provider;

/**
 * Container deserializers that handle "core" XML types: ones included in standard
 * JDK 1.5. Types are directly needed by JAXB, and are thus supported within core
 * mapper package, not in "xc" package.
 *
 * @since 1.3
 */
public class CoreXMLDeserializers
    implements Provider<StdDeserializer<?>>
{
    /**
     * Data type factories are thread-safe after instantiation (and
     * configuration, if any); and since instantion (esp. implementation
     * introspection) can be expensive we better reuse the instance.
     */
    final static DatatypeFactory _dataTypeFactory;
    static {
        try {
            _dataTypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    
    /*
    /**********************************************************
    /* Provider implementation
    /**********************************************************
     */

    /**
     * Method called by {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.BasicDeserializerFactory}
     * to register deserializers this class provides.
     */
    @Override
    public Collection<StdDeserializer<?>> provide()
    {
        return Arrays.asList(new StdDeserializer<?>[] {
            new DurationDeserializer()
            ,new GregorianCalendarDeserializer()
            ,new QNameDeserializer()
        });
    }
    
    /*
    /**********************************************************
    /* Concrete deserializers
    /**********************************************************
     */

    public static class DurationDeserializer
        extends FromStringDeserializer<Duration>
    {
        public DurationDeserializer() { super(Duration.class); }
    
        @Override
        protected Duration _deserialize(String value, DeserializationContext ctxt)
            throws IllegalArgumentException
        {
            return _dataTypeFactory.newDuration(value);
        }
    }

    public static class GregorianCalendarDeserializer
        extends StdScalarDeserializer<XMLGregorianCalendar>
    {
        public GregorianCalendarDeserializer() { super(XMLGregorianCalendar.class); }
        
        @Override
        public XMLGregorianCalendar deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            Date d = _parseDate(jp, ctxt);
            if (d == null) {
                return null;
            }
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(d);
            return _dataTypeFactory.newXMLGregorianCalendar(calendar);
        }
    }

    public static class QNameDeserializer
        extends FromStringDeserializer<QName>
    {
        public QNameDeserializer() { super(QName.class); }
        
        @Override
        protected QName _deserialize(String value, DeserializationContext ctxt)
            throws IllegalArgumentException
        {
            return QName.valueOf(value);
        }
    }
}
