package com.taobao.csp.third.com.alibaba.fastjson.support.spring;

import com.taobao.csp.third.com.alibaba.fastjson.JSON;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializeWriter;
import org.springframework.web.socket.sockjs.frame.AbstractSockJsMessageCodec;

import java.io.IOException;
import java.io.InputStream;

public class FastjsonSockJsMessageCodec extends AbstractSockJsMessageCodec {

    public String[] decode(String content) throws IOException {
        return JSON.parseObject(content, String[].class);
    }

    public String[] decodeInputStream(InputStream content) throws IOException {
        return JSON.parseObject(content, String[].class);
    }

    @Override
    protected char[] applyJsonQuoting(String content) {
        SerializeWriter out = new SerializeWriter();
        try {
            JSONSerializer serializer = new JSONSerializer(out);
            serializer.write(content);
            return out.toCharArrayForSpringWebSocket();
        } finally {
            out.close();
        }
    }

}
