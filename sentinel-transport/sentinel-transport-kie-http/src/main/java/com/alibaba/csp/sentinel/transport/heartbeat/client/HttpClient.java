package com.alibaba.csp.sentinel.transport.heartbeat.client;

import com.alibaba.csp.sentinel.log.RecordLog;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class HttpClient {
    private final static CloseableHttpClient client = HttpClients.createDefault();

    private final static Integer DEFAULT_TIMEOUT = 3000;

    public static HttpResponse post(String url, String body){
        return post(url, body, DEFAULT_TIMEOUT);
    }

    public static HttpResponse post(String url, String body, int timeout){
        HttpPost httpPost = new HttpPost(url);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        httpPost.setConfig(requestConfig);

        httpPost.addHeader("Content-Type", "application/json");

        if(body != null){
            try {
                httpPost.setEntity(new StringEntity(body));
            }catch (UnsupportedEncodingException e){
                RecordLog.error(e.getMessage());
            }
        }

        try (CloseableHttpResponse response = client.execute(httpPost)){
            HttpEntity entity = response.getEntity();
            return new HttpResponse(response.getStatusLine().toString(), EntityUtils.toByteArray(entity));
        }catch (IOException e){
            RecordLog.error(e.getMessage());
            throw new RuntimeException("Post failed.");
        }
    }
}
