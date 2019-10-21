package com.taobao.diamond.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 根据IP进行流控, 控制单个IP的数量以及IP总量
 * 
 * @author leiwen.zh
 * 
 */
public class SimpleIPFlowData {

    private AtomicInteger[] data;

    private int slotCount;

    private int averageCount;

    private ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("diamond ip flow control thread");
            t.setDaemon(true);
            return t;
        }

    });

    class DefaultIPFlowDataManagerTask implements Runnable {

        public void run() {
            rotateSlot();
        }

    }


    public SimpleIPFlowData(int slotCount, int interval) {
        if (slotCount <= 0) {
            this.slotCount = 1;
        }
        else {
            this.slotCount = slotCount;
        }
        data = new AtomicInteger[slotCount];
        for (int i = 0; i < data.length; i++) {
            data[i] = new AtomicInteger(0);
        }
        timer.scheduleAtFixedRate(new DefaultIPFlowDataManagerTask(), interval, interval, TimeUnit.MILLISECONDS);
    }


    public int incrementAndGet(String ip) {
        int index = 0;
        if (ip != null) {
            index = ip.hashCode() % slotCount;
        }
        if (index < 0) {
            index = -index;
        }
        return data[index].incrementAndGet();
    }


    public void rotateSlot() {
        int totalCount = 0;
        for (int i = 0; i < slotCount; i++) {
            totalCount += data[i].get();
            data[i].set(0);
        }
        this.averageCount = totalCount / this.slotCount;
    }


    public int getCurrentCount(String ip) {
        int index = 0;
        if (ip != null) {
            index = ip.hashCode() % slotCount;
        }
        if (index < 0) {
            index = -index;
        }
        return data[index].get();
    }


    public int getAverageCount() {
        return this.averageCount;
    }
}
