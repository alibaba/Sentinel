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
package com.alibaba.csp.sentinel.context;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.EntranceNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot;

/**
 * This class holds metadata of current invocation:<br/>
 *
 * <ul>
 * <li>the {@link EntranceNode}: the root of the current invocation
 * tree.</li>
 * <li>the current {@link Entry}: the current invocation point.</li>
 * <li>the current {@link Node}: the statistics related to the
 * {@link Entry}.</li>
 * <li>the origin:The origin is useful when we want to control different
 * invoker/consumer separately. Usually the origin could be the Service Consumer's app name. </li>
 * </ul>
 * <p>
 * Each {@link SphU}#entry() or {@link SphO}#entry() should be in a {@link Context},
 * if we don't invoke {@link ContextUtil}#enter() explicitly, DEFAULT context will be used.
 * </p>
 * <p>
 * A invocation tree will be created if we invoke {@link SphU}#entry() multi times in
 * the same context.
 * </p>
 * <p>
 * Same resource in different context will count separately, see {@link NodeSelectorSlot}.
 * </p>
 *
 * @author jialiang.linjl
 * @author leyou(lihao)
 * @author Eric Zhao
 * @see ContextUtil
 * @see NodeSelectorSlot
 */
public class Context {

    /**
     * Context name.
     */
    private String name;

    /**
     * The entrance node of current invocation tree.
     */
    private DefaultNode entranceNode;

    /**
     * Current processing entry.
     */
    private Entry curEntry;

    /**
     * the origin of this context, usually the origin is the Service Consumer's app name.
     */
    private String origin = "";

    public Context(DefaultNode entranceNode, String name) {
        super();
        this.name = name;
        this.entranceNode = entranceNode;
    }

    public String getName() {
        return name;
    }

    public Node getCurNode() {
        return curEntry.getCurNode();
    }

    public void setCurNode(Node node) {
        this.curEntry.setCurNode(node);
    }

    public Entry getCurEntry() {
        return curEntry;
    }

    public void setCurEntry(Entry curEntry) {
        this.curEntry = curEntry;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public double getOriginTotalQps() {
        return getOriginNode() == null ? 0 : getOriginNode().totalQps();
    }

    public double getOriginBlockedQps() {
        return getOriginNode() == null ? 0 : getOriginNode().blockedQps();
    }

    public double getOriginPassedReqQps() {
        return getOriginNode() == null ? 0 : getOriginNode().successQps();
    }

    public double getOriginPassedQps() {
        return getOriginNode() == null ? 0 : getOriginNode().passQps();
    }

    public long getOriginTotalRequest() {
        return getOriginNode() == null ? 0 : getOriginNode().totalRequest();
    }

    public long getOriginBlockedRequest() {
        return getOriginNode() == null ? 0 : getOriginNode().blockedRequest();
    }

    public double getOriginAvgRt() {
        return getOriginNode() == null ? 0 : getOriginNode().avgRt();
    }

    public int getOriginCurThreadNum() {
        return getOriginNode() == null ? 0 : getOriginNode().curThreadNum();
    }

    public DefaultNode getEntranceNode() {
        return entranceNode;
    }

    /**
     * Get the parent {@link Node} of the current.
     *
     * @return the parent node of the current.
     */
    public Node getLastNode() {
        if (curEntry != null && curEntry.getLastNode() != null) {
            return curEntry.getLastNode();
        } else {
            return entranceNode;
        }
    }

    public Node getOriginNode() {
        return curEntry == null ? null : curEntry.getOriginNode();
    }
}
