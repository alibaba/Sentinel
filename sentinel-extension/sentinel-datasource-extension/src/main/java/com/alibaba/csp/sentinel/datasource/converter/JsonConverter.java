package com.alibaba.csp.sentinel.datasource.converter;

import com.alibaba.fastjson.JSON;

/**
 * super JsonConverter when type for DataSource is String and type for Sentinel is Object
 *
 * @param <T> type for Sentinel isObject
 *
 * @author Jiajiangnan
 */
public abstract class JsonConverter<S, T> implements SentinelConverter<String, T>{

    @Override
    public String fromSentinel(T source) {
        return JSON.toJSONString(source);
    }

}
