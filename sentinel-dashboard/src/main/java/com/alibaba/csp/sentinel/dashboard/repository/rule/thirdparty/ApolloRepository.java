package com.alibaba.csp.sentinel.dashboard.repository.rule.thirdparty;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloClientManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Providing Apollo data storage services
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloRepository<T extends RuleEntity> implements ThirdPartyRepository<T> {

    private Map<String, Map<Long, T>> portalRules = new ConcurrentHashMap<>(16);

    @Override
    public T save(T entity) {
        portalRules.computeIfAbsent(ApolloClientManagement.getOrCreatePortal(entity.getApp(), entity.getIp(),
                entity.getPort()), p -> new ConcurrentHashMap<>(32))
                .put(entity.getId(), entity);
        return entity;
    }

    @Override
    public T delete(T entity) {
        Map<Long, T> rulesMap = portalRules.get(ApolloClientManagement.getOrCreatePortal(entity.getApp(), entity.getIp(),
                                entity.getPort()));
        if (Objects.nonNull(rulesMap)) {
            rulesMap.remove(entity.getId());
        }
        return entity;
    }
    
    public List<T> findByPortal(ApolloMachineInfo apolloMachineInfo) {
        Map<Long, T> rulesMap = portalRules.get(ApolloClientManagement.computeIfAbsentPortal(apolloMachineInfo));
        if (rulesMap == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(rulesMap.values());
    }
}
