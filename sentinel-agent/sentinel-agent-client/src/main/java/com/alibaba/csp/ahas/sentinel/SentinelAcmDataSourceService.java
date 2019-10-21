package com.alibaba.csp.ahas.sentinel;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.alibaba.csp.ahas.sentinel.cluster.ClusterAssignStateParser;
import com.alibaba.csp.ahas.sentinel.cluster.ClusterClientAssignConfigParser;
import com.alibaba.csp.ahas.sentinel.cluster.ClusterClientCommonConfigParser;
import com.alibaba.csp.ahas.sentinel.cluster.ClusterServerFlowConfigParser;
import com.alibaba.csp.ahas.sentinel.cluster.ClusterServerTransportConfigParser;
import com.alibaba.csp.ahas.sentinel.gateway.GatewayApiDefinitionParser;
import com.alibaba.csp.ahas.sentinel.gateway.GatewayFlowRuleParser;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerFlowConfig;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.acm.DecryptAcmWhiteDataSource;
import com.alibaba.csp.sentinel.datasource.acm.SentinelAcmConstants;
import com.alibaba.csp.sentinel.datasource.acm.parser.DegradeRuleConfigParser;
import com.alibaba.csp.sentinel.datasource.acm.parser.FlowRuleConfigParser;
import com.alibaba.csp.sentinel.datasource.acm.parser.SystemRuleConfigParser;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.function.Function;

import com.taobao.csp.ahas.service.api.client.ClientInfoService;
import com.taobao.diamond.exception.DiamondException;

import static com.alibaba.csp.sentinel.datasource.acm.SentinelAcmConstants.SENTINEL_GROUP_ID;

/**
 * @author Eric Zhao
 */
class SentinelAcmDataSourceService {

    private final ClientInfoService clientInfoService;
    private final SimpleDecryptor decryptor;

    public SentinelAcmDataSourceService(ClientInfoService clientInfoService) {
        this.clientInfoService = clientInfoService;
        this.decryptor = new SimpleDecryptor();
    }

    public void initAcmDataSource() throws DiamondException {
        String currentEndPoint = clientInfoService.getAcmEndpoint();
        String namespace = clientInfoService.getNamespace();
        RecordLog.info("acm endpoint: {0}", currentEndPoint);

        // -Daddress.server.domain=xxx
        System.setProperty("ahas.address.server.domain", currentEndPoint);

        String userId = clientInfoService.getUserId();
        String consoleTenantId = clientInfoService.getTid();

        String appName = AppNameUtil.getAppName();

        initClientRuleDs(userId, namespace, consoleTenantId, appName);

        // NOTE: we should first register the cluster rule data source supplier
        // then init cluster related data source, or the cluster rules WILL BE LOST!
        initClusterRuleDsSupplier(userId, namespace, consoleTenantId);
        initSentinelClusterDs(userId, namespace, consoleTenantId, appName);

        // Register data source related to gateway adapter.
        if (isGatewayAppType()) {
            initApiGatewayDs(userId, namespace, consoleTenantId, appName);
        }
    }

    private void initClientRuleDs(final String userId, final String namespace, final String consoleTenantId,
                                  String appName) throws DiamondException {

        String flowDataId = SentinelAcmConstants.formFlowRuleDataId(userId, namespace, appName);
        FlowRuleManager.register2Property(new DecryptAcmWhiteDataSource<>(consoleTenantId, flowDataId,
            SENTINEL_GROUP_ID, new FlowRuleConfigParser(), decryptor).getProperty());

        String degradeDataId = SentinelAcmConstants.formDegradeRuleDataId(userId, namespace, appName);
        DegradeRuleManager.register2Property(
            new DecryptAcmWhiteDataSource<>(consoleTenantId, degradeDataId, SENTINEL_GROUP_ID,
                new DegradeRuleConfigParser(), decryptor).getProperty());

        String systemDataId = SentinelAcmConstants.formSystemRuleDataId(userId, namespace, appName);
        SystemRuleManager.register2Property(
            new DecryptAcmWhiteDataSource<>(consoleTenantId, systemDataId, SENTINEL_GROUP_ID,
                new SystemRuleConfigParser(), decryptor).getProperty());

        //final String paramFlowDataId = SentinelAcmConstants.formParamFlowRuleDataId(userId, namespace, appName);
        //ParamFlowRuleManager.register2Property(new DecryptAcmWhiteDataSource<>(consoleTenantId, paramFlowDataId,
        //    SENTINEL_GROUP_ID, new ParamFlowRuleParser()).getProperty());

        String log = String.format("acm, flowDataId: %s, degradeDataId: %s, systemDataId: %s, group: %s, tid: %s",
            flowDataId, degradeDataId, systemDataId, SENTINEL_GROUP_ID, consoleTenantId);
        RecordLog.info(log);
    }

