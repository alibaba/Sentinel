package com.alibaba.csp.sentinel.datasource.apollo;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;

import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import java.util.List;

/**
 * A read-only {@code DataSource} with <a href="http://github.com/ctripcorp/apollo">Apollo</a> as its configuration
 * source.
 * <br />
 * When the rule is changed in Apollo, it will take effect in real time.
 *
 * @author Jason Song
 * @author Haojun Ren
 */
public class ApolloDataSource<T> extends AbstractDataSource<String, T> {

    private final Config config;
    private final String ruleKey;
    private final String defaultRuleValue;

    private ConfigChangeListener configChangeListener;

    private int instanceSize = 1;

    /**
     * Constructs the Apollo data source
     *
     * @param namespaceName        the namespace name in Apollo, should not be null or empty
     * @param ruleKey              the rule key in the namespace, should not be null or empty
     * @param defaultRuleValue     the default rule value when the ruleKey is not found or any error
     *                             occurred
     * @param parser               the parser to transform string configuration to actual flow rules
     */
    public ApolloDataSource(String namespaceName, String ruleKey, String defaultRuleValue,
                            Converter<String, T> parser) {
        super(parser);

        Preconditions.checkArgument(!Strings.isNullOrEmpty(namespaceName), "Namespace name could not be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ruleKey), "RuleKey could not be null or empty!");

        this.ruleKey = ruleKey;
        this.defaultRuleValue = defaultRuleValue;

        this.config = ConfigService.getConfig(namespaceName);

        initialize();

        RecordLog.info("Initialized rule for namespace: {}, rule key: {}", namespaceName, ruleKey);
    }

    /**
     * Constructs the Apollo data source
     *
     * @param namespaceName        the namespace name in Apollo, should not be null or empty
     * @param ruleKey              the rule key in the namespace, should not be null or empty
     * @param defaultRuleValue     the default rule value when the ruleKey is not found or any error
     *                             occurred
     * @param parser               the parser to transform string configuration to actual flow rules
     */
    public ApolloDataSource(String namespaceName, String ruleKey, String defaultRuleValue, int instanceSize,
                            Converter<String, T> parser) {
        super(parser);

        Preconditions.checkArgument(!Strings.isNullOrEmpty(namespaceName), "Namespace name could not be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ruleKey), "RuleKey could not be null or empty!");

        this.ruleKey = ruleKey;
        this.defaultRuleValue = defaultRuleValue;

        this.config = ConfigService.getConfig(namespaceName);
        this.instanceSize = instanceSize;
        initializeClusterLocal();

        RecordLog.info("Initialized rule for namespace: {}, rule key: {}", namespaceName, ruleKey);
    }

    private void initialize() {
        initializeConfigChangeListener();
        loadAndUpdateRules();
    }

    private void loadAndUpdateRules() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.warn("[ApolloDataSource] WARN: rule config is null, you may have to check your data source");
            }
            getProperty().updateValue(newValue);
        } catch (Throwable ex) {
            RecordLog.warn("[ApolloDataSource] Error when loading rule config", ex);
        }
    }

    private void initializeConfigChangeListener() {
        configChangeListener = new ConfigChangeListener() {
            @Override
            public void onChange(ConfigChangeEvent changeEvent) {
                ConfigChange change = changeEvent.getChange(ruleKey);
                //change is never null because the listener will only notify for this key
                if (change != null) {
                    RecordLog.info("[ApolloDataSource] Received config changes: {}", change);
                }
                loadAndUpdateRules();
            }
        };
        config.addChangeListener(configChangeListener, Sets.newHashSet(ruleKey));
    }


    private void initializeClusterLocal() {
        initializeClusterLocalConfigChangeListener();
        loadAndUpdateClusterLocalRules();
    }

    private void loadAndUpdateClusterLocalRules() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.warn("[ApolloDataSource] WARN: rule config is null, you may have to check your data source");
            }

            List<FlowRule> list = (List<FlowRule>) newValue;
            for (FlowRule rule : list) {
                if (isClusterLocalRule(rule)) {
                    rule.setCount(Math.rint(rule.getCount() / instanceSize));
                }
            }

            getProperty().updateValue(newValue);
        } catch (Throwable ex) {
            RecordLog.warn("[ApolloDataSource] Error when loading rule config", ex);
        }
    }

    private void initializeClusterLocalConfigChangeListener() {
        configChangeListener = new ConfigChangeListener() {
            @Override
            public void onChange(ConfigChangeEvent changeEvent) {
                ConfigChange change = changeEvent.getChange(ruleKey);
                //change is never null because the listener will only notify for this key
                if (change != null) {
                    RecordLog.info("[ApolloDataSource] Received config changes: {}", change);
                }
                loadAndUpdateClusterLocalRules();
            }
        };
        config.addChangeListener(configChangeListener, Sets.newHashSet(ruleKey));
    }

    public static boolean isClusterLocalRule(FlowRule rule) {
        ClusterFlowConfig clusterConfig = rule.getClusterConfig();
        if (clusterConfig != null && clusterConfig.getThresholdType() == ClusterRuleConstant.FLOW_THRESHOLD_LOCAL) {
            return true;
        }
        return false;
    }

    @Override
    public String readSource() throws Exception {
        return config.getProperty(ruleKey, defaultRuleValue);
    }

    @Override
    public void close() throws Exception {
        config.removeChangeListener(configChangeListener);
    }
}
