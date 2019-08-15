package com.alibaba.csp.sentinel.dashboard.datasource.entity.opentsdb;

import java.util.List;
import java.util.Objects;

/**
 * @author kangjiabang
 */
public class OpenTsdbMetricQueryEntity {

    private String start;

    private String end;

    private List<QueryInfo> queries;


    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public List<QueryInfo> getQueries() {
        return queries;
    }

    public void setQueries(List<QueryInfo> queries) {
        this.queries = queries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenTsdbMetricQueryEntity that = (OpenTsdbMetricQueryEntity) o;
        return Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(queries, that.queries);
    }

    @Override
    public int hashCode() {

        return Objects.hash(start, end, queries);
    }

    @Override
    public String toString() {
        return "OpenTsdbMetricQueryEntity{" +
                "start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", queries=" + queries +
                '}';
    }
}
