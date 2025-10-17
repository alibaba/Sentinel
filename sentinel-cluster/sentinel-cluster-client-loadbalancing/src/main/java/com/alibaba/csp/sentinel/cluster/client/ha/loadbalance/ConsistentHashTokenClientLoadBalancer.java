/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.client.ha.loadbalance;

import com.alibaba.csp.sentinel.cluster.TokenServerDescriptor;
import com.alibaba.csp.sentinel.cluster.client.ClusterTokenClient;
import com.alibaba.csp.sentinel.cluster.client.ha.LoadBalanceContext;
import com.alibaba.csp.sentinel.spi.Spi;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * consistent hash loadbalancer, use the parameter of "ruleId" as the hash parameter
 *
 * @author icodening
 * @date 2022.03.06
 */
@Spi(value = ConsistentHashTokenClientLoadBalancer.NAME)
public class ConsistentHashTokenClientLoadBalancer extends AbstractTokenClientLoadBalancer {

    private static final String RULE_ID = "ruleId";

    public static final String NAME = "consistentHash";

    public static final String HASH_KEY = "hashKey";

    private static final ThreadLocal<MessageDigest> MD5Context = new ThreadLocal<>();

    private volatile ConsistentHashSelector consistentHashSelector;

    @Override
    protected ClusterTokenClient doSelect(List<ClusterTokenClient> clusterTransportClients, LoadBalanceContext loadBalanceContext) {
        if (loadBalanceContext == null) {
            return null;
        }
        Object ruleId = loadBalanceContext.getAttribute(RULE_ID);
        if (ruleId != null) {
            loadBalanceContext.setAttribute(HASH_KEY, String.valueOf(ruleId));
        }
        int currentHashCode = getHashCode(clusterTransportClients);
        if (consistentHashSelector == null
                || consistentHashSelector.hashCode != currentHashCode) {
            //client changed, need rehash
            consistentHashSelector = new ConsistentHashSelector(clusterTransportClients, currentHashCode, getMD5MessageDigest());
        }
        return consistentHashSelector.select(loadBalanceContext);
    }

    protected int getHashCode(List<ClusterTokenClient> clusterTransportClients) {
        int hashCode = 1;
        for (ClusterTokenClient clusterTransportClient : clusterTransportClients) {
            if (clusterTransportClient == null || clusterTransportClient.currentServer() == null) {
                continue;
            }
            TokenServerDescriptor tokenServerDescriptor = clusterTransportClient.currentServer();
            hashCode = 31 * hashCode + (tokenServerDescriptor == null ? 0 : tokenServerDescriptor.hashCode());
        }
        return hashCode;
    }


    private MessageDigest getMD5MessageDigest() {
        try {
            MessageDigest messageDigest = MD5Context.get();
            if (messageDigest == null) {
                messageDigest = MessageDigest.getInstance("MD5");
                MD5Context.set(messageDigest);
            }
            return messageDigest;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ConsistentHashSelector {

        private final TreeMap<Long, ClusterTokenClient> virtualClusterTokenClients;

        private final int hashCode;

        private final MessageDigest md5;

        ConsistentHashSelector(List<ClusterTokenClient> clusterTransportClients, int hashCode, MessageDigest md5) {
            this.virtualClusterTokenClients = new TreeMap<>();
            this.hashCode = hashCode;
            this.md5 = md5;
            for (ClusterTokenClient client : clusterTransportClients) {
                String address = client.currentServer().getHost() + ":" + client.currentServer().getPort();
                for (int i = 0; i < 40; i++) {
                    byte[] digest = md5.digest((address + i).getBytes());
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualClusterTokenClients.put(m, client);
                    }
                }
            }
        }

        public ClusterTokenClient select(LoadBalanceContext loadBalanceContext) {
            String hashKey = loadBalanceContext.getAttribute(HASH_KEY, String.class);
            if (hashKey == null) {
                return virtualClusterTokenClients.firstEntry().getValue();
            }
            byte[] digest = md5.digest(hashKey.getBytes());
            long hash = hash(digest, 0);
            Map.Entry<Long, ClusterTokenClient> entry = virtualClusterTokenClients.ceilingEntry(hash);
            if (entry == null) {
                entry = virtualClusterTokenClients.firstEntry();
            }
            return entry.getValue();
        }


        private long hash(byte[] digest, int number) {
            return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                    | (digest[number * 4] & 0xFF))
                    & 0xFFFFFFFFL;
        }
    }
}
