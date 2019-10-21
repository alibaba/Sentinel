package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonToken;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ArrayBuilders;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.LinkedNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ObjectBuffer;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Default implementation of {@link DeserializationContext}.
 */
public class StdDeserializationContext
    extends DeserializationContext
{
    /**
     * Let's limit length of error messages, for cases where underlying data
     * may be very large -- no point in spamming logs with megs of meaningless
     * data.
     */
    final static int MAX_ERROR_STR_LEN = 500;

    // // // Configuration

    /**
     * Currently active parser used for deserialization.
     * May be different from the outermost parser
     * when content is buffered.
     */
    protected JsonParser _parser;

    /**
     * @since 1.5
     */
    protected final DeserializerProvider _deserProvider;

    /**
     * @since 1.9
     */
    protected final InjectableValues _injectableValues;
    
    // // // Helper object recycling

    protected ArrayBuilders _arrayBuilders;

    protected ObjectBuffer _objectBuffer;

    protected DateFormat _dateFormat;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public StdDeserializationContext(DeserializationConfig config, JsonParser jp,
            DeserializerProvider prov, InjectableValues injectableValues)
    {
    	super(config);
        _parser = jp;
        _deserProvider = prov;
        _injectableValues = injectableValues;
    }

    /*
    /**********************************************************
    /* Public API, accessors
    /**********************************************************
     */

    @Override
    public DeserializerProvider getDeserializerProvider() {
        return _deserProvider;
    }

    /**
     * Method for accessing the currently active parser.
     * May be different from the outermost parser
     * when content is buffered.
     *<p>
     * Use of this method is discouraged: if code has direct access
     * to the active parser, that should be used instead.
     */
    @Override
    public JsonParser getParser() { return _parser; }

    @Override
    public Object findInjectableValue(Object valueId,
            BeanProperty forProperty, Object beanInstance)
    {
        if (_injectableValues == null) {
            throw new IllegalStateException("No 'injectableValues' configured, can not inject value with id ["+valueId+"]");
        }
        return _injectableValues.findInjectableValue(valueId, this, forProperty, beanInstance);
    }
    
    /*
    /**********************************************************
    /* Public API, helper object recycling
    /**********************************************************
     */

    @Override
    public final ObjectBuffer leaseObjectBuffer()
    {
        ObjectBuffer buf = _objectBuffer;
        if (buf == null) {
            buf = new ObjectBuffer();
        } else {
            _objectBuffer = null;
        }
        return buf;
    }

    @Override
    public final void returnObjectBuffer(ObjectBuffer buf)
    {
        /* Already have a reusable buffer? Let's retain bigger one
         * (or if equal, favor newer one, shorter life-cycle)
         */
        if (_objectBuffer == null
            || buf.initialCapacity() >= _objectBuffer.initialCapacity()) {
            _objectBuffer = buf;
        }
    }

    @Override
    public final ArrayBuilders getArrayBuilders()
    {
        if (_arrayBuilders == null) {
            _arrayBuilders = new ArrayBuilders();
        }
        return _arrayBuilders;
    }

    /*
    /**********************************************************
    /* Parsing methods that may use reusable/recyclable objects
    /**********************************************************
     */

    @Override
    public Date parseDate(String dateStr)
        throws IllegalArgumentException
    {
        try {
            return getDateFormat().parse(dateStr);
        } catch (ParseException pex) {
            throw new IllegalArgumentException(pex.getMessage());
        }
    }

    @Override
    public Calendar constructCalendar(Date d)
    {
        /* 08-Jan-2008, tatu: not optimal, but should work for the
         *   most part; let's revise as needed.
         */
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c;
    }
    /*
    /**********************************************************
    /* Public API, problem handling, reporting
    /**********************************************************
     */

    /**
     * Method deserializers can call to inform configured {@link DeserializationProblemHandler}s
     * of an unrecognized property.
     * 
     * @since 1.5
     */
    @Override
    public boolean handleUnknownProperty(JsonParser jp, JsonDeserializer<?> deser, Object instanceOrClass, String propName)
        throws IOException, JsonProcessingException
    {
        LinkedNode<DeserializationProblemHandler> h = _config.getProblemHandlers();
        if (h != null) {
            /* 04-Jan-2009, tatu: Ugh. Need to mess with currently active parser
             *   since parser is not explicitly passed to handler... that was a mistake
             */
            JsonParser oldParser = _parser;
            _parser = jp;
            try {
                while (h != null) {
                    // Can bail out if it's handled
                    if (h.value().handleUnknownProperty(this, deser, instanceOrClass, propName)) {
                        return true;
                    }
                    h = h.next();
                }
            } finally {
                _parser = oldParser;
            }
        }
        return false;
    }

    @Override
    public JsonMappingException mappingException(Class<?> targetClass) {
        return mappingException(targetClass, _parser.getCurrentToken());
    }

    @Override
    public JsonMappingException mappingException(Class<?> targetClass, JsonToken token)
    {
        String clsName = _calcName(targetClass);
        return JsonMappingException.from(_parser, "Can not deserialize instance of "+clsName+" out of "+token+" token");
    }
    
    @Override
    public JsonMappingException instantiationException(Class<?> instClass, Throwable t)
    {
        return JsonMappingException.from(_parser,
                "Can not construct instance of "+instClass.getName()+", problem: "+t.getMessage(),
                t);
    }

    @Override
    public JsonMappingException instantiationException(Class<?> instClass, String msg)
    {
        return JsonMappingException.from(_parser, "Can not construct instance of "+instClass.getName()+", problem: "+msg);
    }
    
    /**
     * Method that will construct an exception suitable for throwing when
     * some String values are acceptable, but the one encountered is not
     */
    @Override
    public JsonMappingException weirdStringException(Class<?> instClass, String msg)
    {
        return JsonMappingException.from(_parser, "Can not construct instance of "+instClass.getName()+" from String value '"+_valueDesc()+"': "+msg);
    }
    
    @Override
    public JsonMappingException weirdNumberException(Class<?> instClass, String msg)
    {
        return JsonMappingException.from(_parser, "Can not construct instance of "+instClass.getName()+" from number value ("+_valueDesc()+"): "+msg);
    }

    @Override
    public JsonMappingException weirdKeyException(Class<?> keyClass, String keyValue, String msg)
    {
        return JsonMappingException.from(_parser, "Can not construct Map key of type "+keyClass.getName()+" from String \""+_desc(keyValue)+"\": "+msg);
    }

    @Override
    public JsonMappingException wrongTokenException(JsonParser jp, JsonToken expToken, String msg)
    {
        return JsonMappingException.from(jp, "Unexpected token ("+jp.getCurrentToken()+"), expected "+expToken+": "+msg);
    }
    
    @Override
    public JsonMappingException unknownFieldException(Object instanceOrClass, String fieldName)
    {
        return UnrecognizedPropertyException.from(_parser, instanceOrClass, fieldName);
    }

    @Override
    public JsonMappingException unknownTypeException(JavaType type, String id)
    {
        return JsonMappingException.from(_parser, "Could not resolve type id '"+id+"' into a subtype of "+type);
    }

    /*
    /**********************************************************
    /* Overridable internal methods
    /**********************************************************
     */

    protected DateFormat getDateFormat()
    {
        if (_dateFormat == null) {
            // must create a clone since Formats are not thread-safe:
            _dateFormat = (DateFormat)_config.getDateFormat().clone();
        }
        return _dateFormat;
    }

    protected String determineClassName(Object instance)
    {
        return ClassUtil.getClassDescription(instance);
    }
    
    /*
    /**********************************************************
    /* Other internal methods
    /**********************************************************
     */

    protected String _calcName(Class<?> cls)
    {
        if (cls.isArray()) {
            return _calcName(cls.getComponentType())+"[]";
        }
        return cls.getName();
    }
    
    protected String _valueDesc()
    {
        try {
            return _desc(_parser.getText());
        } catch (Exception e) {
            return "[N/A]";
        }
    }
    protected String _desc(String desc)
    {
        // !!! should we quote it? (in case there are control chars, linefeeds)
        if (desc.length() > MAX_ERROR_STR_LEN) {
            desc = desc.substring(0, MAX_ERROR_STR_LEN) + "]...[" + desc.substring(desc.length() - MAX_ERROR_STR_LEN);
        }
        return desc;
    }
}
