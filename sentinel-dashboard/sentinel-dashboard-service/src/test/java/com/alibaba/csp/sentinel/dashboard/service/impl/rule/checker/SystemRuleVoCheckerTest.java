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
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.AddSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.DeleteSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.UpdateSystemRuleReqVo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test cases for {@link SystemRuleVoChecker}.
 *
 * @author cdfive
 */
public class SystemRuleVoCheckerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    // region Test cases for SystemRuleVoChecker#checkAdd
    @Test
    public void testCheckAdd_body_null() {
        AddSystemRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        SystemRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_app_blank() {
        AddSystemRuleReqVo reqVo = new AddSystemRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be blank");
        SystemRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_app_all_null() {
        AddSystemRuleReqVo reqVo = new AddSystemRuleReqVo();
        reqVo.setApp("order");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("highestSystemLoad,avgRt,maxThread,qps,highestCpuUsage can't be all null");
        SystemRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_pass() {
        AddSystemRuleReqVo reqVo = new AddSystemRuleReqVo();
        reqVo.setApp("order");
        reqVo.setQps(500D);
        SystemRuleVoChecker.checkAdd(reqVo);
    }
    // endregion Test cases for SystemRuleVoChecker#checkAdd

    // region Test cases for SystemRuleVoChecker#checkUpdate
    @Test
    public void testCheckUpdate_body_null() {
        UpdateSystemRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        SystemRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_app_blank() {
        UpdateSystemRuleReqVo reqVo = new UpdateSystemRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be blank");
        SystemRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_id_null() {
        UpdateSystemRuleReqVo reqVo = new UpdateSystemRuleReqVo();
        reqVo.setApp("order");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id can't be null");
        SystemRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_id_invalid() {
        UpdateSystemRuleReqVo reqVo = new UpdateSystemRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(0L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id must be greater than 0");
        SystemRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_all_null() {
        UpdateSystemRuleReqVo reqVo = new UpdateSystemRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(10001L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("highestSystemLoad,avgRt,maxThread,qps,highestCpuUsage can't be all null");
        SystemRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_pass() {
        UpdateSystemRuleReqVo reqVo = new UpdateSystemRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(10001L);
        reqVo.setQps(500D);
        SystemRuleVoChecker.checkUpdate(reqVo);
    }
    // endregion Test cases for SystemRuleVoChecker#checkUpdate

    // region Test cases for SystemRuleVoChecker#checkDelete
    @Test
    public void testCheckDelete_body_null() {
        DeleteSystemRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        SystemRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_app_blank() {
        DeleteSystemRuleReqVo reqVo = new DeleteSystemRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be blank");
        SystemRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_id_null() {
        DeleteSystemRuleReqVo reqVo = new DeleteSystemRuleReqVo();
        reqVo.setApp("order");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id can't be null");
        SystemRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_id_invliad() {
        DeleteSystemRuleReqVo reqVo = new DeleteSystemRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(0L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id must be greater than 0");
        SystemRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_pass() {
        DeleteSystemRuleReqVo reqVo = new DeleteSystemRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(10001L);
        SystemRuleVoChecker.checkDelete(reqVo);
    }
    // endregion Test cases for SystemRuleVoChecker#checkDelete
}

