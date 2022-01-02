package com.alibaba.csp.sentinel.datasource.converter;

import com.alibaba.fastjson.JSON;

/**
 * JsonConverter when type for DataSource is String and type for Sentinel is instanceof Object which not wrapped by Collection
 *
 * @param <T> type for Sentinel is instanceof Object which not wrapped by Collection
 *
 * @author Jiajiangnan
 */
public class JsonObjectConverter<S, T extends Object> extends JsonConverter <String, T> {

    private final Class<T> sentinelClass;

    public JsonObjectConverter(Class<T> sentinelClass) {
        this.sentinelClass = sentinelClass;
    }

    @Override
    public T toSentinel(String source) {
        return JSON.parseObject(source, sentinelClass);
    }


}
