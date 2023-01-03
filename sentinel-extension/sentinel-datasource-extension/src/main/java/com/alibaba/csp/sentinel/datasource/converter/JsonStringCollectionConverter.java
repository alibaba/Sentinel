package com.alibaba.csp.sentinel.datasource.converter;

import com.alibaba.fastjson.JSON;

import java.util.Collection;
import java.util.List;

/**
 * JsonConverter when type for DataSource is String and type for Sentinel is instanceof Collection
 *
 * @param <S> type for Sentinel which wrapped by Collection
 *
 * @author Jiajiangnan
 */
public class JsonStringCollectionConverter<S extends Object> extends JsonStringConverter<Collection<S>>{

    private final Class<S> sentinelClass;

    public JsonStringCollectionConverter(Class<S> sentinelClass) {
        this.sentinelClass = sentinelClass;
    }

    @Override
    public List<S> toSentinel(String source) {
        return JSON.parseArray(source, sentinelClass);
    }
}
