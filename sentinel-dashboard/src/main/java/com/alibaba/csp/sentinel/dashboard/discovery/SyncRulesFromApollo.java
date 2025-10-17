package com.alibaba.csp.sentinel.dashboard.discovery;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author matthew
 * @date 2021-12-1 17:54
 * @description
 */
@Component
public class SyncRulesFromApollo {

    @Autowired
    private AppManagement appManagement;
    @Autowired
    private SentinelApiClient sentinelApiClient;
    @Autowired
    private InMemoryRuleRepositoryAdapter<FlowRuleEntity> repository;
    @Autowired
    @Qualifier("flowRuleApolloProvider")
    private DynamicRuleProvider<List<FlowRuleEntity>> ruleProvider;
    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private ScheduledExecutorService fetchScheduleService = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("sentinel-dashboard-metrics-fetch-task", true));

    private final long intervalSecond = 5;

    int cores = Runtime.getRuntime().availableProcessors() * 2;
    long keepAliveTime = 0;
    int queueSize = 2048;
    RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();

    private ExecutorService syncApolloRuleService;
    private static Logger logger = LoggerFactory.getLogger(SyncRulesFromApollo.class);

    public SyncRulesFromApollo(){
        syncApolloRuleService = new ThreadPoolExecutor(cores, cores,
                keepAliveTime, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(queueSize),
                new NamedThreadFactory("sentinel-dashboard-metrics-fetchService", true), handler);
        start();
    }




    private void start() {
        fetchScheduleService.scheduleAtFixedRate(() -> {
            try {
                fetchAllApp();
            } catch (Exception e) {
                logger.info("fetchAllApp error:", e);
            }
        }, 10, intervalSecond, TimeUnit.SECONDS);
    }


    private void fetchAllApp() {
        List<String> apps = appManagement.getAppNames();
        if (apps == null) {
            return;
        }
        for (final String app : apps) {
            syncApolloRuleService.submit(() -> {
                try {
                    syncRules(app);
                } catch (Exception e) {
                    logger.error("fetchAppllo error", e);
                }
            });
        }
    }

    private void syncRules(String app) {
        try {
            List<FlowRuleEntity> rules = ruleProvider.getRules(app);
            if (rules != null && !rules.isEmpty()) {
                for (FlowRuleEntity entity : rules) {
                    entity.setApp(app);
                    if (entity.getClusterConfig() != null && entity.getClusterConfig().getFlowId() != null) {
                        entity.setId(entity.getClusterConfig().getFlowId());
                    }
                }
                rules = repository.saveAll(rules);

                AppInfo appInfo = appManagement.getDetailApp(app);
                if (appInfo != null) {
                    List<MachineInfo> list = new ArrayList<>(appInfo.getMachines());
                    Collections.sort(list, Comparator.comparing(MachineInfo::getApp).thenComparing(MachineInfo::getIp).thenComparingInt(MachineInfo::getPort));
                    for (MachineInfo machineInfo : list) {
                        sentinelApiClient.setFlowRuleOfMachineAsync(app, machineInfo.getIp(), machineInfo.getPort(), rules);
                    }
                }
            }

        } catch (Throwable throwable) {
            logger.error("Error when querying flow rules", throwable);
        }
    }
}
