package com.alibaba.acm.shaded.com.aliyuncs.http;

import com.alibaba.acm.shaded.com.aliyuncs.http.clients.CompatibleUrlConnClient;

/**
 * @author VK.Gao
 * @date 2018/03/29
 */
public enum HttpClientType {

    Compatible(CompatibleUrlConnClient.class),
    ApacheHttpClient(com.alibaba.acm.shaded.com.aliyuncs.http.clients.ApacheHttpClient.class),
    OkHttp(null),
    Custom(null),
    ;

    private Class<? extends IHttpClient> implClass;

    HttpClientType(Class<? extends IHttpClient> implClass) {
        this.implClass = implClass;
    }

    public Class<? extends IHttpClient> getImplClass() {
        return implClass;
    }
}
