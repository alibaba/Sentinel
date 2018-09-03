package com.alibaba.csp.sentinel.datasource.jdbc;

import com.alibaba.csp.sentinel.datasource.AutoRefreshDataSource;
import com.alibaba.csp.sentinel.datasource.ConfigParser;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * jdbc DataSource
 *
 * <p>
 * the source data is stored in database, query by sql
 * </p>
 *
 * <p>
 * one table for app: sentinel_app
 * three tables for rules: sentinel_flow_rule, sentinel_degrade_rule, sentinel_system_rule
 * </p>
 *
 * @author cdfive
 * @date 2018-09-01
 */
public class JdbcDataSource<T> extends AutoRefreshDataSource<List<Map<String, Object>>, T> {

    /**sql: find app_id by appName, only enabled and not deleted*/
    private static final String FIND_APP_ID_SQL = "SELECT id FROM sentinel_app WHERE name=? AND enabled=1 AND deleted=0";
    /**sql: find rule list by app_id, only enabled and not deleted*/
    private static final String READ_SOURCE_SQL = "SELECT * FROM %s WHERE app_id=? AND enabled=1 AND deleted=0";

    /**rule type constant*/
    public static final String RULE_TYPE_FLOW = "flow";
    public static final String RULE_TYPE_DEGRADE = "degrade";
    public static final String RULE_TYPE_SYSTEM = "system";

    /**pull mode, pull data by query from db per 30 seconds, by default*/
    private static final long DEFAULT_RULE_REFRESH_SEC = 30;

    /**
     * key:rule type
     * value:rule table name
     */
    private static final Map<String, String> RULE_TYPE_TABLE_NAME_MAP = new HashMap<String, String>() {{
        put(RULE_TYPE_FLOW, "sentinel_flow_rule");
        put(RULE_TYPE_DEGRADE, "sentinel_degrade_rule");
        put(RULE_TYPE_SYSTEM, "sentinel_system_rule");
    }};

    /**app name*/
    private String appName;

    /**app id*/
    private Integer appId;

    /**rule type*/
    private String ruleType;

    /**Spring JdbcTemplate for execute sql query from db*/
    private JdbcTemplate jdbcTemplate;

    /**
     * constructor
     * @param jdbcTemplate Spring JdbcTemplate
     * @param appName app name
     * @param configParser rule parser
     */
    public JdbcDataSource(JdbcTemplate jdbcTemplate, String appName, ConfigParser<List<Map<String, Object>>, T> configParser) {
        this(jdbcTemplate, appName, configParser, DEFAULT_RULE_REFRESH_SEC);
    }

    /**
     * constructor
     * @param jdbcTemplate Spring JdbcTemplate
     * @param appName app name
     * @param configParser rule parser
     * @param ruleRefreshSec pull data by query from db per ruleRefreshSec seconds, 30 seconds by default
     */
    public JdbcDataSource(JdbcTemplate jdbcTemplate, String appName, ConfigParser<List<Map<String, Object>>, T> configParser, Long ruleRefreshSec) {
        super(configParser, ruleRefreshSec == null ? DEFAULT_RULE_REFRESH_SEC * 1000 : ruleRefreshSec * 1000);

        init(jdbcTemplate, appName, getRuleTypeByConfigParser(configParser));
    }

    /**
     * using JdbcDataSource(JdbcTemplate jdbcTemplate, String appName, ConfigParser<List<Map<String, Object>>, T> configParser instead
     *
     * because ruleType can get by configParser's class, see
     * String getRuleTypeByConfigParser(ConfigParser<List<Map<String, Object>>, T> configParser)
     */
    @Deprecated
    public JdbcDataSource(JdbcTemplate jdbcTemplate, String appName, String ruleType, ConfigParser<List<Map<String, Object>>, T> configParser) {
        super(configParser);

        init(jdbcTemplate, appName, ruleType);
    }

