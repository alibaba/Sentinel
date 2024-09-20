package com.alibaba.csp.sentinel.dashboard.datasource.entity.opentsdb;

import com.alibaba.fastjson.JSONObject;

/**
 * @author kangjiabang
 */
public class TsdbMetricQueryResponseEntity {

    private String metric;

    private TagInfo tags;

    private String aggregateTags;

    private JSONObject dps;


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

    public String getAggregateTags() {
        return aggregateTags;
    }

    public void setAggregateTags(String aggregateTags) {
        this.aggregateTags = aggregateTags;
    }

    public JSONObject getDps() {
        return dps;
    }

    public void setDps(JSONObject dps) {
        this.dps = dps;
    }
}
