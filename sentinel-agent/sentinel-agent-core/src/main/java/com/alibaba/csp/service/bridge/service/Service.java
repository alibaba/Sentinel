package com.alibaba.csp.service.bridge.service;

public interface Service {
   void start() throws Exception;

   void restart() throws Exception;

   void pause() throws Exception;

   void destroy();

   boolean isEnabled();

   ServiceState getState();

   String getName();
}
