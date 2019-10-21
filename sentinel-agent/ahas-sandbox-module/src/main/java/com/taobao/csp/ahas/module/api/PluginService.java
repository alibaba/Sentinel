package com.taobao.csp.ahas.module.api;

import com.taobao.csp.ahas.service.bridge.aop.AspectPlugin;
import java.util.List;

public interface PluginService {
   List<AspectPlugin> getAspectPlugins();
}

