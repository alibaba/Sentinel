package com.taobao.csp.third.com.alibaba.fastjson.spi;

import com.taobao.csp.third.com.alibaba.fastjson.parser.ParserConfig;
import com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.ObjectSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializeConfig;

public interface Module {
    ObjectDeserializer createDeserializer(ParserConfig config, Class type);
    ObjectSerializer createSerializer(SerializeConfig config, Class type);
}
