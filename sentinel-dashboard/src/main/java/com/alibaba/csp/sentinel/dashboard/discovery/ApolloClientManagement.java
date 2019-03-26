package com.alibaba.csp.sentinel.dashboard.discovery;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Apollo Client Management
 *
 * @author longqiang
 */
public class ApolloClientManagement {

    private static final Map<String, ApolloOpenApiClient> pool = new ConcurrentHashMap<>(16);

    public static void createClient(ApolloMachineInfo apolloMachineInfo){
        createClient(apolloMachineInfo.getPortalUrl(), apolloMachineInfo.getToken(), apolloMachineInfo.getConnectTimeout(), apolloMachineInfo.getReadTimeout());
    }

    public static void createClient(String portalUrl, String token, int connectTimeout, int readTimeout) {
        ApolloOpenApiClient apolloOpenApiClient = getClient(portalUrl);
        if (apolloOpenApiClient == null) {
            pool.put(portalUrl, ApolloOpenApiClient.newBuilder()
                                                    .withPortalUrl(portalUrl)
                                                    .withConnectTimeout(connectTimeout)
                                                    .withReadTimeout(readTimeout)
                                                    .withToken(token)
                                                    .build());
        }
    }

    public static ApolloOpenApiClient getClient(String portalUrl) {
        return pool.get(portalUrl);
    }

}
