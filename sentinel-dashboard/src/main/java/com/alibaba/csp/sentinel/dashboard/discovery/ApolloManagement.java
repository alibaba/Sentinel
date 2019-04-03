package com.alibaba.csp.sentinel.dashboard.discovery;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Apollo Client Management
 *
 * @author longqiang
 */
public class ApolloManagement {

    private static final Map<String, ApolloOpenApiClient> CLIENT_POOL = new ConcurrentHashMap<>(16);

    private ApolloManagement() { throw new IllegalStateException("Utility class"); }

    public static ApolloOpenApiClient getOrCreateClient(ApolloMachineInfo apolloMachineInfo){
        return getOrCreate(apolloMachineInfo.getPortalUrl(), apolloMachineInfo.getToken(), apolloMachineInfo.getConnectTimeout(), apolloMachineInfo.getReadTimeout());
    }

    public static ApolloOpenApiClient getOrCreate(String portalUrl, String token, int connectTimeout, int readTimeout) {
        return CLIENT_POOL.computeIfAbsent(portalUrl, e -> ApolloOpenApiClient.newBuilder()
                                                                              .withPortalUrl(portalUrl)
                                                                              .withConnectTimeout(connectTimeout)
                                                                              .withReadTimeout(readTimeout)
                                                                              .withToken(token)
                                                                              .build());
    }

}
