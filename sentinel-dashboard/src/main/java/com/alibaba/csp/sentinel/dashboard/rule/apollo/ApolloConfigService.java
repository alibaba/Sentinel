package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;

public class ApolloConfigService implements ConfigService {
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

    @Override
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

    @Override
    public String getConfigAndSignListener(String dataId, String group, long timeoutMs, Listener listener) {
        return null;
    }

    @Override
    public void addListener(String dataId, String group, Listener listener) {

    }

    @Override
    public boolean publishConfig(String dataId, String group, String content) {
        AssertUtil.notEmpty(dataId, "app name cannot be empty");
        String flowDataId = ApolloConfigUtil.getFlowDataId(dataId);
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(content);
        openItemDTO.setComment("config flowRule");
        openItemDTO.setDataChangeCreatedBy(username);
        client.createOrUpdateItem(dataId, env, cluster, namespace, openItemDTO);
        // Release configuration
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleaseComment("Modify or add flowRule");
        namespaceReleaseDTO.setReleasedBy(username);
        namespaceReleaseDTO.setReleaseTitle("config flowRule");
        client.publishNamespace(dataId, env, cluster, namespace, namespaceReleaseDTO);
        return true;
    }

    @Override
    public boolean removeConfig(String dataId, String group) {
        return false;
    }

    @Override
    public void removeListener(String dataId, String group, Listener listener) {

    }

    @Override
    public String getServerStatus() {
        return null;
    }
}
