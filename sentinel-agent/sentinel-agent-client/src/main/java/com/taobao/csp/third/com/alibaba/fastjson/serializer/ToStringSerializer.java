package com.taobao.csp.third.com.alibaba.fastjson.serializer;

import com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.ObjectSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class ToStringSerializer implements ObjectSerializer {

    public static final com.taobao.csp.third.com.alibaba.fastjson.serializer.ToStringSerializer instance = new com.taobao.csp.third.com.alibaba.fastjson.serializer.ToStringSerializer();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                      int features) throws IOException {
        SerializeWriter out = serializer.out;

        if (object == null) {
            out.writeNull();
            return;
        }

        String strVal = object.toString();
        out.writeString(strVal);
    }

}
