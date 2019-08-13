/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.event;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class Event<T> {


    public Event(T data, Object source, Long time) {
        this.data = data;
        this.source = source;
        this.time = time;
    }

    /**
     * Event content
     */
    private T data;

    /**
     * Event produce source
     */
    private Object source;

    /**
     * Event happen time
     */
    private Long time;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "{" +
                "data=" + data +
                ",source=" + source +
                ",time=" + time +
                "}";
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
