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
package com.alibaba.csp.sentinel.traffic.rule.router;

import java.util.Collections;
import java.util.List;

/**
 * @author panxiaojun233
 */
public class TrafficRouter {
    private List<String> hosts = Collections.EMPTY_LIST;
    private List<Route> http = Collections.EMPTY_LIST;
    private List<Route> rpc = Collections.EMPTY_LIST;

    public TrafficRouter() {
    }

    public TrafficRouter(List<String> hosts, List<Route> http, List<Route> rpc) {
        this.hosts = hosts;
        this.http = http;
        this.rpc = rpc;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public List<Route> getHttp() {
        return http;
    }

    public void setHttp(List<Route> http) {
        this.http = http;
    }

    public List<Route> getRpc() {
        return rpc;
    }

    public void setRpc(List<Route> rpc) {
        this.rpc = rpc;
    }

    @Override
    public String toString() {
        return "TrafficRouter{" +
                "hosts=" + hosts +
                ", http=" + http +
                ", rpc=" + rpc +
                '}';
    }
}
