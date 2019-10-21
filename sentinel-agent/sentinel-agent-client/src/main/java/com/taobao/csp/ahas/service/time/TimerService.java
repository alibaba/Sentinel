package com.taobao.csp.ahas.service.time;

public interface TimerService {
   long getPeriodInSeconds();

   void onTime();
}
