package com.alibaba.csp.sentinel.adapter.spring.restclient.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller for RestClient adapter tests.
 *
 * @author uuuyuqi
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Sentinel!";
    }

    @GetMapping("/users/{id}")
    public String getUser(@PathVariable("id") Long id) {
        return "User: " + id;
    }

    @PostMapping("/users")
    public String createUser(@RequestBody String user) {
        return "Created: " + user;
    }

    @GetMapping("/error")
    public ResponseEntity<String> error() {
        return ResponseEntity.status(500).body("Server Error");
    }

    @GetMapping("/delay")
    public String delay() throws InterruptedException {
        Thread.sleep(100);
        return "Delayed response";
    }
}