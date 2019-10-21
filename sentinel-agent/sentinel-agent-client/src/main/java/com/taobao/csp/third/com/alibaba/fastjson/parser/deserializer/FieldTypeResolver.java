package com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer;

import com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer.ParseProcess;

import java.lang.reflect.Type;

public interface FieldTypeResolver extends ParseProcess {
    Type resolve(Object object, String fieldName);
}
