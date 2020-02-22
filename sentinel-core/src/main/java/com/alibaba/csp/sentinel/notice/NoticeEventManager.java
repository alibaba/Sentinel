package com.alibaba.csp.sentinel.notice;

import com.alibaba.csp.sentinel.util.AssertUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lianglin
 * @since 2020-02-22
 */
public class NoticeEventManager {

    private static final NoticeEventManager INSTANCE = new NoticeEventManager();

    private Map<NoticeType, Set<NoticeEventHandler>> noticeHandlers;

    private NoticeEventManager(){
        this.noticeHandlers = new ConcurrentHashMap<>();
    };

    public void register(NoticeType type, NoticeEventHandler handler) {
        AssertUtil.notNull(type, "NoticeType can't be null");
        Set<NoticeEventHandler> noticeHandlers = this.noticeHandlers.get(type);
        if (noticeHandlers == null) {
            noticeHandlers = new HashSet<>();
        }
        noticeHandlers.add(handler);
    }

    public void unregister(NoticeType type, NoticeEventHandler handler) {
        AssertUtil.notNull(type, "NoticeType can't be null");
        Set<NoticeEventHandler> noticeEventHandlers = this.noticeHandlers.get(type);
        if (noticeEventHandlers != null) {
            noticeEventHandlers.remove(handler);
        }

    }






}
