package com.alibaba.csp.sentinel.dashboard.client;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class InfluxDbUtils {
    private static Logger logger = LoggerFactory.getLogger(InfluxDbUtils.class);

    @Autowired
    InfluxDB influxDB;

    String database;

    public InfluxDbUtils(String database) {
        this.database = database;
    }

    public <T> T process(String database, InfluxDBCallback callback) {
        T t = null;
        try {
            t = callback.doCallBack(database, influxDB);
        } catch (Exception e) {
            logger.error("[process exception]", e);
        } finally {
            if (influxDB != null) {
                try {
                    influxDB.close();
                } catch (Exception e) {
                    logger.error("[influxDB.close exception]", e);
                }
            }
        }

        return t;
    }

    public void insert(String database, InfluxDBInsertCallback influxDBInsertCallback) {
        process(database, new InfluxDBCallback() {
            @Override
            public <T> T doCallBack(String database, InfluxDB influxDB) {
                influxDBInsertCallback.doCallBack(database, influxDB);
                return null;
            }
        });

    }
    public QueryResult query(String database, InfluxDBQueryCallback influxDBQueryCallback) {
        return process(database, new InfluxDBCallback() {
            @Override
            public <T> T doCallBack(String database, InfluxDB influxDB) {
                QueryResult queryResult = influxDBQueryCallback.doCallBack(database, influxDB);
                return (T) queryResult;
            }
        });
    }
    public <T> List<T> queryList(String database, String sql, Map<String, Object> paramMap, Class<T> clasz) {
        QueryResult queryResult = query(database, new InfluxDBQueryCallback() {
            @Override
            public QueryResult doCallBack(String database, InfluxDB influxDB) {
                BoundParameterQuery.QueryBuilder queryBuilder = BoundParameterQuery.QueryBuilder.newQuery(sql);
                queryBuilder.forDatabase(database);

                if (paramMap != null && paramMap.size() > 0) {
                    Set<Map.Entry<String, Object>> entries = paramMap.entrySet();
                    for (Map.Entry<String, Object> entry : entries) {
                        queryBuilder.bind(entry.getKey(), entry.getValue());
                    }
                }

                return influxDB.query(queryBuilder.create());
            }
        });
        return new InfluxDBResultMapper().toPOJO(queryResult, clasz);
    }

    public interface InfluxDBCallback {
        <T> T doCallBack(String database, InfluxDB influxDB);
    }

    public interface InfluxDBInsertCallback {
        void doCallBack(String database, InfluxDB influxDB);
    }

    public interface InfluxDBQueryCallback {
        QueryResult doCallBack(String database, InfluxDB influxDB);
    }
}
