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
package com.alibaba.csp.sentinel.command.vo;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

import java.util.UUID;

/**
 * This class is view object of {@link DefaultNode} or {@link ClusterNode}.
 *
 * @author leyou
 */
public class NodeVo {

    private Long timestamp;
    private TrafficStatistics trafficStatistics = new TrafficStatistics(null, null, null, null, null, null, null, null, null, null, null, null, null, null);

    /**
     * {@link DefaultNode} holds statistics of every node in the invoke tree.
     * We use parentId to hold the tree structure.
     *
     * @param node     the DefaultNode to be presented.
     * @param parentId random generated parent node id, may be a random UUID
     * @return node view object.
     */
    public static NodeVo fromDefaultNode(DefaultNode node, String parentId) {
        if (node == null) {
            return null;
        }
        NodeVo vo = new NodeVo();
        vo.trafficStatistics.setId(UUID.randomUUID().toString());
        vo.trafficStatistics.setParentId(parentId);
        vo.trafficStatistics.setResource(node.getId().getShowName());
        vo.trafficStatistics.setThreadNum(node.curThreadNum());
        vo.trafficStatistics.setPassQps((long) node.passQps());
        vo.trafficStatistics.setBlockQps((long) node.blockQps());
        vo.trafficStatistics.setTotalQps((long) node.totalQps());
        vo.trafficStatistics.setAverageRt((long) node.avgRt());
        vo.trafficStatistics.setSuccessQps((long) node.successQps());
        vo.trafficStatistics.setExceptionQps((long) node.exceptionQps());
        vo.trafficStatistics.setOneMinuteException(node.totalException());
        vo.trafficStatistics.setOneMinutePass(node.totalRequest() - node.blockRequest());
        vo.trafficStatistics.setOneMinuteBlock(node.blockRequest());
        vo.trafficStatistics.setOneMinuteTotal(node.totalRequest());
        vo.timestamp = System.currentTimeMillis();
        return vo;
    }

    /**
     * {@link ClusterNode} holds total statistics of the same resource name.
     *
     * @param name resource name.
     * @param node the ClusterNode to be presented.
     * @return node view object.
     */
    public static NodeVo fromClusterNode(ResourceWrapper name, ClusterNode node) {
        return fromClusterNode(name.getShowName(), node);
    }

    /**
     * {@link ClusterNode} holds total statistics of the same resource name.
     *
     * @param name resource name.
     * @param node the ClusterNode to be presented.
     * @return node view object.
     */
    public static NodeVo fromClusterNode(String name, ClusterNode node) {
        if (node == null) {
            return null;
        }
        NodeVo vo = new NodeVo();
        vo.trafficStatistics.setResource(name);
        vo.trafficStatistics.setThreadNum(node.curThreadNum());
        vo.trafficStatistics.setPassQps((long) node.passQps());
        vo.trafficStatistics.setBlockQps((long) node.blockQps());
        vo.trafficStatistics.setTotalQps((long) node.totalQps());
        vo.trafficStatistics.setAverageRt((long) node.avgRt());
        vo.trafficStatistics.setSuccessQps((long) node.successQps());
        vo.trafficStatistics.setExceptionQps((long) node.exceptionQps());
        vo.trafficStatistics.setOneMinuteException(node.totalException());
        vo.trafficStatistics.setOneMinutePass(node.totalRequest() - node.blockRequest());
        vo.trafficStatistics.setOneMinuteBlock(node.blockRequest());
        vo.trafficStatistics.setOneMinuteTotal(node.totalRequest());
        vo.timestamp = System.currentTimeMillis();
        return vo;
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

    public Long getOneMinuteException() {
        return trafficStatistics.getOneMinuteException();
    }

    public void setOneMinuteException(Long oneMinuteException) {
        this.trafficStatistics.setOneMinuteException(oneMinuteException);
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

    public Long getOneMinuteTotal() {
        return trafficStatistics.getOneMinuteTotal();
    }

    public void setOneMinuteTotal(Long oneMinuteTotal) {
        this.trafficStatistics.setOneMinuteTotal(oneMinuteTotal);
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
