package com.alibaba.csp.sentinel.dashboard.controller.kie;

import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerLabel;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.MachineInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author 00451459
 * @since 2020-10-30
 */
@RestController
@RequestMapping(value = "/kie/app")
public class KieAppController {

    @Autowired
    private KieServerManagement kieManagement;

    @GetMapping("/names.json")
    public Result<List<KieServerLabel>> queryApps(HttpServletRequest request) {
        return Result.ofSuccess(kieManagement.getKieLabels());
    }

    @GetMapping("/briefinfos.json")
    public Result<List<KieServerInfo>> queryAppInfos(HttpServletRequest request) {
        List<KieServerInfo> list = new ArrayList<>(kieManagement.getKieInfos());
        Collections.sort(list, Comparator.comparing(AppInfo::getApp));
        return Result.ofSuccess(list);
    }

    @GetMapping(value = "/{serverId}/machines.json")
    public Result<List<MachineInfoVo>> getMachinesByApp(@PathVariable("serverId") String serverId) {
        Optional<KieServerInfo> kieInfo = kieManagement.queryKieInfo(serverId);
        if (!kieInfo.isPresent()) {
            return Result.ofSuccess(null);
        }
        List<MachineInfo> list = new ArrayList<>(kieInfo.get().getMachines());
        Collections.sort(list, Comparator.comparing(MachineInfo::getApp).thenComparing(MachineInfo::getIp).thenComparingInt(MachineInfo::getPort));
        return Result.ofSuccess(MachineInfoVo.fromMachineInfoList(list));
    }
    
    @RequestMapping(value = "/{serverId}/machine/remove.json")
    public Result<String> removeMachineById(
            @PathVariable("serverId") String serverId,
            @RequestParam(name = "ip") String ip,
            @RequestParam(name = "port") int port) {
        Optional<KieServerInfo> kieInfo = kieManagement.queryKieInfo(serverId);
        if (!kieInfo.isPresent()) {
            return Result.ofSuccess(null);
        }
        if (kieManagement.removeMachineInfo(serverId, ip, port)) {
            return Result.ofSuccessMsg("success");
        } else {
            return Result.ofFail(1, "remove failed");
        }
    }
}