    private void initClusterRuleDsSupplier(final String userId, final String acmNamespace,
                                           final String consoleTenantId) {
        ClusterFlowRuleManager.setPropertySupplier(
            new Function<String, SentinelProperty<List<FlowRule>>>() {
                @Override
                public SentinelProperty<List<FlowRule>> apply(String app) {
                    String dataId = SentinelAcmConstants.formFlowRuleDataId(userId, acmNamespace, app);
                    try {
                        return new DecryptAcmWhiteDataSource<>(consoleTenantId, dataId,
                            SENTINEL_GROUP_ID, new FlowRuleConfigParser(), decryptor).getProperty();
                    } catch (Exception ex) {
                        RecordLog.warn(
                            "[SentinelAcmDataSourceService] Error when initializing cluster flow rule data source", ex);
                        throw new IllegalStateException(ex);
                    }
                }
            });

        //ClusterParamFlowRuleManager.setPropertySupplier(
        //    new Function<String, SentinelProperty<List<ParamFlowRule>>>() {
        //        @Override
        //        public SentinelProperty<List<ParamFlowRule>> apply(String app) {
        //            String dataId = SentinelAcmConstants.formParamFlowRuleDataId(userId, acmNamespace, app);
        //            try {
        //                return new DecryptAcmWhiteDataSource<>(consoleTenantId, dataId, SENTINEL_GROUP_ID,
        //                    new ParamFlowRuleParser()).getProperty();
        //            } catch (Exception ex) {
        //                RecordLog.warn("Error when initializing cluster param flow rule data source", ex);
        //                throw new IllegalStateException(ex);
        //            }
        //        }
        //    });
    }

    private void initSentinelClusterDs(String userId, String acmNamespace, String consoleTenantId, String appName)
        throws DiamondException {
        String clusterMapDataId = SentinelAcmConstants.formClusterAssignMapDataId(userId, acmNamespace, appName);

        // Init token server related data source.
        DecryptAcmWhiteDataSource<ServerTransportConfig> serverTransportDs = new DecryptAcmWhiteDataSource<>(
            consoleTenantId,
            clusterMapDataId,
            SENTINEL_GROUP_ID, new ClusterServerTransportConfigParser(), decryptor);
        ClusterServerConfigManager.registerServerTransportProperty(serverTransportDs.getProperty());
        // TODO: a temporary solution for the leak of flow registry.
        final ClusterServerFlowConfigParser serverFlowConfigParser = new ClusterServerFlowConfigParser();
        DecryptAcmWhiteDataSource<ServerFlowConfig> serverFlowDs = new DecryptAcmWhiteDataSource<>(consoleTenantId,
            clusterMapDataId,
            SENTINEL_GROUP_ID, new Converter<String, ServerFlowConfig>() {
            @Override
            public ServerFlowConfig convert(String source) {
                ServerFlowConfig config = serverFlowConfigParser.convert(source);
                if (config != null) {
                    ClusterServerConfigManager.loadGlobalFlowConfig(config);
                }
                return config;
            }
        }, decryptor);

        // Init token client related data source.
        DecryptAcmWhiteDataSource<ClusterClientAssignConfig> clientAssignDs = new DecryptAcmWhiteDataSource<>(
            consoleTenantId,
            clusterMapDataId,
            SENTINEL_GROUP_ID, new ClusterClientAssignConfigParser(), decryptor);
        ClusterClientConfigManager.registerServerAssignProperty(clientAssignDs.getProperty());

        String clientConfigDataId = SentinelAcmConstants.formClusterClientConfigDataId(userId, acmNamespace, appName);
        DecryptAcmWhiteDataSource<ClusterClientConfig> clientConfigDs = new DecryptAcmWhiteDataSource<>(consoleTenantId,
            clientConfigDataId,
            SENTINEL_GROUP_ID, new ClusterClientCommonConfigParser(), decryptor);
        ClusterClientConfigManager.registerClientConfigProperty(clientConfigDs.getProperty());

        // Init cluster state property for extracting mode from cluster map data source.
        DecryptAcmWhiteDataSource<Integer> clusterStateDs = new DecryptAcmWhiteDataSource<>(consoleTenantId,
            clusterMapDataId,
            SENTINEL_GROUP_ID, new ClusterAssignStateParser(), decryptor);
        ClusterStateManager.registerProperty(clusterStateDs.getProperty());

        // Load cluster server namespace set (only current app)
        ClusterServerConfigManager.loadServerNamespaceSet(Collections.singleton(appName));
    }

    private void initApiGatewayDs(String userId, String acmNamespace, String consoleTenantId, String appName)
        throws DiamondException {
        RecordLog.info("[SentinelAcmDsService] Initializing data source for API gateway integration");

        String gatewayFlowRuleDataId = SentinelAcmConstants.formGatewayFlowRuleDataId(userId, acmNamespace, appName);

        // Init gateway flow rule data source.
        DecryptAcmWhiteDataSource<Set<GatewayFlowRule>> gatewayFlowRuleDs = new DecryptAcmWhiteDataSource<>(
            consoleTenantId,
            gatewayFlowRuleDataId,
            SENTINEL_GROUP_ID, new GatewayFlowRuleParser(), decryptor);
        GatewayRuleManager.register2Property(gatewayFlowRuleDs.getProperty());

        String gatewayApiDefDataId = SentinelAcmConstants.formGatewayApiDefinitionDataId(userId, acmNamespace, appName);

        // Init gateway API definition data source.
        DecryptAcmWhiteDataSource<Set<ApiDefinition>> gatewayApiDefinitionDs = new DecryptAcmWhiteDataSource<>(
            consoleTenantId,
            gatewayApiDefDataId,
            SENTINEL_GROUP_ID, new GatewayApiDefinitionParser(), decryptor);
        GatewayApiDefinitionManager.register2Property(gatewayApiDefinitionDs.getProperty());
    }

    private boolean isGatewayAppType() {
        return AhasSentinelConstants.GATEWAY_APP_TYPES.contains(com.taobao.csp.ahas.service.util.AppNameUtil.getAppType());
    }
}
