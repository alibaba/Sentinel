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
package com.alibaba.csp.sentinel.dashboard.apollo.entity;

import java.util.Date;
import java.util.Objects;

/**
 * @author wxq
 * @see <a href="https://github.com/ctripcorp/apollo/blob/master/apollo-portal/src/main/java/com/ctrip/framework/apollo/openapi/entity/ConsumerToken.java">com/ctrip/framework/apollo/openapi/entity/ConsumerToken</a>
 */
public class ConsumerToken extends BaseEntity {

    private Long consumerId;

    private String token;

    private Date expires;

    @Override
    public String toString() {
        return toStringHelper().add("consumerId", consumerId).add("token", token)
                .add("expires", expires).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConsumerToken that = (ConsumerToken) o;
        return Objects.equals(consumerId, that.consumerId) && Objects.equals(token, that.token) && Objects.equals(expires, that.expires);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), consumerId, token, expires);
    }

    public Long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Long consumerId) {
        this.consumerId = consumerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
}
