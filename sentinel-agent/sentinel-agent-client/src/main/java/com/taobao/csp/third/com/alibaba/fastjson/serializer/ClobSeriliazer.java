package com.taobao.csp.third.com.alibaba.fastjson.serializer;

import com.taobao.csp.third.com.alibaba.fastjson.JSONException;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.ObjectSerializer;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.SQLException;

public class ClobSeriliazer implements ObjectSerializer {

    public final static com.taobao.csp.third.com.alibaba.fastjson.serializer.ClobSeriliazer instance = new com.taobao.csp.third.com.alibaba.fastjson.serializer.ClobSeriliazer();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        try {
            if (object == null) {
                serializer.writeNull();
                return;
            }
            
            Clob clob = (Clob) object;
            Reader reader = clob.getCharacterStream();

            StringBuilder buf = new StringBuilder();
            
            try {
                char[] chars = new char[2048];
                for (;;) {
                    int len = reader.read(chars, 0, chars.length);
                    if (len < 0) {
                        break;
                    }
                    buf.append(chars, 0, len);
                }
            } catch(Exception ex) {
                throw new JSONException("read string from reader error", ex);
            }
            
            String text = buf.toString();
            reader.close();
            serializer.write(text);
        } catch (SQLException e) {
            throw new IOException("write clob error", e);
        }
    }

}
