package com.alibaba.csp.sentinel.datasource.converter;

import com.alibaba.fastjson.JSON;

/**
 * super JsonConverter when type for DataSource is String and type for Sentinel is Object
 *
 * @param <S> type for Sentinel
 *
 * @author Jiajiangnan
 */
public abstract class JsonStringConverter<S> implements SentinelConverter<String, S>{

    @Override
    public String fromSentinel(S source) {
        return JSON.toJSONString(source);
    }

}
