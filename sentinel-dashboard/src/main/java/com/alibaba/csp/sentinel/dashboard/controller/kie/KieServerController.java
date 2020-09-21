package com.alibaba.csp.sentinel.dashboard.controller.kie;

import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerManagement;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.kie.KieServerInfoVo;
import com.alibaba.csp.sentinel.log.RecordLog;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            return vo;
        }).collect(Collectors.toSet());

        return Result.ofSuccess(kieServerInfoVos);
    }
}
