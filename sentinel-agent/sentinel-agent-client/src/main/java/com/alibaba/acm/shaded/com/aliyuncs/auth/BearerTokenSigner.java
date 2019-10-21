package com.alibaba.acm.shaded.com.aliyuncs.auth;

public class BearerTokenSigner extends Signer {

    @Override
    public String signString(String stringToSign, AlibabaCloudCredentials credentials) {
        return null;
    }

    @Override
    public String signString(String stringToSign, String accessKeySecret) {
        return null;
    }

    @Override
    public String getSignerName() {
        return null;
    }

    @Override
    public String getSignerVersion() {
        return null;
    }

    @Override
    public String getSignerType() {
        return "BEARERTOKEN";
    }

}
