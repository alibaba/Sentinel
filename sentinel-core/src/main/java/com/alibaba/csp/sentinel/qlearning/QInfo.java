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
package com.alibaba.csp.sentinel.qlearning;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZhouYanjun
 */
public class QInfo {

    String state;
    AtomicInteger action = new AtomicInteger(0);
    volatile double utility;

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getAction() {
        return action.get();
    }

    public void setAction(int action) {
        this.action.set(action);
    }

    public double getUtility() {
        return utility;
    }

    public synchronized void setQInfo(String state, int action, double utility) {
        this.state = state;
        this.action.set(action);
        this.utility = utility;
    }

}