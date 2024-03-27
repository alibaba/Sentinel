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

import java.util.List;

import com.alibaba.csp.sentinel.traffic.rule.router.destination.RouteDestination;
import com.alibaba.csp.sentinel.traffic.rule.router.match.RequestMatch;

/**
 * @author panxiaojun233
 */
public class RouteDetail {
    private String name;
    private List<RequestMatch> match;
    private List<RouteDestination> route;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RequestMatch> getMatch() {
        return match;
    }

    public void setMatch(List<RequestMatch> match) {
        this.match = match;
    }

    public List<RouteDestination> getRoute() {
        return route;
    }

    public void setRoute(List<RouteDestination> route) {
        this.route = route;
    }

    @Override
    public String toString() {
        return "RouteDetail{" +
                "name='" + name + '\'' +
                ", match=" + match +
                ", route=" + route +
                '}';
    }
}
