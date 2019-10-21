package com.taobao.diamond.utils;

import java.util.concurrent.atomic.AtomicLong;


/**
 * 处理超时的工具类, 用于客户端获取数据的总体超时。 每次从网络获取完数据后, 累计totalTime, 每次从网络获取数据前,
 * 检查totalTime是否大于totalTimeout, 是则说明总体超时, totalTime有失效时间, 每次从网络获取数据前, 检查是否失效,
 * 失效则重置totalTime, 重新开始累计
 * 
 * @author leiwen.zh
 * 
 */
public class TimeoutUtils {

    // 累计的获取数据消耗的时间, 单位ms
    private final AtomicLong totalTime = new AtomicLong(0L);

    private volatile long lastResetTime;

    private volatile boolean initialized = false;

    // 获取数据的总体超时, 单位ms
    private long totalTimeout;
    // 累计的获取数据消耗的时间的过期时间, 单位ms
    private long invalidThreshold;


    public TimeoutUtils(long totalTimeout, long invalidThreshold) {
        this.totalTimeout = totalTimeout;
        this.invalidThreshold = invalidThreshold;
    }


    public synchronized void initLastResetTime() {
        if (initialized) {
            return;
        }
        lastResetTime = System.currentTimeMillis();
        initialized = true;
    }


    /**
     * 累计总的时间
     * 
     * @param timeout
     */
    public void addTotalTime(long time) {
        totalTime.addAndGet(time);
    }


    /**
     * 判断是否超时
     * 
     * @return
     */
    public boolean isTimeout() {
        return totalTime.get() > this.totalTimeout;
    }


    /**
     * 总的时间清零
     */
    public void resetTotalTime() {
        if (isTotalTimeExpired()) {
            totalTime.set(0L);
            lastResetTime = System.currentTimeMillis();
        }
    }


    public AtomicLong getTotalTime() {
        return totalTime;
    }


    private boolean isTotalTimeExpired() {
        return System.currentTimeMillis() - lastResetTime > this.invalidThreshold;
    }
}
