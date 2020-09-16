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
package com.alibaba.csp.sentinel.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.serialization.test.beans.Child;
import com.alibaba.csp.sentinel.serialization.test.beans.NonStandardSetter;
import com.alibaba.csp.sentinel.serialization.test.beans.Simple;

public abstract class JsonSerializingTest {
    protected abstract String serialize(Object obj);
    protected abstract <T> T deserialize(String json, Type type);
    protected abstract <T> T deserialize(String json, Class<T> clazz);
    
    private static class InnerObject {
        private long id;
        private String name;
        private Date createTime;
        private Set<String> pids;
        public long getId() {
            return id;
        }
        public void setId(long id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        @SuppressWarnings("unused")
        public void setName(String name) {
            this.name = name;
        }
        public Date getCreateTime() {
            return createTime;
        }
        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }
        public Set<String> getPids() {
            return pids;
        }
        public void setPids(Set<String> pids) {
            this.pids = pids;
        }
    }
    
    private static final class InnerChildObject extends InnerObject {
        private String extraInfo;
        public String getExtraInfo() {
            return extraInfo;
        }
        public void setExtraInfo(String extraInfo) {
            this.extraInfo = extraInfo;
        }
    }
    
    @Before
    public void initLogging() {
        System.setProperty("csp.sentinel.log.output.type", "console");
    }
    
    @Test
    public void abnormalDeserialize() {
        assertNull(deserialize("123456789012", int.class));
        assertNull(deserialize("12345678901234567890", long.class));
        assertNull(deserialize("asdf", long.class));
    }
    
    @Test
    public void simpleClass() throws Exception {
        Simple obj = new Simple();
        obj.setId(1);
        obj.setCreateTime(new Date());
        String str = serialize(obj);
        assertNotNull(str);
        
        Simple obj1 = deserialize(str, Simple.class);
        assertNotNull(obj1);
        assertEquals(obj.getId(), obj1.getId());
        assertEquals(obj.getCreateTime().getTime()/1000, obj1.getCreateTime().getTime()/1000);
        assertNull(obj1.getName());
    }
    
    @Test
    public void childClass() throws Exception {
        Child obj = new Child();
        obj.setId(1);
        obj.setCreateTime(new Date());
        obj.setExtraInfo("extra1");
        String str = serialize(obj);
        assertNotNull(str);
        
        Child obj1 = deserialize(str, Child.class);
        assertNotNull(obj1);
        assertEquals(obj.getId(), obj1.getId());
        assertEquals(obj.getCreateTime().getTime()/1000, obj1.getCreateTime().getTime()/1000);
        assertNull(obj1.getName());
        assertEquals(obj.getExtraInfo(), obj1.getExtraInfo());
    }
    
    @Test
    public void nonStandardSetterClass() throws Exception {
        NonStandardSetter obj = new NonStandardSetter();
        obj.setId(1);
        obj.setCreateTime(new Date());
        obj.setResource("r0");
        String str = serialize(obj);
        assertNotNull(str);
        
        NonStandardSetter obj1 = deserialize(str, NonStandardSetter.class);
        assertNotNull(obj1);
        assertEquals(obj.getId(), obj1.getId());
        assertEquals(obj.getCreateTime().getTime()/1000, obj1.getCreateTime().getTime()/1000);
        assertNull(obj1.getName());
        assertEquals(obj.getResource(), obj1.getResource());
    }
    
    @Test
    public void innerClassArray() throws Exception {
        InnerObject[] arr = new InnerObject[2];
        {
            InnerObject obj = new InnerObject();
            obj.setId(1);
            obj.setCreateTime(new Date());
            arr[0] = obj;
        }
        {
            InnerObject obj = new InnerObject();
            obj.setId(2);
            obj.setCreateTime(new Date());
            arr[1] = obj;
        }
        String str = serialize(arr);
        assertNotNull(str);
        
        InnerObject[] arr1 = deserialize(str, new InnerObject[0].getClass());
        assertNotNull(arr1);
        assertEquals(arr.length, arr1.length);
        for (int i = 0; i < arr1.length; i++) {
            InnerObject obj = arr[i];
            InnerObject obj1 = arr1[i];
            assertEquals(obj.getId(), obj1.getId());
            assertEquals(obj.getCreateTime().getTime()/1000, obj1.getCreateTime().getTime()/1000);
            assertNull(obj1.getName());
        }
    }
    
    @Test
    public void innerClassList() throws Exception {
        List<InnerObject> arr = new ArrayList<>();
        {
            InnerObject obj = new InnerObject();
            obj.setId(1);
            obj.setCreateTime(new Date());
            obj.setPids(new HashSet<>(Arrays.asList("p0", "p1", "p3")));
            arr.add(obj);
        }
        {
            InnerObject obj = new InnerObject();
            obj.setId(2);
            obj.setCreateTime(new Date());
            obj.setPids(new HashSet<>(Arrays.asList("p10", "p1", "p3")));
            arr.add(obj);
        }
        String str = serialize(arr);
        assertNotNull(str);
        
        TypeReferenceForTest<List<InnerObject>> typeRef = new TypeReferenceForTest<List<InnerObject>>() {};
        List<InnerObject> arr1 = deserialize(str, typeRef.getType());
        assertNotNull(arr1);
        assertEquals(arr.size(), arr1.size());
        for (int i = 0; i < arr1.size(); i++) {
            InnerObject obj = arr.get(i);
            InnerObject obj1 = arr1.get(i);
            assertEquals(obj.getId(), obj1.getId());
            assertEquals(obj.getCreateTime().getTime()/1000, obj1.getCreateTime().getTime()/1000);
            assertEquals(obj.getPids(), obj1.getPids());
            assertNull(obj1.getName());
        }
    }
    
    @Test
    public void innerClass() throws Exception {
        InnerObject obj = new InnerObject();
        obj.setId(1);
        obj.setCreateTime(new Date());
        String str = serialize(obj);
        assertNotNull(str);
        
        InnerObject obj1 = deserialize(str, InnerObject.class);
        assertNotNull(obj1);
        assertEquals(obj.getId(), obj1.getId());
        assertEquals(obj.getCreateTime().getTime()/1000, obj1.getCreateTime().getTime()/1000);
        assertNull(obj1.getName());
    }
    
    @Test
    public void innerChildClass() throws Exception {
        InnerChildObject obj = new InnerChildObject();
        obj.setId(1);
        obj.setCreateTime(new Date());
        obj.setExtraInfo("extra1");
        String str = serialize(obj);
        assertNotNull(str);
        
        InnerChildObject obj1 = deserialize(str, InnerChildObject.class);
        assertNotNull(obj1);
        assertEquals(obj.getId(), obj1.getId());
        assertEquals(obj.getCreateTime().getTime()/1000, obj1.getCreateTime().getTime()/1000);
        assertNull(obj1.getName());
        assertEquals(obj.getExtraInfo(), obj1.getExtraInfo());
    }
    
}
