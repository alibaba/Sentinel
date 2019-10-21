package com.taobao.csp.third.com.alibaba.fastjson.serializer;

import com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.ObjectSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializeWriter;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.lang.reflect.Type;

public class AppendableSerializer implements ObjectSerializer {

    public final static com.taobao.csp.third.com.alibaba.fastjson.serializer.AppendableSerializer instance = new com.taobao.csp.third.com.alibaba.fastjson.serializer.AppendableSerializer();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object == null) {
            SerializeWriter out = serializer.out;
            out.writeNull(SerializerFeature.WriteNullStringAsEmpty);
            return;
        }

        serializer.write(object.toString());
    }

}
