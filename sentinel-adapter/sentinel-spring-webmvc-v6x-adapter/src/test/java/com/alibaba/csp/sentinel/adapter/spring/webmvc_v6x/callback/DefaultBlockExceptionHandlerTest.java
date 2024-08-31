package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback;

import com.alibaba.csp.sentinel.adapter.web.common.DefaultBlockExceptionResponse;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;

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
        BlockException dex = new DegradeException("msg");
        MockHttpServletResponse dresp = new MockHttpServletResponse();
        h.handle(req, dresp, resourceName, dex);
        assertEquals(DefaultBlockExceptionResponse.DEGRADE_EXCEPTION.getStatus(), dresp.getStatus());
    }

}