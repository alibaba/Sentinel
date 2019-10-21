package com.taobao.diamond.client.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.taobao.diamond.client.impl.DiamondEnv.log;


/**
 * �¼����ĺͷ��������ࡣ
 */
public class EventDispatcher {

    /**
     * ����¼�������
     */
    static public void addEventListener(EventListener listener) {
        for (Class<? extends Event> type : listener.interest()) {
            getListenerList(type).addIfAbsent(listener);
        }
    }

    /**
     * �����¼������ȷ������¼���ʾ�������¼������֪ͨ���ж�Ӧ�ļ�������
     */
    static public void fireEvent(Event event) {
        if (null == event) { // ����
            return;
        }

        // �������¼���ʾ�������¼�
        for (Event implyEvent : event.implyEvents()) {
            try {
                if (event != implyEvent) { // ������ѭ��
                    fireEvent(implyEvent);
                }
            } catch (Exception e) {
                log.warn("", e.toString(), e);
            }
        }

        for (EventListener listener : getListenerList(event.getClass())) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.warn(e.toString(), e);
            }
        }
    }

    // ���̰߳�ȫ
    static synchronized CopyOnWriteArrayList<EventListener> getListenerList(
            Class<? extends Event> eventType) {
        CopyOnWriteArrayList<EventListener> listeners = listenerMap.get(eventType);
        if (null == listeners) {
            listeners = new CopyOnWriteArrayList<EventListener>();
            listenerMap.put(eventType, listeners);
        }
        return listeners;
    }

    // ========================
    
    static final Map<Class<? extends Event>, CopyOnWriteArrayList<EventListener>> listenerMap //
    = new HashMap<Class<? extends Event>, CopyOnWriteArrayList<EventListener>>();

    // ========================

    /**
     * Client�¼���
     */
    static public abstract class Event {
        /**
         * ���¼����ܰ�ʾ�������¼���Ĭ��ʵ��Ϊ�ա�
         */
        @SuppressWarnings("unchecked")
        protected List<Event> implyEvents() {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * �¼���������
     */
    static public abstract class EventListener {
        public EventListener() {
            EventDispatcher.addEventListener(this); // �Զ�ע���EventDispatcher
        }
        
        /**
         * ����Ȥ���¼��б�
         */
        abstract public List<Class<? extends Event>> interest();

        /**
         * �����¼�
         */
        abstract public void onEvent(Event event);
    }
    
    /** serverList has changed */
    static public class ServerlistChangeEvent extends Event {}
}