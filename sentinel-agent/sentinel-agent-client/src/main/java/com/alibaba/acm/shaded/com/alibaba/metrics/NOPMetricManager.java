package com.alibaba.acm.shaded.com.alibaba.metrics;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * IMetricManager的空实现
 *
 * @author wangtao 2016-06-20 17:11.
 */
public class NOPMetricManager implements IMetricManager {

    private static final SortedMap emptyMap = new TreeMap();
    private static final SortedSet emptySet = new TreeSet();

    @Override
    public Meter getMeter(String group, MetricName name) {
        return NOP_METER;
    }

    @Override
    public Counter getCounter(String group, MetricName name) {
        return NOP_COUNTER;
    }

    @Override
    public Histogram getHistogram(String group, MetricName name) {
        return NOP_HISTOGRAM;
    }

    @Override
    public Histogram getHistogram(String group, MetricName name, ReservoirType type) {
        return NOP_HISTOGRAM;
    }

    @Override
    public Timer getTimer(String group, MetricName name) {
        return NOP_TIMER;
    }

    @Override
    public Timer getTimer(String group, MetricName name, ReservoirType type) {
        return NOP_TIMER;
    }

    @Override
    public Compass getCompass(String group, MetricName name, ReservoirType type) {
        return NOP_COMPASS;
    }

    @Override
    public Compass getCompass(String group, MetricName name) {
        return NOP_COMPASS;
    }

    @Override
    public FastCompass getFastCompass(String group, MetricName name) {
        return NOP_FAST_COMPASS;
    }

    @Override
    public ClusterHistogram getClusterHistogram(String group, MetricName name, long[] buckets) {
        return NOP_CLUSTER_HISTOGRAM;
    }

