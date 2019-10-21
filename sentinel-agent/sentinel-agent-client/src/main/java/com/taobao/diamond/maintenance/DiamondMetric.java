package com.taobao.diamond.maintenance;

import com.alibaba.acm.shaded.com.alibaba.metrics.*;

/**
 * metric��Ϣ
 * 
 * @author Diamond
 *
 */
public class DiamondMetric {

	/**
	 * ʵ��һ��registry������ĵ�һ��ģ�飬�൱��һ��Ӧ�ó����metricsϵͳ��������ά��һ��Map
	 */
	private static final MetricRegistry metricRegistry = MetricManager
			.getIMetricManager().getMetricRegistryByGroup("diamond-client");

	private static FastCompass getConfigCompass = metricRegistry
			.fastCompass(MetricName.build("middleware.diamond.request.compass.get"));

	private static FastCompass publishCompass = metricRegistry
			.fastCompass(MetricName.build("middleware.diamond.request.compass.publish"));

	private static ClusterHistogram getClusterHistogram = metricRegistry
			.clusterHistogram(MetricName.build("middleware.diamond.request.clusterHistogram.get"), new long[]{200, 500, 1000, 3000, 5000, 10000});

	private static ClusterHistogram publishClusterHistogram = metricRegistry
			.clusterHistogram(MetricName.build("middleware.diamond.request.clusterHistogram.publish"), new long[]{200, 500, 1000, 3000, 5000, 10000});

	public static MetricRegistry getMetricRegistry() {
		return metricRegistry;
	}

	public static FastCompass getConfigCompass() {
		return getConfigCompass;
	}

	public static FastCompass getPublishCompass() {
		return publishCompass;
	}

	public static ClusterHistogram getClusterHistogram() {
		return getClusterHistogram;
	}

	public static ClusterHistogram getPublishClusterHistogram() {
		return publishClusterHistogram;
	}
}
