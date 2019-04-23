package com.alibaba.csp.sentinel.dashboard.transpot.fetch.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.management.NacosMachineInfo;
import com.alibaba.csp.sentinel.dashboard.transpot.fetch.FetchAdapter;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nacos fetch rules adapter
 *
 * @author longqiang
 */
public abstract class NacosFetchAdapter<T extends RuleEntity> extends FetchAdapter<T, ConfigService, NacosMachineInfo> {

    private static final Logger logger = LoggerFactory.getLogger(NacosFetchAdapter.class);

    @Override
    protected String getConfig(ConfigService configService, NacosMachineInfo nacosMachineInfo) {
        try {
            return configService.getConfig(getKey(nacosMachineInfo), nacosMachineInfo.getGroup(), nacosMachineInfo.getTimeoutMs());
        } catch (NacosException e) {
            logger.error("[Nacos] fetch rules from nacos failed, rules key:{}, reason:{}", getKey(nacosMachineInfo), e);
        }
        return null;
    }

}
