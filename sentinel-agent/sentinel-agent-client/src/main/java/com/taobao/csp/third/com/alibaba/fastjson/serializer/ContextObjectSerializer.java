package com.taobao.csp.third.com.alibaba.fastjson.serializer;

import com.taobao.csp.third.com.alibaba.fastjson.serializer.BeanContext;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.ObjectSerializer;

import java.io.IOException;

public interface ContextObjectSerializer extends ObjectSerializer {
    void write(JSONSerializer serializer, //
               Object object, //
               BeanContext context) throws IOException;
}
