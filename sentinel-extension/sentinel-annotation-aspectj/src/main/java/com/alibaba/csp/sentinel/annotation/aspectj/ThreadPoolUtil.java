package com.alibaba.csp.sentinel.annotation.aspectj;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {
    private static int threadPools;

    private ThreadPoolUtil() {

    }

    private static ExecutorService executor;

    static {
        threadPools = Runtime.getRuntime().availableProcessors() * 2;
    }

    public synchronized static ExecutorService newSingleThreadExecutor() {
        if (executor == null) {
            executor = new ThreadPoolExecutor(threadPools, threadPools,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
        }
        return executor;
    }
}