package com.alibaba.csp.sentinel.transport.client;

import com.alibaba.csp.sentinel.command.CommandConstants;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author cdfive
 */
public class SentinelTransportClient {

    private static Logger logger = LoggerFactory.getLogger(SentinelTransportClient.class);

    private static final Charset DEFAULT_CHARSET = Charset.forName(SentinelConfig.charset());

    private static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_HEADER_CONTENT_TYPE_URLENCODED = ContentType.create(URLEncodedUtils.CONTENT_TYPE).toString();

    private static final String GET_RULES_PATH = "getRules";
    private static final String SET_RULES_PATH = "setRules";

    private static final String GET_PARAM_FLOW_RULE_PATH = "getParamFlowRules";
    private static final String SET_PARAM_FLOW_RULE_PATH = "setParamFlowRules";

    private CloseableHttpAsyncClient httpClient;

    public SentinelTransportClient() {
        IOReactorConfig ioConfig = IOReactorConfig.custom().setConnectTimeout(3000).setSoTimeout(10000)
                .setIoThreadCount(Runtime.getRuntime().availableProcessors() * 2).build();
        httpClient = HttpAsyncClients.custom().setRedirectStrategy(new DefaultRedirectStrategy() {
            @Override
            protected boolean isRedirectable(final String method) {
                return false;
            }
        }).setMaxConnTotal(4000).setMaxConnPerRoute(1000).setDefaultIOReactorConfig(ioConfig).build();
        httpClient.start();
    }

    public String fetchRules(String ip, int port, String type) {
        Map<String, String> params = null;
        if (StringUtil.isNotEmpty(type)) {
            params = new HashMap<>(1);
            params.put("type", type);
        }

        CompletableFuture<String> completableFuture = executeCommand(null, ip, port, GET_RULES_PATH, params, false);
        String rules = null;
        try {
            rules = completableFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return rules;
    }

    public String fetchParamFlowRules(String ip, int port) {
        CompletableFuture<String> completableFuture = executeCommand(null, ip, port, GET_PARAM_FLOW_RULE_PATH, null, false);
        String rules = null;
        try {
            rules = completableFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return rules;
    }

    public void setRules(String ip, int port, String type, String rules) {
        Map<String, String> params = new HashMap<>(2);
        params.put("type", type);
        params.put("data", rules);
        CompletableFuture<String> completableFuture = executeCommand(null, ip, port, SET_RULES_PATH, params, true);
        try {
            completableFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void setParamFlowRules(String ip, int port, String rules) {
        Map<String, String> params = new HashMap<>(2);
        params.put("data", rules);
        CompletableFuture<String> completableFuture = executeCommand(null, ip, port, SET_PARAM_FLOW_RULE_PATH, params, true);
        try {
            completableFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private CompletableFuture<String> executeCommand(String app, String ip, int port, String api, Map<String, String> params, boolean useHttpPost) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (StringUtil.isBlank(ip) || StringUtil.isBlank(api)) {
            future.completeExceptionally(new IllegalArgumentException("Bad URL or command name"));
            return future;
        }
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("http://");
        urlBuilder.append(ip).append(':').append(port).append('/').append(api);
        if (params == null) {
            params = Collections.emptyMap();
        }
        if (!useHttpPost // || !isSupportPost(app, ip, port)
        ) {
            // Using GET in older versions, append parameters after url
            if (!params.isEmpty()) {
                if (urlBuilder.indexOf("?") == -1) {
                    urlBuilder.append('?');
                } else {
                    urlBuilder.append('&');
                }
                urlBuilder.append(queryString(params));
            }
            return executeCommand(new HttpGet(urlBuilder.toString()));
        } else {
            // Using POST
            return executeCommand(postRequest(urlBuilder.toString(), params, true));
        }
    }

    private CompletableFuture<String> executeCommand(HttpUriRequest request) {
        CompletableFuture<String> future = new CompletableFuture<>();
        httpClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse response) {
                int statusCode = response.getStatusLine().getStatusCode();
                try {
                    String value = getBody(response);
                    if (isSuccess(statusCode)) {
                        future.complete(value);
                    } else {
                        if (isCommandNotFound(statusCode, value)) {
                            future.completeExceptionally(new CommandNotFoundException(request.getURI().getPath()));
                        } else {
                            future.completeExceptionally(new CommandFailedException(value));
                        }
                    }

                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                    logger.error("HTTP request failed: {}", request.getURI().toString(), ex);
                }
            }

            @Override
            public void failed(final Exception ex) {
                future.completeExceptionally(ex);
                logger.error("HTTP request failed: {}", request.getURI().toString(), ex);
            }

            @Override
            public void cancelled() {
                future.complete(null);
            }
        });
        return future;
    }

    private String getBody(HttpResponse response) throws Exception {
        Charset charset = null;
        try {
            String contentTypeStr = response.getFirstHeader(HTTP_HEADER_CONTENT_TYPE).getValue();
            if (StringUtil.isNotEmpty(contentTypeStr)) {
                ContentType contentType = ContentType.parse(contentTypeStr);
                charset = contentType.getCharset();
            }
        } catch (Exception ignore) {
        }
        return EntityUtils.toString(response.getEntity(), charset != null ? charset : DEFAULT_CHARSET);
    }

    private StringBuilder queryString(Map<String, String> params) {
        StringBuilder queryStringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (StringUtil.isEmpty(entry.getValue())) {
                continue;
            }
            String name = urlEncode(entry.getKey());
            String value = urlEncode(entry.getValue());
            if (name != null && value != null) {
                if (queryStringBuilder.length() > 0) {
                    queryStringBuilder.append('&');
                }
                queryStringBuilder.append(name).append('=').append(value);
            }
        }
        return queryStringBuilder;
    }

    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, DEFAULT_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            logger.info("encode string error: {}", str, e);
            return null;
        }
    }

    protected static HttpUriRequest postRequest(String url, Map<String, String> params, boolean supportEnhancedContentType) {
        HttpPost httpPost = new HttpPost(url);
        if (params != null && params.size() > 0) {
            List<NameValuePair> list = new ArrayList<>(params.size());
            for (Map.Entry<String, String> entry : params.entrySet()) {
                list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(list, Consts.UTF_8));
            if (!supportEnhancedContentType) {
                httpPost.setHeader(HTTP_HEADER_CONTENT_TYPE, HTTP_HEADER_CONTENT_TYPE_URLENCODED);
            }
        }
        return httpPost;
    }

//    private boolean isSupportEnhancedContentType(String app, String ip, int port) {
//        return StringUtil.isNotEmpty(app) && Optional.ofNullable(appManagement.getDetailApp(app))
//                .flatMap(e -> e.getMachine(ip, port))
//                .flatMap(m -> VersionUtils.parseVersion(m.getVersion())
//                        .map(v -> v.greaterOrEqual(version171)))
//                .orElse(false);
//    }

    private boolean isCommandNotFound(int statusCode, String body) {
        return statusCode == 400 && StringUtil.isNotEmpty(body) && body.contains(CommandConstants.MSG_UNKNOWN_COMMAND_PREFIX);
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
}
