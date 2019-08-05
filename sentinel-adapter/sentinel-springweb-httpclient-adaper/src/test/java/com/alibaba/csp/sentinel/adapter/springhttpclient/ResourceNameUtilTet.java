package com.alibaba.csp.sentinel.adapter.springhttpclient;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
public class ResourceNameUtilTet {
    @Test
    public void testPort() {
        try {
            URL url = new URL("http://a.com:8080/query?a=c&b=d");
            String resourceName = ResourceNameUtil.getResourceName(url.toURI());
            Assert.assertEquals("http,a.com,8080,/query",resourceName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
