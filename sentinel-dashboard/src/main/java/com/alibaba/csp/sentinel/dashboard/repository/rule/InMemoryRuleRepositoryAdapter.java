/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.repository.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author leyou
 */
public abstract class InMemoryRuleRepositoryAdapter<T extends RuleEntity> implements RuleRepository<T, Long> {

    /**
     * {@code <machine, <id, rule>>}
     */
    private Map<MachineInfo, Map<Long, T>> machineRules = new ConcurrentHashMap<>(16);
    private Map<Long, T> allRules = new ConcurrentHashMap<>(16);

    private Map<String, Map<Long, T>> appRules = new ConcurrentHashMap<>(16);
    private static final int MAX_RULES_SIZE = 10000;

    @Override
    public T save(T entity) {
        if (entity.getId() == null) {
            entity.setId(nextId());
        }
        T processedEntity = preProcess(entity);
        if (processedEntity != null) {
            allRules.put(processedEntity.getId(), processedEntity);
            machineRules.computeIfAbsent(MachineInfo.of(processedEntity.getApp(), processedEntity.getIp(),
                processedEntity.getPort()), e -> new ConcurrentHashMap<>(32))
                .put(processedEntity.getId(), processedEntity);
            appRules.computeIfAbsent(processedEntity.getApp(), v -> new ConcurrentHashMap<>(32))
                .put(processedEntity.getId(), processedEntity);
        }

        return processedEntity;
    }

    @Override
    public List<T> saveAll(List<T> rules) {
        // TODO: check here.
        if (rules == null || rules.isEmpty()) {
            return Lists.newArrayList();
        }
        List<T> savedRules = new ArrayList<>(rules.size());
        for (T rule : rules) {
            savedRules.add(save(rule));
        }
        return savedRules;
    }

    @Override
    public T delete(Long id) {
        T entity = allRules.remove(id);
        if (entity != null) {
            if (appRules.get(entity.getApp()) != null) {
                appRules.get(entity.getApp()).remove(id);
            }
            machineRules.get(MachineInfo.of(entity.getApp(), entity.getIp(), entity.getPort())).remove(id);
        }
        return entity;
    }

    @Override
    public T findById(Long id) {
        return allRules.get(id);
    }

    @Override
    public List<T> findAllByMachine(MachineInfo machineInfo) {
        Map<Long, T> entities = machineRules.get(machineInfo);
        if (entities == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(entities.values());
    }

    @Override
    public List<T> findAllByApp(String appName) {
        AssertUtil.notEmpty(appName, "appName cannot be empty");
        Map<Long, T> entities = appRules.get(appName);
        if (entities == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(entities.values());
    }

    @Override
    public void deleteByMachine(MachineInfo machineInfo) {
        Map<Long, T> removedMachineRules = machineRules.remove(machineInfo);
        if (CollectionUtils.isEmpty(removedMachineRules)) {
            return;
        }
        Map<Long, T> entities = appRules.get(machineInfo.getApp());
        removedMachineRules.keySet().forEach(id -> {
            allRules.remove(id);
            entities.remove(id);
        });
    }

    @Override
    public void deleteByApp(AppInfo appInfo) {
        Map<Long, T> removedAppRules = appRules.remove(appInfo.getApp());
        if (CollectionUtils.isEmpty(removedAppRules)) {
            return;
        }
        Set<MachineInfo> machines = appInfo.getMachines();
        if (!CollectionUtils.isEmpty(machines)) {
            machines.forEach(machineRules::remove);
        }
        removedAppRules.keySet().forEach(allRules::remove);
    }

    protected T preProcess(T entity) {
        return entity;
    }

    /**
     * Get next unused id.
     *
     * @return next unused id
     */
    protected abstract long nextId();
}
