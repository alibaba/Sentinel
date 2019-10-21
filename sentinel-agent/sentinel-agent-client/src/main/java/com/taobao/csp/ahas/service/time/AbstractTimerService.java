package com.taobao.csp.ahas.service.time;

import com.taobao.csp.ahas.service.api.thread.SafeWrappers;
import com.taobao.csp.ahas.service.api.thread.ThreadPoolService;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTimerService implements TimerService {
   protected final ScheduledExecutorService executorService;
   protected long period = 5L;
   private ScheduledFuture task;

   public AbstractTimerService(String name) {
      this.executorService = ThreadPoolService.createSingleThreadScheduledExecutor(name);
   }

   public void init() {
      this.task = this.executorService.scheduleAtFixedRate(SafeWrappers.safeRunnable(this.createTask()), this.period, this.period, TimeUnit.SECONDS);
   }

   private Runnable createTask() {
      return new Runnable() {
         public void run() {
            AbstractTimerService.this.onTime();
         }
      };
   }

   public long getPeriodInSeconds() {
      return this.period;
   }
}
