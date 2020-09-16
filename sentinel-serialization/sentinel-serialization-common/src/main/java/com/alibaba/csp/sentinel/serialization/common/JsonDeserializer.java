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
package com.alibaba.csp.sentinel.serialization.common;

import java.lang.reflect.Type;

/**
 * @author jason
 *
 */
public interface JsonDeserializer {
    /**
     * Parse json string to object.
     * 
     * @param str The json string
     * @param type The target type
     * @return Return the object if deserialize successfully or null
     */
    <T> T deserialize(String str, Type type);
    
    /**
     * Parse json string to object.
     * 
     * @param str The json string
     * @param clazz The target class
     * @return Return the object if deserialize successfully or null
     */
    <T> T deserialize(String str, Class<T> clazz);
}
