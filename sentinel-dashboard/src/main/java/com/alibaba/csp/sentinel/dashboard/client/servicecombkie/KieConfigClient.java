package com.alibaba.csp.sentinel.dashboard.client.servicecombkie;

import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigResponse;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

@Component
public class KieConfigClient {
    private final static CloseableHttpClient client = HttpClients.createDefault();

    public Optional<KieConfigResponse> getConfig(String url) {
        HttpGet httpGet = new HttpGet(url);
        KieConfigResponse kieResponse;
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                RecordLog.warn(String.format("Get config from ServiceComb-kie failed, status code is %d",
                        statusCode));
                return Optional.empty();
            }

            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            kieResponse = JSON.parseObject(result, KieConfigResponse.class);
        } catch (IOException e) {
            RecordLog.error("Get config from ServiceComb-kie failed.", e);
            return Optional.empty();
        }

        return Optional.ofNullable(kieResponse);
    }

    private <T> String toPutMessage(T rules) {
        JSONObject object = new JSONObject();
        object.put("value", JSON.toJSONString(rules));
        return object.toJSONString();
    }

    public <T> Optional<KieConfigResponse> updateConfig(String url, T rule) {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Content-type", "application/json");
        String bodyStr = toPutMessage(rule);

        try {
            httpPut.setEntity(new StringEntity(bodyStr));
        } catch (UnsupportedEncodingException e) {
            RecordLog.error("Trans to json body failed.", e);
            return Optional.empty();
        }

        KieConfigResponse kieResponse;
        try (CloseableHttpResponse response = client.execute(httpPut)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                RecordLog.warn(String.format("Get config from ServiceComb-kie failed, status code is %d",
                        statusCode));
                return Optional.empty();
            }

            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            kieResponse = JSON.parseObject(result, KieConfigResponse.class);
        } catch (IOException e) {
            RecordLog.error("Update config to ServiceComb-kie failed.", e);
            return Optional.empty();
        }

        return Optional.ofNullable(kieResponse);
    }
}