    /**
     * JdbcDataSource(JdbcTemplate jdbcTemplate, String appName, ConfigParser<List<Map<String, Object>>, T> configParser, Long ruleRefreshSec) instead
     *
     * because ruleType can get by configParser's class, see
     * String getRuleTypeByConfigParser(ConfigParser<List<Map<String, Object>>, T> configParser)
     */
    @Deprecated
    public JdbcDataSource(JdbcTemplate jdbcTemplate, String appName, String ruleType, ConfigParser<List<Map<String, Object>>, T> configParser, long recommendRefreshMs) {
        super(configParser, recommendRefreshMs);

        init(jdbcTemplate, appName, ruleType);
    }

    private void init(JdbcTemplate jdbcTemplate, String appName, String ruleType) {
        Assert.notNull(jdbcTemplate, "jdbcTemplate can't be null");
        Assert.notNull(appName, "appName can't be null");
        Assert.notNull(ruleType, "ruleType can't be null");

        Assert.isTrue(RULE_TYPE_TABLE_NAME_MAP.containsKey(ruleType), "ruleType invalid, must be flow|degrade|system");

        this.jdbcTemplate = jdbcTemplate;
        this.appName = appName;
        this.ruleType = ruleType;

        initAppId();

        firstLoad();
    }

    /**
     * query app_id from db by appName
     */
    private void initAppId() {
        Integer appId = jdbcTemplate.query(FIND_APP_ID_SQL, new ResultSetExtractor<Integer>() {
            @Override
            public Integer extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return null;
            }
        }, appName);

        Assert.notNull(appId, "can't find appId, appName=" + appName);

