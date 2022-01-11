package com.alibaba.csp.sentinel.datasource.converter;

import com.alibaba.fastjson.JSON;

/**
 * JsonConverter when type for DataSource is String and type for Sentinel is instanceof Object which not wrapped by Collection
 *
 * @param <S> type for Sentinel is instanceof Object which not wrapped by Collection
 *
 * @author Jiajiangnan
 */
public class JsonStringObjectConverter<S extends Object> extends JsonStringConverter<S>{

    private final Class<S> sentinelClass;

    public JsonStringObjectConverter(Class<S> sentinelClass) {
        this.sentinelClass = sentinelClass;
    }

    @Override
    public S toSentinel(String source) {
        return JSON.parseObject(source, sentinelClass);
    }
}
