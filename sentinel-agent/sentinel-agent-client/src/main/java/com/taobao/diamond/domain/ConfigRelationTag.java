package com.taobao.diamond.domain;

import java.io.Serializable;

public class ConfigRelationTag implements Serializable {
    static final long serialVersionUID = -1L;

    // 不能增加字段
    private long nid;
    private long id;
    private String dataId;
    private String group;
    private String tenant;
    private String tagName;
    private String tagType;

    public long getNid() {
        return nid;
    }

    public void setNid(long nid) {
        this.nid = nid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    @Override
    public String toString() {
        return "ConfigRelationTag{" +
                "nid=" + nid +
                ", id=" + id +
                ", dataId='" + dataId + '\'' +
                ", group='" + group + '\'' +
                ", tenant='" + tenant + '\'' +
                '}';
    }
}
