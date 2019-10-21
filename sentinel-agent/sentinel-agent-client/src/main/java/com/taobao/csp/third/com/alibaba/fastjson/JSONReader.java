package com.taobao.csp.third.com.alibaba.fastjson;

import com.alibaba.fastjson.JSONException;
import com.taobao.csp.third.com.alibaba.fastjson.TypeReference;
import com.taobao.csp.third.com.alibaba.fastjson.parser.*;
import com.taobao.csp.third.com.alibaba.fastjson.util.TypeUtils;

import java.io.Closeable;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.taobao.csp.third.com.alibaba.fastjson.JSONStreamContext.*;

public class JSONReader implements Closeable {

    private final DefaultJSONParser parser;
    private com.taobao.csp.third.com.alibaba.fastjson.JSONStreamContext context;

    public JSONReader(Reader reader){
        this(reader, new Feature[0]);
    }

    public JSONReader(Reader reader, Feature... features){
        this(new JSONReaderScanner(reader));
        for (Feature feature : features) {
            this.config(feature, true);
        }
    }

    public JSONReader(JSONLexer lexer){
        this(new DefaultJSONParser(lexer));
    }

    public JSONReader(DefaultJSONParser parser){
        this.parser = parser;
    }

    public void setTimzeZone(TimeZone timezone) {
        this.parser.lexer.setTimeZone(timezone);
    }

    public void setLocale(Locale locale) {
        this.parser.lexer.setLocale(locale);
    }

    public void config(Feature feature, boolean state) {
        this.parser.config(feature, state);
    }

    public Locale getLocal() {
        return this.parser.lexer.getLocale();
    }

    public TimeZone getTimzeZone() {
        return this.parser.lexer.getTimeZone();
    }

    public void startObject() {
        if (context == null) {
            context = new com.taobao.csp.third.com.alibaba.fastjson.JSONStreamContext(null, com.taobao.csp.third.com.alibaba.fastjson.JSONStreamContext.StartObject);
        } else {
            startStructure();
            context = new com.taobao.csp.third.com.alibaba.fastjson.JSONStreamContext(context, com.taobao.csp.third.com.alibaba.fastjson.JSONStreamContext.StartObject);
        }

        this.parser.accept(JSONToken.LBRACE, JSONToken.IDENTIFIER);
    }

    public void endObject() {
        this.parser.accept(JSONToken.RBRACE);
        endStructure();
    }

    public void startArray() {
        if (context == null) {
            context = new com.taobao.csp.third.com.alibaba.fastjson.JSONStreamContext(null, StartArray);
        } else {
            startStructure();

            context = new com.taobao.csp.third.com.alibaba.fastjson.JSONStreamContext(context, StartArray);
        }
        this.parser.accept(JSONToken.LBRACKET);
    }

    public void endArray() {
        this.parser.accept(JSONToken.RBRACKET);
        endStructure();
    }

    private void startStructure() {
        final int state = context.state;
        switch (state) {
            case PropertyKey:
                parser.accept(JSONToken.COLON);
                break;
            case PropertyValue:
            case ArrayValue:
                parser.accept(JSONToken.COMMA);
                break;
            case StartArray:
            case StartObject:
                break;
            default:
                throw new JSONException("illegal state : " + context.state);
        }
    }

    private void endStructure() {
        context = context.parent;

        if (context == null) {
            return;
        }

        final int state = context.state;
        int newState = -1;
        switch (state) {
            case PropertyKey:
                newState = PropertyValue;
                break;
            case StartArray:
                newState = ArrayValue;
                break;
            case PropertyValue:
            case StartObject:
                newState = PropertyKey;
                break;
            default:
                break;
        }
        if (newState != -1) {
            context.state = newState;
        }
    }

    public boolean hasNext() {
        if (context == null) {
            throw new JSONException("context is null");
        }

        final int token = parser.lexer.token();
        final int state = context.state;
        switch (state) {
            case StartArray:
            case ArrayValue:
                return token != JSONToken.RBRACKET;
            case StartObject:
            case PropertyValue:
                return token != JSONToken.RBRACE;
            default:
                throw new JSONException("illegal state : " + state);
        }
    }

    public int peek() {
        return parser.lexer.token();
    }

    public void close() {
        parser.close();
    }

    public Integer readInteger() {
        Object object;
        if (context == null) {
            object = parser.parse();
        } else {
            readBefore();
            object = parser.parse();
            readAfter();
        }

        return TypeUtils.castToInt(object);
    }

    public Long readLong() {
        Object object;
        if (context == null) {
            object = parser.parse();
        } else {
            readBefore();
            object = parser.parse();
            readAfter();
        }

        return TypeUtils.castToLong(object);
    }

    public String readString() {
        Object object;
        if (context == null) {
            object = parser.parse();
        } else {
            readBefore();
            JSONLexer lexer = parser.lexer;
            if (context.state == com.taobao.csp.third.com.alibaba.fastjson.JSONStreamContext.StartObject && lexer.token() == JSONToken.IDENTIFIER) {
                object = lexer.stringVal();
                lexer.nextToken();
            } else {
                object = parser.parse();
            }
            readAfter();
        }

        return TypeUtils.castToString(object);
    }

    public <T> T readObject(TypeReference<T> typeRef) {
        return readObject(typeRef.getType());
    }

    public <T> T readObject(Type type) {
        if (context == null) {
            return parser.parseObject(type);
        }

        readBefore();
        T object = parser.parseObject(type);
        readAfter();
        return object;
    }

    public <T> T readObject(Class<T> type) {
        if (context == null) {
            return parser.parseObject(type);
        }

        readBefore();
        T object = parser.parseObject(type);
        readAfter();
        return object;
    }

    public void readObject(Object object) {
        if (context == null) {
            parser.parseObject(object);
            return;
        }

        readBefore();
        parser.parseObject(object);
        readAfter();
    }

    public Object readObject() {
        if (context == null) {
            return parser.parse();
        }

        readBefore();
        Object object;
        switch (context.state) {
            case StartObject:
            case PropertyValue:
                object = parser.parseKey();
                break;
            default:
                object = parser.parse();
                break;
        }

        readAfter();
        return object;
    }

    @SuppressWarnings("rawtypes")
    public Object readObject(Map object) {
        if (context == null) {
            return parser.parseObject(object);
        }

        readBefore();
        Object value = parser.parseObject(object);
        readAfter();
        return value;
    }

    private void readBefore() {
        int state = context.state;
        // before
        switch (state) {
            case PropertyKey:
                parser.accept(JSONToken.COLON);
                break;
            case PropertyValue:
                parser.accept(JSONToken.COMMA, JSONToken.IDENTIFIER);
                break;
            case ArrayValue:
                parser.accept(JSONToken.COMMA);
                break;
            case StartObject:
                break;
            case StartArray:
                break;
            default:
                throw new JSONException("illegal state : " + state);
        }
    }

    private void readAfter() {
        int state = context.state;
        int newStat = -1;
        switch (state) {
            case StartObject:
                newStat = PropertyKey;
                break;
            case PropertyKey:
                newStat = PropertyValue;
                break;
            case PropertyValue:
                newStat = PropertyKey;
                break;
            case ArrayValue:
                break;
            case StartArray:
                newStat = ArrayValue;
                break;
            default:
                throw new JSONException("illegal state : " + state);
        }
        if (newStat != -1) {
            context.state = newStat;
        }
    }

}
