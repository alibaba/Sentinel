package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.service.SentinelApolloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * forbid cache consistent problem.
 * export api to clear them.
 *
 * @author wxq
 */
@RestController
@RequestMapping(value = "/sentinel/apollo/")
public class SentinelApolloController {

    private static final Logger logger = LoggerFactory.getLogger(SentinelApolloController.class);

    private final SentinelApolloService sentinelApolloService;

    public SentinelApolloController(SentinelApolloService sentinelApolloService) {
        this.sentinelApolloService = sentinelApolloService;
    }

    @GetMapping("/registered/projects")
    @AuthAction(AuthService.PrivilegeType.READ_METRIC)
    public ResponseEntity<Set<String>> getRegisteredProjects() {
        Set<String> projectNames = this.sentinelApolloService.getRegisteredProjects();
        return ResponseEntity.ok(projectNames);
    }

    @DeleteMapping("/registered/projects")
    @AuthAction(AuthService.PrivilegeType.ALL)
    public ResponseEntity<Set<String>> deleteRegisteredProjects() {
        Set<String> projectNames = this.sentinelApolloService.clearRegisteredProjects();
        return ResponseEntity.ok(projectNames);
    }

}
