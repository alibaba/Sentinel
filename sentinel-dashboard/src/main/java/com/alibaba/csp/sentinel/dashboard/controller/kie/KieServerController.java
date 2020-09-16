package com.alibaba.csp.sentinel.dashboard.controller.kie;

import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerManagement;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

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

    @GetMapping("/{project}/kieInfos")
    public Result<Set<KieServerInfo>> queryKieInfos(@PathVariable("project") String project){
        return Result.ofSuccess(management.queryKieInfos(project));
    }
}
