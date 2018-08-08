package com.alibaba.csp.sentinel.datasource.apollo;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.ConfigParser;
import com.alibaba.csp.sentinel.datasource.DataSource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * A {@link DataSource} with <a href="http://github.com/ctripcorp/apollo">Apollo</a> as its configuration source.
 * <br />
 * When the rule is changed in Apollo, it will take effect in real time.
 *
 * @author Jason Song
 */
public class ApolloDataSource<T> extends AbstractDataSource<String, T> {

  private final Config config;
  private final String flowRulesKey;
  private final String defaultFlowRuleValue;

  /**
   * Constructs the Apollo data source
   *
   * @param namespaceName        the namespace name in Apollo, should not be null or empty
   * @param flowRulesKey         the flow rules key in the namespace, should not be null or empty
   * @param defaultFlowRuleValue the default flow rules value when the flow rules key is not found or any error occurred
   * @param parser               the parser to transform string configuration to actual flow rules
   */
  public ApolloDataSource(String namespaceName, String flowRulesKey, String defaultFlowRuleValue,
      ConfigParser<String, T> parser) {
    super(parser);

    Preconditions.checkArgument(!Strings.isNullOrEmpty(namespaceName), "Namespace name could not be null or empty");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(flowRulesKey), "FlowRuleKey could not be null or empty!");

    this.flowRulesKey = flowRulesKey;
    this.defaultFlowRuleValue = defaultFlowRuleValue;

    this.config = ConfigService.getConfig(namespaceName);

    initialize();

    RecordLog.info(String.format("Initialized rule for namespace: %s, flow rules key: %s", namespaceName, flowRulesKey));
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
    config.addChangeListener(new ConfigChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        ConfigChange change = changeEvent.getChange(flowRulesKey);
        //change is never null because the listener will only notify for this key
        if (change != null) {
          RecordLog.info("[ApolloDataSource] Received config changes: " + change.toString());
        }
        loadAndUpdateRules();
      }
    }, Sets.newHashSet(flowRulesKey));
  }

  @Override
  public String readSource() throws Exception {
    return config.getProperty(flowRulesKey, defaultFlowRuleValue);
  }

  @Override
  public void close() throws Exception {
    // nothing to destroy
  }
}
