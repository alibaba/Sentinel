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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.alibaba.csp.sentinel.log.RecordLog;

class GsonAdaption implements Adaption {
    private static final String MAIN_CLASS_NAME = "com.google.gson.Gson";
    private static final String CONFIG_CLASS_NAME = "com.google.gson.internal.GsonBuildConfig";
    private static final boolean AVAILABLE;
    private static Object INSTANCE;
    private static Method METHOD_TO_JSON_STRING;
    private static Method METHOD_PARSE_OBJECT_BY_TYPE;
    private static Method METHOD_PARSE_OBJECT_BY_CLASS;
    
    static {
        AVAILABLE = check();
    }
    
    private static boolean check() {
        try {
            Class<?> mainClass = Class.forName(MAIN_CLASS_NAME);
            String actualVersionStr = "unknow";
            try {
                Class<?> configClass = Class.forName(CONFIG_CLASS_NAME);
                Field versionField = configClass.getDeclaredField("VERSION");
                actualVersionStr = String.valueOf(versionField.get(mainClass));
            } catch (Exception e) {
            }
            INSTANCE = mainClass.newInstance();
            METHOD_TO_JSON_STRING = mainClass.getDeclaredMethod("toJson", Object.class);
            METHOD_PARSE_OBJECT_BY_TYPE = mainClass.getDeclaredMethod("fromJson", String.class, Type.class);
            METHOD_PARSE_OBJECT_BY_CLASS = mainClass.getDeclaredMethod("fromJson", String.class, Object.class.getClass());
            RecordLog.info("Found available gson-{}", actualVersionStr);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public String getName() {
        return "gson";
    }

    @Override
    public boolean available() {
        return AVAILABLE;
    }

    @Override
    public String serialize(Object obj) throws Exception {
        return (String)METHOD_TO_JSON_STRING.invoke(INSTANCE, obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(String str, Type type) throws Exception {
        return (T)METHOD_PARSE_OBJECT_BY_TYPE.invoke(INSTANCE, str, type);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(String str, Class<T> clazz) throws Exception {
        return (T)METHOD_PARSE_OBJECT_BY_CLASS.invoke(INSTANCE, str, clazz);
    }

}
