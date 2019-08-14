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
package com.alibaba.csp.sentinel.dashboard.controller.cluster;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.dashboard.client.CommandNotFoundException;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.SentinelVersion;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.request.ClusterClientModifyRequest;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.request.ClusterModifyRequest;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.request.ClusterServerModifyRequest;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.AppClusterClientStateWrapVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.AppClusterServerStateWrapVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.ClusterUniversalStatePairVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.ClusterUniversalStateVO;
import com.alibaba.csp.sentinel.dashboard.service.ClusterConfigService;
import com.alibaba.csp.sentinel.dashboard.util.ClusterEntityUtils;
import com.alibaba.csp.sentinel.dashboard.util.VersionUtils;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@RestController
@RequestMapping(value = "/cluster")
public class ClusterConfigController {

    private final Logger logger = LoggerFactory.getLogger(ClusterConfigController.class);

    private final SentinelVersion version140 = new SentinelVersion().setMajorVersion(1).setMinorVersion(4);

    @Autowired
    private AppManagement appManagement;

    @Autowired
    private ClusterConfigService clusterConfigService;

    @PostMapping("/config/modify_single")
    public Result<Boolean> apiModifyClusterConfig(@RequestBody String payload) {
        if (StringUtil.isBlank(payload)) {
            return Result.ofFail(-1, "empty request body");
        }
        try {
            JSONObject body = JSON.parseObject(payload);
            if (body.containsKey(KEY_MODE)) {
                int mode = body.getInteger(KEY_MODE);
                switch (mode) {
                    case ClusterStateManager.CLUSTER_CLIENT:
                        ClusterClientModifyRequest data = JSON.parseObject(payload, ClusterClientModifyRequest.class);
                        Result<Boolean> res = checkValidRequest(data);
                        if (res != null) {
                            return res;
                        }
                        clusterConfigService.modifyClusterClientConfig(data).get();
                        return Result.ofSuccess(true);
                    case ClusterStateManager.CLUSTER_SERVER:
                        ClusterServerModifyRequest d = JSON.parseObject(payload, ClusterServerModifyRequest.class);
                        Result<Boolean> r = checkValidRequest(d);
                        if (r != null) {
                            return r;
                        }
                        // TODO: bad design here, should refactor!
                        clusterConfigService.modifyClusterServerConfig(d).get();
                        return Result.ofSuccess(true);
                    default:
                        return Result.ofFail(-1, "invalid mode");
                }
            }
            return Result.ofFail(-1, "invalid parameter");
        } catch (ExecutionException ex) {
            logger.error("Error when modifying cluster config", ex.getCause());
            return errorResponse(ex);
        } catch (Throwable ex) {
            logger.error("Error when modifying cluster config", ex);
            return Result.ofFail(-1, ex.getMessage());
        }
    }

    private <T> Result<T> errorResponse(ExecutionException ex) {
        if (isNotSupported(ex.getCause())) {
            return unsupportedVersion();
        } else {
            return Result.ofThrowable(-1, ex.getCause());
        }
    }

    @GetMapping("/state_single")
    public Result<ClusterUniversalStateVO> apiGetClusterState(@RequestParam String app,
                                                              @RequestParam String ip,
                                                              @RequestParam Integer port) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app cannot be null or empty");
        }
        if (StringUtil.isEmpty(ip)) {
            return Result.ofFail(-1, "ip cannot be null or empty");
        }
        if (port == null || port <= 0) {
            return Result.ofFail(-1, "Invalid parameter: port");
        }
        if (!checkIfSupported(app, ip, port)) {
            return unsupportedVersion();
        }
        try {
            return clusterConfigService.getClusterUniversalState(app, ip, port)
                .thenApply(Result::ofSuccess)
                .get();
        } catch (ExecutionException ex) {
            logger.error("Error when fetching cluster state", ex.getCause());
            return errorResponse(ex);
        } catch (Throwable throwable) {
            logger.error("Error when fetching cluster state", throwable);
            return Result.ofFail(-1, throwable.getMessage());
        }
    }

    @GetMapping("/server_state/{app}")
    public Result<List<AppClusterServerStateWrapVO>> apiGetClusterServerStateOfApp(@PathVariable String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app cannot be null or empty");
        }
        try {
            return clusterConfigService.getClusterUniversalState(app)
                .thenApply(ClusterEntityUtils::wrapToAppClusterServerState)
                .thenApply(Result::ofSuccess)
                .get();
        } catch (ExecutionException ex) {
            logger.error("Error when fetching cluster server state of app: " + app, ex.getCause());
            return errorResponse(ex);
        } catch (Throwable throwable) {
            logger.error("Error when fetching cluster server state of app: " + app, throwable);
            return Result.ofFail(-1, throwable.getMessage());
        }
    }

    @GetMapping("/client_state/{app}")
    public Result<List<AppClusterClientStateWrapVO>> apiGetClusterClientStateOfApp(@PathVariable String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app cannot be null or empty");
        }
        try {
            return clusterConfigService.getClusterUniversalState(app)
                .thenApply(ClusterEntityUtils::wrapToAppClusterClientState)
                .thenApply(Result::ofSuccess)
                .get();
        } catch (ExecutionException ex) {
            logger.error("Error when fetching cluster token client state of app: " + app, ex.getCause());
            return errorResponse(ex);
        } catch (Throwable throwable) {
            logger.error("Error when fetching cluster token client state of app: " + app, throwable);
            return Result.ofFail(-1, throwable.getMessage());
        }
    }

    @GetMapping("/state/{app}")
    public Result<List<ClusterUniversalStatePairVO>> apiGetClusterStateOfApp(@PathVariable String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app cannot be null or empty");
        }
        try {
            return clusterConfigService.getClusterUniversalState(app)
                .thenApply(Result::ofSuccess)
                .get();
        } catch (ExecutionException ex) {
            logger.error("Error when fetching cluster state of app: " + app, ex.getCause());
            return errorResponse(ex);
        } catch (Throwable throwable) {
            logger.error("Error when fetching cluster state of app: " + app, throwable);
            return Result.ofFail(-1, throwable.getMessage());
        }
    }

    private boolean isNotSupported(Throwable ex) {
        return ex instanceof CommandNotFoundException;
    }

    private boolean checkIfSupported(String app, String ip, int port) {
        try {
            return Optional.ofNullable(appManagement.getDetailApp(app))
                .flatMap(e -> e.getMachine(ip, port))
                .flatMap(m -> VersionUtils.parseVersion(m.getVersion())
                    .map(v -> v.greaterOrEqual(version140)))
                .orElse(true);
            // If error occurred or cannot retrieve machine info, return true.
        } catch (Exception ex) {
            return true;
        }
    }

    private Result<Boolean> checkValidRequest(ClusterModifyRequest request) {
        if (StringUtil.isEmpty(request.getApp())) {
            return Result.ofFail(-1, "app cannot be empty");
        }
        if (StringUtil.isEmpty(request.getIp())) {
            return Result.ofFail(-1, "ip cannot be empty");
        }
        if (request.getPort() == null || request.getPort() < 0) {
            return Result.ofFail(-1, "invalid port");
        }
        if (request.getMode() == null || request.getMode() < 0) {
            return Result.ofFail(-1, "invalid mode");
        }
        if (!checkIfSupported(request.getApp(), request.getIp(), request.getPort())) {
            return unsupportedVersion();
        }
        return null;
    }

    private <R> Result<R> unsupportedVersion() {
        return Result.ofFail(4041, "Sentinel client not supported for cluster flow control (unsupported version or dependency absent)");
    }

    private static final String KEY_MODE = "mode";
}
