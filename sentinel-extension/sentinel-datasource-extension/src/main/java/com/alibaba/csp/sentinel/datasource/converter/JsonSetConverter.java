package com.alibaba.csp.sentinel.datasource.converter;

import com.alibaba.fastjson.JSON;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JsonConverter when type for DataSource is String and type for Sentinel is instanceof Collection
 *
 * @param <T> type for Sentinel which wrapped by Collection
 *
 * @author Jiajiangnan
 */
public class JsonSetConverter<T extends Object> extends JsonConverter <String, Set<T>> {

    private final Class<T> sentinelClass;

    public JsonSetConverter(Class<T> sentinelClass) {
        this.sentinelClass = sentinelClass;
    }

    @Override
    public Set<T> toSentinel(String source) {
        List<T> list = JSON.parseArray(source, sentinelClass);

        if(list != null && list.size() > 0) {
            return  new HashSet<>(list);
        }
        return null;
    }

}
