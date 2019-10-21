package com.alibaba.acm.shaded.com.alibaba.metrics;

import java.lang.reflect.Method;

/**
 * The design concept is heavily borrowed from SLF4j (http://www.slf4j.org/), the logging framework.
 * The application only depends on the metrics api.
 * The implementation will be dynamically bound.
 * If the implementation if not found in classpath, by default the {@link NOPMetricManager} will be bound.
 * @author wangtao 2016-06-17 10:27.
 */
public class MetricManager {

    private static final String BINDER_CLASS = "com.alibaba.acm.shaded.com.alibaba.metrics.MetricManagerBinder";

    public static final IMetricManager NOP_METRIC_MANAGER = new NOPMetricManager();

    private static volatile IMetricManager iMetricManager;

    /**
     * Create a {@link Meter} metric in given group, and name.
     * if not exist, an instance will be created.
     * 根据给定的group和name, 获取一个Meter实例，如果不存在则会创建
     * Meter(计量器)主要用于统计调用qps, 包括最近1min, 5min, 15min的移动平均qps
     *
     * @param group the group of MetricRegistry
     * @param name the name of the metric
     * @return an instance of meter
     */
    public static Meter getMeter(String group, MetricName name) {
        IMetricManager manager = getIMetricManager();
        return manager.getMeter(group, name);
    }

    /**
     * Create a {@link Counter} metric in given group, and name.
     * if not exist, an instance will be created.
     * 根据给定的group和name, 获取一个Counter实例，如果不存在则会创建
     * Counter(计数器), 主要用于用于计数，支持+1, -1, +n, -n等操作
     *
     * @param group the group of MetricRegistry
     * @param name the name of the metric
     * @return an instance of counter
     */
    public static Counter getCounter(String group, MetricName name) {
        IMetricManager manager = getIMetricManager();
        return manager.getCounter(group, name);
    }

    /**
     * Create a {@link Histogram} metric in given group, and name.
     * if not exist, an instance will be created.
     * 根据给定的group和name, 获取一个Histogram实例，如果不存在则会创建
     * Histogram(直方图), 主要用于统计分布情况，例如调用rt分布，
     * 能够迅速了解统计指标的最大值，最小值，平均值，方差，70%,85%,95%分位数等信息
     *
     * @param group the group of MetricRegistry
     * @param name the name of the metric
     * @return an instance of histogram
     */
    public static Histogram getHistogram(String group, MetricName name) {
        IMetricManager manager = getIMetricManager();
        return manager.getHistogram(group, name);
    }

    /**
     * Create a {@link Histogram} metric in given group, name, and {@link ReservoirType} type.
     * if not exist, an instance will be created.
     * 根据给定的group和name和type, 获取一个Histogram实例，如果不存在则会创建
     * Histogram(直方图), 主要用于统计分布情况，例如调用rt分布，
     * 能够迅速了解统计指标的最大值，最小值，平均值，方差，70%，85%，95%分位数等信息
     *
     * @param group the group of MetricRegistry
     * @param name the name of the metric
     * @param type  the type of the {@link ReservoirType}
     * @return an instance of histogram
     */
    public static Histogram getHistogram(String group, MetricName name, ReservoirType type) {
        IMetricManager manager = getIMetricManager();
        return manager.getHistogram(group, name, type);
    }

    /**
     * Create a {@link Timer} metric in given group, and name.
     * if not exist, an instance will be created.
     * 根据给定的group和name, 获取一个Timer实例，如果不存在则会创建
     * Timer(计时器), 主要用于给定指标的qps, rt分布, 可以理解为Meter+Histogram
     * 能够方便的统计某指标的qps, 和rt的最大值，最小值，平均值，方差，70%,85%,95%分位数等信息
     *
     * @param group the group of MetricRegistry
     * @param name the name of the metric
     * @return an instance of timer
     */
    public static Timer getTimer(String group, MetricName name) {
        IMetricManager manager = getIMetricManager();
        return manager.getTimer(group, name);
    }

    /**
     * Create a {@link Timer} metric in given group, name, and {@link ReservoirType} type.
     * if not exist, an instance will be created.
     * 根据给定的group, name, type, 获取一个Timer实例，如果不存在则会创建
     * Timer(计时器), 主要用于给定指标的qps, rt分布, 可以理解为Meter+Histogram
     * 能够方便的统计某指标的qps, 和rt的最大值，最小值，平均值，方差，70%, 85%, 95%分位数等信息
     *
     * @param group the group of MetricRegistry
     * @param name the name of the metric
     * @param type the type of reservoir
     * @return an instance of timer
     */
    public static Timer getTimer(String group, MetricName name, ReservoirType type) {
        IMetricManager manager = getIMetricManager();
        return manager.getTimer(group, name, type);
    }

