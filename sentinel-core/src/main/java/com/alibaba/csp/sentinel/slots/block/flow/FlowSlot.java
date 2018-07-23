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
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

/**
 * <p>
 * Combined the runtime statistics collected from the previous
 * slots(NodeSelectorSlot, ClusterNodeBuilderSlot, and StatistcSlot), FlowSlot
 * will use pre-set rules to decide whether the incoming requests should be
 * blocked.
 *
 * {@code SphU.entry (resourceName) }will throw FlowException if any rule is
 * triggered. user can customize his own logic by catching FlowException.
 *
 * One resource can have multiple flow rules. FlowSlot traverses these rules
 * until one of them is triggered or all rules have been traversed.
 *
 * Each FlowRule is mainly composed of the 2 factors: grade, strategy, path; we
 * can combine these factors to achieve different effects.
 *
 * The grade is defined by the grade field in FlowRule. Here, 0 for thread
 * isolation and 1 for request count shaping. Both thread count and request
 * count are collected in real runtime, and we can view these statistics by
 * following command: {@code
 * curl http：// localhost：8719 / tree？type = root`
 * idx id    thread pass  blocked   success total aRt   1m-pass   1m-block   1m-all   exeption
 * 2   abc647 0      460    46          46   1    27      630       276        897      0
 * }
 *
 * Thread for the count of threads that is currently processing the resource;
 * pass for the count of incoming request within one second; blocked for the
 * count of requests blocked within one second; success for the count of the
 * requests successfully within one second; RT for the average response time of
 * the requests within a second; total for the sum of incoming requests and
 * blocked requests within one second; 1m-pass is for the count of incoming
 * requests within one minute; 1m-block is for the count of a request blocked
 * within one minute; 1m -all is the total of incoming and blocked requests
 * within 1 minute; exception is for the count of exceptions in one second.
 *
 * This stage is usually used to protect resources from occupying. If a resource
 * takes long time to finish, threads will begin to occupy. The longer the
 * response takes, the more threads occupy.
 *
 * Besides counter, thread pool or semaphore can also be used to achieve this.
 *
 * - Thread pool: Allocate a thread pool to handle these resource. When there is
 * no more idle thread in the pool, the request is rejected without affecting
 * other resources.
 *
 * - Semaphore: Use semaphore to control the concurrent count of the threads in
 * this resource.
 *
 * The benefit of using thread pool is that, it can walk away gracefully when
 * time out. But it also bring us the cost of context switch and additional
 * threads. If the incoming requests is already served in a separated thread,
 * for instance, a servelet request, it will almost double the threads count if
 * using thread pool.
 *
 * ### QPS Shaping ### When qps exceeds the threshold, we will take actions to
 * control the incoming request, and is configured by "controlBehavior" field in
 * flowrule
 *
 * 1. immediately reject（RuleConstant.CONTROL_BEHAVIOR_DEFAULT）
 *
 * This is the default behavior. The exceeded request is rejected immediately
 * and the FlowException is thrown
 *
 * 2. Warmup（RuleConstant.CONTROL_BEHAVIOR_WARM_UP）
 *
 * If the usage of system has been low for a while, and a large amount of
 * requests comes, the system might not be able to handle all these requests at
 * once. However if we steady increase the incoming request, the system can warm
 * up and finally be able to handle all the requests.If the usage of system has
 * been low for a while, and a large amount of requests comes, the system might
 * not be able to handle all these requests at once. However if we steady
 * increase the incoming request, the system can warm up and finally be able to
 * handle all the requests. This warmup period can be configured by setting the
 * field "warmUpPeriodSec" in flow rule.
 *
 * 3.Rate limiter(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER) This strategy
 * strictly controls the interval between requests. In other words, it allows
 * requests to pass at a stable rate.
 * <img src="https://github.com/alibaba/Sentinel/wiki/image/queue.gif" width=
 * "300" height="200" /> This strategy is an implement of leaky bucket
 * (https://en.wikipedia.org/wiki/Leaky_bucket). It is used to handle the
 * request at a stable rate and is often used in burst traffic. For instance,
 * Message. When a large number of requests beyond the system’s capacity arrive
 * at the same time, the system using this strategy will handle requests and its
 * fixed rate until all the requests have been processed or time out.
 *
 * @author jialiang.linjl
 */
public class FlowSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, Object... args)
        throws Throwable {

        FlowRuleManager.checkFlow(resourceWrapper, context, node, count);

        fireEntry(context, resourceWrapper, node, count, args);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        fireExit(context, resourceWrapper, count, args);
    }

}
