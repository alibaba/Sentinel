package com.alibaba.acm.shaded.com.alibaba.metrics;

/**
 * A metric which calculates the distribution of a value.
 * 直方分布指标，例如，可以用于统计某个接口的响应时间，可以展示50%, 70%, 90%的请求响应时间落在哪个区间内
 *
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately computing running
 *      variance</a>
 */
public interface Histogram extends Metric, Sampling, Counting {

    /**
     * Adds a recorded value.
     * 将某个整型值添加到
     *
     * @param value the length of the value
     */
    void update(int value);

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    void update(long value);

}
