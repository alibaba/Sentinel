package com.alibaba.jvm.sandbox.api.resource;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.filter.Filter;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchCondition;

/**
 * 事件观察者
 *
 * @author luanjia@taobao.com
 */
public interface ModuleEventWatcher {

    /**
     * 观察事件
     *
     * @param filter    观察类和方法过滤器
     *                  通过这个对象来告知{@code EventWatcher}观察的类和方法
     * @param listener  事件监听器
     *                  观察到的事件将会告知此事件监听器
     * @param progress  观察渲染进度报告
     * @param eventType 观察事件类型
     * @return {@code watchId}，本次观察的唯一编号，{@code watcherId}对象将会是整个操作的唯一KEY，后续删除观察事件的时候也需要通过同一个{@code watcherId}来完成
     */
    int watch(Filter filter, EventListener listener, Progress progress, Event.Type... eventType);

    /**
     * 观察事件
     *
     * @param filter    观察类和方法过滤器
     *                  通过这个对象来告知{@code EventWatcher}观察的类和方法
     * @param listener  事件监听器
     *                  观察到的事件将会告知此事件监听器
     * @param eventType 观察事件类型
     * @return {@code watchId}，本次观察的唯一编号，{@code watcherId}对象将会是整个操作的唯一KEY，后续删除观察事件的时候也需要通过同一个{@code watcherId}来完成
     */
    int watch(Filter filter, EventListener listener, Event.Type... eventType);

    /**
     * 观察事件
     *
     * @param condition 事件观察条件，只有符合条件的类/方法才会被观察
     * @param listener  事件监听器
     *                  观察到的事件将会告知此事件监听器
     * @param progress  观察渲染进度报告
     * @param eventType 观察事件类型
     * @return {@code watchId}，本次观察的唯一编号，{@code watcherId}对象将会是整个操作的唯一KEY，后续删除观察事件的时候也需要通过同一个{@code watcherId}来完成
     * @since {@code sandbox-api:1.0.10}
     */
    int watch(EventWatchCondition condition, EventListener listener, Progress progress, Event.Type... eventType);

    /**
     * 删除观察事件
     *
     * @param watcherId {@code watcherId}观察唯一编号
     *                  在{@link #watch(Filter, EventListener, Progress, Event.Type...)}方法返回值获得
     * @param progress  清除观察渲染进度报告
     *                  删除观察事件时，将会删除掉之前埋入的观察代码，需要对已经渲染的类从新进行代码渲染
     */
    void delete(int watcherId, Progress progress);

    /**
     * 删除观察事件
     *
     * @param watcherId {@code watcherId}观察唯一编号
     *                  在{@link #watch(Filter, EventListener, Progress, Event.Type...)}方法返回值获得
     */
    void delete(int watcherId);

    /**
     * 观察事件
     * <p>
     * 和{@link #watch(Filter, EventListener, Progress, Event.Type...)}不一样的地方是，当观察结束时会自动delete观察事件
     * 还原之前观察时被渲染的代码
     * </p>
     *
     * @param filter    观察类和方法过滤器
     * @param listener  事件监听器
     * @param wProgress 观察渲染进度报告
     * @param watchCb   观察渲染完成回调
     * @param dProgress 清除观察渲染进度报告
     * @param eventType 观察事件类型
     * @throws Throwable 观察渲染完成回调出错
     */
    void watching(Filter filter,
                  EventListener listener,
                  Progress wProgress,
                  WatchCallback watchCb,
                  Progress dProgress,
                  Event.Type... eventType
    ) throws Throwable;

    /**
     * 观察事件
     * <p>
     * 和{@link #watch(Filter, EventListener, Progress, Event.Type...)}不一样的地方是，当观察结束时会自动delete观察事件
     * 还原之前观察时被渲染的代码
     * </p>
     *
     * @param filter    观察类和方法过滤器
     * @param listener  事件监听器
     * @param watchCb   观察渲染完成回调
     * @param eventType 观察事件类型
     * @throws Throwable 观察渲染完成回调出错
     */
    void watching(Filter filter,
                  EventListener listener,
                  WatchCallback watchCb,
                  Event.Type... eventType
    ) throws Throwable;


    /**
     * 观察回调
     * <p>
     * 在{@link #watching(Filter, EventListener, Progress, WatchCallback, Progress, Event.Type...)}完成watch类渲染时回调
     * </p>
     */
    interface WatchCallback {

        /**
         * 观察渲染完成
         * <p>
         * {@code return}或者抛出异常，都将会让{@link #watching(Filter, EventListener, Progress, WatchCallback, Progress, Event.Type...)}
         * 方法进入到结束流程，在结束流程中将会冻结和删除被渲染的代码
         * </p>
         *
         * @throws Throwable 回掉出错
         */
        void watchCompleted() throws Throwable;

    }


    /**
     * 进度报告
     * <p>
     * 观察类是需要对类进行增强，有时候需要对大量的类进行渲染，耗时比较长。
     * 通过这样的报告方式可以让外部感知到当前渲染的进度。
     * 在渲染完成之前，事件是不会触发给到{@link EventListener}的
     * </p>
     */
    interface Progress {

        /**
         * 进度开始
         *
         * @param total 总共需要渲染类的总数
         */
        void begin(int total);

        /**
         * 进度报告(成功)
         *
         * @param clazz 当前进行行变的类
         * @param index 当前形变类的序号,从0开始
         */
        void progressOnSuccess(Class clazz, int index);

        /**
         * 进度报告(失败)
         *
         * @param clazz 当前进行行变的类
         * @param index 当前形变类的序号,从0开始
         * @param cause 失败异常
         */
        void progressOnFailed(Class clazz, int index, Throwable cause);

        /**
         * 进度结束
         * <p>如果是add方法，则影响的数量是递增；</p>
         * <p>如果是remove方法，则影响的数量是递减；</p>
         * <p>当彻remove完成之后，cCnt = mCnt = 0;</p>
         *
         * @param cCnt 影响类总数
         * @param mCnt 影响方法总数
         */
        void finish(int cCnt, int mCnt);

    }

}
