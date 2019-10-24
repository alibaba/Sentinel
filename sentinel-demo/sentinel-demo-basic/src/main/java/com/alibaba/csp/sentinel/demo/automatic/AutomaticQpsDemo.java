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
package com.alibaba.csp.sentinel.demo.automatic;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AutomaticQpsDemo {


    //模拟3个流量不同的服务

    private static AtomicInteger[] pass = new AtomicInteger[3];
    private static AtomicInteger[] block = new AtomicInteger[3];
    private static AtomicInteger[] total = new AtomicInteger[3];
    private static volatile boolean stop = false;

    private static int seconds = 60 + 40;

    public static void main(String[] args) throws Exception {

        for(int i = 0;i<3;i++){
            pass[i] = new AtomicInteger();
            block[i] = new AtomicInteger();
            total[i] = new AtomicInteger();
        }

        tick();
        // first make the system run on a very low condition
        simulateTraffic();

        System.out.println("===== begin to do automatic flow control");

    }


    private static void simulateTraffic() {
        // 模拟不同服务的流量
        for (int i = 0; i < 18; i++) {
            Thread t = new Thread(new RunTask("a",0));
            t.setName("simulate-traffic-Task-A");
            t.start();
        }
        for (int i = 0; i < 15; i++) {
            Thread t = new Thread(new RunTask("b",1));
            t.setName("simulate-traffic-Task-B");
            t.start();
        }
        for (int i = 0; i < 13; i++) {
            Thread t = new Thread(new DegradedRunTask("c",2));
            t.setName("simulate-traffic-Task-C");
            t.start();
        }
    }

    private static void tick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
    }

    static class TimerTask implements Runnable {

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("begin to statistic!!!");

            long oldTotal[] = {0,0,0};
            long oldPass[] = {0,0,0};
            long oldBlock[] = {0,0,0};

            String[] resourceName ={"a","b","c"};

            while (!stop) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                System.out.println("Time: "+TimeUtil.currentTimeMillis());

                for(int i =0; i<3;i++) {
                    long globalTotal = total[i].get();
                    long oneSecondTotal = globalTotal - oldTotal[i];
                    oldTotal[i] = globalTotal;

                    long globalPass = pass[i].get();
                    long oneSecondPass = globalPass - oldPass[i];
                    oldPass[i] = globalPass;

                    long globalBlock = block[i].get();
                    long oneSecondBlock = globalBlock - oldBlock[i];
                    oldBlock[i] = globalBlock;

                    System.out.println("Flow on Resource "+resourceName[i]
                            + ", total:" + oneSecondTotal
                            + ", pass:" + oneSecondPass
                            + ", block:" + oneSecondBlock);
                }

                if (seconds-- <= 0) {
                    stop = true;
                }
            }

        }
    }

    static class RunTask implements Runnable {

        private String resourceName;
        private int resourceCode;

        public RunTask(String resourceName, int resourceCode){
            this.resourceName = resourceName;
            this.resourceCode = resourceCode;
        }

        @Override
        public void run() {
            while (!stop) {
                Entry entry = null;

                try {
                    entry = SphU.entry(resourceName);
                    // token acquired, means pass
                    DoublingTest.threeSum(300);
                    pass[resourceCode].addAndGet(1);

                } catch (BlockException e1) {
                    block[resourceCode].incrementAndGet();
                } catch (Exception e2) {
                    // biz exception
                } finally {
                    total[resourceCode].incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }

                Random random2 = new Random();
                try {
                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(200));
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    static class DegradedRunTask implements Runnable {

        private String resourceName;
        private int resourceCode;

        public DegradedRunTask(String resourceName, int resourceCode){
            this.resourceName = resourceName;
            this.resourceCode = resourceCode;
        }

        @Override
        public void run() {
            while (!stop) {
                Entry entry = null;

                try {
                    entry = SphU.entry(resourceName);
                    // token acquired, means pass
                    DoublingTest.threeSum(500);
                    pass[resourceCode].addAndGet(1);
                } catch (BlockException e1) {
                    block[resourceCode].incrementAndGet();
                } catch (Exception e2) {
                    // biz exception
                } finally {
                    total[resourceCode].incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }

                Random random2 = new Random();
                try {
                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(400));
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    static class DoublingTest {

        public static double threeSum(int N){

            Random r = new Random(1);
            int[] a = new int[N];
            for(int i =0;i<N;i++){
                a[i] = r.nextInt();
            }
            int cnt = count(a);
            return cnt;

        }

        public static int count(int[] a){
            int N = a.length;
            int cnt = 0;
            for(int i = 0; i<N;i++){
                for(int j = i+1;j<N;j++){
                    for(int k = j+1; k<N;k++){
                        if(a[i]+a[j]+a[k] == 0){
                            cnt++;
                        }
                    }
                }
            }
            return cnt;
        }
    }

}
