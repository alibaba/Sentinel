package com.alibaba.csp.sentinel.dashboard.util;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.WriteApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 * @date 2018-10-19
 */
@Component
public class InfluxDBUtils {

    private static Logger logger = LoggerFactory.getLogger(InfluxDBUtils.class);

    private static String url;

    private static String username;

    private static String password;

    private static InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

    @Value("${influxdb.url}")
    public void setUrl(String url) {
        InfluxDBUtils.url = url;
    }

    @Value("${influxdb.username}")
    public void setUsername(String username) {
        InfluxDBUtils.username = username;
    }

    @Value("${influxdb.password}")
    public void setPassword(String password) {
        InfluxDBUtils.password = password;
    }

    public static void init(String url, String username, String password) {
        InfluxDBUtils.url = url;
        InfluxDBUtils.username = username;
        InfluxDBUtils.password = password;
    }

    public static <T> T process(String database, InfluxDBCallback callback) {
        T t = null;
        try {
            InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://123.56.187.228:9999",
                    "N4-jSEhI67HFVqPfdWi1Fd-S9-kDJKeFtmd6At0U-54HOzEm8_1XJ3Um3FBTiJXmury5qxzo5FE0asmZOB1G-A=="
                            .toCharArray());
            try (WriteApi writeApi = influxDBClient.getWriteApi()) {
                writeApi.writeMeasurement("sentinel_metric", "03dba1e36bbc6000", ChronoUnit.NANOS, temperature);
            }
            influxDB.setDatabase(database);

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

    public static void insert(String database, InfluxDBInsertCallback influxDBInsertCallback) {
        process(database, new InfluxDBCallback() {
            @Override
            public <T> T doCallBack(String database, InfluxDB influxDB) {
                influxDBInsertCallback.doCallBack(database, influxDB);
                return null;
            }
        });

    }

    public static QueryResult query(String database, InfluxDBQueryCallback influxDBQueryCallback) {
        return process(database, new InfluxDBCallback() {
            @Override
            public <T> T doCallBack(String database, InfluxDB influxDB) {
                QueryResult queryResult = influxDBQueryCallback.doCallBack(database, influxDB);
                return (T) queryResult;
            }
        });
    }

    public static <T> List<T> queryList(String database, String sql, Map<String, Object> paramMap, Class<T> clasz) {
        QueryResult queryResult = query(database, new InfluxDBQueryCallback() {
            @Override
            public QueryResult doCallBack(String database, InfluxDBClient influxDB) {
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

        return resultMapper.toPOJO(queryResult, clasz);
    }

    public interface InfluxDBCallback {
        <T> T doCallBack(String database, InfluxDB influxDB);
    }

    public interface InfluxDBInsertCallback {
        void doCallBack(String database, InfluxDBClient influxDB);
    }

    public interface InfluxDBQueryCallback {
        QueryResult doCallBack(String database, InfluxDBClient influxDB);
    }
}
