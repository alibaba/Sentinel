package com.alibaba.csp.sentinel.node;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

/**
 * a concurrent counter implemented by {@link java.util.concurrent.Semaphore}
 * <p>
 * In principle, statistics and limiting concurrency values require the use of semaphores.
 * If you use an atomic counter like AtomicInteger, it will lead to race conditions, which
 * will not achieve the desired effect in extreme cases.
 * </p>
 *
 * @author yizhenqiang
 */
public class ConcurrentCounter {

    /**
     * The volatitle keyword is needed here because the semaphore may be replaced by the whole
     */
    private AtomicReference<PermitsSemaphore> threshold = new AtomicReference<PermitsSemaphore>();

    /**
     * Because the {@link #threshold} may be replaced entirely, you need to ensure that the {@link PermitsSemaphore#acquire()} and
     * {@link PermitsSemaphore#release()} operations are based on the same semaphore, so you need to use {@link ThreadLocal} here.
     */
    private static ThreadLocal<PermitsSemaphore> guaranteedUseSame = new ThreadLocal<PermitsSemaphore>();

    public ConcurrentCounter() {}

    public ConcurrentCounter(int threshold) {
        this.threshold.set(new PermitsSemaphore(threshold));
    }

    /**
     * Try to +1 the counter and compare it with maxConcurrentValue
     *
     * @param maxConcurrentValue
     * @return true-has not exceeded the threshold, false-has exceeded the threshold
     */
    public boolean tryCompareAndIncrease (int maxConcurrentValue) {
        PermitsSemaphore curThreshold = threshold.get();
        if (curThreshold == null || curThreshold.getPermits() != maxConcurrentValue) {
            threshold.compareAndSet(curThreshold, new PermitsSemaphore(maxConcurrentValue));
            // can not be null
            curThreshold = threshold.get();
        }

        guaranteedUseSame.set(curThreshold);

        return curThreshold.tryAcquire();
    }

    /**
     * Try to -1 the counter
     */
    public void tryDecrease () {
        PermitsSemaphore curThreshold = guaranteedUseSame.get();
        if (curThreshold != null) {
            curThreshold.release();

            guaranteedUseSame.remove();
        }
    }

    /**
     * get the current concurrency value
     *
     * @return
     */
    public int getCurConcurrentValue () {
        PermitsSemaphore permitsSemaphore = threshold.get();
        return permitsSemaphore == null ? 0 : permitsSemaphore.getCurValue();
    }

    /**
     * The semaphore in the JDK doesn't even keep the number of permissions in the constructor, so wrap it here.
     */
    private class PermitsSemaphore extends Semaphore {

        /**
         * maximum permissive number
         */
        private int permits;

        public PermitsSemaphore(int permits) {
            super(permits);
        }

        public int getCurValue() {
            return permits - availablePermits();
        }

        public int getPermits() {
            return permits;
        }
    }
}
