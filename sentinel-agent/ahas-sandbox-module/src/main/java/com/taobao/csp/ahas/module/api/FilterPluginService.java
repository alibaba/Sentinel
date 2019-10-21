package com.taobao.csp.ahas.module.api;

import com.taobao.csp.ahas.module.api.PluginService;
import com.taobao.csp.ahas.service.bridge.aop.AspectPlugin;
import com.taobao.csp.ahas.module.api.dubbo.DubboFilterAspectPlugin;
import com.taobao.csp.ahas.module.api.gateway.ScGatewayFilterAspectPlugin;
import com.taobao.csp.ahas.module.api.servlet.ServletContextAspectPlugin;
import com.taobao.csp.ahas.module.api.zuul.ZuulFilterAspectPlugin;
import java.util.ArrayList;
import java.util.List;

public class FilterPluginService implements PluginService {
   public List<AspectPlugin> getAspectPlugins() {
      ArrayList<AspectPlugin> aspectPlugins = new ArrayList();
      aspectPlugins.add(new ServletContextAspectPlugin());
      aspectPlugins.add(new DubboFilterAspectPlugin());
      aspectPlugins.add(new ScGatewayFilterAspectPlugin());
      aspectPlugins.add(new ZuulFilterAspectPlugin());
      return aspectPlugins;
   }
}
