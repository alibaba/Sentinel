/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.serialization.jackson;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.serialization.common.JsonDeserializer;
import com.alibaba.csp.sentinel.serialization.common.JsonSerializer;
import com.alibaba.csp.sentinel.serialization.common.JsonTransformerLoader;
import com.alibaba.csp.sentinel.serialization.test.JsonSerializingTest;

/**
 * @author jason
 *
 */
public class JacksonTransformerTest extends JsonSerializingTest {
    private JsonSerializer serializer;
    private JsonDeserializer deserializer;
    
    @Before
    public void init() {
        serializer = JsonTransformerLoader.serializer();
        deserializer = JsonTransformerLoader.deserializer();
    }
    
    @Test
    public void loadTest() {
        assertTrue(serializer instanceof JacksonTransformer);
        assertTrue(deserializer instanceof JacksonTransformer);
    }
    
    @Override
    protected String serialize(Object obj) {
        return this.serializer.serialize(obj);
    }

    @Override
    protected <T> T deserialize(String json, Type type) {
        return this.deserializer.deserialize(json, type);
    }

    @Override
    protected <T> T deserialize(String json, Class<T> clazz) {
        return this.deserializer.deserialize(json, clazz);
    }
}
