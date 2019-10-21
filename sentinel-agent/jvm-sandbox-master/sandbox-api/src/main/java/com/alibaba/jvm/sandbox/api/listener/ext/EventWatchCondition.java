package com.alibaba.jvm.sandbox.api.listener.ext;

import com.alibaba.jvm.sandbox.api.filter.ExtFilter;
import com.alibaba.jvm.sandbox.api.filter.Filter;

/**
 * 事件观察条件
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
public interface EventWatchCondition {

    /**
     * 获取"或"关系的查询过滤器数组
     * <p>
     * 这里会有点绕，因为在{@code sandbox-api:1.0.10}的时候已经将这个接口披露出来了
     * 所以为了向下兼容API，必须保持{@link Filter}的声明。但实际上内部可能是一个{@link ExtFilter}，
     * 会在容器内部做兼容性判断。
     *
     * @return "或"关系的查询过滤器数组
     * @since {@code sandbox-api:1.0.10}
     * @since {@code sandbox-api:1.0.11}
     * </p>
     */
    Filter[] getOrFilterArray();

}
