package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;

public class ApolloConfigService extends ConfigService {
    private String token;
    private String username;
    private String env;
    private String cluster;
    private String namespace;
    private String serverAddr;

    private ApolloOpenApiClient client;

    public ApolloConfigService(String token, String username, String env, String cluster, String namespace, String serverAddr) {
        this.token = token;
        this.env = env;
        this.username = username;
        this.namespace = namespace;
        this.cluster = cluster;
        this.serverAddr = serverAddr;
        client = ApolloOpenApiClient.newBuilder()
                .withPortalUrl(serverAddr)
                .withToken(token)
                .build();
    }

    public String getConfig(String appId, String group, long timeoutMs){
        String flowDataId = ApolloConfigUtil.getDataId(appId,group);
        OpenNamespaceDTO openNamespaceDTO = client.getNamespace(flowDataId, env, cluster, namespace);
        return openNamespaceDTO
                .getItems()
                .stream()
                .map(OpenItemDTO::getValue)
                .findFirst()
                .orElse("");
    }

    public void publishConfig(String dataId, String group, String content) {

    }
}
