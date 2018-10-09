package com.alibaba.csp.sentinel.transport;

abstract class ConfigUtils {

    /**
     * return prometheus push gateway way address
     * @return prometheus push gateway way address
     */
    static String getPrometheusAddress() {
        String pushServer = System.getProperty("sentinel.prometheus.gateway");
        return pushServer != null ? pushServer : "127.0.0.1:9091";
    }

    /**
     * return server's web port
     * @return server's web port
     */
    static String getAppWebPort() {
        String portString = System.getProperty("sentinel.web.port");
        return portString != null ? portString : "0";
    }

}
