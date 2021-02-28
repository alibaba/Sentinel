package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.auth.FakeAuthServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.servlet.http.HttpServletRequest;

/**
 * disable auth in test.
 *
 * @author wxq
 */
@TestConfiguration
@Import(AuthConfiguration.class)
public class NoAuthConfigurationTest {

    @Bean
    public AuthService<HttpServletRequest> httpServletRequestAuthService() {
        return new FakeAuthServiceImpl();
    }

}
