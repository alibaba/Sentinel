//package com.alibaba.csp.sentinel.dashboard.web.controller.rule;
//
//import com.alibaba.csp.sentinel.dashboard.entity.rule.DegradeRuleEntity;
//import com.alibaba.csp.sentinel.dashboard.web.auth.AuthAction;
//import com.alibaba.csp.sentinel.dashboard.web.auth.AuthService;
//import com.alibaba.csp.sentinel.dashboard.web.domain.Result;
//import com.alibaba.csp.sentinel.dashboard.web.vo.req.MachineReqVo;
//import com.alibaba.csp.sentinel.dashboard.web.vo.req.rule.degrade.AddDegradeRuleReqVo;
//import com.alibaba.csp.sentinel.dashboard.web.vo.req.rule.degrade.UpdateDegradeRuleReqVo;
//import com.alibaba.csp.sentinel.util.StringUtil;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Date;
//import java.util.List;
//
///**
// * @author cdfive
// */
//@RestController
//@RequestMapping("/degrade")
//public class DegradeRuleController extends BaseRuleController<DegradeRuleEntity> {
//
//
//    @GetMapping("/rules")
//    @AuthAction(AuthService.PrivilegeType.READ_RULE)
//    public Result<List<DegradeRuleEntity>> queryDegradeRuleList(MachineReqVo reqVo) throws Exception{
//        List<DegradeRuleEntity> rules = queryRuleList(reqVo);
//        return Result.ofSuccess(rules);
//    }
//
//    @PostMapping("/rule")
//    @AuthAction(value = AuthService.PrivilegeType.WRITE_RULE)
//    public Result<DegradeRuleEntity> addFlowRule(@RequestBody AddDegradeRuleReqVo reqVo) throws Exception {
//        if (reqVo == null) {
//            return Result.ofFail(-1, "invalid body");
//        }
//
//        if (StringUtil.isBlank(reqVo.getApp())) {
//            return Result.ofFail(-1, "app can't be null or empty");
//        }
//
//        if (StringUtil.isBlank(reqVo.getResource())) {
//            return Result.ofFail(-1, "resource can't be null or empty");
//        }
//
//        if (reqVo.getGrade() == null) {
//            return Result.ofFail(-1, "grade can't be null");
//        }
//        if (reqVo.getGrade() != 0 && reqVo.getGrade() != 1 && reqVo.getGrade() != 2) {
//            return Result.ofFail(-1, "grade must be 0 or 1, but " + reqVo.getGrade() + " got");
//        }
//
//        if (reqVo.getCount() == null) {
//            return Result.ofFail(-1, "count can't be null");
//        }
//        if (reqVo.getCount() < 0) {
//            return Result.ofFail(-1, "count should be at lease zero");
//        }
//
//        if (reqVo.getTimeWindow() == null) {
//            return Result.ofFail(-1, "timeWindow can't be null");
//        }
//        if (reqVo.getTimeWindow() < 0) {
//            return Result.ofFail(-1, "timeWindow should be at lease zero");
//        }
//
//        DegradeRuleEntity entity = new DegradeRuleEntity();
//        entity.setId(idGenerator.nextLongId());
//        entity.setApp(reqVo.getApp());
//        entity.setIp(reqVo.getIp());
//        entity.setPort(reqVo.getPort());
//        entity.setResource(reqVo.getResource());
////        entity.setLimitApp(reqVo.get);
//        entity.setGrade(reqVo.getGrade());
//        entity.setCount(reqVo.getCount());
//        entity.setTimeWindow(reqVo.getTimeWindow());
//        entity.setGmtCreate(new Date());
//        entity.setGmtModified(new Date());
//
//        addRule(reqVo, entity);
//
//        return Result.ofSuccess(entity);
//    }
//
//
//    @PutMapping("/rule/{id}")
//    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
//    public Result<DegradeRuleEntity> apiUpdateFlowRule(@PathVariable("id") Long id, @RequestBody UpdateDegradeRuleReqVo reqVo) throws Exception {
//        if (reqVo == null) {
//            return Result.ofFail(-1, "invalid body");
//        }
//
//        if (StringUtil.isBlank(reqVo.getApp())) {
//            return Result.ofFail(-1, "app can't be null or empty");
//        }
//
//        if (reqVo.getGrade() == null) {
//            return Result.ofFail(-1, "grade can't be null");
//        }
//        if (reqVo.getGrade() != 0 && reqVo.getGrade() != 1) {
//            return Result.ofFail(-1, "grade must be 0 or 1, but " + reqVo.getGrade() + " got");
//        }
//
//        if (reqVo.getCount() == null) {
//            return Result.ofFail(-1, "count can't be null");
//        }
//        if (reqVo.getCount() < 0) {
//            return Result.ofFail(-1, "count should be at lease zero");
//        }
//
//        if (reqVo.getTimeWindow() == null) {
//            return Result.ofFail(-1, "timeWindow can't be null");
//        }
//        if (reqVo.getTimeWindow() < 0) {
//            return Result.ofFail(-1, "timeWindow should be at lease zero");
//        }
//
//        DegradeRuleEntity updatedEntity = updateRule(reqVo, id, toUpdateEntity -> {
//            toUpdateEntity.setGrade(reqVo.getGrade());
//            toUpdateEntity.setCount(reqVo.getCount());
//            toUpdateEntity.setTimeWindow(reqVo.getTimeWindow());
//            toUpdateEntity.setGmtModified(new Date());
//        });
//
//        return Result.ofSuccess(updatedEntity);
//    }
//
//    @DeleteMapping("/rule/{id}")
//    @AuthAction(AuthService.PrivilegeType.DELETE_RULE)
//    public Result<Long> apiDeleteRule(@PathVariable("id") Long id, @RequestBody MachineReqVo reqVo) throws Exception {
//        deleteRule(reqVo, id);
//        return Result.ofSuccess(id);
//    }
//}
