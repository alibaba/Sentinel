package com.alibaba.csp.sentinel.notice;

/**
 * @author lianglin
 * @since 2020-02-22
 */
public abstract class NoticeEventHandler<T> {

    abstract void handle(NoticeEvent<T> event);

}
