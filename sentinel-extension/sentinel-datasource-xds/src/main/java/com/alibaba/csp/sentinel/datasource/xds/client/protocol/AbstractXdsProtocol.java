/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource.xds.client.protocol;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.csp.sentinel.datasource.xds.client.XdsClient;
import com.alibaba.csp.sentinel.datasource.xds.client.XdsUrlType;
import com.alibaba.csp.sentinel.datasource.xds.client.filiter.XdsFilter;
import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.datasource.xds.util.MD5Util;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.CollectionUtil;

import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;

/**
 * @author lwj
 * @since 2.0.0
 */
public abstract class AbstractXdsProtocol<T, R extends XdsFilter<List<T>>> {

    protected XdsConfigProperties xdsConfigProperties;

    protected XdsClient xdsClient;
    protected List<R> filterList = new ArrayList<>();
    private PushMachine pushMachine;

    /**
     * Resources that can be observed
     */
    private Set<String> resourceNames = new HashSet<>();
    private Class tClass;
    private String resourceHashCache = null;

    public AbstractXdsProtocol(XdsConfigProperties xdsConfigProperties) {
        this.xdsConfigProperties = xdsConfigProperties;
        this.tClass = getTClass();
    }

    public void setXdsClient(XdsClient xdsClient) {
        this.xdsClient = xdsClient;
    }

    public void addFilters(R filter) {
        filterList.add(filter);
    }

    public List<T> decodeXdsResponse(DiscoveryResponse response) {
        List<T> res = new ArrayList<>();
        for (com.google.protobuf.Any any : response.getResourcesList()) {
            try {
                T tmp = (T) any.unpack(tClass);
                if (null == tmp) {
                    continue;
                }
                res.add(tmp);
            } catch (Exception e) {
                RecordLog.error("[XdsDataSource] Unpack {} failed in protocol = {}", tClass.getName(), getTypeUrl(), e);
            }
        }
        return res;
    }

    public abstract Set<String> resolveResourceNames(List<T> resources);

    /**
     * Only observe their own resources
     */
    public synchronized void observeResource() {
        observeResource(null);
    }

    /**
     * Observe the specified resource
     *
     * @param resourceNames
     */
    public synchronized void observeResource(Set<String> resourceNames) {
        XdsUrlType typeUrl = getTypeUrl();
        RecordLog.info("[XdsDataSource] observe resource ,typeUrl={},resource= {}", typeUrl, resourceNames);

        if (resourceNames == null) {
            resourceNames = new HashSet<>();
        }
        xdsClient.sendXdsRequest(typeUrl, resourceNames);
    }

    /**
     * @return
     */
    public Set<String> getResourceNames() {
        return resourceNames;
    }

    public void setInitStateMachine(PushMachine pushMachine) {
        this.pushMachine = pushMachine;
    }

    protected void fireXdsFilters(List<T> resources) {
        try {
            this.resourceNames = resolveResourceNames(resources);
            RecordLog.info("[XdsDataSource] resolving resource name = {}", this.resourceNames);
        } catch (Exception e) {
            RecordLog.error("[XdsDataSource] Error on resolving resource ={} ", resources);
        }
        for (XdsFilter filter : filterList) {
            try {
                if (!filter.resolve(resources)) {
                    return;
                }
            } catch (Exception e) {
                RecordLog.error("[XdsDataSource] Error on executing Xds filter {}", filter.getClass().getName(), e);
            }
        }

    }

    /**
     * If the push is the same as last time, it is not processed directly
     *
     * @param resources
     */
    public synchronized void onResponseDecoded(List<T> resources) {
        if (CollectionUtil.isEmpty(resources)) {
            return;
        }
        String hash = MD5Util.getMd5(resources.toString());
        if (hash.equals(resourceHashCache)) {
            return;
        }
        resourceHashCache = hash;
        fireXdsFilters(resources);
        pushMachine.push(this);
    }

    public abstract XdsUrlType getTypeUrl();

    private Class getTClass() {
        Type[] types = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
        Class tClass = (Class) types[0];
        return tClass;
    }

}
