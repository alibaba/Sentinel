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
package com.alibaba.csp.sentinel.slots.statistic;

import java.util.Collection;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.qlearning.QLearningMetric;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import com.alibaba.csp.sentinel.slots.block.flow.PriorityWaitException;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.spi.SpiOrder;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * <p>
 * A processor slot that dedicates to real time statistics.
 * When entering this slot, we need to separately count the following
 * information:
 * <ul>
 * <li>{@link ClusterNode}: total statistics of a cluster node of the resource ID.</li>
 * <li>Origin node: statistics of a cluster node from different callers/origins.</li>
 * <li>{@link DefaultNode}: statistics for specific resource name in the specific context.</li>
 * <li>Finally, the sum statistics of all entrances.</li>
 * </ul>
 * </p>
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
@SpiOrder(-7000)
public class StatisticSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    public static int currentState;

    public static double successQPS;
    public static double avg_RT;

    public static double currentUtility;
    public static double nextUtility;
    public static double utilityIncrease;



    public static double alpha = 100;
    public static double beta = 1;

    public static double delta = 1;
    public static double gamma = 1;

    public static int rewardValue = 10;
    public static int punishValue = -1;

    QLearningMetric qLearningMetric = QLearningMetric.getInstance();

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count,
                      boolean prioritized, Object... args) throws Throwable {
        try {
//
//            final long l = System.currentTimeMillis();
//            final int i = (int)( l % 100 );
//            if (i > 90) {
//                testS.setTest(200.00);
//            }
////            System.out.println(node.getUtilityIncrease());
////            System.out.println(Constants.ENTRY_NODE.getUtilityIncrease());
//            System.out.println(testS.getTest());
//            System.out.println("**************************************************************");
//            System.out.println("avgRT " + Constants.ENTRY_NODE.avgRt());
//            System.out.println("avg_RT " + Constants.ENTRY_NODE.avgRt());



            // Do some checking.
            fireEntry(context, resourceWrapper, node, count, prioritized, args);

            // Request passed, add thread count and pass count.
            node.increaseThreadNum();
            node.addPassRequest(count);

            if (context.getCurEntry().getOriginNode() != null) {
                // Add count for origin node.
                context.getCurEntry().getOriginNode().increaseThreadNum();
                context.getCurEntry().getOriginNode().addPassRequest(count);
            }

            if (resourceWrapper.getEntryType() == EntryType.IN) {
                // Add count for global inbound entry node for global statistics.
                Constants.ENTRY_NODE.increaseThreadNum();
                Constants.ENTRY_NODE.addPassRequest(count);
            }

            // Handle pass event with registered entry callback handlers.
            for (ProcessorSlotEntryCallback<DefaultNode> handler : StatisticSlotCallbackRegistry.getEntryCallbacks()) {
                handler.onPass(context, resourceWrapper, node, count, args);
            }

//            System.out.println("_______Accept________");
//            System.out.println("Accept________________ successQps " + Constants.ENTRY_NODE.successQps());
//            System.out.println(testS.getTest());

        } catch (PriorityWaitException ex) {
            node.increaseThreadNum();
            if (context.getCurEntry().getOriginNode() != null) {
                // Add count for origin node.
                context.getCurEntry().getOriginNode().increaseThreadNum();
            }

            if (resourceWrapper.getEntryType() == EntryType.IN) {
                // Add count for global inbound entry node for global statistics.
                Constants.ENTRY_NODE.increaseThreadNum();
            }
            // Handle pass event with registered entry callback handlers.
            for (ProcessorSlotEntryCallback<DefaultNode> handler : StatisticSlotCallbackRegistry.getEntryCallbacks()) {
                handler.onPass(context, resourceWrapper, node, count, args);
            }
        } catch (BlockException e) {

            // Blocked, set block exception to current entry.
            context.getCurEntry().setBlockError(e);

            // Add block count.
            node.increaseBlockQps(count);
            if (context.getCurEntry().getOriginNode() != null) {
                context.getCurEntry().getOriginNode().increaseBlockQps(count);
            }

            if (resourceWrapper.getEntryType() == EntryType.IN) {
                // Add count for global inbound entry node for global statistics.
                Constants.ENTRY_NODE.increaseBlockQps(count);
            }

            // Handle block event with registered entry callback handlers.
            for (ProcessorSlotEntryCallback<DefaultNode> handler : StatisticSlotCallbackRegistry.getEntryCallbacks()) {
                handler.onBlocked(e, context, resourceWrapper, node, count, args);
            }

//            System.out.println("_______Block________");
//            System.out.println("Block___________ SuccessQps " + Constants.ENTRY_NODE.successQps());
//            System.out.println(testS.getTest());

            throw e;

        } catch (Throwable e) {
            // Unexpected internal error, set error to current entry.
            context.getCurEntry().setError(e);

            throw e;
        }
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        Node node = context.getCurNode();

        //写入表之前，还要获取一下currentState哦！

        // 统计Ut = log(QPS) - log(RT)
        successQPS = Constants.ENTRY_NODE.successQps();
        avg_RT = Constants.ENTRY_NODE.avgRt();
        currentUtility = alpha * successQPS - beta * avg_RT;
//        System.out.println("  ");
//        System.out.println("*******************************************************" + currentUtility );
//        System.out.println("CU = " + currentUtility );
//        System.out.println("avgRT " + Constants.ENTRY_NODE.avgRt());

        if (context.getCurEntry().getBlockError() == null) {
            // Calculate response time (use completeStatTime as the time of completion).
            long completeStatTime = TimeUtil.currentTimeMillis();
            context.getCurEntry().setCompleteTimestamp(completeStatTime);
            long rt = completeStatTime - context.getCurEntry().getCreateTimestamp();

            Throwable error = context.getCurEntry().getError();

            // Record response time and success count.
            recordCompleteFor(node, count, rt, error);
            recordCompleteFor(context.getCurEntry().getOriginNode(), count, rt, error);
            if (resourceWrapper.getEntryType() == EntryType.IN) {
                recordCompleteFor(Constants.ENTRY_NODE, count, rt, error);
            }


            if(this.qLearningMetric.isTrain()) {
                // 记录当前的增量。
                this.qLearningMetric.addTrainNum();
                if (this.qLearningMetric.getTrainNum() <= this.qLearningMetric.getMaxTrainNum()) {
                    recordUtilityIncrease();
                    updateQ();
                } else {
                    this.qLearningMetric.setTrain(false);
                    System.out.println("-------------------TRAINING END--------------------");
                    this.qLearningMetric.showPolicy();
                    System.out.println(" ");
                }
            }
            else{

            }


//            System.out.println( "_____Accept____ " + nextUtility + "               CU = " + currentUtility);
//            System.out.println(this.testS.getTest());
        } else {
            // 记录当前的增量。
            this.qLearningMetric.addTrainNum();
            if(this.qLearningMetric.getTrainNum() < this.qLearningMetric.getMaxTrainNum()){
                recordUtilityIncrease();
                updateQ();
            }
            else{
                System.out.println("-------------------TRAINING END--------------------");
                this.qLearningMetric.showPolicy();
            }
//            System.out.println("_____Block_____ " + nextUtility + "                CU = " + currentUtility);
//            System.out.println(this.testS.getTest());
//            System.out.println("Block___________ avgRT " + Constants.ENTRY_NODE.avgRt());
        }

        // Handle exit event with registered exit callback handlers.
        Collection<ProcessorSlotExitCallback> exitCallbacks = StatisticSlotCallbackRegistry.getExitCallbacks();
        for (ProcessorSlotExitCallback handler : exitCallbacks) {
            handler.onExit(context, resourceWrapper, count, args);
        }

        fireExit(context, resourceWrapper, count);
    }

    private void recordCompleteFor(Node node, int batchCount, long rt, Throwable error) {
        if (node == null) {
            return;
        }
        node.addRtAndSuccess(rt, batchCount);
        node.decreaseThreadNum();

        if (error != null && !(error instanceof BlockException)) {
            node.increaseExceptionQps(batchCount);
        }
    }

    private void recordUtilityIncrease() {
        //计算效用增量
        successQPS = Constants.ENTRY_NODE.successQps();
        avg_RT = Constants.ENTRY_NODE.avgRt();
        nextUtility = alpha * successQPS - beta * avg_RT;

        utilityIncrease = nextUtility - currentUtility;

//        System.out.println( "_____Accept____ " + nextUtility + "               CU = " + currentUtility);

        this.qLearningMetric.setUtilityIncrease(utilityIncrease);
    }

    private void updateQ(){
        int reward = getReward();
        int state = this.qLearningMetric.getState();
        int action = this.qLearningMetric.getAction();
        double q = this.qLearningMetric.getQValue(state,action);
        //执行action之后的下一个state属于哪个state。
//            locateNextState();

        double cpuUsage = SystemRuleManager.getCurrentCpuUsage();
        int nextState = SystemRuleManager.locateState(cpuUsage);

        double maxQ = this.qLearningMetric.getmaxQ(nextState);

        double qValue = q + delta * (reward + gamma * maxQ - q);
        this.qLearningMetric.setQ(state, action, qValue);
    }

    private int getReward() {
        if (this.qLearningMetric.getUtilityIncrease() >= 0) {
            return rewardValue;
        } else {
            return punishValue;
        }
    }



}
