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


    public Event(T data, String source, int type) {
        this.data = data;
        this.source = source;
        this.type = type;
        this.timeStamp = System.currentTimeMillis();
    }

    /**
     * Event content
     */
    private T data;


    /**
     * Event produce source
     */
    private String source;

    /**
     * Event happen time
     */
    private long timeStamp;

    /**
     * @see EventType
     */
    private int type;


    public T getData() {
        return this.data;
    }

    public void setBody(T data) {
        this.data = data;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(final long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "{" +
                "data=" + data +
                ",source=" + source +
                ",type=" + type +
                ",timeStamp=" + timeStamp +
                "}";
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


}
