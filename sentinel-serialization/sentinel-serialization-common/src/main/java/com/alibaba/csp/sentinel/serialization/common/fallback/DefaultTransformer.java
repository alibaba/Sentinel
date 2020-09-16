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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.alibaba.csp.sentinel.serialization.common.JsonDeserializer;
import com.alibaba.csp.sentinel.serialization.common.JsonSerializer;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Default transformer which can adapt to gson, fastjson in order and dynamically.
 * 
 * @author jason
 *
 */
public class DefaultTransformer implements JsonSerializer, JsonDeserializer {
    private Adaption adaption;
    
    public DefaultTransformer() {
        List<Adaption> adaptions = Arrays.asList(
            new GsonAdaption(), 
            new FastjsonAdaption()
        );
        for (Adaption item : adaptions) {
            if (item.available()) {
                this.adaption = item;
                break;
            }
        }
        AssertUtil.notNull(this.adaption, "No serializing tool could be found");
    }

    @Override
    public String serialize(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return this.adaption.serialize(obj);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public <T> T deserialize(String str, Type type) {
        if (StringUtil.isBlank(str)) {
            return null;
        }
        try {
            return this.adaption.deserialize(str, type);
        } catch (Exception e) {
        }
        return null;
    }
    
    @Override
    public <T> T deserialize(String str, Class<T> clazz) {
        if (StringUtil.isBlank(str)) {
            return null;
        }
        try {
            return this.adaption.deserialize(str, clazz);
        } catch (Exception e) {
        }
        return null;
    }

}
