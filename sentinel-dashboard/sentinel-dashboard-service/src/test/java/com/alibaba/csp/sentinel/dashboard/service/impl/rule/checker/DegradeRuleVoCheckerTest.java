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
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.AddDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.DeleteDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.UpdateDegradeRuleReqVo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test cases for {@link DegradeRuleVoChecker}.
 *
 * @author cdfive
 */
public class DegradeRuleVoCheckerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    // region Test cases for DegradeRuleVoChecker#checkAdd
    @Test
    public void testCheckAdd_body_null() {
        AddDegradeRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_app_null() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be null or empty");
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_resource_null() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        reqVo.setApp("product");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("resource can't be null or empty");
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_grade_null() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade can't be null");
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_grade_invalid() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setGrade(3);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade must be 0, 1 or 2, but 3 got");
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_count_null() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setGrade(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count can't be null");
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_count_invalid() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setGrade(0);
        reqVo.setCount(-1D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count must be greater than or equal to 0");
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_count_invalid_grade_exception_radio() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setGrade(1);
        reqVo.setCount(1.5D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count must be less than 1 when grade is 1");
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_timeWindow_null() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setGrade(0);
        reqVo.setCount(100D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("timeWindow can't be null");
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_timeWindow_invalid() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setGrade(0);
        reqVo.setCount(100D);
        reqVo.setTimeWindow(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("timeWindow must be greater than 0");
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_pass_grade_rt() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setGrade(0);
        reqVo.setCount(100D);
        reqVo.setTimeWindow(30);
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_pass_grade_exception_radio() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setGrade(1);
        reqVo.setCount(0.5D);
        reqVo.setTimeWindow(60);
        DegradeRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_pass_grade_exception_count() {
        AddDegradeRuleReqVo reqVo = new AddDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setResource("/product/list");
        reqVo.setGrade(2);
        reqVo.setCount(50D);
        reqVo.setTimeWindow(10);
        DegradeRuleVoChecker.checkAdd(reqVo);
    }
    // endregion Test cases for DegradeRuleVoChecker#checkAdd

    // region Test cases for DegradeRuleVoChecker#checkUpdate
    @Test
    public void testCheckUpdate_body_null() {
        UpdateDegradeRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_app_null() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be null or empty");
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_id_null() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id can't be null");
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_id_invalid() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(0L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id must be greater than 0");
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_grade_null() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade can't be null");
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_grade_invalid() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setGrade(3);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade must be 0, 1 or 2, but 3 got");
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_count_null() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setGrade(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count can't be null");
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_count_invalid() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setGrade(0);
        reqVo.setCount(-1D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count must be greater than or equal to 0");
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_count_invalid_grade_exception_radio() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setGrade(1);
        reqVo.setCount(1.5D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count must be less than 1 when grade is 1");
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_timeWindow_null() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setGrade(0);
        reqVo.setCount(100D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("timeWindow can't be null");
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_timeWindow_invalid() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setGrade(0);
        reqVo.setCount(100D);
        reqVo.setTimeWindow(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("timeWindow must be greater than 0");
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_pass_grade_rt() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setGrade(0);
        reqVo.setCount(100D);
        reqVo.setTimeWindow(30);
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUupdate_pass_grade_exception_radio() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setGrade(1);
        reqVo.setCount(0.5D);
        reqVo.setTimeWindow(60);
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_pass_grade_exception_count() {
        UpdateDegradeRuleReqVo reqVo = new UpdateDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        reqVo.setGrade(2);
        reqVo.setCount(50D);
        reqVo.setTimeWindow(10);
        DegradeRuleVoChecker.checkUpdate(reqVo);
    }
    // endregion Test cases for DegradeRuleVoChecker#checkUpdate

    // region Test cases for DegradeRuleVoChecker#checkDelete
    @Test
    public void testCheckDelete_body_null() {
        DeleteDegradeRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        DegradeRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_app_null() {
        DeleteDegradeRuleReqVo reqVo = new DeleteDegradeRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be null");
        DegradeRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_id_null() {
        DeleteDegradeRuleReqVo reqVo = new DeleteDegradeRuleReqVo();
        reqVo.setApp("product");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id can't be null");
        DegradeRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_id_invalid() {
        DeleteDegradeRuleReqVo reqVo = new DeleteDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(0L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id must be greater than 0");
        DegradeRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_pass() {
        DeleteDegradeRuleReqVo reqVo = new DeleteDegradeRuleReqVo();
        reqVo.setApp("product");
        reqVo.setId(10001L);
        DegradeRuleVoChecker.checkDelete(reqVo);
    }
    // endregion Test cases for DegradeRuleVoChecker#checkDelete
}
