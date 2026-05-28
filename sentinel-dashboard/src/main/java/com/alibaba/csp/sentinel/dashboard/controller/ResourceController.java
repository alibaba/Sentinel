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
package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.command.vo.NodeVo;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.domain.ResourceTreeNode;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.ResourceVo;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Carpenter Lee
 */
@RestController
@RequestMapping(value = "/resource")
public class ResourceController {

    private static Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    private SentinelApiClient httpFetcher;

    /**
     * Fetch real time statistics info of the machine.
     *
     * @param ip        ip to fetch
     * @param port      port of the ip
     * @param type      one of [root, default, cluster], 'root' means fetching from tree root node, 'default' means
     *                  fetching from tree default node, 'cluster' means fetching from cluster node.
     * @param searchKey key to search
     * @return node statistics info.
     */
    @GetMapping("/machineResource.json")
    public Result<List<ResourceVo>> fetchResourceChainListOfMachine(String ip, Integer port, String type,
                                                                    String searchKey) {
        if (StringUtil.isEmpty(ip) || port == null) {
            return Result.ofFail(-1, "invalid param, give ip, port");
        }
        final String ROOT = "root";
        final String DEFAULT = "default";
        if (StringUtil.isEmpty(type)) {
            type = ROOT;
        }
        if (ROOT.equalsIgnoreCase(type) || DEFAULT.equalsIgnoreCase(type)) {
            List<NodeVo> nodeVos = httpFetcher.fetchResourceOfMachine(ip, port, type);
            if (nodeVos == null) {
                return Result.ofSuccess(null);
            }
            ResourceTreeNode treeNode = ResourceTreeNode.fromNodeVoList(nodeVos);
            treeNode.searchIgnoreCase(searchKey);
            return Result.ofSuccess(ResourceVo.fromResourceTreeNode(treeNode));
        } else {
            // Normal (cluster node).
            List<NodeVo> nodeVos = httpFetcher.fetchClusterNodeOfMachine(ip, port, true);
            if (nodeVos == null) {
                return Result.ofSuccess(null);
            }
            if (StringUtil.isNotEmpty(searchKey)) {
                nodeVos = nodeVos.stream().filter(node -> node.getResource()
                    .toLowerCase().contains(searchKey.toLowerCase()))
                    .collect(Collectors.toList());
            }
            return Result.ofSuccess(ResourceVo.fromNodeVoList(nodeVos));
        }
    }
}
