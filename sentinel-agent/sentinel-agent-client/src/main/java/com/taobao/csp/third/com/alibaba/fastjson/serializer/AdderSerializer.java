package com.taobao.csp.third.com.alibaba.fastjson.serializer;

import com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.ObjectSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by wenshao on 14/03/2017.
 */
public class AdderSerializer implements ObjectSerializer {
    public static final com.taobao.csp.third.com.alibaba.fastjson.serializer.AdderSerializer instance = new com.taobao.csp.third.com.alibaba.fastjson.serializer.AdderSerializer();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object instanceof LongAdder) {
            out.writeFieldValue('{', "value", ((LongAdder) object).longValue());
            out.write('}');
        } else  if (object instanceof DoubleAdder) {
            out.writeFieldValue('{', "value", ((DoubleAdder) object).doubleValue());
            out.write('}');
        }
    }
}
