package com.alibaba.csp.sentinel.demo.qlearning;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.qlearning.QLearningMetric;
import com.alibaba.csp.sentinel.qlearning.qtable.QTableStorage;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class UserPeakCpuDemo {
    private static final String KEY = "abc";

    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();

    private static volatile boolean stop = false;
    private static volatile boolean stop1 = false;
    private static volatile boolean stop2 = false;
    private static volatile boolean start2 = false;
    private static volatile boolean flag = false;
    private static final int threadCount = 50;
    private static int seconds = 60 + 40;

    static QLearningMetric qLearningMetric = QLearningMetric.getInstance();
    static QTableStorage qTableStorage = new QTableStorage();

    private static boolean isQLearning = true;//set a switch， when it is true it will apply Qlearnig algorithm.
    private static boolean isTraining = false;//set a switch， when it is true it will update q table.

    private static String qTablePath = "sentinel-demo/sentinel-demo-basic/src/main/java/com/alibaba/csp/sentinel/demo/qlearning/"+"UserPeakCpuDemo-QTable.txt";

    private static boolean ifCheckCPU = true;//when it is true it will take Cpu as a state.

    public static void main(String[] args) throws Exception {

        QLearningMetric qLearningMetric = QLearningMetric.getInstance();
        qLearningMetric.setLearning(isQLearning);
        qLearningMetric.setTraining(isTraining);
        qLearningMetric.setIfCheckCPU(ifCheckCPU);

        qLearningMetric.setQtable(qTableStorage.read(qTablePath));

        Entry entry = null;
        try {
            entry = SphU.entry(KEY);
        } catch (Exception e) {
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }

        tick();
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(new StableTask());
            t.setName("stable-task");
            t.start();
        }
        Thread.sleep(20000);

        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new RunTask1());
            t.setName("sentinel-run-task1");
            t.start();
        }
        Thread.sleep(40000);
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new RunTask2());
            t.setName("sentinel-run-task2");
            t.start();
        }
    }
    static void CpuRunMethod(){
        int num = 5000;
        Random random = new Random();
        List<Integer> randomList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            randomList.add(random.nextInt(num));
        }
        Collections.sort(randomList);
    }

    static class StableTask implements Runnable {
        @Override
        public void run() {
            while (!stop) {
                Entry entry = null;
                try {
                    entry = SphU.entry(KEY);
                    CpuRunMethod();
                    // token acquired, means pass
                    Random random2 = new Random();
                    try {
                        TimeUnit.MILLISECONDS.sleep(random2.nextInt(480));
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    pass.incrementAndGet();
                } catch (BlockException e1) {
                    Random random2 = new Random();
                    try {
                        TimeUnit.MILLISECONDS.sleep(random2.nextInt(500));
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    block.incrementAndGet();
                } catch (Exception e2) {
                    // biz exception
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }
            }
        }
    }
    static class RunTask1 implements Runnable {
        @Override
        public void run() {
            while (!stop1) {
                Entry entry = null;
                try {
                    entry = SphU.entry(KEY);
                    CpuRunMethod();
                    pass.addAndGet(1);
                } catch (BlockException e1) {
                    Random random2 = new Random();
                    try {
                        TimeUnit.MILLISECONDS.sleep(random2.nextInt(15));
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    block.incrementAndGet();
                } catch (Exception e2) {
                    // biz exception
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }
            }
        }
    }

    static class RunTask2 implements Runnable {
        @Override
        public void run() {
            while (!stop2) {
                Entry entry = null;
                try {
                    entry = SphU.entry(KEY);
                    CpuRunMethod();
                    pass.addAndGet(1);
                } catch (BlockException e1) {
                    Random random2 = new Random();
                    try {
                        TimeUnit.MILLISECONDS.sleep(random2.nextInt(10));
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    block.incrementAndGet();
                } catch (Exception e2) {
                    // biz exception
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }

            }
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

            long oldTotal = 0;
            long oldPass = 0;
            long oldBlock = 0;
            while (!stop) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                long globalTotal = total.get();
                long oneSecondTotal = globalTotal - oldTotal;
                oldTotal = globalTotal;

                long globalPass = pass.get();
                long oneSecondPass = globalPass - oldPass;
                oldPass = globalPass;

                long globalBlock = block.get();
                long oneSecondBlock = globalBlock - oldBlock;
                oldBlock = globalBlock;

                System.out.println(seconds + " send qps is: " + oneSecondTotal);
                System.out.print(TimeUtil.currentTimeMillis() + ", total:" + oneSecondTotal
                        + ", pass:" + oneSecondPass
                        + ", block:" + oneSecondBlock);
                if (qLearningMetric.isTrain()) {
                    System.out.println(" ------now is training------ ");
                } else {
                    System.out.println(" ------now is testing------ ");
                }

                if(seconds <= 70) {
                    stop1 = true;
                }
                if(seconds <= 20) {
                    stop2 = true;
                }
                if (seconds-- <= 0) {
                    stop = true;
                }
            }

            long cost = System.currentTimeMillis() - start;
            System.out.println("time cost: " + cost + " ms");
            System.out.println("total:" + total.get() + ", pass:" + pass.get()
                    + ", block:" + block.get());

            ConcurrentHashMap<String, double[]> qtable = qLearningMetric.getQtable();
            showPolicy(qtable);
            qTableStorage.save(qtable,qTablePath);

            System.exit(0);
        }
    }

    public static synchronized void showPolicy(ConcurrentHashMap<String, double[]> Qtable) {
        String fromState;
        int toAction;

        System.out.println("\n  ======= Show Policy ======= ");

        for (Map.Entry entry : Qtable.entrySet()) {
            fromState = (String) entry.getKey();
            toAction = qLearningMetric.policy(fromState);
            System.out.println("Current State: " + fromState + "       Action: " + qLearningMetric.getActionNames()[toAction] + "        Q Value: " + Qtable.get(fromState)[toAction]);
        }
    }
}

