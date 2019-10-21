package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;

@JacksonStdImpl
public class CalendarDeserializer
    extends StdScalarDeserializer<Calendar>
{
    /**
     * We may know actual expected type; if so, it will be
     * used for instantiation.
     */
    protected final Class<? extends Calendar> _calendarClass;
    
    public CalendarDeserializer() { this(null); }
    public CalendarDeserializer(Class<? extends Calendar> cc) {
        super(Calendar.class);
        _calendarClass = cc;
    }

    @Override
    public Calendar deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        Date d = _parseDate(jp, ctxt);
        if (d == null) {
            return null;
        }
        if (_calendarClass == null) {
            return ctxt.constructCalendar(d);
        }
        try {
            Calendar c = _calendarClass.newInstance();            
            c.setTimeInMillis(d.getTime());
            return c;
        } catch (Exception e) {
            throw ctxt.instantiationException(_calendarClass, e);
        }
    }
}
