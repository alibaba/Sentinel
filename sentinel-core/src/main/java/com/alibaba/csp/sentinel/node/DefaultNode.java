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
package com.alibaba.csp.sentinel.node;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot;

/**
 * <p>
 * A {@link Node} use to hold statistics for specific resource name in the specific context.
 * Each distinct resource in each distinct {@link Context} will corresponding to a {@link DefaultNode}.
 * </p>
 * <p>
 * This class may have a list of sub {@link DefaultNode}s. sub-node will be created when
 * call {@link SphU}#entry() or {@link SphO}@entry() multi times in the same {@link Context}.
 * </p>
 *
 * @author qinan.qn
 * @see NodeSelectorSlot
 */
public class DefaultNode extends StatisticNode {

    private ResourceWrapper id;

    private volatile HashSet<Node> childList = new HashSet<Node>();

    private ClusterNode clusterNode;

    public DefaultNode(ResourceWrapper id, ClusterNode clusterNode) {
        this.id = id;
        this.clusterNode = clusterNode;
    }

    public ResourceWrapper getId() {
        return id;
    }

    public ClusterNode getClusterNode() {
        return clusterNode;
    }

    public void setClusterNode(ClusterNode clusterNode) {
        this.clusterNode = clusterNode;
    }

    public void addChild(Node node) {

        if (!childList.contains(node)) {

            synchronized (this) {
                if (!childList.contains(node)) {
                    HashSet<Node> newSet = new HashSet<Node>(childList.size() + 1);
                    newSet.addAll(childList);
                    newSet.add(node);
                    childList = newSet;
                }
            }
            RecordLog.info(String.format("Add child %s to %s", ((DefaultNode)node).id.getName(), id.getName()));
        }
    }

    public void removeChildList() {
        this.childList = new HashSet<Node>();
    }

    public Set<Node> getChildList() {
        return childList;
    }

    @Override
    public void increaseBlockQps() {
        super.increaseBlockQps();
        this.clusterNode.increaseBlockQps();
    }

    @Override
    public void increaseExceptionQps() {
        super.increaseExceptionQps();
        this.clusterNode.increaseExceptionQps();
    }

    @Override
    public void rt(long rt) {
        super.rt(rt);
        this.clusterNode.rt(rt);
    }

    @Override
    public void increaseThreadNum() {
        super.increaseThreadNum();
        this.clusterNode.increaseThreadNum();
    }

    @Override
    public void decreaseThreadNum() {
        super.decreaseThreadNum();
        this.clusterNode.decreaseThreadNum();
    }

    @Override
    public void addPassRequest() {
        super.addPassRequest();
        this.clusterNode.addPassRequest();
    }

    public void printDefaultNode() {
        visitTree(0, this);
    }

    private void visitTree(int level, DefaultNode node) {
        for (int i = 0; i < level; ++i) {
            System.out.print("-");
        }
        if (!(node instanceof EntranceNode)) {
            System.out.println(
                String.format("%s(thread:%s pq:%s bq:%s tq:%s rt:%s 1mp:%s 1mb:%s 1mt:%s)", node.id.getShowName(),
                    node.curThreadNum(), node.passQps(), node.blockQps(), node.totalQps(), node.avgRt(),
                    node.totalRequest() - node.blockRequest(), node.blockRequest(), node.totalRequest()));
        } else {
            System.out.println(
                String.format("Entry-%s(t:%s pq:%s bq:%s tq:%s rt:%s 1mp:%s 1mb:%s 1mt:%s)", node.id.getShowName(),
                    node.curThreadNum(), node.passQps(), node.blockQps(), node.totalQps(), node.avgRt(),
                    node.totalRequest() - node.blockRequest(), node.blockRequest(), node.totalRequest()));
        }
        for (Node n : node.getChildList()) {
            DefaultNode dn = (DefaultNode)n;
            visitTree(level + 1, dn);
        }
    }

}
