package com.alibaba.acm.shaded.com.alibaba.metrics;


import java.util.SortedMap;
import java.util.SortedSet;

/**
 * A registry of metric instances.
 */
public abstract class MetricRegistry implements MetricSet {


    // ******************** start static method ************************
    /**
     * @see #name(String, String...)
     */
    public static MetricName name(Class<?> klass, String... names) {
        return name(klass.getName(), names);
    }

    /**
     * Shorthand method for backwards compatibility in creating metric names.
     *
     * Uses {@link MetricName#build(String...)} for its
     * heavy lifting.
     *
     * @see MetricName#build(String...)
     * @param name The first element of the name
     * @param names The remaining elements of the name
     * @return A metric name matching the specified components.
     */
    public static MetricName name(String name, String... names) {
        final int length;

        if (names == null) {
            length = 0;
        } else {
            length = names.length;
        }

        final String[] parts = new String[length + 1];
        parts[0] = name;

        for (int i = 0; i < length; i++) {
            parts[i+1] = names[i];
        }

        return MetricName.build(parts);
    }

    // ******************** end static method ************************


    /**
     * Given a {@link Metric}, registers it under the given name.
     *
     * @param name   the name of the metric
     * @param metric the metric
     * @param <T>    the type of the metric
     * @return {@code metric}
     * @throws IllegalArgumentException if the name is already registered
     */
    public abstract <T extends Metric> T register(String name, T metric) throws IllegalArgumentException;

    /**
     * Given a {@link Metric}, registers it under the given name.
     *
     * @param name   the name of the metric
     * @param metric the metric
     * @param <T>    the type of the metric
     * @return {@code metric}
     * @throws IllegalArgumentException if the name is already registered
     */
    public abstract <T extends Metric> T register(MetricName name, T metric) throws IllegalArgumentException;

    /**
     * Given a metric set, registers them.
     *
     * @param metrics    a set of metrics
     * @throws IllegalArgumentException if any of the names are already registered
     */
    public abstract void registerAll(MetricSet metrics) throws IllegalArgumentException;

    /**
     * Creates a new {@link Counter} and registers it under the given name.
     *
     * @param name the name of the metric
     * @return a new {@link Counter}
     */
    public abstract Counter counter(String name);

    /**
     * Return the {@link Counter} registered under this name; or create and register
     * a new {@link Counter} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Counter}
     */
    public abstract Counter counter(MetricName name);

    /**
     * Return the {@link Histogram} registered under this name; or create and register
     * a new {@link Histogram} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Histogram}
     */
    public abstract Histogram histogram(MetricName name);

    /**
     * Create a histogram with given name, and reservoir type
     * @param name the name of the metric
     * @param type the type of reservoir
     * @return a histogram instance
     */
    public abstract Histogram histogram(MetricName name, ReservoirType type);

    /**
     * Creates a new {@link Histogram} and registers it under the given name.
     *
     * @param name the name of the metric
     * @return a new {@link Histogram}
     */
    public abstract Histogram histogram(String name);

    /**
     * Return the {@link Meter} registered under this name; or create and register
     * a new {@link Meter} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Meter}
     */
    public abstract Meter meter(MetricName name);

    /**
     * Creates a new {@link Meter} and registers it under the given name.
     *
     * @param name the name of the metric
     * @return a new {@link Meter}
     */
    public abstract Meter meter(String name);

    /**
     * Return the {@link Timer} registered under this name; or create and register
     * a new {@link Timer} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Timer}
     */
    public abstract Timer timer(MetricName name);

    /**
     * Create a timer with given name, and reservoir type
     * @param name the name of the metric
     * @param type the type of reservoir
     * @return a timer instance
     */
    public abstract Timer timer(MetricName name, ReservoirType type);

    /**
     * Creates a new {@link Timer} and registers it under the given name.
     *
     * @param name the name of the metric
     * @return a new {@link Timer}
     */
    public abstract Timer timer(String name);

    /**
     * Return the {@link Compass} registered under this name; or create and register
     * a new {@link Timer} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Compass}
     */
    public abstract Compass compass(MetricName name);

    /**
     * Create a compass with given name, and reservoir type
     * @param name the name of the metric
     * @param type the type of reservoir
     * @return a compass instance
     */
    public abstract Compass compass(MetricName name, ReservoirType type);

    /**
     * Create a FastCompass with given name
     * @param name the name of the metric
     * @return a FastCompass instance
     */
    public abstract FastCompass fastCompass(MetricName name);

    /**
     * Creates a new {@link Compass} and registers it under the given name.
     *
     * @param name the name of the metric
     * @return a new {@link Compass}
     */
    public abstract Compass compass(String name);


