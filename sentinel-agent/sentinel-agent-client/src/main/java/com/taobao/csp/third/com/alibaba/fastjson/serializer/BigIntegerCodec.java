/*
 * Copyright 1999-2018 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taobao.csp.third.com.alibaba.fastjson.serializer;

import com.taobao.csp.third.com.alibaba.fastjson.parser.DefaultJSONParser;
import com.taobao.csp.third.com.alibaba.fastjson.parser.JSONLexer;
import com.taobao.csp.third.com.alibaba.fastjson.parser.JSONToken;
import com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.ObjectSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializeWriter;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializerFeature;
import com.taobao.csp.third.com.alibaba.fastjson.util.TypeUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * @author wenshao[szujobs@hotmail.com]
 */
public class BigIntegerCodec implements ObjectSerializer, ObjectDeserializer {
    private final static BigInteger LOW = BigInteger.valueOf(-9007199254740991L);
    private final static BigInteger HIGH = BigInteger.valueOf(9007199254740991L);

    public final static com.taobao.csp.third.com.alibaba.fastjson.serializer.BigIntegerCodec instance = new com.taobao.csp.third.com.alibaba.fastjson.serializer.BigIntegerCodec();

    public void write(JSONSerializer serializer
            , Object object
            , Object fieldName
            , Type fieldType, int features) throws IOException
    {
        SerializeWriter out = serializer.out;

        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }

        BigInteger val = (BigInteger) object;
        String str = val.toString();
        if (str.length() >= 16
                && SerializerFeature.isEnabled(features, out.features, SerializerFeature.BrowserCompatible)
                && (val.compareTo(LOW) < 0
                    || val.compareTo(HIGH) > 0))
        {
            out.writeString(str);
            return;
        }
        out.write(str);
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialze(DefaultJSONParser parser) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == JSONToken.LITERAL_INT) {
            String val = lexer.numberString();
            lexer.nextToken(JSONToken.COMMA);
            return (T) new BigInteger(val);
        }

        Object value = parser.parse();
        return value == null //
            ? null //
            : (T) TypeUtils.castToBigInteger(value);
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_INT;
    }
}
