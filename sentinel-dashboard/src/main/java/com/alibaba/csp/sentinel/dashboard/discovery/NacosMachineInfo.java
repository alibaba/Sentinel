package com.alibaba.csp.sentinel.dashboard.discovery;

import com.alibaba.nacos.api.PropertyKeyConst;

import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Properties;

/**
 * the machine info of nacos
 *
 * @author longqiang
 */
public class NacosMachineInfo extends DataSourceMachineInfo {

    private Properties properties;
    private String group;
    private long timeoutMs;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) throws UnknownHostException {
        if (Objects.isNull(properties.getProperty(PropertyKeyConst.SERVER_ADDR))) {
            throw new UnknownHostException("Nacos server address can't be null");
        }
        this.properties = properties;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    @Override
    public String toString() {
        return new StringBuilder("NacosMachineInfo{")
                .append(super.toString())
                .append(", properties='").append(properties).append('\'')
                .append(", group='").append(group).append('\'')
                .append(", timeoutMs=").append(timeoutMs).toString();
    }
}
