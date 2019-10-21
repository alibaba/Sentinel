package com.alibaba.acm.shaded.com.aliyuncs.auth;

public class BearerTokenCredentials implements AlibabaCloudCredentials {
    
    private String bearerToken;
    
    public BearerTokenCredentials(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public String getAccessKeyId() {
        return null;
    }

    @Override
    public String getAccessKeySecret() {
        return null;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        bearerToken = bearerToken;
    }

}
