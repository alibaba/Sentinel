package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.AtomicBooleanDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.CalendarDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.ClassDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.DateDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.FromStringDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.JavaTypeDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StdDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.StringDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.TimestampDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.TokenBufferDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.UntypedObjectDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.*;

/**
 * Helper class used to contain simple/well-known deserializers for core JDK types.
 *<p>
 * Note: as of Jackson 1.9, we use type-erased class for registering, since
 * some types may come either as type-erased or typed (for example,
 * <code>java.lang.Class</code>).
 */
class StdDeserializers
{
    final HashMap<ClassKey, JsonDeserializer<Object>> _deserializers
        = new HashMap<ClassKey, JsonDeserializer<Object>>();

    private StdDeserializers()
    {
        // First, add the fall-back "untyped" deserializer:
        add(new UntypedObjectDeserializer());

        // Then String and String-like converters:
        StdDeserializer<?> strDeser = new StringDeserializer();
        add(strDeser, String.class);
        add(strDeser, CharSequence.class);
        add(new ClassDeserializer());

        // Then primitive-wrappers (simple):
        add(new StdDeserializer.BooleanDeserializer(Boolean.class, null));
        add(new StdDeserializer.ByteDeserializer(Byte.class, null));
        add(new StdDeserializer.ShortDeserializer(Short.class, null));
        add(new StdDeserializer.CharacterDeserializer(Character.class, null));
        add(new StdDeserializer.IntegerDeserializer(Integer.class, null));
        add(new StdDeserializer.LongDeserializer(Long.class, null));
        add(new StdDeserializer.FloatDeserializer(Float.class, null));
        add(new StdDeserializer.DoubleDeserializer(Double.class, null));
        
        /* And actual primitives: difference is the way nulls are to be
         * handled...
         */
        add(new StdDeserializer.BooleanDeserializer(Boolean.TYPE, Boolean.FALSE));
        add(new StdDeserializer.ByteDeserializer(Byte.TYPE, Byte.valueOf((byte)(0))));
        add(new StdDeserializer.ShortDeserializer(Short.TYPE, Short.valueOf((short)0)));
        add(new StdDeserializer.CharacterDeserializer(Character.TYPE, Character.valueOf('\0')));
        add(new StdDeserializer.IntegerDeserializer(Integer.TYPE, Integer.valueOf(0)));
        add(new StdDeserializer.LongDeserializer(Long.TYPE, Long.valueOf(0L)));
        add(new StdDeserializer.FloatDeserializer(Float.TYPE, Float.valueOf(0.0f)));
        add(new StdDeserializer.DoubleDeserializer(Double.TYPE, Double.valueOf(0.0)));
        
        // and related
        add(new StdDeserializer.NumberDeserializer());
        add(new StdDeserializer.BigDecimalDeserializer());
        add(new StdDeserializer.BigIntegerDeserializer());
        
        add(new CalendarDeserializer());
        add(new DateDeserializer());
        /* 24-Jan-2010, tatu: When including type information, we may
         *    know that we specifically need GregorianCalendar...
         */
        add(new CalendarDeserializer(GregorianCalendar.class),
                GregorianCalendar.class);
        add(new StdDeserializer.SqlDateDeserializer());
        add(new TimestampDeserializer());

        // From-string deserializers:
        for (StdDeserializer<?> deser : FromStringDeserializer.all()) {
            add(deser);
        }

        // And finally some odds and ends

        // to deserialize Throwable, need stack trace elements:
        add(new StdDeserializer.StackTraceElementDeserializer());

        // [JACKSON-283] need to support atomic types, too
        // (note: AtomicInteger/Long work due to single-arg constructor)
        add(new AtomicBooleanDeserializer());

        // including some core Jackson types:
        add(new TokenBufferDeserializer());
        add(new JavaTypeDeserializer());
    }

    /**
     * Public accessor to deserializers for core types.
     */
    public static HashMap<ClassKey, JsonDeserializer<Object>> constructAll()
    {
        return new StdDeserializers()._deserializers;
    }

    private void add(StdDeserializer<?> stdDeser)
    {
        add(stdDeser, stdDeser.getValueClass());
    }

    private void add(StdDeserializer<?> stdDeser, Class<?> valueClass)
    {
        // must do some unfortunate casting here...
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) stdDeser;
        // Not super clean, but default TypeFactory does work here:
        _deserializers.put(new ClassKey(valueClass), deser);
    }
}
