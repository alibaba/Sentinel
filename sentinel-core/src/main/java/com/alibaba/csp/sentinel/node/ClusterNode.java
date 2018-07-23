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

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * <p>
 * This class stores summary runtime statistics of the resource, including rt, thread count, qps
 * and so on. Same resource shares the same {@link ClusterNode} globally, no matter in witch
 * {@link com.alibaba.csp.sentinel.context.Context}.
 * </p>
 * <p>
 * To distinguish invocation from different origin (declared in
 * {@link ContextUtil#enter(String name, String origin)}),
 * one {@link ClusterNode} holds an {@link #originCountMap}, this map holds {@link StatisticNode}
 * of different origin. Use {@link #getOriginNode(String)} to get {@link Node} of the specific
 * origin.<br/>
 * Note that 'origin' usually is Service Consumer's app name.
 * </p>
 *
 * @author qinan.qn
 * @author jialiang.linjl
 */
public class ClusterNode extends StatisticNode {

    /**
     * <p>
     * the longer the application runs, the more stable this mapping will
     * become. so we don't concurrent map but a lock. as this lock only happens
     * at the very beginning while concurrent map will hold the lock all the
     * time
     * </p>
     */
    private HashMap<String, StatisticNode> originCountMap = new HashMap<String, StatisticNode>();
    private ReentrantLock lock = new ReentrantLock();

    /**
     * Get {@link Node} of the specific origin. Usually the origin is the Service Consumer's app name.
     *
     * @param origin The caller's name. It is declared in the
     *               {@link ContextUtil#enter(String name, String origin)}.
     * @return the {@link Node} of the specific origin.
     */
    public Node getOriginNode(String origin) {
        StatisticNode statisticNode = originCountMap.get(origin);
        if (statisticNode == null) {
            try {
                lock.lock();
                statisticNode = originCountMap.get(origin);
                if (statisticNode == null) {
                    statisticNode = new StatisticNode();
                    HashMap<String, StatisticNode> newMap = new HashMap<String, StatisticNode>(
                        originCountMap.size() + 1);
                    newMap.putAll(originCountMap);
                    newMap.put(origin, statisticNode);
                    originCountMap = newMap;
                }
            } finally {
                lock.unlock();
            }
        }
        return statisticNode;
    }

    public synchronized HashMap<String, StatisticNode> getOriginCountMap() {
        return originCountMap;
    }

    /**
     * Add exception count only when {@code throwable} is not {@link BlockException#isBlockException(Throwable)}
     *
     * @param throwable
     * @param count     count to add.
     */
    public void trace(Throwable throwable, int count) {
        if (!BlockException.isBlockException(throwable)) {
            for (int i = 0; i < count; i++) {
                this.increaseExceptionQps();
            }
        }
    }
}
