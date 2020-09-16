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

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.serialization.common.fallback.DefaultTransformer;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.SpiLoader;

/**
 * Helper for json converting.
 * 
 * @author jason
 *
 */
public final class JsonTransformerLoader {
    private static JsonSerializer SERIALIZER;
    private static JsonDeserializer DESERIALIZER;
    
    static {
        load();
    }
    
    private static void load() {
        SERIALIZER = SpiLoader.loadFirstInstanceOrDefault(JsonSerializer.class, DefaultTransformer.class);
        DESERIALIZER = SpiLoader.loadFirstInstanceOrDefault(JsonDeserializer.class, DefaultTransformer.class);
        AssertUtil.notNull(SERIALIZER, "No JSON serializer found");
        AssertUtil.notNull(DESERIALIZER, "No JSON deserializer found");
        RecordLog.info("Use {} as json serializer", SERIALIZER.getClass().getSimpleName());
        RecordLog.info("Use {} as json deserializer", DESERIALIZER.getClass().getSimpleName());
    }
    
    public static JsonSerializer serializer() {
        return SERIALIZER;
    }
    
    public static JsonDeserializer deserializer() {
        return DESERIALIZER;
    }
}