    @Override
    public List<String> listMetricGroups() {
        return Collections.emptyList();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public Map<String, Set<MetricName>> listMetricNamesByGroup() {
        return Collections.emptyMap();
    }

    @Override
    public MetricRegistry getMetricRegistryByGroup(String group) {
        return NOP_REGISTRY;
    }

	@Override
    @SuppressWarnings("unchecked")
	public SortedMap<MetricName, Gauge> getGauges(String group, MetricFilter filter) {
        return emptyMap;
	}

	@Override
    @SuppressWarnings("unchecked")
	public SortedMap<MetricName, Counter> getCounters(String group, MetricFilter filter) {
		return emptyMap;
	}

	@Override
    @SuppressWarnings("unchecked")
	public SortedMap<MetricName, Histogram> getHistograms(String group, MetricFilter filter) {
		return emptyMap;
	}

	@Override
    @SuppressWarnings("unchecked")
	public SortedMap<MetricName, Meter> getMeters(String group, MetricFilter filter) {
		return emptyMap;
	}

	@Override
    @SuppressWarnings("unchecked")
	public SortedMap<MetricName, Timer> getTimers(String group, MetricFilter filter) {
		return emptyMap;
	}

    @Override
    @SuppressWarnings("unchecked")
    public SortedMap<MetricName, Compass> getCompasses(String group, MetricFilter filter) {
        return emptyMap;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public SortedMap<MetricName, FastCompass> getFastCompasses(String group, MetricFilter filter) {
        return emptyMap;
    }

    @Override
    public void register(String group, MetricName name, Metric metric) {

    }

    static final Meter NOP_METER = new Meter() {
        @Override
        public void mark() {
        }

        @Override
        public void mark(long n) {
        }

        @Override
        public long getCount() {
            return 0;
        }

        @Override
        public double getFifteenMinuteRate() {
            return 0;
        }

        @Override
        public double getFiveMinuteRate() {
            return 0;
        }

        @Override
        public double getMeanRate() {
            return 0;
        }

        @Override
        public double getOneMinuteRate() {
            return 0;
        }

        @Override
        public Map<Long, Long> getInstantCount() {
            return emptyMap;
        }

        @Override
        public int getInstantCountInterval() {
            return 0;
        }

        @Override
        public Map<Long, Long> getInstantCount(long startTime) {
            return emptyMap;
        }

        @Override
        public long lastUpdateTime() {
            return 0;
        }
    };

    static final BucketCounter NOP_BUCKET_COUNTER = new BucketCounter() {

        @Override
        public void update() {

        }

        @Override
        public void update(long n) {

        }

        @Override
        public Map<Long, Long> getBucketCounts() {
            return emptyMap;
        }

        @Override
        public Map<Long, Long> getBucketCounts(long startTime) {
            return emptyMap;
        }

        @Override
        public int getBucketInterval() {
            return 0;
        }

        @Override
        public void inc() {

        }

        @Override
        public void inc(long n) {

        }

        @Override
        public void dec() {

        }

        @Override
        public void dec(long n) {

        }

        @Override
        public long getCount() {
            return 0;
        }

        @Override
        public long lastUpdateTime() {
            return 0;
        }
    };

    static final Counter NOP_COUNTER = new Counter() {
        @Override
        public void inc() {
        }

        @Override
        public void inc(long n) {
        }

        @Override
        public void dec() {
        }

        @Override
        public void dec(long n) {
        }

        @Override
        public long getCount() {
            return 0;
        }

        @Override
        public long lastUpdateTime() {
            return 0;
        }
    };

    static final Histogram NOP_HISTOGRAM = new Histogram() {
        @Override
        public void update(int value) {
        }

        @Override
        public void update(long value) {
        }

        @Override
        public long getCount() {
            return 0;
        }

        @Override
        public Snapshot getSnapshot() {
            return NOP_SNAPSHOT;
        }

        @Override
        public long lastUpdateTime() {
            return 0;
        }
    };

    private static final Timer.Context NOP_CONTEXT = new Timer.Context() {

        @Override
        public void close() throws IOException {
        }

        @Override
        public long stop() {
            return 0;
        }
    };

    private static final Compass.Context NOP_COMPASS_CONTEXT = new Compass.Context() {
        @Override
        public long stop() {
            return 0;
        }

        @Override
        public void success() {

        }

        @Override
        public void error(String errorCode) {

        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public void markAddon(String suffix) {

        }
    };

    private static final Snapshot NOP_SNAPSHOT = new Snapshot() {
        @Override
        public double getValue(double quantile) {
            return 0;
        }

        @Override
        public long[] getValues() {
            return new long[0];
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public double getMedian() {
            return 0;
        }

        @Override
        public double get75thPercentile() {
            return 0;
        }

        @Override
        public double get95thPercentile() {
            return 0;
        }

        @Override
        public double get98thPercentile() {
            return 0;
        }

        @Override
        public double get99thPercentile() {
            return 0;
        }

        @Override
        public double get999thPercentile() {
            return 0;
        }

        @Override
        public long getMax() {
            return 0;
        }

        @Override
        public double getMean() {
            return 0;
        }

        @Override
        public long getMin() {
            return 0;
        }

        @Override
        public double getStdDev() {
            return 0;
        }

        @Override
        public void dump(OutputStream output) {
        }
    };

    static final Timer NOP_TIMER = new Timer() {
        @Override
        public void update(long duration, TimeUnit unit) {
        }

        @Override
        public <T> T time(Callable<T> event) throws Exception {
            return event.call();
        }

        @Override
        public Context time() {
            return NOP_CONTEXT;
        }

        @Override
        public long getCount() {
            return 0;
        }

        @Override
        public double getFifteenMinuteRate() {
            return 0;
        }

        @Override
        public double getFiveMinuteRate() {
            return 0;
        }

        @Override
        public double getMeanRate() {
            return 0;
        }

        @Override
        public double getOneMinuteRate() {
            return 0;
        }

        @Override
        public Snapshot getSnapshot() {
            return NOP_SNAPSHOT;
        }

        @Override
        public Map<Long, Long> getInstantCount() {
            return emptyMap;
        }

        @Override
        public int getInstantCountInterval() {
            return 0;
        }

        @Override
        public Map<Long, Long> getInstantCount(long startTime) {
            return emptyMap;
        }

        @Override
        public long lastUpdateTime() {
            return 0;
        }
    };

    static final Compass NOP_COMPASS = new Compass() {

        @Override
        public Map<String, BucketCounter> getErrorCodeCounts() {
            return Collections.emptyMap();
        }

        @Override
        public double getSuccessRate() {
            return 0;
        }

        @Override
        public long getSuccessCount() {
            return 0;
        }

        @Override
        public void update(long duration, TimeUnit unit) {

        }

        @Override
        public void update(long duration, TimeUnit unit, boolean isSuccess, String errorCode, String addon) {

        }

        @Override
        public <T> T time(Callable<T> event) throws Exception {
            return event.call();
        }

        @Override
        public Compass.Context time() {
            return NOP_COMPASS_CONTEXT;
        }

        @Override
        public long getCount() {
            return 0;
        }

        @Override
        public double getFifteenMinuteRate() {
            return 0;
        }

        @Override
        public double getFiveMinuteRate() {
            return 0;
        }

        @Override
        public double getMeanRate() {
            return 0;
        }

        @Override
        public double getOneMinuteRate() {
            return 0;
        }

        @Override
        public Snapshot getSnapshot() {
            return NOP_SNAPSHOT;
        }

        @Override
        public Map<Long, Long> getInstantCount() {
            return emptyMap;
        }

        @Override
        public Map<String, BucketCounter> getAddonCounts() {
            return emptyMap;
        }

        @Override
        public BucketCounter getBucketSuccessCount() {
            return NOP_BUCKET_COUNTER;
        }

        @Override
        public int getInstantCountInterval() {
            return 0;
        }

        @Override
        public Map<Long, Long> getInstantCount(long startTime) {
            return emptyMap;
        }

        @Override
        public long lastUpdateTime() {
            return 0;
        }
    };

    static final FastCompass NOP_FAST_COMPASS = new FastCompass() {
        @Override
        public void record(long duration, String subCategory) {

        }

        @Override
        public Map<String, Map<Long, Long>> getMethodCountPerCategory() {
            return emptyMap;
        }

        @Override
        public Map<String, Map<Long, Long>> getMethodRtPerCategory() {
            return emptyMap;
        }

        @Override
        public Map<String, Map<Long, Long>> getMethodCountPerCategory(long startTime) {
            return emptyMap;
        }

        @Override
        public Map<String, Map<Long, Long>> getMethodRtPerCategory(long startTime) {
            return emptyMap;
        }

        @Override
        public int getBucketInterval() {
            return 0;
        }

        @Override
        public Map<String, Map<Long, Long>> getCountAndRtPerCategory() {
            return emptyMap;
        }

        @Override
        public Map<String, Map<Long, Long>> getCountAndRtPerCategory(long startTime) {
            return emptyMap;
        }

        @Override
        public long lastUpdateTime() {
            return 0;
        }
    };

    static final ClusterHistogram NOP_CLUSTER_HISTOGRAM = new ClusterHistogram() {
        @Override
        public void update(long value) {

        }

        @Override
        public Map<Long, Map<Long, Long>> getBucketValues(long startTime) {
            return emptyMap;
        }

        @Override
        public long lastUpdateTime() {
            return 0;
        }
    };

    private static final MetricRegistry NOP_REGISTRY = new MetricRegistry() {
        @Override
        public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {
            return metric;
        }

        @Override
        public <T extends Metric> T register(MetricName name, T metric) throws IllegalArgumentException {
            return metric;
        }

        @Override
        public void registerAll(MetricSet metrics) throws IllegalArgumentException {

        }

        @Override
        public Counter counter(String name) {
            return NOP_COUNTER;
        }

        @Override
        public Counter counter(MetricName name) {
            return NOP_COUNTER;
        }

        @Override
        public Histogram histogram(MetricName name) {
            return NOP_HISTOGRAM;
        }

        @Override
        public Histogram histogram(MetricName name, ReservoirType type) {
            return NOP_HISTOGRAM;
        }

        @Override
        public Histogram histogram(String name) {
            return NOP_HISTOGRAM;
        }

        @Override
        public Meter meter(MetricName name) {
            return NOP_METER;
        }

        @Override
        public Meter meter(String name) {
            return NOP_METER;
        }

        @Override
        public Timer timer(MetricName name) {
            return NOP_TIMER;
        }

        @Override
        public Timer timer(String name) {
            return NOP_TIMER;
        }

        @Override
        public Timer timer(MetricName name, ReservoirType type) {
            return NOP_TIMER;
        }

        @Override
        public Compass compass(MetricName name) {
            return NOP_COMPASS;
        }

        @Override
        public Compass compass(String name) {
            return NOP_COMPASS;
        }

        @Override
        public Compass compass(MetricName name, ReservoirType type) {
            return NOP_COMPASS;
        }

        @Override
        public FastCompass fastCompass(MetricName name) {
            return NOP_FAST_COMPASS;
        }

        @Override
        public ClusterHistogram clusterHistogram(MetricName name, long[] buckets) {
            return NOP_CLUSTER_HISTOGRAM;
        }

        @Override
        public boolean remove(MetricName name) {
            return false;
        }

        @Override
        public void removeMatching(MetricFilter filter) {

        }

        @Override
        public void addListener(MetricRegistryListener listener) {

        }

        @Override
        public void removeListener(MetricRegistryListener listener) {

        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedSet<MetricName> getNames() {
            return emptySet;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Gauge> getGauges() {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Gauge> getGauges(MetricFilter filter) {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Counter> getCounters() {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Counter> getCounters(MetricFilter filter) {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Histogram> getHistograms() {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Histogram> getHistograms(MetricFilter filter) {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Meter> getMeters() {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Meter> getMeters(MetricFilter filter) {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Timer> getTimers() {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Timer> getTimers(MetricFilter filter) {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Compass> getCompasses(MetricFilter filter) {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Compass> getCompasses() {
            return emptyMap;
        }

        @Override
        public SortedMap<MetricName, FastCompass> getFastCompasses() {
            return emptyMap;
        }

        @Override
        public SortedMap<MetricName, FastCompass> getFastCompasses(MetricFilter filter) {
            return emptyMap;
        }

        @Override
        public SortedMap<MetricName, ClusterHistogram> getClusterHistograms(MetricFilter filter) {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SortedMap<MetricName, Metric> getMetrics(MetricFilter filter) {
            return emptyMap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<MetricName, Metric> getMetrics() {
            return emptyMap;
        }

        @Override
        public long lastUpdateTime() {
            return 0;
        }
    };

    @Override
    public Map<MetricName, Metric> getMetrics(String group) {
        return emptyMap;
    }

    @Override
    public Map<Class<? extends Metric>, Map<MetricName, ? extends Metric>> getCategoryMetrics(String group) {
        return getCategoryMetrics(group, MetricFilter.ALL);
    }

    @Override
    public Map<Class<? extends Metric>, Map<MetricName, ? extends Metric>> getCategoryMetrics(String group,
            MetricFilter filter) {
        Map<Class<? extends Metric>, Map<MetricName, ? extends Metric>> result = new HashMap<Class<? extends Metric>, Map<MetricName, ? extends Metric>>();

        Map<MetricName, Gauge> gauges = Collections.emptyMap();
        Map<MetricName, Counter> counters = Collections.emptyMap();
        Map<MetricName, Histogram> histograms = Collections.emptyMap();
        Map<MetricName, Meter> meters = Collections.emptyMap();
        Map<MetricName, Timer> timers = Collections.emptyMap();
        Map<MetricName, Compass> compasses = Collections.emptyMap();
        Map<MetricName, FastCompass> fastCompasses = Collections.emptyMap();

        result.put(Gauge.class, gauges);
        result.put(Counter.class, counters);
        result.put(Histogram.class, histograms);
        result.put(Meter.class, meters);
        result.put(Timer.class, timers);
        result.put(Compass.class, compasses);
        result.put(FastCompass.class, fastCompasses);
        
        return result;
    }

    @Override
    public void clear() {

    }

    @Override
    public Map<Class<? extends Metric>, Map<MetricName, ? extends Metric>> getAllCategoryMetrics(MetricFilter filter) {
        return emptyMap;
    }

}
