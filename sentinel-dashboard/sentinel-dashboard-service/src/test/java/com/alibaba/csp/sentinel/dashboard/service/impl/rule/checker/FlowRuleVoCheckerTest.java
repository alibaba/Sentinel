/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker;

import com.alibaba.csp.sentinel.dashboard.service.api.exception.DashboardServiceException;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.DeleteFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.UpdateFlowRuleReqVo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test cases for {@link FlowRuleVoChecker}.
 *
 * @author cdfive
 */
public class FlowRuleVoCheckerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    // region Test cases for FlowRuleVoChecker#checkAdd
    @Test
    public void testCheckAdd_body_null() {
        AddFlowRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_app_blank() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be blank");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_resource_blank() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("resource can't be blank");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_limitApp_blank() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("limitApp can't be blank");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_grade_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade can't be null");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_grade_invalid() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade must be 0 or 1, but 2 got");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_count_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count can't be null");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_count_invalid() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(-5D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count must be greater than or equal to 0");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_strategy_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("strategy can't be null");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_strategy_invalid() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(3);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("strategy must be 0, 1 or 2, but 3 got");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_strategy_relate_refResource_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(1);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("refResource can't be null or empty when strategy is 1 or 2");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_strategy_chain_refResource_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("refResource can't be null or empty when strategy is 1 or 2");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_controlBehavior_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("controlBehavior can't be null");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_controlBehavior_invalid() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(3);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("controlBehavior must be 0, 1 or 2, but 3 got");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_controlBehavior_warmUp_warmUpPeriodSec_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(1);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("warmUpPeriodSec can't be null when controlBehavior is 1");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_controlBehavior_warmUp_warmUpPeriodSec_invalid() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(1);
        reqVo.setWarmUpPeriodSec(-1);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("warmUpPeriodSec must be greater than 0 when controlBehavior is 1");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_controlBehavior_rateLimiter_maxQueueingTimeMs_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("maxQueueingTimeMs can't be null when controlBehavior is 2");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_controlBehavior_rateLimiter_maxQueueingTimeMs_invalid() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(2);
        reqVo.setMaxQueueingTimeMs(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("maxQueueingTimeMs must be greater than 0 when controlBehavior is 2");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_clusterMode_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("clusterMode can't be null");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_clusterMode_clusterConfig_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(true);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("clusterConfig can't be null");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_clusterMode_thresholdType_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(true);
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = new AddFlowRuleReqVo.ClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("thresholdType can't be null");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_clusterMode_thresholdType_invalid() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(true);
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = new AddFlowRuleReqVo.ClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("thresholdType must be 0 or 1, but 2 got");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_clusterMode_fallbackToLocalWhenFail_null() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(true);
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = new AddFlowRuleReqVo.ClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("fallbackToLocalWhenFail can't be null");
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_pass_grade_thread() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(false);
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_pass_grade_qps() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(1);
        reqVo.setCount(100D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(false);
        FlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_pass_clusterMode() {
        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(true);
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = new AddFlowRuleReqVo.ClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(0);
        clusterConfigReqVo.setFallbackToLocalWhenFail(true);
        FlowRuleVoChecker.checkAdd(reqVo);
    }
    // endregion Test cases for FlowRuleVoChecker#checkAdd

    // region Test cases for FlowRuleVoChecker#checkUpdate
    @Test
    public void testCheckUpdate_body_null() {
        UpdateFlowRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_app_blank() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be blank");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_id_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id can't be null");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_id_invalid() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(-1L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id must be greater than 0");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_limitApp_blank() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("limitApp can't be blank");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_grade_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade can't be null");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_grade_invalid() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade must be 0 or 1, but 2 got");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_count_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count can't be null");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_count_invalid() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(-5D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count must be greater than or equal to 0");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_strategy_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("strategy can't be null");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_strategy_invalid() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(3);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("strategy must be 0, 1 or 2, but 3 got");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_strateg_relate_refResource_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(1);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("refResource can't be null or empty when strategy is 1 or 2");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_strateg_chain_refResource_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("refResource can't be null or empty when strategy is 1 or 2");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_controlBehavior_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("controlBehavior can't be null");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_controlBehavior_invalid() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(3);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("controlBehavior must be 0, 1 or 2, but 3 got");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_controlBehavior_warmUp_warmUpPeriodSec_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(1);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("warmUpPeriodSec can't be null when controlBehavior is 1");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_controlBehavior_warmUp_warmUpPeriodSec_invalid() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(1);
        reqVo.setWarmUpPeriodSec(-1);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("warmUpPeriodSec must be greater than 0 when controlBehavior is 1");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_controlBehavior_rateLimiter_maxQueueingTimeMs_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("maxQueueingTimeMs can't be null when controlBehavior is 2");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_controlBehavior_rateLimiter_maxQueueingTimeMs_invalid() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(2);
        reqVo.setMaxQueueingTimeMs(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("maxQueueingTimeMs must be greater than 0 when controlBehavior is 2");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_clusterMode_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("clusterMode can't be null");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_clusterMode_clusterConfig_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(true);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("clusterConfig can't be null");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_clusterMode_thresholdType_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(true);
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = new AddFlowRuleReqVo.ClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("thresholdType can't be null");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_clusterMode_thresholdType_invalid() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(true);
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = new AddFlowRuleReqVo.ClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("thresholdType must be 0 or 1, but 2 got");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_clusterMode_fallbackToLocalWhenFail_null() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(true);
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = new AddFlowRuleReqVo.ClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("fallbackToLocalWhenFail can't be null");
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_pass_grade_thread() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(false);
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_pass_grade_qps() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(1);
        reqVo.setCount(100D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(false);
        FlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_pass_clusterMode() {
        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setLimitApp("default");
        reqVo.setGrade(0);
        reqVo.setCount(5D);
        reqVo.setStrategy(0);
        reqVo.setControlBehavior(0);
        reqVo.setClusterMode(true);
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = new AddFlowRuleReqVo.ClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(0);
        clusterConfigReqVo.setFallbackToLocalWhenFail(true);
        FlowRuleVoChecker.checkUpdate(reqVo);
    }
    // endregion Test cases for FlowRuleVoChecker#checkUpdate

    // region Test cases for FlowRuleVoChecker#checkDelete
    @Test
    public void testCheckDelete_body_null() {
        DeleteFlowRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        FlowRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_app_blank() {
        DeleteFlowRuleReqVo reqVo = new DeleteFlowRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be blank");
        FlowRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_id_null() {
        DeleteFlowRuleReqVo reqVo = new DeleteFlowRuleReqVo();
        reqVo.setApp("product");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id can't be null");
        FlowRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_id_invalid() {
        DeleteFlowRuleReqVo reqVo = new DeleteFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(-1L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id must be greater than 0");
        FlowRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_pass() {
        DeleteFlowRuleReqVo reqVo = new DeleteFlowRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        FlowRuleVoChecker.checkDelete(reqVo);
    }
    // endregion Test cases for FlowRuleVoChecker#checkDelete
}
