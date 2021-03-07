package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.dashboard.service.SentinelApolloPublicNamespaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Set;

/**
 * forbid cache consistent problem.
 * export api to clear them.
 *
 * @author wxq
 */
@RestController
@RequestMapping(value = "/apollo/public/namespace/cache")
public class SentinelApolloPublicNamespaceController {

    private static final Logger logger = LoggerFactory.getLogger(SentinelApolloPublicNamespaceController.class);

    private final SentinelApolloPublicNamespaceService sentinelApolloPublicNamespaceService;

    public SentinelApolloPublicNamespaceController(SentinelApolloPublicNamespaceService sentinelApolloPublicNamespaceService) {
        this.sentinelApolloPublicNamespaceService = sentinelApolloPublicNamespaceService;
    }

    @GetMapping("/list")
    public ResponseEntity<Set<String>> list() {
        Set<String> projectNames = this.sentinelApolloPublicNamespaceService.listCachedProjectNames();
        return ResponseEntity.ok(projectNames);
    }

    @RequestMapping("/clear")
    public ResponseEntity<Set<String>> clear(@RequestParam String projectName) {
        this.sentinelApolloPublicNamespaceService.clearCacheOfProject(projectName);
        logger.info("clear project [{}]'s public namespace cache in memory", projectName);
        return ResponseEntity.ok(Collections.singleton(projectName));
    }

    @RequestMapping("/clear/all")
    public ResponseEntity<Set<String>> clearAll() {
        Set<String> projectNames = this.sentinelApolloPublicNamespaceService.clearAllCachedProjectNames();
        return ResponseEntity.ok(projectNames);
    }

}
