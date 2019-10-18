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
package com.alibaba.csp.sentinel.slots.statistic.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * <p>
 * Basic data structure for statistic metrics in Sentinel.
 * Sentinel中统计指标的基本数据结构。
 * </p>
 * <p>
 * Leap array use sliding window algorithm to count data. Each bucket cover {@code windowLengthInMs} time span,
 * and the total time span is {@link #intervalInMs}, so the total bucket amount is:
 * {@code sampleCount = intervalInMs / windowLengthInMs}.
 * Leap array使用滑动窗口算法对数据进行计数。每个存储段都覆盖{@code windowLengthInMs}时间跨度，
 * 而总时间跨度是{@link #intervalInMs}，因此总存储区量为：{@code sampleCount = intervalInMs / windowLengthInMs}。
 * </p>
 *
 * @param <T> type of statistic data
 * @author jialiang.linjl
 * @author Eric Zhao
 * @author Carpenter Lee
 */
public abstract class LeapArray<T> {

    /**
     * 时间窗口大小
     */
    protected int windowLengthInMs;
    /**
     * 桶的总数
     */
    protected int sampleCount;
    /**
     * 总时间长度
     */
    protected int intervalInMs;

    /**
     * 时间窗口的数组
     */
    protected final AtomicReferenceArray<WindowWrap<T>> array;

    /**
     * The conditional (predicate) update lock is used only when current bucket is deprecated.
     * 条件（谓词）更新锁仅在不使用当前存储桶时使用。
     */
    private final ReentrantLock updateLock = new ReentrantLock();

    /**
     * The total bucket count is: {@code sampleCount = intervalInMs / windowLengthInMs}.
     * 存储桶总数为：{@code sampleCount = intervalInMs / windowLengthInMs}。
     *
     * @param sampleCount  bucket count of the sliding window 滑动窗口拥有的桶总数
     * @param intervalInMs the total time interval of this {@link LeapArray} in milliseconds 此{@link LeapArray}的总时间间隔（以毫秒为单位）
     */
    public LeapArray(int sampleCount, int intervalInMs) {
        AssertUtil.isTrue(sampleCount > 0, "bucket count is invalid: " + sampleCount);
        AssertUtil.isTrue(intervalInMs > 0, "total time interval of the sliding window should be positive");
        AssertUtil.isTrue(intervalInMs % sampleCount == 0, "time span needs to be evenly divided");

        this.windowLengthInMs = intervalInMs / sampleCount;
        this.intervalInMs = intervalInMs;
        this.sampleCount = sampleCount;

        this.array = new AtomicReferenceArray<>(sampleCount);
    }

    /**
     * Get the bucket at current timestamp.
     * 获取当前时间戳的存储桶。
     *
     * @return the bucket at current timestamp
     */
    public WindowWrap<T> currentWindow() {
        /**
         * 这里获取当前时间戳使用的是TimeUtil，TimeUtik会启动
         * 一个守护线程每sleep 1ms唤醒并且调用System.currentTimeMillis
         * 记录当前时间戳到volatile变量中样每秒调用System.currentTimeMillis
         * 的次数稳定为1000次，如果不通过这个Util的话调用的次数无法估计，有可能远大于1000次
         */
        return currentWindow(TimeUtil.currentTimeMillis());
    }

    /**
     * Create a new statistic value for bucket.
     * 为存储桶创建一个新的统计值。
     *
     * @param timeMillis current time in milliseconds
     * @return the new empty bucket
     */
    public abstract T newEmptyBucket(long timeMillis);

    /**
     * Reset given bucket to provided start time and reset the value.
     * 将给定存储桶重置为提供的开始时间，然后重置该值。
     *
     * @param startTime  the start time of the bucket in milliseconds
     * @param windowWrap current bucket
     * @return new clean bucket at given start time
     */
    protected abstract WindowWrap<T> resetWindowTo(WindowWrap<T> windowWrap, long startTime);

    private int calculateTimeIdx(/*@Valid*/ long timeMillis) {
        // 获取当前毫秒对应到window length的一个id
        long timeId = timeMillis / windowLengthInMs;
        // Calculate current index so we can map the timestamp to the leap array.
        // 获取这个id对应到滚动数组中的具体index
        // 通过取模操作完成了数组的滚动
        return (int)(timeId % array.length());
    }

    protected long calculateWindowStart(/*@Valid*/ long timeMillis) {
        // 计算出这个时间窗口对应的开始时间戳
        return timeMillis - timeMillis % windowLengthInMs;
    }

    /**
     * Get bucket item at provided timestamp.
     * 根据提供的时间戳获取存储桶
     * 每次操作调用currentWindow相当于是一次对齐操作，无论是增加计数还是统计，
     * 调用currentWindow之后保证了我们底层存储的AtomicReferenceArray中对应index存放的
     * 肯定是当前时间戳对应的window，而绝不可能是陈旧的信息
     *
     * @param timeMillis a valid timestamp in milliseconds 有效时间戳（以毫秒为单位）
     * @return current bucket item at provided timestamp if the time is valid; null if time is invalid
     */
    public WindowWrap<T> currentWindow(long timeMillis) {
        if (timeMillis < 0) {
            return null;
        }
        // 获取timeMillis在滑动窗口中的index
        int idx = calculateTimeIdx(timeMillis);
        //  计算出这个时间窗口对应的开始时间戳
        long windowStart = calculateWindowStart(timeMillis);

        /*
         * Get bucket item at given time from the array.
         * 从数组中获取给定时间的存储桶项目。
         *
         * (1) Bucket is absent, then just create a new bucket and CAS update to circular array.
         *      不存在存储桶，则只需创建一个新存储桶，然后将CAS更新为圆形数组即可。
         * (2) Bucket is up-to-date, then just return the bucket.
         *      存储桶是最新的，然后只需返回存储桶即可。
         * (3) Bucket is deprecated, then reset current bucket and clean all deprecated buckets.
         *      不建议使用存储桶，然后重置当前存储桶并清理所有不推荐使用的存储桶。
         */
        while (true) {
            // 获取index对应的窗口
            WindowWrap<T> old = array.get(idx);
            if (old == null) {
                /*
                 *     B0       B1      B2    NULL      B4
                 * ||_______|_______|_______|_______|_______||___
                 * 200     400     600     800     1000    1200  timestamp
                 *                             ^
                 *                          time=888
                 *            bucket is empty, so create new and update
                 *              存储桶为空，因此需要新建和更新
                 *
                 * If the old bucket is absent, then we create a new bucket at {@code windowStart},
                 * then try to update circular array via a CAS operation. Only one thread can
                 * succeed to update, while other threads yield its time slice.
                 * 如果不存在旧存储桶，则我们在{@code windowStart}，
                 * 处创建一个新存储桶，然后尝试通过CAS操作更新循环数组。
                 * 只有一个线程可以成功更新，而其他线程则产生其时间片。
                 */
                // 创建一个新的窗口，通过newEmptyBucket来获取新的统计桶
                WindowWrap<T> window = new WindowWrap<T>(windowLengthInMs, windowStart, newEmptyBucket(timeMillis));
                if (array.compareAndSet(idx, null, window)) {
                    // Successfully updated, return the created bucket.
                    // 成功更新后，返回创建的存储桶。
                    return window;
                } else {
                    // Contention failed, the thread will yield its time slice to wait for bucket available.
                    // 竞争失败，线程将让出时间片以等待存储桶使用。
                    Thread.yield();
                }
            } else if (windowStart == old.windowStart()) {
                /*
                 *     B0       B1      B2     B3      B4
                 * ||_______|_______|_______|_______|_______||___
                 * 200     400     600     800     1000    1200  timestamp
                 *                             ^
                 *                          time=888
                 *            startTime of Bucket 3: 800, so it's up-to-date
                 *
                 * If current {@code windowStart} is equal to the start timestamp of old bucket,
                 * that means the time is within the bucket, so directly return the bucket.
                 * 如果当前{@code windowStart}等于旧存储桶的开始时间戳，则
                 * 表示时间在存储桶内，因此请直接返回存储桶。
                 */
                return old;
            } else if (windowStart > old.windowStart()) {
                /*
                 *   (old)
                 *             B0       B1      B2    NULL      B4
                 * |_______||_______|_______|_______|_______|_______||___
                 * ...    1200     1400    1600    1800    2000    2200  timestamp
                 *                              ^
                 *                           time=1676
                 *          startTime of Bucket 2: 400, deprecated, should be reset
                 *          时段2的startTime：已弃用的400应该重置
                 *
                 * If the start timestamp of old bucket is behind provided time, that means
                 * the bucket is deprecated. We have to reset the bucket to current {@code windowStart}.
                 * Note that the reset and clean-up operations are hard to be atomic,
                 * so we need a update lock to guarantee the correctness of bucket update.
                 *
                 * The update lock is conditional (tiny scope) and will take effect only when
                 * bucket is deprecated, so in most cases it won't lead to performance loss.
                 * 如果旧存储桶的开始时间戳落后于提供的时间，则表示
                 * 该存储桶已弃用。我们必须将存储桶重置为当前的{@code windowStart}。
                 * 请注意，重置和清除操作很难实现原子操作，因此我们需要一个更新锁来确保存储桶更新的正确性。
                 * 更新锁是有条件的（范围很小），仅在不建议使用存储桶时才生效，因此在大多数情况下，它不会导致性能损失。
                 */
                if (updateLock.tryLock()) {
                    try {
                        // Successfully get the update lock, now we reset the bucket.
                        // 成功获取更新锁，重置存储桶。
                        return resetWindowTo(old, windowStart);
                    } finally {
                        updateLock.unlock();
                    }
                } else {
                    // Contention failed, the thread will yield its time slice to wait for bucket available.
                    // 如果获取锁失败，说明已经有其他线程获取锁并进行更新
                    // 因此调用线程让步 并进入下一次自旋循环
                    Thread.yield();
                }
            } else if (windowStart < old.windowStart()) {
                // Should not go through here, as the provided time is already behind.
                // 如果请求的时间比现存的还小，直接返回一个空的，说明这次请求的时间戳已经陈旧了
                return new WindowWrap<T>(windowLengthInMs, windowStart, newEmptyBucket(timeMillis));
            }
        }
    }

    /**
     * Get the previous bucket item before provided timestamp.
     *
     * @param timeMillis a valid timestamp in milliseconds
     * @return the previous bucket item before provided timestamp
     */
    public WindowWrap<T> getPreviousWindow(long timeMillis) {
        if (timeMillis < 0) {
            return null;
        }
        int idx = calculateTimeIdx(timeMillis - windowLengthInMs);
        timeMillis = timeMillis - windowLengthInMs;
        WindowWrap<T> wrap = array.get(idx);

        if (wrap == null || isWindowDeprecated(wrap)) {
            return null;
        }

        if (wrap.windowStart() + windowLengthInMs < (timeMillis)) {
            return null;
        }

        return wrap;
    }

    /**
     * Get the previous bucket item for current timestamp.
     *
     * @return the previous bucket item for current timestamp
     */
    public WindowWrap<T> getPreviousWindow() {
        return getPreviousWindow(TimeUtil.currentTimeMillis());
    }

    /**
     * Get statistic value from bucket for provided timestamp.
     * 从存储桶中获取提供的时间戳的统计值。
     *
     * @param timeMillis a valid timestamp in milliseconds
     * @return the statistic value if bucket for provided timestamp is up-to-date; otherwise null
     */
    public T getWindowValue(long timeMillis) {
        if (timeMillis < 0) {
            return null;
        }
        int idx = calculateTimeIdx(timeMillis);

        WindowWrap<T> bucket = array.get(idx);
        //检查给定的时间戳记是否在当前存储桶中。
        if (bucket == null || !bucket.isTimeInWindow(timeMillis)) {
            return null;
        }

        return bucket.value();
    }

    /**
     * Check if a bucket is deprecated, which means that the bucket
     * has been behind for at least an entire window time span.
     *
     * @param windowWrap a non-null bucket
     * @return true if the bucket is deprecated; otherwise false
     */
    public boolean isWindowDeprecated(/*@NonNull*/ WindowWrap<T> windowWrap) {
        return isWindowDeprecated(TimeUtil.currentTimeMillis(), windowWrap);
    }

    public boolean isWindowDeprecated(long time, WindowWrap<T> windowWrap) {
        // 如果当前时间与对应时间窗口开始时间的差值大于整个统计时长
        // 这意味着该存储桶已经落后了至少整个窗口时间段，无需纳入统计中。
        return time - windowWrap.windowStart() > intervalInMs;
    }

    /**
     * Get valid bucket list for entire sliding window.
     * The list will only contain "valid" buckets.
     *
     * @return valid bucket list for entire sliding window.
     */
    public List<WindowWrap<T>> list() {
        return list(TimeUtil.currentTimeMillis());
    }

    public List<WindowWrap<T>> list(long validTime) {
        int size = array.length();
        List<WindowWrap<T>> result = new ArrayList<WindowWrap<T>>(size);

        for (int i = 0; i < size; i++) {
            WindowWrap<T> windowWrap = array.get(i);
            if (windowWrap == null || isWindowDeprecated(validTime, windowWrap)) {
                continue;
            }
            result.add(windowWrap);
        }

        return result;
    }

    /**
     * Get all buckets for entire sliding window including deprecated buckets.
     *
     * @return all buckets for entire sliding window
     */
    public List<WindowWrap<T>> listAll() {
        int size = array.length();
        List<WindowWrap<T>> result = new ArrayList<WindowWrap<T>>(size);

        for (int i = 0; i < size; i++) {
            WindowWrap<T> windowWrap = array.get(i);
            if (windowWrap == null) {
                continue;
            }
            result.add(windowWrap);
        }

        return result;
    }

    /**
     * Get aggregated value list for entire sliding window.
     * The list will only contain value from "valid" buckets.
     * 获取整个滑动窗口的汇总值列表。该列表将仅包含“有效”存储桶中的值。
     *
     * @return aggregated value list for entire sliding window
     */
    public List<T> values() {
        return values(TimeUtil.currentTimeMillis());
    }

    public List<T> values(long timeMillis) {
        if (timeMillis < 0) {
            return new ArrayList<T>();
        }
        int size = array.length();
        List<T> result = new ArrayList<T>(size);

        for (int i = 0; i < size; i++) {
            WindowWrap<T> windowWrap = array.get(i);
            // 如果当前时间窗口为null或者无效则跳过
            if (windowWrap == null || isWindowDeprecated(timeMillis, windowWrap)) {
                continue;
            }
            result.add(windowWrap.value());
        }
        return result;
    }

    /**
     * Get the valid "head" bucket of the sliding window for provided timestamp.
     * Package-private for test.
     *
     * @param timeMillis a valid timestamp in milliseconds
     * @return the "head" bucket if it exists and is valid; otherwise null
     */
    WindowWrap<T> getValidHead(long timeMillis) {
        // Calculate index for expected head time.
        int idx = calculateTimeIdx(timeMillis + windowLengthInMs);

        WindowWrap<T> wrap = array.get(idx);
        if (wrap == null || isWindowDeprecated(wrap)) {
            return null;
        }

        return wrap;
    }

    /**
     * Get the valid "head" bucket of the sliding window at current timestamp.
     *
     * @return the "head" bucket if it exists and is valid; otherwise null
     */
    public WindowWrap<T> getValidHead() {
        return getValidHead(TimeUtil.currentTimeMillis());
    }

    /**
     * Get sample count (total amount of buckets).
     *
     * @return sample count
     */
    public int getSampleCount() {
        return sampleCount;
    }

    /**
     * Get total interval length of the sliding window in milliseconds.
     *
     * @return interval in second
     */
    public int getIntervalInMs() {
        return intervalInMs;
    }

    /**
     * Get total interval length of the sliding window.
     *
     * @return interval in second
     */
    public double getIntervalInSecond() {
        return intervalInMs / 1000.0;
    }

    public void debug(long time) {
        StringBuilder sb = new StringBuilder();
        List<WindowWrap<T>> lists = list(time);
        sb.append("Thread_").append(Thread.currentThread().getId()).append("_");
        for (WindowWrap<T> window : lists) {
            sb.append(window.windowStart()).append(":").append(window.value().toString());
        }
        System.out.println(sb.toString());
    }

    public long currentWaiting() {
        // TODO: default method. Should remove this later.
        return 0;
    }

    public void addWaiting(long time, int acquireCount) {
        // Do nothing by default.
        throw new UnsupportedOperationException();
    }
}
