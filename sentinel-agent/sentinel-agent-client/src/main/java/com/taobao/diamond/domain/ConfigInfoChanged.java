package com.taobao.diamond.domain;

/**
 * 变化的配置信息, 聚合时使用
 * 
 * @author leiwen.zh
 * 
 */
public class ConfigInfoChanged {

    private String dataId;
    private String group;
    private String tenant;

    public ConfigInfoChanged(String dataId, String group, String tenant) {
        this.dataId = dataId;
        this.group = group;
        this.setTenant(tenant);
    }


    public ConfigInfoChanged() {

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


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataId == null) ? 0 : dataId.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConfigInfoChanged other = (ConfigInfoChanged) obj;
        if (dataId == null) {
            if (other.dataId != null)
                return false;
        }
        else if (!dataId.equals(other.dataId))
            return false;
        if (group == null) {
            if (other.group != null)
                return false;
        }
        else if (!group.equals(other.group))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "ConfigInfoChanged [dataId=" + dataId + ", group=" + group + "]";
    }


	public String getTenant() {
		return tenant;
	}


	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

}
