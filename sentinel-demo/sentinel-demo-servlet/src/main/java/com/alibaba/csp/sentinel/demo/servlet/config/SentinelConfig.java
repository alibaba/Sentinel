package com.alibaba.csp.sentinel.demo.servlet.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.DefaultUrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2024/6/24
 */
public class SentinelConfig implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        initConfig();
    }

    public static void initConfig() {
        System.out.println("Init sentinel config");

        WebCallbackManager.setUrlBlockHandler(new DefaultUrlBlockHandler());
        WebCallbackManager.setRequestOriginParser(request -> request.getHeader("S-user"));
        WebCallbackManager.setUrlCleaner(new MyUrlCleaner());
    }

    static class MyUrlCleaner implements UrlCleaner {
        @Override
        public String clean(String originUrl) {
            if (originUrl.matches("/foo/\\d+")) {
                return "/foo/*";
            }

            return originUrl;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
