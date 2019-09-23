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
package com.alibaba.csp.sentinel.enums;

/**
 * Define the Authority Strategy as enum instead of just two int values,
 * and move the judgement logic as its own behavior.
 * @author Weihua
 * @since 1.7.0
 */
public enum AuthorityStrategy {

    White(0, "White List"), Black(1, "Black List");

    private int value;
    private String desc;

    AuthorityStrategy(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * It's kind of strategy pattern behavior.
     * It behaves just according to <code>contains</code> and <code>value</code>.
     * @param contains whether limitApp contains origin or not
     * @return whether pass or not
     */
    public boolean pass(boolean contains){
        return (this.value == 0 && contains) || (this.value == 1 && !contains);
    }

    public String toString(){
        return name();
    }

}
