package com.alibaba.acm.shaded.org.codehaus.jackson.map.ext;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonMappingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.CalendarSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.SerializerBase;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.ToStringSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.Provider;

/**
 * Provider for serializers of XML types that are part of full JDK 1.5, but
 * that some alleged 1.5 platforms are missing (Android, GAE).
 * And for this reason these are added using more dynamic mechanism.
 *<p>
 * Note: since many of classes defined are abstract, caller must take
 * care not to just use straight equivalency check but rather consider
 * subclassing as well.
 *
 * @since 1.4
 */
public class CoreXMLSerializers
    implements Provider<Map.Entry<Class<?>,JsonSerializer<?>>>
{
    final static HashMap<Class<?>,JsonSerializer<?>> _serializers = new HashMap<Class<?>,JsonSerializer<?>>();
    /**
     * We will construct instances statically, during class loading, to try to
     * make things fail-fast, i.e. to catch problems as soon as possible.
     */
    static {
        ToStringSerializer tss = ToStringSerializer.instance;
        _serializers.put(Duration.class, tss);
        _serializers.put(XMLGregorianCalendar.class, new XMLGregorianCalendarSerializer());
        _serializers.put(QName.class, tss);
    }
    
    @Override
    public Collection<Map.Entry<Class<?>,JsonSerializer<?>>> provide() {
        return _serializers.entrySet();
    }

    public static class XMLGregorianCalendarSerializer extends SerializerBase<XMLGregorianCalendar>
    {
        public XMLGregorianCalendarSerializer() {
            super(XMLGregorianCalendar.class);
        }

        @Override
        public void serialize(XMLGregorianCalendar value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonGenerationException {
            CalendarSerializer.instance.serialize(value.toGregorianCalendar(), jgen, provider);
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
            return CalendarSerializer.instance.getSchema(provider, typeHint);
        }
    }
}
