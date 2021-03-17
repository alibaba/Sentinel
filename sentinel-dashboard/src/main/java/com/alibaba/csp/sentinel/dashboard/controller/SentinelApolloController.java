package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.dashboard.service.SentinelApolloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Set<String>> getRegisteredProjects() {
        Set<String> projectNames = this.sentinelApolloService.getRegisteredProjects();
        return ResponseEntity.ok(projectNames);
    }

    @GetMapping("/namespace/cache/list")
    public ResponseEntity<Set<String>> list() {
        return null;
    }

    @RequestMapping("/namespace/cache/clear")
    public ResponseEntity<Set<String>> clear(@RequestParam String projectName) {
        return null;
    }

    @RequestMapping("/namespace/cache/clear/all")
    public ResponseEntity<Set<String>> clearAll() {
        return null;
    }

}
