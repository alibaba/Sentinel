package com.alibaba.csp.sentinel.dashboard.client.servicecombkie;

import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigLabel;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigResponse;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
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
    private static CloseableHttpClient client;

    public KieConfigClient() {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();
        client = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
    }

    private <T> String toPutMessage(T value) {
        JSONObject object = new JSONObject();
        object.put("value", JSON.toJSONString(value));
        return object.toJSONString();
    }

    private <T> Optional<String> sendToKie(HttpUriRequest request) {
        CloseableHttpResponse response = null;
        try {
            response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                RecordLog.warn(String.format("Get config from ServiceComb-kie failed, status code is %d",
                        statusCode));
                return Optional.empty();
            }
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            return Optional.ofNullable(result);
        } catch (IOException e) {
            RecordLog.error("Send to ServiceComb-kie failed.", e);
            return Optional.empty();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private <T> String toPostMessage(String key, T value, KieConfigLabel configLabel) {
        JSONObject object = new JSONObject();
        object.put("key", key);
        object.put("value", JSON.toJSONString(value));

        JSONObject labels = new JSONObject();
        labels.put("app", configLabel.getApp());
        labels.put("environment", configLabel.getEnvironment());
        labels.put("service", configLabel.getService());
        labels.put("version", configLabel.getVersion());
        labels.put("resource", configLabel.getResource());

        object.put("labels", labels);
        return object.toJSONString();
    }

    public Optional<KieConfigResponse> getConfig(String url) {
        HttpGet httpGet = new HttpGet(url);
        Optional<String> result = sendToKie(httpGet);

        if (!result.isPresent()) {
            return Optional.empty();
        }

        KieConfigResponse kieResponse = JSON.parseObject(result.get(), KieConfigResponse.class);
        return Optional.ofNullable(kieResponse);
    }


    public <T> Optional<KieConfigResponse> updateConfig(String url, T value) {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Content-type", "application/json");
        String bodyStr = toPutMessage(value);

        try {
            httpPut.setEntity(new StringEntity(bodyStr));
        } catch (UnsupportedEncodingException e) {
            RecordLog.error("Trans to json body failed.", e);
            return Optional.empty();
        }

        Optional<String> result = sendToKie(httpPut);

        if (!result.isPresent()) {
            return Optional.empty();
        }

        KieConfigResponse kieResponse = JSON.parseObject(result.get(), KieConfigResponse.class);
        return Optional.ofNullable(kieResponse);
    }

    public <T> Optional<KieConfigResponse> addConfig(String url, String key, T rule, KieConfigLabel label) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-type", "application/json");
        String bodyStr = toPostMessage(key, rule, label);

        try {
            httpPost.setEntity(new StringEntity(bodyStr));
        } catch (UnsupportedEncodingException e) {
            RecordLog.error("Trans to json body failed.", e);
            return Optional.empty();
        }

        Optional<String> result = sendToKie(httpPost);

        if (!result.isPresent()) {
            return Optional.empty();
        }

        KieConfigResponse kieResponse = JSON.parseObject(result.get(), KieConfigResponse.class);
        return Optional.ofNullable(kieResponse);
    }

    public Optional<String> deleteConfig(String urlPrefix, String id) {
        String url = urlPrefix + "/" + id;
        HttpDelete httpDelete = new HttpDelete(url);

        return sendToKie(httpDelete);

    }
}

