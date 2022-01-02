package com.alibaba.csp.sentinel.datasource.converter;

/**
 * Default Converter when source type for DataSource is the same to type for Sentinel
 *
 * @param <T> the same type
 *
 * @author Jiajiangnan
 */
public class EmptyConverter<T> implements SentinelConverter<T, T>{

    @Override
    public T toSentinel(T source) {
        return source;
    }

    @Override
    public T fromSentinel(T source) {
        return source;
    }
}
