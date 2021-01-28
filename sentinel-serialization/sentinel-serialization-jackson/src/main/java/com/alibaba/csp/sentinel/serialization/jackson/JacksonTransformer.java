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

import java.lang.reflect.Type;

import com.alibaba.csp.sentinel.serialization.common.JsonDeserializer;
import com.alibaba.csp.sentinel.serialization.common.JsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The transformer using jackson
 * 
 * @author jason
 *
 */
public class JacksonTransformer implements JsonSerializer, JsonDeserializer {
    private ObjectMapper mapper;
    
    public JacksonTransformer() {
        this.mapper = new ObjectMapper();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(String str, Type type) {
        try {
            return (T) this.mapper.readValue(str, new SimpleTypeReference(type));
        } catch (JsonProcessingException e) {
        }
        return null;
    }

    @Override
    public <T> T deserialize(String str, Class<T> clazz) {
        try {
            return this.mapper.readValue(str, clazz);
        } catch (JsonProcessingException e) {
        }
        return null;
    }

    @Override
    public String serialize(Object obj) {
        try {
            return this.mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
        }
        return null;
    }

}
