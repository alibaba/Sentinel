package com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class KieConfigItem {
    @JSONField(name = "id")
    private String id;

    @JSONField(name = "label_format")
    private String labelFormat;

    @JSONField(name = "key")
    private String key;

    @JSONField(name = "value")
    private String value;

    @JSONField(name = "value_type")
    private String valueType;

    @JSONField(name = "create_revision")
    private int createRevision;

    @JSONField(name = "update_revision")
    private int updateRevision;

    @JSONField(name = "status")
    private String status;

    @JSONField(name = "create_time")
    private String createTime;

    @JSONField(name = "update_time")
    private String updateTime;

    @JSONField(name = "labels")
    private KieConfigLabels labels;
}
