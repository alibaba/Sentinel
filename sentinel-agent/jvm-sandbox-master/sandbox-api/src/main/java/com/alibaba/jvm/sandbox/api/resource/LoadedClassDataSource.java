package com.alibaba.jvm.sandbox.api.resource;

import com.alibaba.jvm.sandbox.api.filter.Filter;

import java.util.Iterator;
import java.util.Set;

/**
 * 已加载类数据源
 *
 * @author luanjia@taobao.com
 */
public interface LoadedClassDataSource {

    /**
     * 获取所有已加载的类集合
     *
     * @return 所有已加载的类集合
     */
    Set<Class<?>> list();

    /**
     * 根据过滤器搜索出匹配的类集合
     *
     * @param filter 扩展过滤器
     * @return 匹配的类集合
     */
    Set<Class<?>> find(Filter filter);

    /**
     * 获取所有已加载类的集合迭代器
     * <p>
     * 对比 {@link #list()} 而言，有更优的内存、CPU开销
     *
     * @return 所有已加载的类集合迭代器
     * @since {@code sandbox-api:1.0.15}
     */
    Iterator<Class<?>> iteratorForLoadedClasses();

}
