package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.param;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class HttpServletRequestItemParserTest {

    @Test
    public void getPath() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getPathInfo()).thenReturn("/a/b/c");

        HttpServletRequestItemParser reqItem = new HttpServletRequestItemParser();
        String path = reqItem.getPath(req);
        assertEquals("/a/b/c", path);
    }

    @Test
    public void getRemoteAddress() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn("1.2.3.4");

        HttpServletRequestItemParser reqItem = new HttpServletRequestItemParser();
        String addr = reqItem.getRemoteAddress(req);
        assertEquals("1.2.3.4", addr);
    }

    @Test
    public void getHeader() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getHeader("test-header")).thenReturn("test-value");

        HttpServletRequestItemParser reqItem = new HttpServletRequestItemParser();
        String headerValue = reqItem.getHeader(req, "test-header");
        assertEquals("test-value", headerValue);
    }

    @Test
    public void getUrlParam() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getParameter("testK")).thenReturn("testV");

        HttpServletRequestItemParser reqItem = new HttpServletRequestItemParser();
        String paramValue = reqItem.getUrlParam(req, "testK");
        assertEquals("testV", paramValue);
    }

    @Test
    public void getCookieValue() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Cookie[] cookies = new Cookie[0];
        cookies = ArrayUtils.add(cookies, new Cookie("testK", "testV"));
        cookies = ArrayUtils.add(cookies, new Cookie("testK1", "testV1"));
        when(req.getCookies()).thenReturn(cookies);

        HttpServletRequestItemParser reqItem = new HttpServletRequestItemParser();
        String paramValue = reqItem.getCookieValue(req, "testK");
        assertEquals("testV", paramValue);
    }

    @Test
    public void getBodyValue() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContent("{\"testK\": \"testV\"}".getBytes());

        HttpServletRequestItemParser reqItem = new HttpServletRequestItemParser();
        String bodyValue = reqItem.getBodyValue(req, "testK");
        assertEquals("testV", bodyValue);
    }

    @Test
    public void getPathValue() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        Map<String, String> vars = new HashMap<>();
        vars.put("testK", "testV");
        vars.put("testK1", "testV1");
        req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, vars);

        HttpServletRequestItemParser reqItem = new HttpServletRequestItemParser();
        String pathValue = reqItem.getPathValue(req, "testK");
        assertEquals("testV", pathValue);
    }
}
