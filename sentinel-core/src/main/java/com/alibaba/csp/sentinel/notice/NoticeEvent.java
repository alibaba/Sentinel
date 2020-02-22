package com.alibaba.csp.sentinel.notice;

/**
 * @author lianglin
 * @since 2020-02-22
 */
public class NoticeEvent<T> {


    public NoticeEvent(NoticeType type, T data) {
        this.type = type;
        this.data = data;
    }

    private NoticeType type;

    private T data;


    public NoticeType getType() {
        return type;
    }

    public void setType(NoticeType type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
