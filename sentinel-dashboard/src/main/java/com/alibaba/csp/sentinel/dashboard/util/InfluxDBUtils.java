package com.alibaba.csp.sentinel.dashboard.util;

import com.alibaba.fastjson.JSON;
import java.util.List;
import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.domain.Permission;
import org.influxdata.client.domain.PermissionResource;
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

    public static <T> T process(String bucket, InfluxDBCallback callback) {
        T t = null;
        InfluxDBClient influxDBClient = null;
        try {
            influxDBClient = InfluxDBClientFactory.create(url, findToken().toCharArray());
            t = callback.doCallBack(bucket, influxDBClient);
        } catch (Exception e) {
            logger.error("[process exception]", e);
        } finally {
            if (influxDBClient != null) {
                try {
                    influxDBClient.close();
                } catch (Exception e) {
                    logger.error("[influxDB.close exception]", e);
                }
            }
        }

        return t;
    }

    public static void insert(String bucket, InfluxDBInsertCallback influxDBInsertCallback) {
        process(bucket, new InfluxDBCallback() {
            @Override
            public <T> T doCallBack(String bucket, InfluxDBClient influxDB) {
                influxDBInsertCallback.doCallBack(bucket, influxDB);
                return null;
            }
        });

    }

    public static <T> List<T> query(String orgID, InfluxDBQueryCallback influxDBQueryCallback) {
        return process(orgID, new InfluxDBCallback() {
            @Override
            public <T> T doCallBack(String database, InfluxDBClient influxDB) {
                return (T) influxDBQueryCallback.doCallBack(orgID, influxDB);
            }
        });
    }

    public static <T> List<T> queryList(String orgID, String sql, Class<T> clasz) {
        return query(orgID, new InfluxDBQueryCallback() {
            @Override
            public <T> List<T> doCallBack(String orgID, InfluxDBClient influxDB) {
                return (List<T>) influxDB.getQueryApi().query(sql, orgID, clasz);
            }
        });
    }

    private static String findToken() throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(url,
                username, password.toCharArray());

        String token = influxDBClient.getAuthorizationsApi()
                .findAuthorizations()
                .stream()
                .filter(authorization -> authorization.getPermissions().stream()
                        .map(Permission::getResource)
                        .anyMatch(resource ->
                                resource.getType().equals(PermissionResource.TypeEnum.ORGS) &&
                                        resource.getId() == null &&
                                        resource.getOrgID() == null))
                .findFirst()
                .orElseThrow(IllegalStateException::new).getToken();

        influxDBClient.close();

        return token;
    }

    public interface InfluxDBCallback {
        <T> T doCallBack(String bucket, InfluxDBClient influxDB);
    }

    public interface InfluxDBInsertCallback {
        void doCallBack(String bucket, InfluxDBClient influxDB);
    }

    public interface InfluxDBQueryCallback {
        <T> List<T> doCallBack(String orgID, InfluxDBClient influxDB);
    }
}
