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

import com.google.common.base.MoreObjects;

import java.util.Date;
import java.util.Objects;

/**
 * @author wxq
 * @see <a href="https://github.com/ctripcorp/apollo/blob/master/apollo-common/src/main/java/com/ctrip/framework/apollo/common/entity/BaseEntity.java">com/ctrip/framework/apollo/common/entity/BaseEntity</a>
 */
public abstract class BaseEntity {

    private Long id;

    private Boolean isDeleted;

    private String dataChangeCreatedBy;

    private Date dataChangeCreatedTime;

    private String dataChangeLastModifiedBy;

    private Date dataChangeLastModifiedTime;

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this).omitNullValues().add("id", id)
                .add("dataChangeCreatedBy", dataChangeCreatedBy)
                .add("dataChangeCreatedTime", dataChangeCreatedTime)
                .add("dataChangeLastModifiedBy", dataChangeLastModifiedBy)
                .add("dataChangeLastModifiedTime", dataChangeLastModifiedTime);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(isDeleted, that.isDeleted) && Objects.equals(dataChangeCreatedBy, that.dataChangeCreatedBy) && Objects.equals(dataChangeCreatedTime, that.dataChangeCreatedTime) && Objects.equals(dataChangeLastModifiedBy, that.dataChangeLastModifiedBy) && Objects.equals(dataChangeLastModifiedTime, that.dataChangeLastModifiedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isDeleted, dataChangeCreatedBy, dataChangeCreatedTime, dataChangeLastModifiedBy, dataChangeLastModifiedTime);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public String getDataChangeCreatedBy() {
        return dataChangeCreatedBy;
    }

    public void setDataChangeCreatedBy(String dataChangeCreatedBy) {
        this.dataChangeCreatedBy = dataChangeCreatedBy;
    }

    public Date getDataChangeCreatedTime() {
        return dataChangeCreatedTime;
    }

    public void setDataChangeCreatedTime(Date dataChangeCreatedTime) {
        this.dataChangeCreatedTime = dataChangeCreatedTime;
    }

    public String getDataChangeLastModifiedBy() {
        return dataChangeLastModifiedBy;
    }

    public void setDataChangeLastModifiedBy(String dataChangeLastModifiedBy) {
        this.dataChangeLastModifiedBy = dataChangeLastModifiedBy;
    }

    public Date getDataChangeLastModifiedTime() {
        return dataChangeLastModifiedTime;
    }

    public void setDataChangeLastModifiedTime(Date dataChangeLastModifiedTime) {
        this.dataChangeLastModifiedTime = dataChangeLastModifiedTime;
    }
}
