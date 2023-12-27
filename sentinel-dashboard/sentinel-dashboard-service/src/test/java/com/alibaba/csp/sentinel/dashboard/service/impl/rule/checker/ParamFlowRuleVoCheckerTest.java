package com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker;

import com.alibaba.csp.sentinel.dashboard.service.api.exception.DashboardServiceException;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.AddParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.DeleteParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.UpdateParamFlowRuleReqVo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test cases for {@link ParamFlowRuleVoChecker}.
 *
 * @author cdfive
 */
public class ParamFlowRuleVoCheckerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    // region Test cases for ParamFlowRuleVo#checkAdd
    @Test
    public void testCheckAdd_body_null() {
        AddParamFlowRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_app_blank() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be blank");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_resource_blank() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("resource can't be blank");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_grade_null() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade can't be null");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_grade_invalid() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade in parameter flow control must be 1");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_paramIdx_null() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("paramIdx can't be null");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_paramIdx_invalid() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(-1);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("paramIdx must be greater than or equal to 0");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_count_null() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count can't be null");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_count_invalid() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(-1D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count must be greater than or equal to 0");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_durationInSec_null() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("durationInSec can't be null");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_durationInSec_invalid() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(-1L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("durationInSec must be greater than or equal to 0");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_clusterMode_null() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("clusterMode can't be null");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_clusterConfig_null() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(true);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("clusterConfig can't be null");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_thresholdType_null() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(true);
        AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfigReqVo = new AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("thresholdType can't be null");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_thresholdType_invalid() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(true);
        AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfigReqVo = new AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("thresholdType must be 0 or 1, but 2 got");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_fallbackToLocalWhenFail_null() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(true);
        AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfigReqVo = new AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(1);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("fallbackToLocalWhenFail can't be null");
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_pass() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(false);
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }

    @Test
    public void testCheckAdd_pass_clusterMode() {
        AddParamFlowRuleReqVo reqVo = new AddParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setResource("/order/list");
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(true);
        AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfigReqVo = new AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(1);
        clusterConfigReqVo.setFallbackToLocalWhenFail(true);
        ParamFlowRuleVoChecker.checkAdd(reqVo);
    }
    // endregion

    // region Test cases for ParamFlowRuleVo#checkUpdate
    @Test
    public void testCheckUpdate_body_null() {
        UpdateParamFlowRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_app_blank() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be blank");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_id_null() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id can't be null");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_id_invalid() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(0L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id must be greater than 0");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_grade_null() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade can't be null");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_grade_invalid() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("grade in parameter flow control must be 1");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_paramIdx_null() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("paramIdx can't be null");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_paramIdx_invalid() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(-1);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("paramIdx must be greater than or equal to 0");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_count_null() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count can't be null");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_count_invalid() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(-1D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("count must be greater than or equal to 0");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_durationInSec_null() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("durationInSec can't be null");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_durationInSec_invalid() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(-1L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("durationInSec must be greater than or equal to 0");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_clusterMode_null() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("clusterMode can't be null");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_clusterConfig_null() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(true);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("clusterConfig can't be null");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_thresholdType_null() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(true);
        AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfigReqVo = new AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("thresholdType can't be null");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_thresholdType_invalid() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(true);
        AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfigReqVo = new AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(2);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("thresholdType must be 0 or 1, but 2 got");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_fallbackToLocalWhenFail_null() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(true);
        AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfigReqVo = new AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(0);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("fallbackToLocalWhenFail can't be null");
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_pass() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(false);
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }

    @Test
    public void testCheckUpdate_pass_clusterMode() {
        UpdateParamFlowRuleReqVo reqVo = new UpdateParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        reqVo.setGrade(1);
        reqVo.setParamIdx(0);
        reqVo.setCount(5D);
        reqVo.setDurationInSec(1L);
        reqVo.setClusterMode(true);
        AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo clusterConfigReqVo = new AddParamFlowRuleReqVo.ParamFlowClusterConfigReqVo();
        reqVo.setClusterConfig(clusterConfigReqVo);
        clusterConfigReqVo.setThresholdType(0);
        clusterConfigReqVo.setFallbackToLocalWhenFail(true);
        ParamFlowRuleVoChecker.checkUpdate(reqVo);
    }
    // endregion

    // region Test cases for ParamFlowRuleVo#checkDelete
    @Test
    public void testCheckDelete_body_null() {
        DeleteParamFlowRuleReqVo reqVo = null;
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("body can't be null");
        ParamFlowRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_app_blank() {
        DeleteParamFlowRuleReqVo reqVo = new DeleteParamFlowRuleReqVo();
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("app can't be blank");
        ParamFlowRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_id_null() {
        DeleteParamFlowRuleReqVo reqVo = new DeleteParamFlowRuleReqVo();
        reqVo.setApp("order");
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id can't be null");
        ParamFlowRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_id_invalid() {
        DeleteParamFlowRuleReqVo reqVo = new DeleteParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(0L);
        exception.expect(DashboardServiceException.class);
        exception.expectMessage("id must be greater than 0");
        ParamFlowRuleVoChecker.checkDelete(reqVo);
    }

    @Test
    public void testCheckDelete_pass() {
        DeleteParamFlowRuleReqVo reqVo = new DeleteParamFlowRuleReqVo();
        reqVo.setApp("order");
        reqVo.setId(1001L);
        ParamFlowRuleVoChecker.checkDelete(reqVo);
    }
    // endregion
}
