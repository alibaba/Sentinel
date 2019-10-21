package com.alibaba.acm.shaded.com.alibaba.metrics;


/**
 * <pre>
 * A gauge metric is an instantaneous reading of a particular value. To instrument a queue's depth,
 * for example:
 *
 * final Queue&lt;String&gt; queue = new ConcurrentLinkedQueue&lt;String&gt;();
 * final Gauge&lt;Integer&gt; queueDepth = new Gauge&lt;Integer&gt;() {
 *     public Integer getValue() {
 *         return queue.size();
 *     }
 * };
 *
 * 一种实时数据的度量，反映的是瞬态的数据，不具有累加性。
 * 具体的实现由具体定义，例如，获取当前jvm的活跃线程数
 * </pre>
 *
 * @param <T> the type of the metric's value
 */
public interface Gauge<T> extends Metric {
    /**
     * Returns the metric's current value.
     *
     * @return the metric's current value
     */
    T getValue();
}
