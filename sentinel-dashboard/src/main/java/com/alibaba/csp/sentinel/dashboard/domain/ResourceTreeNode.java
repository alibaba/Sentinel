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
package com.alibaba.csp.sentinel.dashboard.domain;

import com.alibaba.csp.sentinel.command.vo.NodeVo;
import com.alibaba.csp.sentinel.command.vo.TrafficStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author leyou
 */
public class ResourceTreeNode {

    private boolean visible = true;

    private List<ResourceTreeNode> children = new ArrayList<>();
    private TrafficStatistics trafficStatistics = new TrafficStatistics(null, null, null, null, null, null, null, null, null, null, null, null, null, null);

    public static ResourceTreeNode fromNodeVoList(List<NodeVo> nodeVos) {
        if (nodeVos == null || nodeVos.isEmpty()) {
            return null;
        }
        ResourceTreeNode root = null;
        Map<String, ResourceTreeNode> map = new HashMap<>();
        for (NodeVo vo : nodeVos) {
            ResourceTreeNode node = fromNodeVo(vo);
            map.put(node.trafficStatistics.getId(), node);
            // real root
            if (node.trafficStatistics.getParentId() == null || node.trafficStatistics.getParentId().isEmpty()) {
                root = node;
            } else if (map.containsKey(node.trafficStatistics.getParentId())) {
                map.get(node.trafficStatistics.getParentId()).children.add(node);
            } else {
                // impossible
            }
        }
        return root;
    }

    public static ResourceTreeNode fromNodeVo(NodeVo vo) {
        ResourceTreeNode node = new ResourceTreeNode();
        node.trafficStatistics.setId(vo.getId());
        node.trafficStatistics.setParentId(vo.getParentId());
        node.trafficStatistics.setResource(vo.getResource());
        node.trafficStatistics.setThreadNum(vo.getThreadNum());
        node.trafficStatistics.setPassQps(vo.getPassQps());
        node.trafficStatistics.setBlockQps(vo.getBlockQps());
        node.trafficStatistics.setTotalQps(vo.getTotalQps());
        node.trafficStatistics.setAverageRt(vo.getAverageRt());
        node.trafficStatistics.setSuccessQps(vo.getSuccessQps());
        node.trafficStatistics.setExceptionQps(vo.getExceptionQps());
        node.trafficStatistics.setOneMinutePass(vo.getOneMinutePass());
        node.trafficStatistics.setOneMinuteBlock(vo.getOneMinuteBlock());
        node.trafficStatistics.setOneMinuteException(vo.getOneMinuteException());
        node.trafficStatistics.setOneMinuteTotal(vo.getOneMinuteTotal());
        return node;
    }

    public void searchIgnoreCase(String searchKey) {
        search(this, searchKey);
    }

    /**
     * This node is visible only when searchKey matches this.resource or at least
     * one of this's children is visible
     */
    private boolean search(ResourceTreeNode node, String searchKey) {
        // empty matches all
        if (searchKey == null || searchKey.isEmpty() ||
                node.trafficStatistics.getResource().toLowerCase().contains(searchKey.toLowerCase())) {
            node.visible = true;
        } else {
            node.visible = false;
        }

        boolean found = false;
        for (ResourceTreeNode c : node.children) {
            found |= search(c, searchKey);
        }
        node.visible |= found;
        return node.visible;
    }

    public String getId() {
        return trafficStatistics.getId();
    }

    public void setId(String id) {
        this.trafficStatistics.setId(id);
    }

    public String getParentId() {
        return trafficStatistics.getParentId();
    }

    public void setParentId(String parentId) {
        this.trafficStatistics.setParentId(parentId);
    }

    public String getResource() {
        return trafficStatistics.getResource();
    }

    public void setResource(String resource) {
        this.trafficStatistics.setResource(resource);
    }

    public Integer getThreadNum() {
        return trafficStatistics.getThreadNum();
    }

    public void setThreadNum(Integer threadNum) {
        this.trafficStatistics.setThreadNum(threadNum);
    }

    public Long getPassQps() {
        return trafficStatistics.getPassQps();
    }

    public void setPassQps(Long passQps) {
        this.trafficStatistics.setPassQps(passQps);
    }

    public Long getBlockQps() {
        return trafficStatistics.getBlockQps();
    }

    public void setBlockQps(Long blockQps) {
        this.trafficStatistics.setBlockQps(blockQps);
    }

    public Long getTotalQps() {
        return trafficStatistics.getTotalQps();
    }

    public void setTotalQps(Long totalQps) {
        this.trafficStatistics.setTotalQps(totalQps);
    }

    public Long getAverageRt() {
        return trafficStatistics.getAverageRt();
    }

    public void setAverageRt(Long averageRt) {
        this.trafficStatistics.setAverageRt(averageRt);
    }

    public Long getSuccessQps() {
        return trafficStatistics.getSuccessQps();
    }

    public void setSuccessQps(Long successQps) {
        this.trafficStatistics.setSuccessQps(successQps);
    }

    public Long getExceptionQps() {
        return trafficStatistics.getExceptionQps();
    }

    public void setExceptionQps(Long exceptionQps) {
        this.trafficStatistics.setExceptionQps(exceptionQps);
    }

    public Long getOneMinutePass() {
        return trafficStatistics.getOneMinutePass();
    }

    public void setOneMinutePass(Long oneMinutePass) {
        this.trafficStatistics.setOneMinutePass(oneMinutePass);
    }

    public Long getOneMinuteBlock() {
        return trafficStatistics.getOneMinuteBlock();
    }

    public void setOneMinuteBlock(Long oneMinuteBlock) {
        this.trafficStatistics.setOneMinuteBlock(oneMinuteBlock);
    }

    public Long getOneMinuteException() {
        return trafficStatistics.getOneMinuteException();
    }

    public void setOneMinuteException(Long oneMinuteException) {
        this.trafficStatistics.setOneMinuteException(oneMinuteException);
    }

    public Long getOneMinuteTotal() {
        return trafficStatistics.getOneMinuteTotal();
    }

    public void setOneMinuteTotal(Long oneMinuteTotal) {
        this.trafficStatistics.setOneMinuteTotal(oneMinuteTotal);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<ResourceTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<ResourceTreeNode> children) {
        this.children = children;
    }
}

