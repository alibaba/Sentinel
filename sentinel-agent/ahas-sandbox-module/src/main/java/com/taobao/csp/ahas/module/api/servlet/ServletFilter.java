package com.taobao.csp.ahas.module.api.servlet;

public interface ServletFilter {
   String getFilterName();

   String[] getUrlPatterns();
}
