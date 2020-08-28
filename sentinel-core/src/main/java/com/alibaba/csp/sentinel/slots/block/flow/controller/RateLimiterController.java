/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.flow.controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;

import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.node.Node;

/**
 *  以漏桶方式进行限流处理；限流方式为：根据设置的tps数量，把tps值分割到最小1毫秒的单位时间内需要漏出的水滴数量，内部以该周期速度对漏桶中的水滴进行漏出，外部访问
 * 的时候，根据需要获取的访问数量，放入对应的水滴值进入漏桶中；如果能够把水滴放入漏桶中，则访问成功，否则根据设置的等待时间进行等待，然后在尝试是否可以把对应的水滴
 * 放入漏桶中，如果尝试成功，则该次访问成功，否则该次访问失败。
 *
 * @author jialiang.linjl
 */
public class RateLimiterController implements TrafficShapingController {


    private final int maxQueueingTimeMs;
    private final double count;

    private final Object water = new Object();   // 该桶中的水滴，因为仅仅及计算速度，所有只需要一个水滴实例即可
    private BlockingQueue<Object> bucket;      // 水桶
    private long sleep;        // 每次水滴漏水后需要等待的单位时间，最低时间1毫秒
    private int stepCount;    // 1秒钟时间内漏水后需要等待的次数 
    private int[] step;       // 在1秒内，每个等待单位时间中需要漏出的水滴数量，根据设置的速度和每次的等待时间计算而得
    private Thread waterThread;      // 水桶漏水线程

    public RateLimiterController(int timeOut, double count) {
        this.maxQueueingTimeMs = timeOut;
        this.count = count;
        init();
    }

    //
    private void init() {

        // 根据速度计算每次漏水后需要等待的单位时间
        long temp = (long) (1000 / count);
        if (temp < 1) {
            // 最少需要等待1毫秒
            temp = 1;
        }
        sleep = temp;

        // 计算1秒时间内需要获取水滴后等待的次数
        stepCount = (int) (1000 / sleep);
        step = new int[stepCount];

        // 平均每单位时间中获取的水滴个数
        double each = count / stepCount;
        double tt;

        // 计算每个单位等待时间内需要获取的水滴个数     
        int pre = 0;
        for (int i = 0; i < stepCount; i++) {
            tt = each * (i + 1) - pre;
            step[i] = (int) Math.round(tt);
            pre += step[i];
        }

        // 水桶的容量，以指定速度的 1/100为水桶的容量
        int size = (int) (count * 1 / 100);
        if (size < 1) {
            size = 1;
        }
        bucket = new LinkedBlockingQueue<>(size);

        // 初始化漏水线程
        waterThread = createWaterThread();
        waterThread.start();
    }

    // 产生线程
    private Thread createWaterThread() {
        return new Thread(new Runnable() {
            public void run() {
                dropWater();
            }
        }, "drop_water");
    }

    // 以固定速度进行漏水处理
    private void dropWater() {
        long current;
        long delta;
        int period;
        int index;
        int i;
        // 在指定周期内没有数据，则退出线程，以便在替换规则的时候，能够垃圾回收旧的规则对象
        boolean flag = true;
        // 水滴下一次漏出的时间点
        long next = System.currentTimeMillis();
        while (flag) {
            try {
                flag = false;
                // 30个周期内（秒）都没有水滴，则退出线程
                for (period = 0; period < 30; period++) {
                    // 1秒内的水滴周期个数
                    for (index = 0; index < stepCount; index++) {
                        // 每个周期中需要漏出的水滴数量
                        for (i = 0; i < step[index]; i++) {
                            if (bucket.poll() != null) {
                                flag = true;
                            }
                        }
                        // 下一个漏出点
                        next += sleep;
                        current = System.currentTimeMillis();
                        // 减去中间消耗及线程等待切换时间
                        delta = next - current;
                        // 需要等待到下一个漏出点
                        if (delta >= 0) {
                            Thread.sleep(delta);
                        }
                    }
                }
            }
            catch (Throwable e) {
                //e.printStackTrace();
            }
        }
    }

    @Override
    public boolean canPass(Node node, int acquireCount) {
        return canPass(node, acquireCount, false);
    }

    @Override
    public boolean canPass(Node node, int acquireCount, boolean prioritized) {
        // Pass when acquire count is less or equal than 0.
        if (acquireCount <= 0) {
            return true;
        }
        // Reject when count is less or equal than 0.
        // Otherwise,the costTime will be max of long and waitTime will overflow in some cases.
        if (count <= 0) {
            return false;
        }
        try {
            if (!waterThread.isAlive()) {
                synchronized (bucket) {
                    if (!waterThread.isAlive()) {
                        Thread pthread0 = createWaterThread();
                        waterThread = pthread0;
                        waterThread.start();
                    }
                }
            }

            long first = TimeUtil.currentTimeMillis();
            // 如果要严格限制峰值，则需要启用注释部分，在从漏桶中漏出水滴的时候，如果开始桶为空，则在第一秒钟，会多于设置值的水滴放入漏桶中。
            // 就是在一秒内除了漏出限定速率的水滴外，还会把水桶装满
            /*double passQps = node.passQps();
            if (passQps > count) {
                Thread.sleep(1000 - first % 1000);
            }*/

            // 放入水滴，成功测通过，不成功测不通过
            boolean flag = true;
            long current;
            long wait;
            for (int i = 0; i < acquireCount; i++) {
                current = TimeUtil.currentTimeMillis();
                // 等待时间需要减去前面已经消耗掉的时间
                wait = maxQueueingTimeMs - current + first;
                if (wait < 0) {
                    wait = 0;
                }
                // 只要有一次放入不成功就算整个不成功
                flag &= bucket.offer(water, wait, TimeUnit.MILLISECONDS);
            }
            return flag;
        }
        catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }

}
