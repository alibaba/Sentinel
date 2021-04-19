/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.apollo.service;

import com.alibaba.csp.sentinel.dashboard.apollo.config.SentinelApolloOpenApiProperties;
import com.alibaba.csp.sentinel.dashboard.apollo.converter.ResponseEntityConverter;
import com.alibaba.csp.sentinel.dashboard.apollo.entity.ConsumerRole;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Call apollo portal.
 *
 * @author wxq
 * @see <a href="https://github.com/ctripcorp/apollo/blob/master/apollo-portal/src/main/java/com/ctrip/framework/apollo/portal/controller/ConsumerController.java">ConsumerController.java</a> for HTTP API information.
 */
@Service
public class ApolloPortalService {

    private static final Logger logger = LoggerFactory.getLogger(ApolloPortalService.class);

    private final SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties;

    private final URI authorizedURI;

    public ApolloPortalService(SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties) {
        this.sentinelApolloOpenApiProperties = sentinelApolloOpenApiProperties;
        final String portalUrl = sentinelApolloOpenApiProperties.getPortalUrl();
        final String token = sentinelApolloOpenApiProperties.getToken();
        final String authorizedPath = String.format("consumers/%s/assign-role?type=AppRole", token);
        this.authorizedURI = URI.create(portalUrl).resolve(authorizedPath);
    }

    private static HttpHeaders buildHttpHeaders(String jsessionid) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

        // cookie
        httpHeaders.add(HttpHeaders.COOKIE, "JSESSIONID=" + jsessionid);

        return httpHeaders;
    }

    /**
     *
     * @throws RestClientResponseException if meet some HTTP status code are not well
     */
    public ResponseEntity<ConsumerRole[]> assignAppRoleToConsumer(String jsessionid, String appId) throws RestClientResponseException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = buildHttpHeaders(jsessionid);

        final String jsonString = JSONObject.toJSONString(Collections.singletonMap("appId", appId));
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonString, httpHeaders);

        ResponseEntity<ConsumerRole[]> responseEntity = restTemplate.postForEntity(authorizedURI, httpEntity, ConsumerRole[].class);

        return responseEntity;
    }

    /**
     * Use admin permission to call apollo portal, authorize managed app id, type is app.
     * @param jsessionid JESSIONID in Cookie
     * @param appIds app id in apollo
     * @return authorization's result, key is app id, value is http response
     */
    public Map<String, ResponseEntity<String>> assignAppRoleToConsumer(final String jsessionid, Set<String> appIds) {
        Function<String, String> keyFunction = Function.identity();
        Function<String, ResponseEntity<String>> valueFunction = appId -> {
            try {
                ResponseEntity<ConsumerRole[]> responseEntity = assignAppRoleToConsumer(jsessionid, appId);
                return ResponseEntityConverter.convert2String(responseEntity);
            } catch (RestClientResponseException e) {
                String body = e.getResponseBodyAsString();
                HttpHeaders httpHeaders = e.getResponseHeaders();
                HttpStatus httpStatus = HttpStatus.valueOf(e.getRawStatusCode());
                logger.error("failed to assignAppRoleToConsumer, appId = [{}], http status code = [{}], body = [{}]", appId, e.getRawStatusCode(), body);
                ResponseEntity<String> responseEntity = new ResponseEntity<>(body, httpHeaders, httpStatus);
                return responseEntity;
            }
        };

        Map<String, ResponseEntity<String>> result = appIds.parallelStream()
                .collect(
                        Collectors.toMap(
                                keyFunction,
                                valueFunction
                        )
                );
        return result;
    }

    /**
     * Use admin permission to call apollo portal, authorize managed app id, type is app.
     * @param jsessionid JESSIONID in Cookie
     * @param appIds app id in apollo
     * @return authorization's result, key is app id, value is true if success, others if failed.
     */
    public Map<String, String> assignAppRoleToSentinelDashboard(final String jsessionid, Set<String> appIds) {
        Map<String, ResponseEntity<String>> assignResult = assignAppRoleToConsumer(jsessionid, appIds);

        final Map<String, String> registryResult = new HashMap<>();
        for (Map.Entry<String, ResponseEntity<String>> entry : assignResult.entrySet()) {
            String projectName = entry.getKey();
            ResponseEntity<String> responseEntity = entry.getValue();
            logger.debug("registry project [{}] ResponseEntity [{}] ", projectName, responseEntity);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                registryResult.put(projectName, Boolean.TRUE.toString());
            } else {
                registryResult.put(projectName, responseEntity.toString());
            }
        }

        return registryResult;
    }

}
