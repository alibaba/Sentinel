package com.alibaba.jvm.sandbox.api.http.printer;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.min;

/**
 * 高性能文本输出
 * 高性能背后的代价就是相对比较高的CPU开销
 *
 * @author luanjia@taobao.com
 */
public class ConcurrentLinkedQueuePrinter implements Printer {

    private static final String NUL_STRING = new String(new byte[]{0x00});
    private final PrintWriter writer;
    private final ConcurrentLinkedQueue<String> writeQueue;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final int capacity;

    // 是否被打断
    private final AtomicBoolean isBrokenRef = new AtomicBoolean(false);

    private final long delayStepTimeMs;
    private final long delayMaxTimeMs;
    private volatile long delayTimeMs;

    /**
     * 构造文本输出
     *
     * @param writer          {@link PrintWriter}
     * @param delayStepTimeMs 轮空延时步长(单位毫秒)
     *                        Printer会每隔这个时间间隔检查是否有输出,如果没有任何输出则会延时累加,
     *                        整体累加延时不会超过{@link #delayMaxTimeMs}
     * @param delayMaxTimeMs  最大轮空延时
     * @param capacity        队列最大容量
     *                        超过最大容量的输入将会被主动丢弃
     */
    public ConcurrentLinkedQueuePrinter(final PrintWriter writer,
                                        final long delayStepTimeMs,
                                        final long delayMaxTimeMs,
                                        final int capacity) {
        this.writer = writer;
        this.writeQueue = new ConcurrentLinkedQueue<String>();
        this.delayStepTimeMs = delayStepTimeMs;
        this.delayMaxTimeMs = delayMaxTimeMs;
        this.delayTimeMs = delayStepTimeMs;
        this.capacity = capacity;
    }

    /**
     * 构造文本输出
     * <ul>
     * <li>{@code delayStepTimeMs}=20</li>
     * <li>{@code delayMaxTimeMs}=200</li>
     * <li>{@link Integer#MAX_VALUE}</li>
     * </ul>
     *
     * @param writer {@link PrintWriter}
     */
    public ConcurrentLinkedQueuePrinter(final PrintWriter writer) {
        this(writer, 20, 200, Integer.MAX_VALUE);
    }

    private boolean isOverCapacity() {
        return writeQueue.size() >= capacity;
    }

    @Override
    public Printer print(String string) {
        if (!isOverCapacity()) {
            writeQueue.offer(string);
        }
        return this;
    }

    @Override
    public Printer println(String string) {
        if (!isOverCapacity()) {
            writeQueue.offer(string + "\n");
        }
        return this;
    }


    private void commit() {
        while (!writeQueue.isEmpty()) {
            final String string = writeQueue.poll();
            if (null == string) {
                writer.print(NUL_STRING);
            } else {
                writer.print(string);
            }
        }
    }

    @Override
    public Printer flush() {
        commit();
        writer.flush();
        return this;
    }

    private long computeDelayTimeMs() {
        if (delayTimeMs >= delayMaxTimeMs) {
            return delayTimeMs;
        } else {
            final long newDelayTime = delayTimeMs + delayStepTimeMs;
            delayTimeMs = min(newDelayTime, delayMaxTimeMs);
            return delayTimeMs;
        }
    }

    private void resetDelayTimeMs() {
        delayTimeMs = delayStepTimeMs;
    }

    private void delay() throws InterruptedException {
        // 如果最大延时时间不为正数，说明不要延时
        if (delayMaxTimeMs <= 0) {
            return;
        }
        lock.lock();
        try {
            condition.await(computeDelayTimeMs(), TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }


    @Override
    public Printer waitingForBroken() {
        waitingForBroken(0L, TimeUnit.MILLISECONDS);
        return this;
    }

    @Override
    public boolean waitingForBroken(final long time,
                                    final TimeUnit unit) {

        // 超时等待时间
        final long timeMs = unit.toMillis(time);

        // 是否需要进行超时控制
        final boolean isTimeoutControl = timeMs > 0;

        // 方法执行开始时间(超时等待计时开始)
        final long startMs = isTimeoutControl
                ? System.currentTimeMillis()
                : 0;

        try {
            int heartBeat = 0;
            while (!writer.checkError()
                    && !isBrokenRef.get()
                    && !Thread.currentThread().isInterrupted()) {

                if (isTimeoutControl
                        && System.currentTimeMillis() - startMs >= timeMs) {
                    return true;
                }

                if (writeQueue.isEmpty()) {
                    delay();
                    if (heartBeat++ > 20) {
                        heartBeat = 0;
                        writer.write(0x0);
                    }
                } else {
                    flush();
                    resetDelayTimeMs();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            flush();
        } catch (Throwable cause) {
            // maybe IOException
        }

        return false;

    }

    @Override
    public Printer broken() {
        isBrokenRef.set(true);
        return this;
    }

    @Override
    public boolean isBroken() {
        return isBrokenRef.get();
    }

    @Override
    public void close() {
        writeQueue.clear();
        if (null != writer) {
            try {
                writer.close();
            } catch (Throwable cause) {
                // ignore...
            }
        }
    }

}
