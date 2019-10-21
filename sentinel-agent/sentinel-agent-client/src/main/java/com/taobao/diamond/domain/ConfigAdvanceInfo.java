package com.taobao.diamond.domain;

import java.io.Serializable;

public class ConfigAdvanceInfo  implements Serializable {
    static final long serialVersionUID = -1L;
    private long createTime;
    private long modifyTime;
    private String createUser;
    private String createIp;
    private String desc;
    private String use;
    private String effect;
    private String type;
    private String schema;
    private String config_tags;
    public long getCreateTime() {
        return createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public long getModifyTime() {
        return modifyTime;
    }
    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }
    public String getCreateUser() {
        return createUser;
    }
    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }
    public String getCreateIp() {
        return createIp;
    }
    public void setCreateIp(String createIp) {
        this.createIp = createIp;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getUse() {
        return use;
    }
    public void setUse(String use) {
        this.use = use;
    }
    public String getEffect() {
        return effect;
    }
    public void setEffect(String effect) {
        this.effect = effect;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getSchema() {
        return schema;
    }
    public void setSchema(String schema) {
        this.schema = schema;
    }
    public String getConfig_tags() {
        return config_tags;
    }
    public void setConfig_tags(String config_tags) {
        this.config_tags = config_tags;
    }


}
