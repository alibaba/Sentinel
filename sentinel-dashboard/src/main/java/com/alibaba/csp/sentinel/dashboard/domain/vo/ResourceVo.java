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
package com.alibaba.csp.sentinel.dashboard.domain.vo;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.command.vo.NodeVo;

import com.alibaba.csp.sentinel.dashboard.domain.ResourceTreeNode;

/**
 * @author leyou
 */
public class ResourceVo {
    private String parentTtId;
    private String ttId;
    private String resource;

    private Integer threadNum;
    private Long passQps;
    private Long blockQps;
    private Long totalQps;
    private Long averageRt;
    private Long passRequestQps;
    private Long exceptionQps;
    private Long oneMinutePass;
    private Long oneMinuteBlock;
    private Long oneMinuteException;
    private Long oneMinuteTotal;

    private boolean visible = true;

    public ResourceVo() {
    }

    public static List<ResourceVo> fromNodeVoList(List<NodeVo> nodeVos) {
        if (nodeVos == null) {
            return null;
        }
        List<ResourceVo> list = new ArrayList<>();
        for (NodeVo nodeVo : nodeVos) {
            ResourceVo vo = new ResourceVo();
            vo.parentTtId = nodeVo.getParentId();
            vo.ttId = nodeVo.getId();
            vo.resource = nodeVo.getResource();
            vo.threadNum = nodeVo.getThreadNum();
            vo.passQps = nodeVo.getPassQps();
            vo.blockQps = nodeVo.getBlockQps();
            vo.totalQps = nodeVo.getTotalQps();
            vo.averageRt = nodeVo.getAverageRt();
            vo.exceptionQps = nodeVo.getExceptionQps();
            vo.oneMinutePass = nodeVo.getOneMinutePass();
            vo.oneMinuteBlock = nodeVo.getOneMinuteBlock();
            vo.oneMinuteException = nodeVo.getOneMinuteException();
            vo.oneMinuteTotal = nodeVo.getOneMinuteTotal();
            list.add(vo);
        }
        return list;
    }

    public static List<ResourceVo> fromResourceTreeNode(ResourceTreeNode root) {
        if (root == null) {
            return null;
        }
        List<ResourceVo> list = new ArrayList<>();
        visit(root, list, false, true);
        //if(!list.isEmpty()){
        //    list.remove(0);
        //}
        return list;
    }

    /**
     * This node is visible when this.visible==true or one of this's parents is visible,
     * root node is always invisible.
     */
    private static void visit(ResourceTreeNode node, List<ResourceVo> list, boolean parentVisible, boolean isRoot) {
        boolean visible = !isRoot && (node.isVisible() || parentVisible);
        //boolean visible = node.isVisible();
        if (visible) {
            ResourceVo vo = new ResourceVo();
            vo.parentTtId = node.getParentId();
            vo.ttId = node.getId();
            vo.resource = node.getResource();
            vo.threadNum = node.getThreadNum();
            vo.passQps = node.getPassQps();
            vo.blockQps = node.getBlockQps();
            vo.totalQps = node.getTotalQps();
            vo.averageRt = node.getAverageRt();
            vo.exceptionQps = node.getExceptionQps();
            vo.oneMinutePass = node.getOneMinutePass();
            vo.oneMinuteBlock = node.getOneMinuteBlock();
            vo.oneMinuteException = node.getOneMinuteException();
            vo.oneMinuteTotal = node.getOneMinuteTotal();
            vo.visible = node.isVisible();
            list.add(vo);
        }
        for (ResourceTreeNode c : node.getChildren()) {
            visit(c, list, visible, false);
        }
    }

    public String getParentTtId() {
        return parentTtId;
    }

    public void setParentTtId(String parentTtId) {
        this.parentTtId = parentTtId;
    }

    public String getTtId() {
        return ttId;
    }

    public void setTtId(String ttId) {
        this.ttId = ttId;
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

    public Long getPassRequestQps() {
        return passRequestQps;
    }

    public void setPassRequestQps(Long passRequestQps) {
        this.passRequestQps = passRequestQps;
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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