    /**
     * Creates a new {@link ClusterHistogram} and registers it under the given name.
     *
     * @param name the name of the metric
     * @param buckets the array of long values for buckets
     * @return a new {@link ClusterHistogram}
     */
    public abstract ClusterHistogram clusterHistogram(MetricName name, long[] buckets);


    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     * @return whether or not the metric was removed
     */
    public abstract boolean remove(MetricName name);

    /**
     * Removes all metrics which match the given filter.
     *
     * @param filter a filter
     */
    public abstract void removeMatching(MetricFilter filter);

    /**
     * Adds a {@link MetricRegistryListener} to a collection of listeners that will be notified on
     * metric creation.  Listeners will be notified in the order in which they are added.
     * <p/>
     * <b>N.B.:</b> The listener will be notified of all existing metrics when it first registers.
     *
     * @param listener the listener that will be notified
     */
    public abstract void addListener(MetricRegistryListener listener);

    /**
     * Removes a {@link MetricRegistryListener} from this registry's collection of listeners.
     *
     * @param listener the listener that will be removed
     */
    public abstract void removeListener(MetricRegistryListener listener);

    /**
     * Returns a set of the names of all the metrics in the registry.
     *
     * @return the names of all the metrics
     */
    public abstract SortedSet<MetricName> getNames();

    /**
     * Returns a map of all the gauges in the registry and their names.
     *
     * @return all the gauges in the registry
     */
    public abstract SortedMap<MetricName, Gauge> getGauges();

    /**
     * Returns a map of all the gauges in the registry and their names which match the given filter.
     *
     * @param filter    the metric filter to match
     * @return all the gauges in the registry
     */
    public abstract SortedMap<MetricName, Gauge> getGauges(MetricFilter filter);

    /**
     * Returns a map of all the counters in the registry and their names.
     *
     * @return all the counters in the registry
     */
    public abstract SortedMap<MetricName, Counter> getCounters();

    /**
     * Returns a map of all the counters in the registry and their names which match the given
     * filter.
     *
     * @param filter    the metric filter to match
     * @return all the counters in the registry
     */
    public abstract SortedMap<MetricName, Counter> getCounters(MetricFilter filter);

    /**
     * Returns a map of all the histograms in the registry and their names.
     *
     * @return all the histograms in the registry
     */
    public abstract SortedMap<MetricName, Histogram> getHistograms();

    /**
     * Returns a map of all the histograms in the registry and their names which match the given
     * filter.
     *
     * @param filter    the metric filter to match
     * @return all the histograms in the registry
     */
    public abstract SortedMap<MetricName, Histogram> getHistograms(MetricFilter filter);

    /**
     * Returns a map of all the meters in the registry and their names.
     *
     * @return all the meters in the registry
     */
    public abstract SortedMap<MetricName, Meter> getMeters();

    /**
     * Returns a map of all the meters in the registry and their names which match the given filter.
     *
     * @param filter    the metric filter to match
     * @return all the meters in the registry
     */
    public abstract SortedMap<MetricName, Meter> getMeters(MetricFilter filter);

    /**
     * Returns a map of all the timers in the registry and their names.
     *
     * @return all the timers in the registry
     */
    public abstract SortedMap<MetricName, Timer> getTimers();

    /**
     * Returns a map of all the timers in the registry and their names which match the given filter.
     *
     * @param filter    the metric filter to match
     * @return all the timers in the registry
     */
    public abstract SortedMap<MetricName, Timer> getTimers(MetricFilter filter);


    /**
     * Returns a map of all the compasses in the registry and their names.
     *
     * @return all the compasses in the registry
     */
    public abstract SortedMap<MetricName, Compass> getCompasses();

    /**
     * Returns a map of all the compasses in the registry and their names which match the given filter.
     *
     * @param filter    the metric filter to match
     * @return all the compasses in the registry
     */
    public abstract SortedMap<MetricName, Compass> getCompasses(MetricFilter filter);

    /**
     * Returns a map of all the compasses in the registry and their names.
     *
     * @return all the compasses in the registry
     */
    public abstract SortedMap<MetricName, FastCompass> getFastCompasses();

    /**
     * Returns a map of all the compasses in the registry and their names which match the given filter.
     *
     * @param filter    the metric filter to match
     * @return all the compasses in the registry
     */
    public abstract SortedMap<MetricName, FastCompass> getFastCompasses(MetricFilter filter);

    /**
     * Returns a map of all the {@link ClusterHistogram} in the registry and their names which match the given filter.
     *
     * @param filter    the metric filter to match
     * @return all the {@link ClusterHistogram} in the registry
     */
    public abstract SortedMap<MetricName, ClusterHistogram> getClusterHistograms(MetricFilter filter);



    /**
     * Returns a map of all the metrics in the registry and their names which match the given filter
     * @param filter the metric filter to match
     * @return all the metrics in the registry
     */
    public abstract SortedMap<MetricName, Metric> getMetrics(MetricFilter filter);


}
