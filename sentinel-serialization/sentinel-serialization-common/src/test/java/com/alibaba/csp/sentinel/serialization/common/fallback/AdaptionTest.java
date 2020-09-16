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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.serialization.test.JsonSerializingTest;

public abstract class AdaptionTest extends JsonSerializingTest {
    private Adaption adaption;
    
    protected abstract Adaption getAdaption();
    protected abstract String getName();
    
    @Before
    public void initAdaption() {
        this.adaption = getAdaption();
    }
    
    @Test
    public void basic() {
        assertEquals(getName(), adaption.getName());
        assertTrue(adaption.available());
    }
    
    @Override
    public String serialize(Object obj) {
        try {
            return this.adaption.serialize(obj);
        } catch (Exception e) {
        }
        return null;
    }
    
    @Override
    protected <T> T deserialize(String json, Class<T> clazz) {
        try {
            return this.adaption.deserialize(json, clazz);
        } catch (Exception e) {
        }
        return null;
    }
    
    @Override
    protected <T> T deserialize(String json, Type type) {
        try {
            return this.adaption.deserialize(json, type);
        } catch (Exception e) {
        }
        return null;
    }
}
