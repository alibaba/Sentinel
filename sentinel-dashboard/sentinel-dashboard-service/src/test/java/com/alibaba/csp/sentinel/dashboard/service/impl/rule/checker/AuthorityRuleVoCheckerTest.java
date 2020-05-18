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
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.AddAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.DeleteAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.UpdateAuthorityReqVo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test cases for {@link AuthorityRuleVoChecker}.
 *
 * @author cdfive
 */
public class AuthorityRuleVoCheckerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    // region Test cases for AuthorityRuleVoChecker#checkAdd
    @Test
    public void testCheckAdd_body_null() {
        AddAuthorityReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        AuthorityRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_app_null() {
        AddAuthorityReqVo reqVo = new AddAuthorityReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be null or empty");
        AuthorityRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_resource_null() {
        AddAuthorityReqVo reqVo = new AddAuthorityReqVo();
        reqVo.setApp("order");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("resource can't be null or empty");
        AuthorityRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_limitApp_null() {
        AddAuthorityReqVo reqVo = new AddAuthorityReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/confirm");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("limitApp can't be null or empty");
        AuthorityRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_strategy_null() {
        AddAuthorityReqVo reqVo = new AddAuthorityReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/confirm");
        reqVo.setLimitApp("product");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("strategy can't be null");
        AuthorityRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_strategy_invalid() {
        AddAuthorityReqVo reqVo = new AddAuthorityReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/confirm");
        reqVo.setLimitApp("product");
        reqVo.setStrategy(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("strategy must be 0 or 1, but 2 got");
        AuthorityRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_pass() {
        AddAuthorityReqVo reqVo = new AddAuthorityReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/confirm");
        reqVo.setLimitApp("product");
        reqVo.setStrategy(0);
        AuthorityRuleVoChecker.checkAdd(reqVo);
    }
    // endregion Test cases for AuthorityRuleVoChecker#checkAdd

    // region Test cases for AuthorityRuleVoChecker#checkUpdate
    @Test
    public void testCheckUpdate_body_null() {
        UpdateAuthorityReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        AuthorityRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_app_null() {
        UpdateAuthorityReqVo reqVo = new UpdateAuthorityReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be null or empty");
        AuthorityRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_limitApp_null() {
        UpdateAuthorityReqVo reqVo = new UpdateAuthorityReqVo();
        reqVo.setApp("order");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("limitApp can't be null or empty");
        AuthorityRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_strategy_null() {
        UpdateAuthorityReqVo reqVo = new UpdateAuthorityReqVo();
        reqVo.setApp("order");
        reqVo.setLimitApp("product");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("strategy can't be null");
        AuthorityRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_strategy_invalid() {
        UpdateAuthorityReqVo reqVo = new UpdateAuthorityReqVo();
        reqVo.setApp("order");
        reqVo.setLimitApp("product");
        reqVo.setStrategy(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("strategy must be 0 or 1, but 2 got");
        AuthorityRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_pass() {
        UpdateAuthorityReqVo reqVo = new UpdateAuthorityReqVo();
        reqVo.setApp("order");
        reqVo.setLimitApp("product");
        reqVo.setStrategy(0);
        AuthorityRuleVoChecker.checkUpdate(reqVo);
    }
    // endregion Test cases for AuthorityRuleVoChecker#checkUpdate

    // region Test cases for AuthorityRuleVoChecker#checkDelete
    @Test
    public void testCheckDelete_body_null() {
        DeleteAuthorityReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        AuthorityRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_app_null() {
        DeleteAuthorityReqVo reqVo = new DeleteAuthorityReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be null or empty");
        AuthorityRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_id_null() {
        DeleteAuthorityReqVo reqVo = new DeleteAuthorityReqVo();
        reqVo.setApp("order");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id can't be null");
        AuthorityRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_id_invalid() {
        DeleteAuthorityReqVo reqVo = new DeleteAuthorityReqVo();
        reqVo.setApp("order");
        reqVo.setId(0L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id must be greater than 0");
        AuthorityRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_pass() {
        DeleteAuthorityReqVo reqVo = new DeleteAuthorityReqVo();
        reqVo.setApp("order");
        reqVo.setId(10001L);
        AuthorityRuleVoChecker.checkDelete(reqVo);
    }
    // endregion Test cases for AuthorityRuleVoChecker#checkDelete
}
