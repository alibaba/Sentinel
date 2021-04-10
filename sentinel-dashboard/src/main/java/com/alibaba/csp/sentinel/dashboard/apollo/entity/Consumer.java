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
 * @see <a href="https://github.com/ctripcorp/apollo/blob/master/apollo-portal/src/main/java/com/ctrip/framework/apollo/openapi/entity/Consumer.java">com/ctrip/framework/apollo/openapi/entity/Consumer</a>
 */
public class Consumer extends BaseEntity {

    private String name;

    private String appId;

    private String orgId;

    private String orgName;

    private String ownerName;

    private String ownerEmail;

    @Override
    public String toString() {
        return toStringHelper().add("name", name).add("appId", appId)
                .add("orgId", orgId)
                .add("orgName", orgName)
                .add("ownerName", ownerName)
                .add("ownerEmail", ownerEmail).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Consumer consumer = (Consumer) o;
        return Objects.equals(name, consumer.name) && Objects.equals(appId, consumer.appId) && Objects.equals(orgId, consumer.orgId) && Objects.equals(orgName, consumer.orgName) && Objects.equals(ownerName, consumer.ownerName) && Objects.equals(ownerEmail, consumer.ownerEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, appId, orgId, orgName, ownerName, ownerEmail);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
