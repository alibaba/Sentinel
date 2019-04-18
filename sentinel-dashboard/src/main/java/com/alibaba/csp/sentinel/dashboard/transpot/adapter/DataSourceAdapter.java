package com.alibaba.csp.sentinel.dashboard.transpot.adapter;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.DataSourceMachineInfo;

import java.util.List;

/**
 * Public Method Adaptation for Third Party Data Sources
 *
 * @author longqiang
 */
public interface DataSourceAdapter<T extends RuleEntity> {

    /**
     * Get the key of the rule to be published
     *
     * @param dataSourceMachineInfo third-party data source machine information
     * @return key
     */
    String getKey(DataSourceMachineInfo dataSourceMachineInfo);

    /**
     * Convert to the corresponding rules
     *
     * @param app project name
     * @param ip project deployment IP
     * @param port project deployment port
     * @param value rules fetched from third-party data sources
     * @return java.util.List<T>
     */
    List<T> convert(String app, String ip, int port, String value);

    /**
     * Process rules let them store to third party data source
     *
     * @param rules rules to prepare for processing
     * @return java.lang.String
     */
    String processRules(List<T> rules);

}
