package com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer;

import com.taobao.csp.third.com.alibaba.fastjson.parser.DefaultJSONParser;
import com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;

public abstract class ContextObjectDeserializer implements ObjectDeserializer {
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        return deserialze(parser, type, fieldName, null, 0);
    }
    
    public abstract <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName, String format, int features); 
}
