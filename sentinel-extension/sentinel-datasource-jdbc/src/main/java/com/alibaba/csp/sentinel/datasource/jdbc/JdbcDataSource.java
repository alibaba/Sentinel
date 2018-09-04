package com.alibaba.csp.sentinel.datasource.jdbc;

import com.alibaba.csp.sentinel.datasource.AutoRefreshDataSource;
import com.alibaba.csp.sentinel.datasource.ConfigParser;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * jdbc implement of DataSource
 *
 * @see com.alibaba.csp.sentinel.datasource.DataSource
 * @see com.alibaba.csp.sentinel.datasource.AutoRefreshDataSource
 *
 * <p>
 *
 * </p>
 *
 * @author cdfive
 * @date 2018-09-01
 */
public class JdbcDataSource<T> extends AutoRefreshDataSource<List<Map<String, Object>>, T> {

    /**
     * the default time interval to refresh sentinel rules, 30 seconds
     *
     * <p>
     * pull mode, pull data by sql query from database
     * </p>
     *
     * <p>
     * for not query database frequently, the default is 30s<br />
     * for short numbers, use second instead of millisecond
     * </p>
     */
    protected static final Long DEFAULT_RULE_REFRESH_SEC = 30L;

    /**
     * javax.sql.DataSource Object, which related to user's database
     */
    private DataSource dbDataSource;

    /**
     * sql which query <b>effective</b> sentinel rules from databse
     */
    private String sql;

    /**
     * sql paramters
     */
    private Object[] sqlParameters;


    /**
     * constructor
     * for extends use
     * @param dbDataSource javax.sql.DataSource Object, which related to user's database
     * @param configParser rule parser
     */
    public JdbcDataSource(DataSource dbDataSource, ConfigParser<List<Map<String, Object>>, T> configParser) {
        this(dbDataSource, configParser, DEFAULT_RULE_REFRESH_SEC);
    }

    /**
     * constructor
     * for extends use
     * @param dbDataSource javax.sql.DataSource Object, which related to user's database
     * @param configParser rule parser
     * @param ruleRefreshSec the time interval to refresh sentinel rules, in second
     */
    public JdbcDataSource(DataSource dbDataSource, ConfigParser<List<Map<String, Object>>, T> configParser, Long ruleRefreshSec) {
        super(configParser, ruleRefreshSec * 1000);

        this.dbDataSource = dbDataSource;
    }

    /**
     * constructor
     * @param dbDataSource javax.sql.DataSource Object, which related to user's database
     * @param sql sql which query <b>effective</b> sentinel rules from databse
     * @param configParser rule parser
     */
    public JdbcDataSource(DataSource dbDataSource, String sql, ConfigParser<List<Map<String, Object>>, T> configParser) {
        this(dbDataSource, sql, null, configParser, DEFAULT_RULE_REFRESH_SEC);
    }

    /**
     * constructor
     * @param dbDataSource javax.sql.DataSource Object, which related to user's database
     * @param sql sql which query <b>effective</b> sentinel rules from databse
     * @param sqlParameters sql parameters
     * @param configParser rule parser
     */
    public JdbcDataSource(DataSource dbDataSource, String sql, Object[] sqlParameters, ConfigParser<List<Map<String, Object>>, T> configParser) {
        this(dbDataSource, sql, sqlParameters, configParser, DEFAULT_RULE_REFRESH_SEC);
    }

    /**
     * constructor
     * @param dbDataSource javax.sql.DataSource Object, which related to user's database
     * @param sql sql which query <b>effective</b> sentinel rules from databse
     * @param sqlParameters sql parameters
     * @param configParser rule parser
     * @param ruleRefreshSec the time interval to refresh sentinel rules, in second
     */
    public JdbcDataSource(DataSource dbDataSource, String sql, Object[] sqlParameters, ConfigParser<List<Map<String, Object>>, T> configParser, Long ruleRefreshSec) {
        super(configParser, ruleRefreshSec * 1000);

        checkNotNull(dbDataSource, "javax.sql.DataSource dbDataSource can't be null");
        checkNotEmpty(sql, "sql can't be null or empty");

        this.dbDataSource = dbDataSource;
        this.sql = sql;
        this.sqlParameters = sqlParameters;

        firstLoad();
    }

    /**
     * load the data from source firstly
     */
    protected void firstLoad() {
        try {
            T newValue = loadConfig();
            getProperty().updateValue(newValue);
        } catch (Exception e) {
            RecordLog.warn("loadConfig exception", e);// need to write to app log?
        }
    }

    /**
     * query sentinel rules from db by app_id
     */
    @Override
    public List<Map<String, Object>> readSource() throws Exception {
        List<Map<String, Object>> list = findListMapBySql();
        return list;
    }

    /**
     * query sql from databse, using dbDataSource,sql,sqlParameters
     *
     * <P>
     *  Note:
     *  Map's key is the column name of select, if has alias name, alias name prefered
     *
     *  eg: select grade,limit_app as limitApp,... from flow_rule_table
     *
     *  the keys is grade,limitApp
     * </P>
     * @return List<Map<String, Object>>
     * @throws SQLException
     */
    private List<Map<String, Object>> findListMapBySql() throws SQLException {
        Connection connection = dbDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (sqlParameters != null) {
            for (int i = 0; i < sqlParameters.length; i++) {
                preparedStatement.setObject(i + 1, sqlParameters[i]);
            }
        }

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<String, Object>();
            list.add(map);
            for (int i = 1; i <= columnCount; i++) {
                String columnName = resultSetMetaData.getColumnLabel(i);// get column alias name as key
                if (columnName == null || columnName.isEmpty()) {
                    columnName = resultSetMetaData.getColumnName(i);// get column name as key
                }
                map.put(columnName, resultSet.getObject(i));
            }
        }
        return list;
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

    /**
     * check null object
     */
    private void checkNotNull(Object obj, String msg) {
        if (obj == null) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * check empty string
     */
    private void checkNotEmpty(String str, String msg) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**getters and setters*/
    public DataSource getDbDataSource() {
        return dbDataSource;
    }

    public void setDbDataSource(DataSource dbDataSource) {
        this.dbDataSource = dbDataSource;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getSqlParameters() {
        return sqlParameters;
    }

    public void setSqlParameters(Object[] sqlParameters) {
        this.sqlParameters = sqlParameters;
    }
}
