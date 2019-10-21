package com.alibaba.edas.acm.domain;

import java.io.Serializable;

/**
 * Created by qingliang on 2018/3/8.
 */
public class ConfigKey implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1748953484511867581L;

    private String dataId;
    private String group;

    public ConfigKey() {
    };

    public ConfigKey(String dataId, String group) {
        this.dataId = dataId;
        this.group = group;
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

}
