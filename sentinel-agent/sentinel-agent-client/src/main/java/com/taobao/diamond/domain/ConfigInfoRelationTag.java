package com.taobao.diamond.domain;

public class ConfigInfoRelationTag  extends ConfigInfo {
    private static final long serialVersionUID = 4511997359365712505L;

    private String configTags;

    public String getConfigTags() {
        return configTags;
    }

    public void setConfigTags(String configTags) {
        this.configTags = configTags;
    }

    @Override
    public String toString() {
        return "ConfigInfoRelationTag{" + "id=" + getId() + ", dataId='" + getDataId() + '\'' + ", group='" + getGroup() + '\''
                + ", tenant='" + getTenant() + '\'' + ", appName='" + getAppName() + '\'' + ", content='" + getContent() + '\''
                + ", md5='" + getMd5() + '\'' + '}' + ",configTags='" + configTags + '\'' + '}';
    }
}
