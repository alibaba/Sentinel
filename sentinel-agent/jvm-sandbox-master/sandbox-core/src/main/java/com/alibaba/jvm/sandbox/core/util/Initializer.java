package com.alibaba.jvm.sandbox.core.util;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 初始化工具
 * 线程安全
 * Created by luanjia@taobao.com on 15/11/20.
 */
public class Initializer {

    // 是否循环状态
    private final boolean isCycleState;
    // 读写锁
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    // 初始化状态
    private volatile State state = State.NEW;

    /**
     * 构造初始化器(默认为非循环状态)
     */
    public Initializer() {
        this(false);
    }

    /**
     * 构造初始化器
     *
     * @param isCycleState 是否循环状态
     */
    public Initializer(boolean isCycleState) {
        this.isCycleState = isCycleState;
    }

    /**
     * 判断是否未初始化
     *
     * @return 是否未初始化
     */
    public final boolean isNew() {
        return getState() == State.NEW;
    }

    /**
     * 判断是否已初始化完成
     *
     * @return 是否已初始化完成
     */
    public final boolean isInitialized() {
        return getState() == State.INITIALIZED;
    }

    /**
     * 判断是否已被销毁
     *
     * @return 是否已被销毁
     */
    public final boolean isDestroyed() {
        return getState() == State.DESTROYED;
    }

    /**
     * 获取当前状态
     *
     * @return 当前状态
     */
    public final State getState() {
        rwLock.readLock().lock();
        try {
            return state;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 初始化过程
     *
     * @param processor 过程回调
     * @throws Throwable 初始化异常
     */
    public final void initProcess(final Processor processor) throws Throwable {
        rwLock.writeLock().lock();
        try {

            if (State.NEW != state) {
                throw new IllegalStateException("init process fail, because state != NEW");
            }

            processor.process();
            state = State.INITIALIZED;

        } finally {
            rwLock.writeLock().unlock();
        }
    }


    /**
     * 销毁过程
     *
     * @param processor 规程回调
     * @throws Throwable 销毁异常
     */
    public final void destroyProcess(final Processor processor) throws Throwable {
        rwLock.writeLock().lock();
        try {

            if (State.INITIALIZED != state) {
                throw new IllegalStateException("destroy process fail, because state != INITIALIZED");
            }

            processor.process();
            state = isCycleState
                    ? State.NEW
                    : State.DESTROYED
            ;

        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * 状态枚举
     * <p>
     * 非循环状态
     * NEW -> INITIALIZED -> DESTROYED
     * </p>
     * <p>
     * 循环状态
     * NEW -> INITIALIZED -> NEW
     * </p>
     */
    enum State {

        /**
         * 初始状态(未初始化)
         */
        NEW,

        /**
         * 初始化完成
         */
        INITIALIZED,

        /**
         * 销毁完成
         */
        DESTROYED
    }

    /**
     * 处理过程器
     */
    public interface Processor {

        /**
         * 处理过程
         *
         * @throws Throwable 处理失败
         */
        void process() throws Throwable;
    }

}