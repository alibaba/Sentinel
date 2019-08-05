package com.alibaba.csp.sentinel.datasource.spring.cloud.config;

import com.alibaba.csp.sentinel.log.RecordLog;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigClientStateHolder;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.config.client.ConfigClientProperties.*;

/**
 * <p>
 * {@link SentinelRuleLocator}  which pull sentinel rules from remote server.
 * It retrieve configurations of spring-cloud-config client configurations from  {@link org.springframework.core.env.Environment}
 * Such as spring.cloud.config.uri=uri, spring.cloud.config.profile=profile .... and so on.
 * When pull rules successfully, save to {@link SentinelRuleStorage} for ${@link SpringCloudConfigDataSource} retrieve.
 * </p>
 *
 * @author lianglin
 * @since 1.7.0
 */
@Order(0)
public class SentinelRuleLocator implements PropertySourceLocator {


    private RestTemplate restTemplate;
    private ConfigClientProperties defaultProperties;
    private org.springframework.core.env.Environment environment;

    public SentinelRuleLocator(ConfigClientProperties defaultProperties, org.springframework.core.env.Environment environment) {
        this.defaultProperties = defaultProperties;
        this.environment = environment;
    }


    /**
     * Responsible for pull data from remote server
     *
     * @param environment
     * @return correct data if success else a empty propertySource or null
     */
    @Override
    @Retryable(interceptor = "configServerRetryInterceptor")
    public org.springframework.core.env.PropertySource<?> locate(
            org.springframework.core.env.Environment environment) {
        ConfigClientProperties properties = this.defaultProperties.override(environment);
        CompositePropertySource composite = new CompositePropertySource("configService");
        RestTemplate restTemplate = this.restTemplate == null
                ? getSecureRestTemplate(properties)
                : this.restTemplate;
        Exception error = null;
        String errorBody = null;
        try {
            String[] labels = new String[]{""};
            if (StringUtils.hasText(properties.getLabel())) {
                labels = StringUtils
                        .commaDelimitedListToStringArray(properties.getLabel());
            }
            String state = ConfigClientStateHolder.getState();
            // Try all the labels until one works
            for (String label : labels) {
                Environment result = getRemoteEnvironment(restTemplate, properties,
                        label.trim(), state);
                if (result != null) {
                    log(result);
                    // result.getPropertySources() can be null if using xml
                    if (result.getPropertySources() != null) {
                        for (PropertySource source : result.getPropertySources()) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> map = (Map<String, Object>) source
                                    .getSource();
                            composite.addPropertySource(
                                    new MapPropertySource(source.getName(), map));
                        }
                    }
                    SentinelRuleStorage.setRulesSource(composite);
                    return composite;
                }
            }
        } catch (HttpServerErrorException e) {
            error = e;
            if (MediaType.APPLICATION_JSON
                    .includes(e.getResponseHeaders().getContentType())) {
                errorBody = e.getResponseBodyAsString();
            }
        } catch (Exception e) {
            error = e;
        }
        if (properties.isFailFast()) {
            throw new IllegalStateException(
                    "Could not locate PropertySource and the fail fast property is set, failing",
                    error);
        }
        RecordLog.warn("Could not locate PropertySource: " + (errorBody == null
                ? error == null ? "label not found" : error.getMessage()
                : errorBody));
        return null;

    }

    public org.springframework.core.env.PropertySource<?> refresh() {
        return locate(environment);
    }

    private void log(Environment result) {

        RecordLog.info(String.format(
                "Located environment: name=%s, profiles=%s, label=%s, version=%s, state=%s",
                result.getName(),
                result.getProfiles() == null ? ""
                        : Arrays.asList(result.getProfiles()),
                result.getLabel(), result.getVersion(), result.getState()));

        List<PropertySource> propertySourceList = result.getPropertySources();
        if (propertySourceList != null) {
            int propertyCount = 0;
            for (PropertySource propertySource : propertySourceList) {
                propertyCount += propertySource.getSource().size();
            }
            RecordLog.info(String.format(
                    "Environment %s has %d property sources with %d properties.",
                    result.getName(), result.getPropertySources().size(),
                    propertyCount));
        }


    }


    private Environment getRemoteEnvironment(RestTemplate restTemplate,
                                             ConfigClientProperties properties, String label, String state) {
        String path = "/{name}/{profile}";
        String name = properties.getName();
        String profile = properties.getProfile();
        String token = properties.getToken();
        int noOfUrls = properties.getUri().length;
        if (noOfUrls > 1) {
            RecordLog.info("Multiple Config Server Urls found listed.");
        }

        RecordLog.info("properties = {0},label={1}, state={2}", properties, label, state);

        Object[] args = new String[]{name, profile};
        if (StringUtils.hasText(label)) {
            if (label.contains("/")) {
                label = label.replace("/", "(_)");
            }
            args = new String[]{name, profile, label};
            path = path + "/{label}";
        }
        ResponseEntity<Environment> response = null;

        for (int i = 0; i < noOfUrls; i++) {
            Credentials credentials = properties.getCredentials(i);
            String uri = credentials.getUri();
            String username = credentials.getUsername();
            String password = credentials.getPassword();

            RecordLog.info("Fetching config from server at : " + uri);

            try {
                HttpHeaders headers = new HttpHeaders();
                addAuthorizationToken(properties, headers, username, password);
                if (StringUtils.hasText(token)) {
                    headers.add(TOKEN_HEADER, token);
                }
                if (StringUtils.hasText(state) && properties.isSendState()) {
                    headers.add(STATE_HEADER, state);
                }

                final HttpEntity<Void> entity = new HttpEntity<>((Void) null, headers);
                response = restTemplate.exchange(uri + path, HttpMethod.GET, entity,
                        Environment.class, args);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                    throw e;
                }
            } catch (ResourceAccessException e) {
                RecordLog.info("Connect Timeout Exception on Url - " + uri
                        + ". Will be trying the next url if available");
                if (i == noOfUrls - 1) {
                    throw e;
                } else {
                    continue;
                }
            }

            if (response == null || response.getStatusCode() != HttpStatus.OK) {
                return null;
            }

            Environment result = response.getBody();
            return result;
        }

        return null;
    }


    private RestTemplate getSecureRestTemplate(ConfigClientProperties client) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (client.getRequestReadTimeout() < 0) {
            throw new IllegalStateException("Invalid Value for Read Timeout set.");
        }
        requestFactory.setReadTimeout(client.getRequestReadTimeout());
        RestTemplate template = new RestTemplate(requestFactory);
        Map<String, String> headers = new HashMap<>(client.getHeaders());
        if (headers.containsKey(AUTHORIZATION)) {
            // To avoid redundant addition of header
            headers.remove(AUTHORIZATION);
        }
        if (!headers.isEmpty()) {
            template.setInterceptors(Arrays.<ClientHttpRequestInterceptor>asList(
                    new GenericRequestHeaderInterceptor(headers)));
        }

        return template;
    }

    private void addAuthorizationToken(ConfigClientProperties configClientProperties,
                                       HttpHeaders httpHeaders, String username, String password) {
        String authorization = configClientProperties.getHeaders().get(AUTHORIZATION);

        if (password != null && authorization != null) {
            throw new IllegalStateException(
                    "You must set either 'password' or 'authorization'");
        }

        if (password != null) {
            byte[] token = Base64Utils.encode((username + ":" + password).getBytes());
            httpHeaders.add("Authorization", "Basic " + new String(token));
        } else if (authorization != null) {
            httpHeaders.add("Authorization", authorization);
        }

    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public static class GenericRequestHeaderInterceptor
            implements ClientHttpRequestInterceptor {

        private final Map<String, String> headers;

        public GenericRequestHeaderInterceptor(Map<String, String> headers) {
            this.headers = headers;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.getHeaders().add(header.getKey(), header.getValue());
            }
            return execution.execute(request, body);
        }

        protected Map<String, String> getHeaders() {
            return headers;
        }

    }
}
