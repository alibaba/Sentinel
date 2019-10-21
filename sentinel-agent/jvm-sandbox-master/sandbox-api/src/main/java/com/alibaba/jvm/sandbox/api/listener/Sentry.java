package com.alibaba.jvm.sandbox.api.listener;

import com.alibaba.jvm.sandbox.api.listener.ext.Attachment;

/**
 * 哨兵
 * <p>
 * 哨兵类是一个辅助类，能帮你简化在事件/通知机制处理时方法集联被割裂的编码复杂度。
 * 核心是一个状态机，让哨兵从一个哨点移动到另外一个哨点，从而在事件驱动的架构下完成割裂方法的协同
 * </p>
 *
 * @param <M> 哨点类型，一般喜欢用枚举
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 * @deprecated
 */
@Deprecated
public class Sentry<M> implements Attachment {

    /**
     * 数据封装对象
     */
    class WrapData {
        M monitor = init;
        Object attachment;
    }

    private final M init;
    private final ThreadLocal<WrapData> wrapDataRef = new ThreadLocal<WrapData>() {
        @Override
        protected WrapData initialValue() {
            return new WrapData();
        }
    };

    /**
     * 构建哨兵
     *
     * @param init 哨点初始值
     */
    public Sentry(M init) {
        this.init = init;
    }

    // 获取数据封装
    private WrapData getData() {
        return wrapDataRef.get();
    }

    /**
     * 如果可以，让哨兵移动到下一个哨点
     *
     * @param expect 期待当前的哨点
     * @param next   需要移到的下一个哨点
     * @return TRUE:移动成功；FALSE：移动失败
     */
    public boolean next(final M expect, final M next) {
        final WrapData data = getData();
        if (expect.equals(data.monitor)) {
            data.monitor = next;
            return true;
        }
        return false;
    }

    @Override
    public void attach(Object attachment) {
        getData().attachment = attachment;
    }

    @Override
    public <T> T attachment() {
        return (T) getData().attachment;
    }

}
