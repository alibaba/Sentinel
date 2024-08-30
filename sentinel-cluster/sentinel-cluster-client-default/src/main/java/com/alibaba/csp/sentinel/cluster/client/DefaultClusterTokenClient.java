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
package com.alibaba.csp.sentinel.cluster.client;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.ClusterErrorMessages;
import com.alibaba.csp.sentinel.cluster.ClusterTransportClient;
import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.TokenServerDescriptor;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.client.config.ServerChangeObserver;
import com.alibaba.csp.sentinel.cluster.log.ClusterClientStatLogUtil;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.request.data.FlowRequestData;
import com.alibaba.csp.sentinel.cluster.request.data.ParamFlowRequestData;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import com.alibaba.csp.sentinel.cluster.response.data.FlowTokenResponseData;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;

/**
 * Default implementation of {@link ClusterTokenClient}.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class DefaultClusterTokenClient implements ClusterTokenClient {
    public class CachedTokenData {
        AtomicInteger count;
        AtomicInteger lastStatus;
        AtomicLong lastWaitUntilMs;
        AtomicInteger lastWaitPrefetchCnt;
        AtomicInteger lastRemaining;
        public CachedTokenData() {
            count = new AtomicInteger(0);
            lastStatus = new AtomicInteger(TokenResultStatus.OK);
            lastWaitUntilMs = new AtomicLong(0);
            lastWaitPrefetchCnt = new AtomicInteger(0);
            lastRemaining = new AtomicInteger(0);
        }
    }

    private ClusterTransportClient transportClient;
    private TokenServerDescriptor serverDescriptor;

    private final AtomicBoolean shouldStart = new AtomicBoolean(false);
    private int checkInterval = 2;
    ConcurrentHashMap<Long, CachedTokenData> localPrefetchedTokens = new ConcurrentHashMap<>();
    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private final ScheduledExecutorService prefetchScheduler = Executors.newScheduledThreadPool(2,
        new NamedThreadFactory("sentinel-cluster-prefetch-scheduler", true));

    public DefaultClusterTokenClient() {
        ClusterClientConfigManager.addServerChangeObserver(new ServerChangeObserver() {
            @Override
            public void onRemoteServerChange(ClusterClientAssignConfig assignConfig) {
                changeServer(assignConfig);
            }
        });
        initNewConnection();
    }

    private boolean serverEqual(TokenServerDescriptor descriptor, ClusterClientAssignConfig config) {
        if (descriptor == null || config == null) {
            return false;
        }
        return descriptor.getHost().equals(config.getServerHost()) && descriptor.getPort() == config.getServerPort();
    }

    private void initNewConnection() {
        if (transportClient != null) {
            return;
        }
        String host = ClusterClientConfigManager.getServerHost();
        int port = ClusterClientConfigManager.getServerPort();
        if (StringUtil.isBlank(host) || port <= 0) {
            return;
        }

        try {
            this.transportClient = new NettyTransportClient(host, port);
            this.serverDescriptor = new TokenServerDescriptor(host, port);
            RecordLog.info("[DefaultClusterTokenClient] New client created: {}", serverDescriptor);
        } catch (Exception ex) {
            RecordLog.warn("[DefaultClusterTokenClient] Failed to initialize new token client", ex);
        }
    }

    private void changeServer(/*@Valid*/ ClusterClientAssignConfig config) {
        if (serverEqual(serverDescriptor, config)) {
            return;
        }
        try {
            if (transportClient != null) {
                transportClient.stop();
            }
            // Replace with new, even if the new client is not ready.
            this.transportClient = new NettyTransportClient(config.getServerHost(), config.getServerPort());
            this.serverDescriptor = new TokenServerDescriptor(config.getServerHost(), config.getServerPort());
            startClientIfScheduled();
            RecordLog.info("[DefaultClusterTokenClient] New client created: {}", serverDescriptor);
        } catch (Exception ex) {
            RecordLog.warn("[DefaultClusterTokenClient] Failed to change remote token server", ex);
        }
    }

    private void startClientIfScheduled() throws Exception {
        if (shouldStart.get()) {
            if (transportClient != null) {
                transportClient.start();
            } else {
                RecordLog.warn("[DefaultClusterTokenClient] Cannot start transport client: client not created");
            }
        }
    }

    private void stopClientIfStarted() throws Exception {
        if (shouldStart.compareAndSet(true, false)) {
            if (transportClient != null) {
                transportClient.stop();
            }
        }
    }

    @Override
    public void start() throws Exception {
        if (shouldStart.compareAndSet(false, true)) {
            startClientIfScheduled();
        }
    }

    @Override
    public void stop() throws Exception {
        stopClientIfStarted();
    }

    @Override
    public int getState() {
        if (transportClient == null) {
            return ClientConstants.CLIENT_STATUS_OFF;
        }
        return transportClient.isReady() ? ClientConstants.CLIENT_STATUS_STARTED : ClientConstants.CLIENT_STATUS_OFF;
    }

    @Override
    public TokenServerDescriptor currentServer() {
        return serverDescriptor;
    }

    public void setInterval(int val) {
        checkInterval = val;
    }

    public void resetCache() {
        localPrefetchedTokens.clear();
    }

    public int currentRuleCached(Long flowId) {
        CachedTokenData d = localPrefetchedTokens.get(flowId);
        if (d == null) {
            return 0;
        }
        return d.count.get();
    }

    private void preFetch(Long flowId, CachedTokenData value, int prefetchCnt) {
        long waitUntil = value.lastWaitUntilMs.get();
        if (waitUntil > 0 && System.currentTimeMillis() < waitUntil) {
            return;
        }
        if (waitUntil > 0) {
            value.count.addAndGet(value.lastWaitPrefetchCnt.get());
            value.lastStatus.set(TokenResultStatus.OK);
            value.lastWaitUntilMs.set(0);
            value.lastWaitPrefetchCnt.set(0);
        }
        int current = value.count.get();
        if (current >= prefetchCnt / 2) {
            return;
        }
        if (current < -1 * prefetchCnt) {
            // avoid too much prefetch
            current = -1 * prefetchCnt;
        }
        prefetchCnt = prefetchCnt - current;
        TokenResult fetched = requestToken(flowId, prefetchCnt, true);
        value.lastWaitUntilMs.set(0);
        value.lastStatus.set(fetched.getStatus());
        value.lastRemaining.set(fetched.getRemaining());
        if (fetched.getStatus() == TokenResultStatus.OK) {
            value.count.addAndGet(prefetchCnt);
        } else if (fetched.getStatus() == TokenResultStatus.SHOULD_WAIT) {
            value.lastWaitUntilMs.set(System.currentTimeMillis() + fetched.getWaitInMs());
            value.lastWaitPrefetchCnt.set(prefetchCnt);
        }
    }
    
    private TokenResult tryLocalCachedToken(CachedTokenData data, int acquireCount, int prefetchCnt) {
        int count = data.count.get();
        TokenResult ret = new TokenResult(data.lastStatus.get());
        ret.setFromCached(true);
        ret.setRemaining(data.lastRemaining.get());
        if (count >= acquireCount) {
            // here we allow the concurrency which may cause decrease to negative count, it
            // is just skipped some requests
            // and it will be refilled by the bg prefetch in next round.
            data.count.addAndGet(-1 * acquireCount);
            ret.setStatus(TokenResultStatus.OK);
            return ret;
        }
        if (acquireCount > prefetchCnt) {
            return null;
        }
        if (ret.getStatus() == TokenResultStatus.SHOULD_WAIT) {
            int newN = data.count.addAndGet(-1 * acquireCount);
            if (newN + data.lastWaitPrefetchCnt.get() < -1 * prefetchCnt) {
                data.count.addAndGet(acquireCount);
                if (acquireCount <= prefetchCnt / 2) {
                    // since last status is still waiting, we should not block directly, make it failover to local
                    ret.setStatus(TokenResultStatus.FAIL);
                    return ret;
                }
                // for the large acquireCount, we can try remote again, since large request will
                // much slower which will have less pressure to remote
                return null;
            }
            int waitMs = (int) (data.lastWaitUntilMs.get() - System.currentTimeMillis());
            if (waitMs > 0) {
                ret.setWaitInMs(waitMs);
            }
            return ret;
        } else if (ret.getStatus() == TokenResultStatus.OK) {
            // last ok, but the cached count is not enough, we can preuse it to avoid remote
            // request too often,
            // otherwise just try remote request
            int newN = data.count.addAndGet(-1 * acquireCount);
            if (newN < -1 * prefetchCnt * 2) {
                // preuse failed since not enough, added it back
                data.count.addAndGet(acquireCount);
                if (acquireCount <= prefetchCnt / 2) {
                    // since last is still ok, we should not block directly, make it failover to local
                    ret.setStatus(TokenResultStatus.FAIL);
                    return ret;
                }
                // for the large acquireCount, we can try remote again, since large request will much slower which will have less pressure to remote
                return null;
            }
            // preuse ok
            return ret;
        } else {
            // should fail directly
            return ret;
        }
    }

    @Override
    public TokenResult requestTokenWithCache(Long flowId, int acquireCount, int prefetchCnt) {
        if (notValidRequest(flowId, acquireCount)) {
            return badRequest();
        }
        // try local prefetched first
        CachedTokenData data = localPrefetchedTokens.get(flowId);
        if (data != null) {
            TokenResult ret = tryLocalCachedToken(data, acquireCount, prefetchCnt);
            if (ret != null) {
                return ret;
            }
        } else {
            localPrefetchedTokens.computeIfAbsent(flowId, k -> {
                CachedTokenData v = new CachedTokenData();
                prefetchScheduler.scheduleAtFixedRate(() -> {
                    try {
                        preFetch(flowId, v, prefetchCnt);
                    } catch (Throwable e) {
                        RecordLog.info("[DefaultClusterTokenClient] prefetch failed for flowId {}", flowId, e);
                    }
                }, 0, checkInterval, TimeUnit.MILLISECONDS);
                return v;
            });
        }
        // fallback to remote request
        return requestToken(flowId, acquireCount, true);
    }

    @Override
    public TokenResult requestToken(Long flowId, int acquireCount, boolean prioritized) {
        if (notValidRequest(flowId, acquireCount)) {
            return badRequest();
        }
        FlowRequestData data = new FlowRequestData().setCount(acquireCount)
            .setFlowId(flowId).setPriority(prioritized);
        ClusterRequest<FlowRequestData> request = new ClusterRequest<>(ClusterConstants.MSG_TYPE_FLOW, data);
        try {
            TokenResult result = sendTokenRequest(request);
            logForResult(result);
            return result;
        } catch (Exception ex) {
            ClusterClientStatLogUtil.log(ex.getMessage());
            return new TokenResult(TokenResultStatus.FAIL);
        }
    }

    @Override
    public TokenResult requestParamToken(Long flowId, int acquireCount, Collection<Object> params) {
        if (notValidRequest(flowId, acquireCount) || params == null || params.isEmpty()) {
            return badRequest();
        }
        ParamFlowRequestData data = new ParamFlowRequestData().setCount(acquireCount)
            .setFlowId(flowId).setParams(params);
        ClusterRequest<ParamFlowRequestData> request = new ClusterRequest<>(ClusterConstants.MSG_TYPE_PARAM_FLOW, data);
        try {
            TokenResult result = sendTokenRequest(request);
            logForResult(result);
            return result;
        } catch (Exception ex) {
            ClusterClientStatLogUtil.log(ex.getMessage());
            return new TokenResult(TokenResultStatus.FAIL);
        }
    }

    @Override
    public TokenResult requestConcurrentToken(String clientAddress, Long ruleId, int acquireCount) {
        return null;
    }

    @Override
    public void releaseConcurrentToken(Long tokenId) {
    }

    private void logForResult(TokenResult result) {
        switch (result.getStatus()) {
            case TokenResultStatus.NO_RULE_EXISTS:
                ClusterClientStatLogUtil.log(ClusterErrorMessages.NO_RULES_IN_SERVER);
                break;
            case TokenResultStatus.TOO_MANY_REQUEST:
                ClusterClientStatLogUtil.log(ClusterErrorMessages.TOO_MANY_REQUESTS);
                break;
            default:
        }
    }

    private TokenResult sendTokenRequest(ClusterRequest request) throws Exception {
        if (transportClient == null) {
            RecordLog.warn(
                "[DefaultClusterTokenClient] Client not created, please check your config for cluster client");
            return clientFail();
        }
        ClusterResponse response = transportClient.sendRequest(request);
        TokenResult result = new TokenResult(response.getStatus());
        if (response.getData() != null) {
            FlowTokenResponseData responseData = (FlowTokenResponseData)response.getData();
            result.setRemaining(responseData.getRemainingCount())
                .setWaitInMs(responseData.getWaitInMs());
        }
        return result;
    }

    private boolean notValidRequest(Long id, int count) {
        return id == null || id <= 0 || count <= 0;
    }

    private TokenResult badRequest() {
        return new TokenResult(TokenResultStatus.BAD_REQUEST);
    }

    private TokenResult clientFail() {
        return new TokenResult(TokenResultStatus.FAIL);
    }
}
