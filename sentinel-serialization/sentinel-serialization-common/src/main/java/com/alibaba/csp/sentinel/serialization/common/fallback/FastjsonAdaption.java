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
package com.alibaba.csp.sentinel.serialization.common.fallback;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.VersionUtil;

class FastjsonAdaption implements Adaption {
    private static final String MAIN_CLASS_NAME = "com.alibaba.fastjson.JSON";
    private static final String FEATUER_CLASS_NAME = "com.alibaba.fastjson.parser.Feature";
    private static final int LOWEST_VERSION = 0x01020C00; // from 1.2.12
    private static final boolean AVAILABLE;
    private static Class<?> CLASS;
    private static Method METHOD_TO_JSON_STRING;
    private static Method METHOD_PARSE_OBJECT_BY_TYPE;
    private static Method METHOD_PARSE_OBJECT_BY_CLASS;
    
    static {
        AVAILABLE = check();
    }
    
    private static boolean check() {
        try {
            CLASS = Class.forName(MAIN_CLASS_NAME);
            Field versionField = CLASS.getDeclaredField("VERSION");
            String actualVersionStr = String.valueOf(versionField.get(CLASS));
            int actualVersion = VersionUtil.fromVersionString(actualVersionStr);
            if (actualVersion < LOWEST_VERSION) {
                RecordLog.info("Found incompatible fastjson-{} which is too old", actualVersionStr);
                return false;
            }
            METHOD_TO_JSON_STRING = CLASS.getDeclaredMethod("toJSONString", Object.class);
            Class<?> CLASS_FEATURE = Class.forName(FEATUER_CLASS_NAME);
            METHOD_PARSE_OBJECT_BY_TYPE = CLASS.getDeclaredMethod("parseObject", String.class, Type.class, Array.newInstance(CLASS_FEATURE, 0).getClass());
            METHOD_PARSE_OBJECT_BY_CLASS = CLASS.getDeclaredMethod("parseObject", String.class, Object.class.getClass());
            RecordLog.info("Found available fastjson-{}", actualVersionStr);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public String getName() {
        return "fastjson";
    }

    @Override
    public boolean available() {
        return AVAILABLE;
    }

    @Override
    public String serialize(Object obj) throws Exception {
        return (String)METHOD_TO_JSON_STRING.invoke(CLASS, obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(String str, Type type) throws Exception {
        return (T)METHOD_PARSE_OBJECT_BY_TYPE.invoke(CLASS, str, type, null);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(String str, Class<T> clazz) throws Exception {
        return (T)METHOD_PARSE_OBJECT_BY_CLASS.invoke(CLASS, str, clazz);
    }

}
