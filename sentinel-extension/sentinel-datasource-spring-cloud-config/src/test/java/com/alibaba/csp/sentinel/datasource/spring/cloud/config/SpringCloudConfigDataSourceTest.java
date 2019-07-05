package com.alibaba.csp.sentinel.datasource.spring.cloud.config;

import com.alibaba.csp.sentinel.datasource.spring.cloud.config.application.WebApp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author lianglin
 * @since 2019-07-05 18:19
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApp.class)
public class SpringCloudConfigDataSourceTest {

    @Test
    public void contextLoads() {
    }
}