    /**
     * Create a {@link Compass} metric in given group, and name.
     * if not exist, an instance will be created.
     * 根据给定的group和name, 获取一个Compass实例，如果不存在则会创建
     * Compass(罗盘), 主要用于统计给定指标的qps, rt分布，调用成功次数，以及错误码分布等信息
     *
     * @param group the group of MetricRegistry
     * @param name the name of the metric
     * @return an instance of compass
     */
    public static Compass getCompass(String group, MetricName name) {
        IMetricManager manager = getIMetricManager();
        return manager.getCompass(group, name);
    }

    /**
     * Create a {@link Compass} metric in given group, name, and {@link ReservoirType} type.
     * if not exist, an instance will be created.
     * 根据给定的group, name和type, 获取一个Compass实例，如果不存在则会创建
     * Compass(罗盘), 主要用于统计给定指标的qps, rt分布，调用成功次数，以及错误码分布等信息
     *
     * @param group the group of MetricRegistry
     * @param name the name of the metric
     * @return an instance of compass
     */
    public static Compass getCompass(String group, MetricName name, ReservoirType type) {
        IMetricManager manager = getIMetricManager();
        return manager.getCompass(group, name, type);
    }

    /**
     * Create a {@link FastCompass} metric in given group, and name
     * if not exist, an instance will be created.
     * 根据给定的group和name, 获取一个{@link FastCompass}实例，如果不存在则会创建
     * {@link FastCompass}, 主要用于在高吞吐率场景下，统计给定指标的qps, 平均rt，成功率，以及错误码等指标
     *
     * @param group the group of MetricRegistry
     * @param name the name of the metric
     * @return an instance of {@link FastCompass}
     */
    public static FastCompass getFastCompass(String group, MetricName name) {
        IMetricManager manager = getIMetricManager();
        return manager.getFastCompass(group, name);
    }

    /**
     * Create a {@link ClusterHistogram} metric in given group, and name
     * if not exist, an instance will be created.
     * 根据给定的group和name, 获取一个{@link ClusterHistogram}实例，如果不存在则会创建
     * {@link ClusterHistogram}, 主要用于集群分位数统计
     *
     * @param group the group of MetricRegistry
     * @param name the name of the metric
     * @return an instance of {@link ClusterHistogram}
     */
    public static ClusterHistogram getClusterHistogram(String group, MetricName name, long[] buckets) {
        IMetricManager manager = getIMetricManager();
        return manager.getClusterHistogram(group, name, buckets);
    }

    /**
     * Create a {@link ClusterHistogram} metric in given group, and name
     * if not exist, an instance will be created.
     * 根据给定的group和name, 获取一个{@link ClusterHistogram}实例，如果不存在则会创建
     * {@link ClusterHistogram}, 主要用于集群分位数统计
     *
     * @param group the group of MetricRegistry
     * @param name the name of the metric
     * @return an instance of {@link ClusterHistogram}
     */
    public static ClusterHistogram getClusterHistogram(String group, MetricName name) {
        IMetricManager manager = getIMetricManager();
        return manager.getClusterHistogram(group, name, null);
    }


    /**
     * Register a customized metric to specified group.
     * @param group the group name of MetricRegistry
     * @param metric the metric to register
     */
    public static void register(String group, MetricName name, Metric metric) {
        IMetricManager manager = getIMetricManager();
        manager.register(group, name, metric);
    }

    /**
     * get dynamically bound {@link IMetricManager} instance
     * @return the {@link IMetricManager} instance bound
     */
    @SuppressWarnings("unchecked")
    public static IMetricManager getIMetricManager() {
        if (iMetricManager == null) {
            synchronized (MetricManager.class) {
                if (iMetricManager == null) {
                    try {
                        Class binderClazz = MetricManager.class.getClassLoader().loadClass(BINDER_CLASS);
                        Method getSingleton = binderClazz.getMethod("getSingleton");
                        Object binderObject = getSingleton.invoke(null);
                        Method getMetricManager = binderClazz.getMethod("getMetricManager");
                        iMetricManager = (IMetricManager) getMetricManager.invoke(binderObject);
                    } catch (Exception e) {
                        iMetricManager = NOP_METRIC_MANAGER;
                    }
                }
            }
        }
        return iMetricManager;
    }

}
