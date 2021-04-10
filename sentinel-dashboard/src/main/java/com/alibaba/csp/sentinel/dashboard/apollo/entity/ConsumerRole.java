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

import java.util.Objects;

/**
 * @author wxq
 * @see <a href="https://github.com/ctripcorp/apollo/blob/master/apollo-portal/src/main/java/com/ctrip/framework/apollo/openapi/entity/ConsumerRole.java">com/ctrip/framework/apollo/openapi/entity/ConsumerRole</a>
 */
public class ConsumerRole extends BaseEntity {

    private Long consumerId;

    private Long roleId;

    @Override
    public String toString() {
        return toStringHelper().add("consumerId", consumerId).add("roleId", roleId).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConsumerRole that = (ConsumerRole) o;
        return Objects.equals(consumerId, that.consumerId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), consumerId, roleId);
    }

    public Long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Long consumerId) {
        this.consumerId = consumerId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
