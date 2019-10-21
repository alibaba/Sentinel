package com.taobao.csp.third.com.alibaba.fastjson.support.springfox;

import com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.ObjectSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializeWriter;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Swagger的Json处理，解决/v2/api-docs获取不到内容导致获取不到API页面内容的问题
 *
 * @author zhaiyongchao [http://blog.didispace.com]
 * @since 1.2.15
 */
public class SwaggerJsonSerializer implements ObjectSerializer {

    public final static com.taobao.csp.third.com.alibaba.fastjson.support.springfox.SwaggerJsonSerializer instance = new com.taobao.csp.third.com.alibaba.fastjson.support.springfox.SwaggerJsonSerializer();

    public void write(JSONSerializer serializer, //
                      Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.getWriter();
        Json json = (Json) object;
        String value = json.value();
        out.write(value);
    }

}
