package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;

/**
 * @author FengJianxin
 */
public class ApolloOpenApiClientProvider {

    private ApolloOpenApiClient client;


    public ApolloOpenApiClientProvider(String portalUrl, String token) {
        set(portalUrl, token);
    }

    public ApolloOpenApiClient get() {
        return client;
    }

    public ApolloOpenApiClient set(String portalUrl, String token) {
        this.client = newClient(portalUrl, token);
        return get();
    }

    private ApolloOpenApiClient newClient(String portalUrl, String token) {
        return ApolloOpenApiClient.newBuilder()
                .withPortalUrl(portalUrl)
                .withToken(token)
                .build();
    }

}