        this.appId = appId;
    }

    private void firstLoad() {
        try {
            T newValue = loadConfig();
            getProperty().updateValue(newValue);
        } catch (Exception e) {
            RecordLog.warn("loadConfig exception", e);
        }
    }

    /**
     * query sentinel rules from db by app_id
     */
    @Override
    public List<Map<String, Object>> readSource() throws Exception {
        String ruleTableName = RULE_TYPE_TABLE_NAME_MAP.get(ruleType);

        List<Map<String, Object>> list = jdbcTemplate.queryForList(String.format(READ_SOURCE_SQL, ruleTableName), appId);
        return list;
    }

    /**
     * get ruleType by configParse's class
     * @param configParser
     * @return
     */
    private String getRuleTypeByConfigParser(ConfigParser<List<Map<String, Object>>, T> configParser) {
        if (configParser instanceof JdbcFlowRuleParser) {
            return RULE_TYPE_FLOW;
        }

        if (configParser instanceof JdbcDegradeRuleParser) {
            return RULE_TYPE_DEGRADE;
        }

        if (configParser instanceof JdbcSystemRuleParser) {
            return RULE_TYPE_SYSTEM;
        }

        throw new IllegalArgumentException("configParser invalid");
    }

    /**
     * parse List<Map<String, Object> to List<FlowRule>
     */
    public static class JdbcFlowRuleParser implements ConfigParser<List<Map<String, Object>>, List<FlowRule>> {
        @Override
        public List<FlowRule> parse(List<Map<String, Object>> list) {
            if (list == null) {
                return null;
            }

            List<FlowRule> flowRules = new ArrayList<FlowRule>();
            for (Map<String, Object> map : list) {
                FlowRule flowRule = new FlowRule();
                flowRules.add(flowRule);

                flowRule.setResource(getMapStringVal(map, "resource"));
                flowRule.setLimitApp(getMapStringVal(map, "limit_app"));
                flowRule.setGrade(getMapIntVal(map, "grade"));
                flowRule.setCount(getMapDoubleVal(map, "_count"));
                flowRule.setStrategy(getMapIntVal(map, "strategy"));
                flowRule.setControlBehavior(getMapIntVal(map, "control_behavior"));
                flowRule.setWarmUpPeriodSec(getMapIntVal(map, "warm_up_period_sec"));
                flowRule.setMaxQueueingTimeMs(getMapIntVal(map, "max_queueing_time_ms"));
            }

            return flowRules;
        }
    }

    /**
     * parse List<Map<String, Object> to List<DegradeRule>
     */
    public static class JdbcDegradeRuleParser implements ConfigParser<List<Map<String, Object>>, List<DegradeRule>> {
        @Override
        public List<DegradeRule> parse(List<Map<String, Object>> list) {
            if (list == null) {
                return null;
            }

            List<DegradeRule> degradeRules = new ArrayList<DegradeRule>();
            for (Map<String, Object> map : list) {
                DegradeRule degradeRule = new DegradeRule();
                degradeRules.add(degradeRule);

                degradeRule.setResource(getMapStringVal(map, "resource"));
                degradeRule.setLimitApp(getMapStringVal(map, "limit_app"));
                degradeRule.setGrade(getMapIntVal(map, "grade"));
                degradeRule.setCount(getMapDoubleVal(map, "_count"));
            }

            return degradeRules;
        }
    }

    /**
     * parse List<Map<String, Object> to List<SystemRule>
     */
    public static class JdbcSystemRuleParser implements ConfigParser<List<Map<String, Object>>, List<SystemRule>> {
        @Override
        public List<SystemRule> parse(List<Map<String, Object>> list) {
            if (list == null) {
                return null;
            }

            List<SystemRule> systemRules = new ArrayList<SystemRule>();
            for (Map<String, Object> map : list) {
                SystemRule systemRule = new SystemRule();
                systemRules.add(systemRule);

                systemRule.setResource(getMapStringVal(map, "resource"));
                systemRule.setLimitApp(getMapStringVal(map, "limit_app"));
                systemRule.setHighestSystemLoad(getMapDoubleVal(map, "highest_system_load"));
                systemRule.setQps(getMapDoubleVal(map, "qps"));
                systemRule.setAvgRt(getMapLongVal(map, "avg_rt"));
                systemRule.setMaxThread(getMapLongVal(map, "max_thread"));
            }

            return systemRules;
        }
    }

    /**get string value of key from map, default value is null*/
    private static String getMapStringVal(Map<String, Object> map, String key) {
        return getMapVal(map, key, null, new ParseMapValCallback<String>() {
            @Override
            public String parseVal(String strVal) {
                return strVal;
            }
        });
    }

    /**get int value of key from map, default value is 0*/
    private static int getMapIntVal(Map<String, Object> map, String key) {
        return getMapVal(map, key, 0, new ParseMapValCallback<Integer>() {
            @Override
            public Integer parseVal(String strVal) {
                return Integer.parseInt(strVal);
            }
        });
    }

    /**get long value of key from map, default value is 0L*/
    private static long getMapLongVal(Map<String, Object> map, String key) {
        return getMapVal(map, key, 0L, new ParseMapValCallback<Long>() {
            @Override
            public Long parseVal(String strVal) {
                return Long.parseLong(strVal);
            }
        });
    }

    /**get double value of key from map, default value is 0D*/
    private static double getMapDoubleVal(Map<String, Object> map, String key) {
        return getMapVal(map, key, 0D, new ParseMapValCallback<Double>() {
            @Override
            public Double parseVal(String strVal) {
                return Double.parseDouble(strVal);
            }
        });
    }

    /**
     * get value of key from the map
     *
     * if map doesn't contains key or the value of key from the map is null, then return default value
     * else use ParseMapValCallback to parse the string value to the return type T
     * @param map map
     * @param key key
     * @param defVal default value
     * @param parseMapValCallback
     * @param <T> the type
     * @return result of type T
     */
    private static <T> T getMapVal(Map<String, Object> map, String key, T defVal, ParseMapValCallback<T> parseMapValCallback) {
        if (!map.containsKey(key)) {
            return defVal;
        }

        Object obj = map.get(key);
        if (obj == null) {
            return defVal;
        }

        return parseMapValCallback.parseVal(obj.toString());
    }

    /**
     * map value parse callback function
     * @param <T> type
     */
    private interface ParseMapValCallback<T> {
        /**
         * ParseMapValCallback
         * @param strVal the string value
         * @return the parsed result of type T
         */
        T parseVal(String strVal);
    }
}
