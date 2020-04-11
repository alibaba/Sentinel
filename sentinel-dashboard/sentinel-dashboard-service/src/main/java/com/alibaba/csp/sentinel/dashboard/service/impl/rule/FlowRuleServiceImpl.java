package com.alibaba.csp.sentinel.dashboard.service.impl.rule;

import com.alibaba.csp.sentinel.dashboard.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.api.rule.FlowRuleService;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.DeleteFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.UpdateFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.flow.QueryFlowRuleListRespVo;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author cdfive
 */
@Service
public class FlowRuleServiceImpl extends BaseRuleService<FlowRuleEntity> implements FlowRuleService {

    @Override
    public List<QueryFlowRuleListRespVo> queryFlowRuleList(MachineReqVo reqVo) throws Exception {
        List<FlowRuleEntity> entities = queryRuleList(reqVo);
        return entities.stream().map(o -> RuleVoConvertor.convert(o)).collect(Collectors.toList());
    }

    @Override
    public void addFlowRule(AddFlowRuleReqVo reqVo) throws Exception {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotBlank(reqVo.getResource(), "resource");

        checkNotBlank(reqVo.getLimitApp(), "limitApp");

        checkNotNull(reqVo.getGrade(), "grade");
        checkInValues(reqVo.getGrade(), "grade",0, 1);

        checkNotNull(reqVo.getCount(), "count");
        checkCondition(reqVo.getCount() >= 0, "count must be at lease zero");

        checkNotNull(reqVo.getStrategy(), "strategy");
        if (reqVo.getStrategy() != 0) {
            checkNotBlankMessage(reqVo.getRefResource(), "refResource can't be null or empty when strategy!=0");
        }
//        checkCondition(reqVo.getStrategy() == 0 || !StringUtil.isBlank(reqVo.getRefResource()), "refResource can't be null or empty when strategy!=0");

        checkNotNull(reqVo.getControlBehavior(), "controlBehavior");
        if (reqVo.getControlBehavior() == 1) {
            checkNotNullMessage(reqVo.getWarmUpPeriodSec(), "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (reqVo.getControlBehavior() == 2) {
            checkNotNullMessage(reqVo.getMaxQueueingTimeMs(), "maxQueueingTimeMs can't be null when controlBehavior==2");
        }

        if (reqVo.getClusterMode()) {
            checkNotNull(reqVo.getClusterConfig(), " clusterConfig");
        }

        FlowRuleEntity entity = new FlowRuleEntity();
        Date date = new Date();
        entity.setId(idGenerator.nextLongId());
        entity.setApp(reqVo.getApp());
        entity.setIp(reqVo.getIp());
        entity.setPort(reqVo.getPort());
        entity.setResource(reqVo.getResource());
        entity.setLimitApp(reqVo.getLimitApp());
        entity.setGrade(reqVo.getGrade());
        entity.setCount(reqVo.getCount());
        entity.setStrategy(reqVo.getStrategy());
        entity.setControlBehavior(reqVo.getControlBehavior());
        entity.setRefResource(reqVo.getRefResource());
        entity.setWarmUpPeriodSec(reqVo.getWarmUpPeriodSec());
        entity.setMaxQueueingTimeMs(reqVo.getMaxQueueingTimeMs());
        entity.setClusterMode(reqVo.getClusterMode());
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = reqVo.getClusterConfig();
        if (clusterConfigReqVo != null) {
            ClusterFlowConfig clusterFlowConfig = new ClusterFlowConfig();
            clusterFlowConfig.setThresholdType(clusterConfigReqVo.getThresholdType());
            clusterFlowConfig.setFallbackToLocalWhenFail(clusterConfigReqVo.getFallbackToLocalWhenFail());
        }
        entity.setGmtCreate(date);
        entity.setGmtModified(date);

        addRule(reqVo, entity);
    }

    @Override
    public void updateFlowRule(UpdateFlowRuleReqVo reqVo) throws Exception {
        checkNotNull(reqVo, "body");

        checkNotNull(reqVo.getId(), "id");
        checkCondition(reqVo.getId() > 0, "id must be greater than 0");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotBlank(reqVo.getLimitApp(), "limitApp");

        checkNotNull(reqVo.getGrade(), "grade");
        checkInValues(reqVo.getGrade(), "grade",0, 1);

        checkNotNull(reqVo.getCount(), "count");
        checkCondition(reqVo.getCount() >= 0, "count must be at lease zero");

        checkNotNull(reqVo.getStrategy(), "strategy");
        if (reqVo.getStrategy() != 0) {
            checkNotBlankMessage(reqVo.getRefResource(), "refResource can't be null or empty when strategy!=0");
        }

        checkNotNull(reqVo.getControlBehavior(), "controlBehavior");
        if (reqVo.getControlBehavior() == 1) {
            checkNotNullMessage(reqVo.getWarmUpPeriodSec(), "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (reqVo.getControlBehavior() == 2) {
            checkNotNullMessage(reqVo.getMaxQueueingTimeMs(), "maxQueueingTimeMs can't be null when controlBehavior==2");
        }

        if (reqVo.getClusterMode()) {
            checkNotNull(reqVo.getClusterConfig(), " clusterConfig");
        }

        updateRule(reqVo, reqVo.getId(), new UpdateRuleCallback<FlowRuleEntity>() {
            @Override
            public void doUpdateRule(FlowRuleEntity toUpdateRuleEntity) {
                toUpdateRuleEntity.setLimitApp(reqVo.getLimitApp());
                toUpdateRuleEntity.setGrade(reqVo.getGrade());
                toUpdateRuleEntity.setCount(reqVo.getCount());
                toUpdateRuleEntity.setStrategy(reqVo.getStrategy());
                toUpdateRuleEntity.setControlBehavior(reqVo.getControlBehavior());
                toUpdateRuleEntity.setRefResource(reqVo.getRefResource());
                toUpdateRuleEntity.setWarmUpPeriodSec(reqVo.getWarmUpPeriodSec());
                toUpdateRuleEntity.setMaxQueueingTimeMs(reqVo.getMaxQueueingTimeMs());
                toUpdateRuleEntity.setClusterMode(reqVo.getClusterMode());
                AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = reqVo.getClusterConfig();
                if (clusterConfigReqVo != null) {
                    ClusterFlowConfig clusterFlowConfig = new ClusterFlowConfig();
                    clusterFlowConfig.setThresholdType(clusterConfigReqVo.getThresholdType());
                    clusterFlowConfig.setFallbackToLocalWhenFail(clusterConfigReqVo.getFallbackToLocalWhenFail());
                }
                toUpdateRuleEntity.setGmtModified(new Date());
            }
        });
    }

    @Override
    public void deleteFlowRule(DeleteFlowRuleReqVo reqVo) throws Exception {
        deleteRule(reqVo, reqVo.getId());
    }
}
