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

import java.util.UUID;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

/**
 * This class is view object of {@link DefaultNode} or {@link ClusterNode}.
 *
 * @author leyou
 */
public class NodeVo {

    private String id;
    private String parentId;
    private String resource;

    private Integer threadNum;
    private Long passQps;
    private Long blockQps;
    private Long totalQps;
    private Long averageRt;
    private Long successQps;
    private Long exceptionQps;
    private Long oneMinutePass;
    private Long oneMinuteBlock;
    private Long oneMinuteException;
    private Long oneMinuteTotal;

    private Long timestamp;

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
        vo.id = UUID.randomUUID().toString();
        vo.parentId = parentId;
        vo.resource = node.getId().getShowName();
        vo.threadNum = node.curThreadNum();
        vo.passQps = (long) node.passQps();
        vo.blockQps = (long) node.blockQps();
        vo.totalQps = (long) node.totalQps();
        vo.averageRt = (long) node.avgRt();
        vo.successQps = (long) node.successQps();
        vo.exceptionQps = (long) node.exceptionQps();
        vo.oneMinuteException = node.totalException();
        vo.oneMinutePass = node.totalRequest() - node.blockRequest();
        vo.oneMinuteBlock = node.blockRequest();
        vo.oneMinuteTotal = node.totalRequest();
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
        vo.resource = name;
        vo.threadNum = node.curThreadNum();
        vo.passQps = (long) node.passQps();
        vo.blockQps = (long) node.blockQps();
        vo.totalQps = (long) node.totalQps();
        vo.averageRt = (long) node.avgRt();
        vo.successQps = (long) node.successQps();
        vo.exceptionQps = (long) node.exceptionQps();
        vo.oneMinuteException = node.totalException();
        vo.oneMinutePass = node.totalRequest() - node.blockRequest();
        vo.oneMinuteBlock = node.blockRequest();
        vo.oneMinuteTotal = node.totalRequest();
        vo.timestamp = System.currentTimeMillis();
        return vo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Integer getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
    }

    public Long getPassQps() {
        return passQps;
    }

    public void setPassQps(Long passQps) {
        this.passQps = passQps;
    }

    public Long getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(Long blockQps) {
        this.blockQps = blockQps;
    }

    public Long getTotalQps() {
        return totalQps;
    }

    public void setTotalQps(Long totalQps) {
        this.totalQps = totalQps;
    }

    public Long getAverageRt() {
        return averageRt;
    }

    public void setAverageRt(Long averageRt) {
        this.averageRt = averageRt;
    }

    public Long getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(Long successQps) {
        this.successQps = successQps;
    }

    public Long getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(Long exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public Long getOneMinuteException() {
        return oneMinuteException;
    }

    public void setOneMinuteException(Long oneMinuteException) {
        this.oneMinuteException = oneMinuteException;
    }

    public Long getOneMinutePass() {
        return oneMinutePass;
    }

    public void setOneMinutePass(Long oneMinutePass) {
        this.oneMinutePass = oneMinutePass;
    }

    public Long getOneMinuteBlock() {
        return oneMinuteBlock;
    }

    public void setOneMinuteBlock(Long oneMinuteBlock) {
        this.oneMinuteBlock = oneMinuteBlock;
    }

    public Long getOneMinuteTotal() {
        return oneMinuteTotal;
    }

    public void setOneMinuteTotal(Long oneMinuteTotal) {
        this.oneMinuteTotal = oneMinuteTotal;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
