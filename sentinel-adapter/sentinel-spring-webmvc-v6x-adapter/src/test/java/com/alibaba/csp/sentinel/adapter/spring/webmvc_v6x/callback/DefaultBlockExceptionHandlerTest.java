package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config.WebServletLocalConfig;
import com.alibaba.csp.sentinel.setting.fallback.BlockFallbackConfig;
import com.alibaba.csp.sentinel.setting.fallback.BlockFallbackUtils;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import org.hamcrest.text.IsEqualIgnoringCase;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static com.alibaba.csp.sentinel.setting.SentinelAdapterConstants.WEB_FALLBACK_CONTENT_TYPE_JSON;
import static com.alibaba.csp.sentinel.setting.SentinelAdapterConstants.WEB_FALLBACK_CONTENT_TYPE_TEXT;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mockStatic;

public class DefaultBlockExceptionHandlerTest {

    @Test
    public void handle_writeBlockPage() throws Exception {
        DefaultBlockExceptionHandler h = new DefaultBlockExceptionHandler();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/a/b/c");
        req.setQueryString("a=1&b=2");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String resourceName = "/a/b/c";
        BlockException ex = new FlowException("msg");
        h.handle(req, resp, resourceName, ex);
        assertEquals(429, resp.getStatus());
    }

    @Test
    public void handle_writeBlockPageWith() throws Exception {
        DefaultBlockExceptionHandler h = new DefaultBlockExceptionHandler();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/a/b/c");
        req.setQueryString("a=1&b=2");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String resourceName = "/a/b/c";
        BlockException ex = new FlowException("msg");

        try (MockedStatic<BlockFallbackUtils> mockedBlockFallbackUtils = mockStatic(BlockFallbackUtils.class)) {
            BlockFallbackConfig.WebBlockFallbackBehavior behavior = new BlockFallbackConfig.WebBlockFallbackBehavior();
            behavior.setWebRespStatusCode(444);
            behavior.setWebRespContentType(WEB_FALLBACK_CONTENT_TYPE_JSON);
            behavior.setWebRespMessage("webRespMessage");
            mockedBlockFallbackUtils.when(() -> BlockFallbackUtils.getFallbackBehavior(resourceName, ex))
                    .thenReturn(behavior);

            h.handle(req, resp, resourceName, ex);
            assertEquals(444, resp.getStatus());
            assertThat(resp.getContentType(), containsString("application/json"));
            assertThat(resp.getCharacterEncoding(), IsEqualIgnoringCase.equalToIgnoringCase("utf-8"));
            assertEquals("webRespMessage", resp.getContentAsString());

            behavior.setWebRespContentType(WEB_FALLBACK_CONTENT_TYPE_TEXT);
            h.handle(req, resp, resourceName, ex);
            assertEquals(444, resp.getStatus());
            assertThat(resp.getContentType(), containsString("text/plain"));
            assertThat(resp.getCharacterEncoding(), IsEqualIgnoringCase.equalToIgnoringCase("utf-8"));
            assertEquals("webRespMessage", resp.getContentAsString());
        }
    }

    @Test
    public void handle_sendRedirect() throws Exception {
        DefaultBlockExceptionHandler h = new DefaultBlockExceptionHandler();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/a/b/c");
        req.setQueryString("a=1&b=2");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String resourceName = "/a/b/c";
        BlockException ex = new FlowException("msg");

        try (MockedStatic<WebServletLocalConfig> mocked = mockStatic(WebServletLocalConfig.class)) {
            mocked.when(WebServletLocalConfig::getBlockPage)
                    .thenReturn("/blocked");

            h.handle(req, resp, resourceName, ex);
            assertEquals(302, resp.getStatus());
            assertEquals("/blocked?http_referer=http://localhost/a/b/c?a=1&b=2", resp.getRedirectedUrl());
        }
    }
}
