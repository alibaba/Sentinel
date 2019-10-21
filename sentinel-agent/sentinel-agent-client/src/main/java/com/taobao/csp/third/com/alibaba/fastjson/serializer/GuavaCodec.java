package com.taobao.csp.third.com.alibaba.fastjson.serializer;

import com.taobao.csp.third.com.alibaba.fastjson.JSONObject;
import com.taobao.csp.third.com.alibaba.fastjson.parser.DefaultJSONParser;
import com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.ObjectSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializeWriter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by wenshao on 15/01/2017.
 */
public class GuavaCodec implements ObjectSerializer, ObjectDeserializer {
    public static com.taobao.csp.third.com.alibaba.fastjson.serializer.GuavaCodec instance = new com.taobao.csp.third.com.alibaba.fastjson.serializer.GuavaCodec();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object instanceof Multimap) {
            Multimap multimap = (Multimap) object;
            serializer.write(multimap.asMap());
        }
    }

    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Type rawType = type;
        if (type instanceof ParameterizedType) {
            rawType = ((ParameterizedType) type).getRawType();
        }

        if (rawType == ArrayListMultimap.class) {
            ArrayListMultimap multimap = ArrayListMultimap.create();
            JSONObject object = parser.parseObject();
            for (Map.Entry entry : object.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Collection) {
                    multimap.putAll(entry.getKey(), (List) value);
                } else {
                    multimap.put(entry.getKey(), value);
                }
            }

            return (T) multimap;
        }
        return null;
    }

    public int getFastMatchToken() {
        return 0;
    }
}
