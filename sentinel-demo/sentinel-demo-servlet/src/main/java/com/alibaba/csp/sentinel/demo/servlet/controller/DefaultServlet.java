package com.alibaba.csp.sentinel.demo.servlet.controller;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2024/6/24
 */
public class DefaultServlet implements Servlet {
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        String path = ((HttpServletRequest) servletRequest).getPathInfo();

        if (path.startsWith("/foo")) {
            handleFoo(servletRequest, servletResponse);
        } else if (path.startsWith("/bar")) {
            handleBar(servletRequest, servletResponse);
        } else {
            notFound(servletRequest, servletResponse);
        }
    }

    private void notFound(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        httpServletResponse.setStatus(404);
        httpServletResponse.setContentType("text/plain");
        httpServletResponse.getWriter().write(httpServletRequest.getServletPath() + " not found.");
        httpServletResponse.getWriter().close();
    }

    private void handleBar(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        httpServletResponse.setStatus(200);
        httpServletResponse.setContentType("text/plain");
        httpServletResponse.getWriter().write("bar");
        httpServletResponse.getWriter().close();
    }

    private void handleFoo(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String path = httpServletRequest.getPathInfo();
        String id = path.replaceAll("/foo/(\\d+)", "$1");

        httpServletResponse.setStatus(200);
        httpServletResponse.setContentType("text/plain");
        httpServletResponse.getWriter().write("Hello " + id);
        httpServletResponse.getWriter().close();
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {
    }
}
