package com.alibaba.csp.sentinel.dashboard.datasource.entity.opentsdb;

import java.util.Objects;

/**
 * @author kangjiabang
 */
public class OpenTsdbMetricEntity {

    private String metric;

    private long timestamp;

    private String value;

    private TagInfo tags;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TagInfo getTags() {
        return tags;
    }

    public void setTags(TagInfo tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenTsdbMetricEntity that = (OpenTsdbMetricEntity) o;
        return timestamp == that.timestamp &&
                Objects.equals(metric, that.metric) &&
                Objects.equals(value, that.value) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {

        return Objects.hash(metric, timestamp, value, tags);
    }

    @Override
    public String toString() {
        return "OpenTsdbMetricEntity{" +
                "metric='" + metric + '\'' +
                ", timestamp=" + timestamp +
                ", value='" + value + '\'' +
                ", tags=" + tags +
                '}';
    }
}
