package com.alibaba.csp.sentinel.datasource.converter;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * JsonConverter when type for DataSource is String and type for Sentinel is instanceof Collection
 *
 * @param <T> type for Sentinel which wrapped by Collection
 *
 * @author Jiajiangnan
 */
public class JsonArrayConverter<T extends Object> extends JsonConverter <String, List<T>> {

    private final Class<T> ruleClass;

    public JsonArrayConverter(Class<T> ruleClass) {
        this.ruleClass = ruleClass;
    }

    @Override
    public List<T> toSentinel(String source) {
        return JSON.parseArray(source, ruleClass);
    }


}
