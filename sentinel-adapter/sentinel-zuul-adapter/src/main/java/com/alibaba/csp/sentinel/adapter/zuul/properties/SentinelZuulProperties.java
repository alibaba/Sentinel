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

package com.alibaba.csp.sentinel.adapter.zuul.properties;

import static com.alibaba.csp.sentinel.adapter.zuul.constants.ZuulConstant.SEND_RESPONSE_FILTER_ORDER;

/**
 * Sentinel Spring Cloud Zuul AutoConfiguration property.
 *
 * @author tiger
 */
public class SentinelZuulProperties {

    private boolean enabled = false;

    private Order order = new Order();

    public static class Order {

        private int post = SEND_RESPONSE_FILTER_ORDER - 10;

        private int pre = 10000;

        private int error = -1;

        public int getPost() {
            return post;
        }

        public void setPost(int post) {
            this.post = post;
        }

        public int getPre() {
            return pre;
        }

        public void setPre(int pre) {
            this.pre = pre;
        }

        public int getError() {
            return error;
        }

        public void setError(int error) {
            this.error = error;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
