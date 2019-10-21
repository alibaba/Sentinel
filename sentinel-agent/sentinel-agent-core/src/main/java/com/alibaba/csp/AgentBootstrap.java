package com.alibaba.csp;

import com.alibaba.csp.service.bootstrap.AbstractBootstrap;
import com.alibaba.csp.service.bootstrap.Bootstrap;

public class AgentBootstrap extends AbstractBootstrap {
   private static final Bootstrap BOOTSTRAP = new AgentBootstrap();

   public static void launch() {
      BOOTSTRAP.launch("JAVA_AGENT");
   }
}
