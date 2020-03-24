package com.alibaba.csp.sentinel.dashboard.vo.req;

import java.io.Serializable;

/**
 * @author cdfive
 */
public class MachineReqVo implements Serializable {

    private static final long serialVersionUID = -2736553821813423555L;

    /**应用名*/
    private String app;

    /**应用所在机器ip*/
    private String ip;

    /**应用所在机器端口,指应用端Sentinel Transport的端口*/
    private Integer port;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
