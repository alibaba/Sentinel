package com.alibaba.csp.sentinel.dashboard.datasource.entity.opentsdb;

import java.util.Objects;
/**
 * @author kangjiabang
 */
public class QueryInfo {

    private String aggregator = "sum";

    private String metric;

    private TagInfo tags;


    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
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
        QueryInfo queryInfo = (QueryInfo) o;
        return Objects.equals(aggregator, queryInfo.aggregator) &&
                Objects.equals(metric, queryInfo.metric) &&
                Objects.equals(tags, queryInfo.tags);
    }

    @Override
    public int hashCode() {

        return Objects.hash(aggregator, metric, tags);
    }

    @Override
    public String toString() {
        return "QueryInfo{" +
                "aggregator='" + aggregator + '\'' +
                ", metric='" + metric + '\'' +
                ", tags=" + tags +
                '}';
    }
}
