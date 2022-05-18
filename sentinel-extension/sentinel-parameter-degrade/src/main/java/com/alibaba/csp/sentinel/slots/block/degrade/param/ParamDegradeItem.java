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
package com.alibaba.csp.sentinel.slots.block.degrade.param;

import com.alibaba.csp.sentinel.util.StringUtil;
import java.util.Objects;

/**
 * A flow control item for a specific parameter value.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamDegradeItem {

    private String object;
    private double count;
    private String classType;

    public ParamDegradeItem() {
    }

    public ParamDegradeItem(String object, double count, String classType) {
        this.object = object;
        this.count = count;
        this.classType = classType;
    }

    public static <T> ParamDegradeItem newItem(T object, double count) {
        if (object == null) {
            throw new IllegalArgumentException("Invalid object: null");
        }
        return new ParamDegradeItem(object.toString(), count, object.getClass().getName());
    }

    public String getObject() {
        return object;
    }

    public ParamDegradeItem setObject(String object) {
        this.object = object;
        return this;
    }

    public double getCount() {
        return count;
    }

    public ParamDegradeItem setCount(double count) {
        this.count = count;
        return this;
    }

    public String getClassType() {
        return classType;
    }

    public ParamDegradeItem setClassType(String classType) {
        this.classType = classType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParamDegradeItem item = (ParamDegradeItem) o;

        if (!StringUtil.equals(object, item.getObject())) {
            return false;
        }
        if (Double.compare(count, item.getCount()) != 0) {
            return false;
        }
        return StringUtil.equals(classType, item.getClassType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), object, count, classType);
    }

    @Override
    public String toString() {
        return "ParamDegradeItem{" +
                "object=" + object +
                ", count=" + count +
                ", classType='" + classType + '\'' +
                '}';
    }

    @Override
    protected ParamDegradeItem clone() {
        ParamDegradeItem paramDegradeItem = new ParamDegradeItem();
        paramDegradeItem.setObject(this.object);
        paramDegradeItem.setCount(this.count);
        paramDegradeItem.setClassType(this.classType);
        return paramDegradeItem;
    }
}
