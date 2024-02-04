package com.alibaba.csp.sentinel.dashboard.datasource.entity.opentsdb;

import java.util.Objects;

/**
 * @author kangjiabang
 */
public class TagInfo {

    private String resourceCode;

    private String index;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TagInfo tagInfo = (TagInfo) o;
        return  Objects.equals(resourceCode, tagInfo.resourceCode) &&
                Objects.equals(index, tagInfo.index);
    }

    @Override
    public int hashCode() {

        return Objects.hash(resourceCode, index);
    }

    @Override
    public String toString() {
        return "TagInfo{" +
                "resourceCode='" + resourceCode + '\'' +
                ", index='" + index + '\'' +
                '}';
    }
}
