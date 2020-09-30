package com.alibaba.csp.sentinel.dashboard.controller.kie;

import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerLabel;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.kie.KieServerInfoVo;
import com.alibaba.csp.sentinel.log.RecordLog;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Carpenter Lee
 */
@RestController
@RequestMapping(value = "/kie")
public class KieServerController {
    @Autowired
    private KieServerManagement management;

    @GetMapping("/projects")
    public Result<Set<String>> queryProjects(HttpServletRequest request){
        return Result.ofSuccess(management.queryProjects());
    }

    @GetMapping("/kieInfos")
    public Result<Set<KieServerInfoVo>> queryKieInfos(@RequestParam String project,
                                                      @RequestParam String environment){
        Set<KieServerInfo> kieServerInfos = management.queryKieInfos(project, environment);
        Set<KieServerInfoVo> kieServerInfoVos = kieServerInfos.stream().map(x -> {
            KieServerInfoVo vo = new KieServerInfoVo();
            try {
                BeanUtils.copyProperties(x, vo);
            }catch (BeansException e){
                RecordLog.error("Query kie infos error.", e);
            }

            KieServerLabel label = x.getLabel();
            vo.setApp(label.getApp());
            vo.setEnvironment(label.getEnvironment());
            vo.setProject(label.getProject());
            vo.setService(label.getService());
            vo.setServerVersion(label.getServerVersion());
            return vo;
        }).collect(Collectors.toSet());

        return Result.ofSuccess(kieServerInfoVos);
    }

    @GetMapping("/{serverId}/machineInfos")
    public Result<Set<MachineInfo>> queryMachineInfos(@PathVariable("serverId") String serverId){
        Set<MachineInfo> machineInfos = management.getMachineInfos(serverId);
        return Result.ofSuccess(machineInfos);
    }
}
