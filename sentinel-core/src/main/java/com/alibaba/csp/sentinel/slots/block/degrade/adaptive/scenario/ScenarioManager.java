package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario;

import com.alibaba.csp.sentinel.log.RecordLog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Management of scenarios and threshold configuration.
 *
 * @author ylnxwlp
 */
public class ScenarioManager {

    private static final Map<Scenario.SystemScenario, Scenario> SCENARIO_MAP = new ConcurrentHashMap<>();

    private static final Map<String, Map<Scenario.SystemScenario, ScenarioConfig>> CONFIG_MAP = new ConcurrentHashMap<>();

    static {
        reset();
    }

    public static void reset() {
        SCENARIO_MAP.clear();
        CONFIG_MAP.clear();
        registerScenario(new OverLoadScenario());
    }

    public static void registerScenario(Scenario scenario) {
        if (scenario == null) {
            RecordLog.error("[ScenarioManager] Scenario strategy cannot be null");
            return;
        }
        SCENARIO_MAP.put(scenario.getScenarioType(), scenario);
    }

    public static Scenario getScenario(Scenario.SystemScenario scenario) {
        if (scenario == null) {
            RecordLog.error("Scenario cannot be null");
            return null;
        }
        return SCENARIO_MAP.get(scenario);
    }

    public static ScenarioConfig getConfig(String resourceName, Scenario.SystemScenario scenarioType) {
        if (resourceName == null || scenarioType == null) {
            RecordLog.warn("[ScenarioManager] resourceName or scenarioType is null, use default config");
            return new DefaultScenarioConfig();
        }

        Map<Scenario.SystemScenario, ScenarioConfig> scenarioConfigMap = CONFIG_MAP.get(resourceName);
        if (scenarioConfigMap == null) {
            scenarioConfigMap = new ConcurrentHashMap<>();
            Map<Scenario.SystemScenario, ScenarioConfig> existing = CONFIG_MAP.putIfAbsent(resourceName, scenarioConfigMap);
            if (existing != null) {
                scenarioConfigMap = existing;
            }
        }

        ScenarioConfig config = scenarioConfigMap.get(scenarioType);
        if (config == null) {
            switch (scenarioType) {
                case OVER_LOAD:
                    config = new OverloadScenarioConfig(resourceName);
                    break;
                // TODO Integrate more scenarios
                default:
                    RecordLog.warn("[ScenarioManager] Unsupported scenarioType: {}", scenarioType);
                    return new DefaultScenarioConfig();
            }
            ScenarioConfig existingConfig = scenarioConfigMap.putIfAbsent(scenarioType, config);
            if (existingConfig != null) {
                config = existingConfig;
            }
        }
        return config;
    }

    public static Map<Scenario.SystemScenario, Scenario> getAllScenarios() {
        return new HashMap<>(SCENARIO_MAP);
    }

    public static Map<String, Map<Scenario.SystemScenario, ScenarioConfig>> getAllConfigs() {
        Map<String, Map<Scenario.SystemScenario, ScenarioConfig>> outerCopy =
                new HashMap<>(CONFIG_MAP.size());
        for (Map.Entry<String, Map<Scenario.SystemScenario, ScenarioConfig>> e : CONFIG_MAP.entrySet()) {
            Map<Scenario.SystemScenario, ScenarioConfig> inner = e.getValue();
            Map<Scenario.SystemScenario, ScenarioConfig> innerCopy =
                    (inner == null) ? new HashMap<>() : new HashMap<>(inner);
            outerCopy.put(e.getKey(), innerCopy);
        }
        return outerCopy;
    }
}
