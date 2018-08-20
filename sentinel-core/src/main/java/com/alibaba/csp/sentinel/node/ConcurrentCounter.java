package com.alibaba.csp.sentinel.node;

import com.alibaba.csp.sentinel.log.RecordLog;

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

    /**
     * save cur execute {@link ConcurrentCounter#tryAcquire} result
     */
    private static ThreadLocal<Boolean> curTryAcquireResult = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.TRUE;
        }
    };

    public ConcurrentCounter() {
    }

    public ConcurrentCounter(int threshold) {
        this.threshold.set(new PermitsSemaphore(threshold));
    }


    /**
     * try update this ConcurrentCounter threshold
     * @param threshold
     */
    public void tryUpdateThredhold(int threshold) {
        if (threshold < 0) {
            RecordLog.warn("ConcurrentCounter#updateThredhold fail, threshold can not smaller than 0, threshold:" + threshold);
            return;
        }

        PermitsSemaphore permitsSemaphore = this.threshold.get();
        if (permitsSemaphore == null || permitsSemaphore.getPermits() != threshold) {
            this.threshold.compareAndSet(permitsSemaphore, new PermitsSemaphore(threshold));
        }
    }

    /**
     * try acquire a resource and save the result to {@link ConcurrentCounter#curTryAcquireResult}
     *
     * @return true-has not exceeded the threshold, false-has exceeded the threshold
     */
    public boolean tryAcquire() {

        PermitsSemaphore permitsSemaphore = threshold.get();
        if (permitsSemaphore == null) {
            return true;
        }

        /**
         * guaranteed next {@link ConcurrentCounter#} use the same PermitsSemaphore
         */
        guaranteedUseSame.set(permitsSemaphore);

        boolean result = permitsSemaphore.tryAcquire();
        curTryAcquireResult.set(result);

        return result;
    }

    /**
     * release a resource
     */
    public void release() {
        PermitsSemaphore permitsSemaphore = guaranteedUseSame.get();
        Boolean tryAcquireResult = curTryAcquireResult.get();

        if (permitsSemaphore != null && Boolean.TRUE.equals(tryAcquireResult)) {
            permitsSemaphore.release();

            guaranteedUseSame.remove();
            curTryAcquireResult.remove();
        }
    }

    /**
     * get the current try acquire result
     *
     * @return
     */
    public boolean getCurTryAcquireResult() {
        Boolean result = curTryAcquireResult.get();
        return result != null ? result : true;
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
            this.permits = permits;
        }

        public int getCurValue() {
            return permits - availablePermits();
        }

        public int getPermits() {
            return permits;
        }
    }
}