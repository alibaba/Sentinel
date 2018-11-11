/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.block.flow.param;

/**
 * A flow control item for a specific parameter value.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamFlowItem {

    private String object;
    private Integer count;
    private String classType;

    public ParamFlowItem() {}

    public ParamFlowItem(String object, Integer count, String classType) {
        this.object = object;
        this.count = count;
        this.classType = classType;
    }

    public static <T> ParamFlowItem newItem(T object, Integer count) {
        if (object == null) {
            throw new IllegalArgumentException("Invalid object: null");
        }
        return new ParamFlowItem(object.toString(), count, object.getClass().getName());
    }

    public String getObject() {
        return object;
    }

    public ParamFlowItem setObject(String object) {
        this.object = object;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public ParamFlowItem setCount(Integer count) {
        this.count = count;
        return this;
    }

    public String getClassType() {
        return classType;
    }

    public ParamFlowItem setClassType(String classType) {
        this.classType = classType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ParamFlowItem item = (ParamFlowItem)o;

        if (object != null ? !object.equals(item.object) : item.object != null) { return false; }
        if (count != null ? !count.equals(item.count) : item.count != null) { return false; }
        return classType != null ? classType.equals(item.classType) : item.classType == null;
    }

    @Override
    public int hashCode() {
        int result = object != null ? object.hashCode() : 0;
        result = 31 * result + (count != null ? count.hashCode() : 0);
        result = 31 * result + (classType != null ? classType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ParamFlowItem{" +
            "object=" + object +
            ", count=" + count +
            ", classType='" + classType + '\'' +
            '}';
    }
}
