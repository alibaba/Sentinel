package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent;

import java.util.concurrent.*;

public class RequestFuture<V> implements Future<V> {

    protected volatile Object result;

    private static final SuccessSignal SUCCESS_SIGNAL = new SuccessSignal();

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) {
            return false;
        }
        synchronized (this) {
            if (isDone()) {
                return false;
            }
            result = new CauseHolder(new CancellationException());
            notifyAll();
        }
        return true;
    }


    @Override
    public boolean isCancelled() {
        return result != null && result instanceof CauseHolder && ((CauseHolder) result).cause instanceof CancellationException;
    }

    @Override
    public boolean isDone() {
        return result != null;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        await();

        Throwable cause = cause();
        if (cause == null) {
            return getNow();
        }
        if (cause instanceof CancellationException) {
            throw (CancellationException) cause;
        }
        throw new ExecutionException(cause);
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (await(timeout, unit)) {
            Throwable cause = cause();
            if (cause == null) {
                return getNow();
            }
            if (cause instanceof CancellationException) {
                throw (CancellationException) cause;
            }
            throw new ExecutionException(cause);
        }
        throw new TimeoutException();
    }


    public boolean isSuccess() {
        return result == null ? false : !(result instanceof CauseHolder);
    }

    public V getNow() {
        return (V) (result == SUCCESS_SIGNAL ? null : result);
    }


    public Throwable cause() {
        if (result != null && result instanceof CauseHolder) {
            return ((CauseHolder) result).cause;
        }
        return null;
    }

    public RequestFuture<V> await() throws InterruptedException {
        return await0(true);
    }


    private RequestFuture<V> await0(boolean interruptable) throws InterruptedException {
        if (!isDone()) {
            if (interruptable && Thread.interrupted()) {
                throw new InterruptedException("thread " + Thread.currentThread().getName() + " has been interrupted.");
            }

            boolean interrupted = false;
            synchronized (this) {
                while (!isDone()) {
                    try {
                        wait(); // 释放锁进入waiting状态，等待其它线程调用本对象的notify()/notifyAll()方法
                    } catch (InterruptedException e) {
                        if (interruptable) {
                            throw e;
                        } else {
                            interrupted = true;
                        }
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return this;
    }

    public boolean await(long timeoutMillis) throws InterruptedException {
        return await0(TimeUnit.MILLISECONDS.toNanos(timeoutMillis), true);
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return await0(unit.toNanos(timeout), true);
    }

    private boolean await0(long timeoutNanos, boolean interruptable) throws InterruptedException {
        if (isDone()) {
            return true;
        }

        if (timeoutNanos <= 0) {
            return isDone();
        }

        if (interruptable && Thread.interrupted()) {
            throw new InterruptedException(toString());
        }

        long startTime = timeoutNanos <= 0 ? 0 : System.nanoTime();
        long waitTime = timeoutNanos;
        boolean interrupted = false;

        try {
            synchronized (this) {
                if (isDone()) {
                    return true;
                }

                if (waitTime <= 0) {
                    return isDone();
                }

                for (; ; ) {
                    try {
                        wait(waitTime / 1000000, (int) (waitTime % 1000000));
                    } catch (InterruptedException e) {
                        if (interruptable) {
                            throw e;
                        } else {
                            interrupted = true;
                        }
                    }

                    if (isDone()) {
                        return true;
                    } else {
                        waitTime = timeoutNanos - (System.nanoTime() - startTime);
                        if (waitTime <= 0) {
                            return isDone();
                        }
                    }
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public RequestFuture<V> awaitUninterruptibly() {
        try {
            return await0(false);
        } catch (InterruptedException e) { // 这里若抛异常了就无法处理了
            throw new java.lang.InternalError();
        }
    }

    public boolean awaitUninterruptibly(long timeoutMillis) {
        try {
            return await0(TimeUnit.MILLISECONDS.toNanos(timeoutMillis), false);
        } catch (InterruptedException e) {
            throw new java.lang.InternalError();
        }
    }

    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        try {
            return await0(unit.toNanos(timeout), false);
        } catch (InterruptedException e) {
            throw new java.lang.InternalError();
        }
    }

    protected RequestFuture<V> setFailure(Throwable cause) {
        if (setFailure0(cause)) {
            return this;
        }
        throw new IllegalStateException("complete already: " + this);
    }

    private boolean setFailure0(Throwable cause) {
        if (isDone()) {
            return false;
        }

        synchronized (this) {
            if (isDone()) {
                return false;
            }
            result = new CauseHolder(cause);
            notifyAll();
        }

        return true;
    }

    protected RequestFuture<V> setSuccess(Object result) {
        if (setSuccess0(result)) { // 设置成功后通知监听器
            return this;
        }
        throw new IllegalStateException("complete already: " + this);
    }

    private boolean setSuccess0(Object result) {
        if (isDone()) {
            return false;
        }

        synchronized (this) {
            if (isDone()) {
                return false;
            }
            if (result == null) {
                this.result = SUCCESS_SIGNAL;
            } else {
                this.result = result;
            }
            notifyAll();
        }
        return true;
    }

    private static class SuccessSignal {

    }

    private static final class CauseHolder {
        final Throwable cause;

        CauseHolder(Throwable cause) {
            this.cause = cause;
        }
    }
}