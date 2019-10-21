package com.taobao.csp.third.com.alibaba.fastjson.serializer;

import com.taobao.csp.third.com.alibaba.fastjson.parser.DefaultJSONParser;
import com.taobao.csp.third.com.alibaba.fastjson.parser.JSONToken;
import com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.ObjectSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

public class ByteBufferCodec implements ObjectSerializer, ObjectDeserializer {
    public final static com.taobao.csp.third.com.alibaba.fastjson.serializer.ByteBufferCodec instance = new com.taobao.csp.third.com.alibaba.fastjson.serializer.ByteBufferCodec();

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        ByteBufferBean bean = parser.parseObject(ByteBufferBean.class);
        return (T) bean.byteBuffer();
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LBRACKET;
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        ByteBuffer byteBuf = (ByteBuffer) object;

        byte[] array = byteBuf.array();

        SerializeWriter out = serializer.out;
        out.write('{');

        out.writeFieldName("array");
        out.writeByteArray(array);
        out.writeFieldValue(',', "limit", byteBuf.limit());
        out.writeFieldValue(',', "position", byteBuf.position());

        out.write('}');
    }

    public static class ByteBufferBean {
        public byte[] array;
        public int limit;
        public int position;

        public ByteBuffer byteBuffer() {
            ByteBuffer buf = ByteBuffer.wrap(array);
            buf.limit(limit);
            buf.position(position);
            return buf;
        }
    }
}
