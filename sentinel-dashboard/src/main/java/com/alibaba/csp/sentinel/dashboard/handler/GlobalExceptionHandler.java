/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.csp.sentinel.dashboard.handler;

import com.alibaba.csp.sentinel.dashboard.client.CommandNotFoundException;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author chenglu
 * @date 2021-06-02
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * handle the {@link ExecutionException}, if the Exception's cause is {@link CommandNotFoundException}, then return code 4041.
     *
     * @param e {@link ExecutionException}
     * @param webRequest webRequest
     * @return fail message.
     */
    @ExceptionHandler({ExecutionException.class})
    @ResponseBody
    public Result<String> handleExecutionException(ExecutionException e, WebRequest webRequest) {
        Map<String, String[]> params = webRequest.getParameterMap();
        String contextPath = resolveContextPath(webRequest);
        logger.error("[HandleExecutionException] Request failed, context path: {}, request params: {}, error: ",
                contextPath, buildParamMessage(params), e.getCause());
        if (isNotSupported(e.getCause())) {
            return Result.ofFail(4041, "Sentinel client not supported for cluster flow control (unsupported version or dependency absent)");
        } else {
            return Result.ofFail(-1, "Operation failed, please check dashboard log for more information!");
        }
    }
    
    /**
     * check the exception if the version not supported.
     *
     * @param ex exception.
     * @return check result.
     */
    private boolean isNotSupported(Throwable ex) {
        return ex instanceof CommandNotFoundException;
    }
    
    /**
     * global exception handle, handle all the exception not include {@link ExecutionException}.
     *
     * @param e exception
     * @param webRequest request.
     * @return common failed exception.
     */
    @ExceptionHandler({Throwable.class})
    @ResponseBody
    public Result<String> handleException(Throwable e, WebRequest webRequest) {
        Map<String, String[]> params = webRequest.getParameterMap();
        String contextPath = resolveContextPath(webRequest);
        logger.error("[handleException] Request failed, context path: {}, request params: {}, error: ",
                contextPath, buildParamMessage(params), e);
        return Result.ofFail(-1, "Operation failed, please check dashboard log for more information!");
    }
    
    /**
     * resolve context path.
     *
     * @param webRequest web request.
     * @return context path.
     */
    private String resolveContextPath(WebRequest webRequest) {
        if (webRequest instanceof ServletWebRequest) {
            return ((ServletWebRequest) webRequest).getRequest().getRequestURI();
        }
        return webRequest.getContextPath();
    }
    
    /**
     * build the parameters message.
     *
     * @param params parameters.
     * @return message info.
     */
    private String buildParamMessage(Map<String, String[]> params) {
        if (CollectionUtils.isEmpty(params)) {
            return "";
        }
        return params.entrySet().stream()
                .map(e ->
                    e.getKey() + ": " + Arrays.toString(e.getValue())
                ).collect(Collectors.joining(", "));
    }
}
