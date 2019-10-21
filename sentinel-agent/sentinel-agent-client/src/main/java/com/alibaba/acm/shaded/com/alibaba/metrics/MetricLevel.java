package com.alibaba.acm.shaded.com.alibaba.metrics;

/**
 * enum的数值不能为负数，且不能太大
 * @author duanling 2016-09-07
 * @author wangtao 2016-07-07 11:48.
 */
public enum MetricLevel {

    TRIVIAL, // 轻微指标

    MINOR,   // 次要指标

    NORMAL,  // 一般指标

    MAJOR,   // 重要指标

    CRITICAL; // 关键指标

    static {
        for (MetricLevel level : MetricLevel.values()) {
            if (level.ordinal() < 0) {
                throw new RuntimeException("MetricLevel can not < 0");
            }
        }
    }

    public static int getMaxValue() {
        MetricLevel[] levels = MetricLevel.values();
        int max = levels[0].ordinal();
        for (MetricLevel level : levels) {
            int value = level.ordinal();
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}
