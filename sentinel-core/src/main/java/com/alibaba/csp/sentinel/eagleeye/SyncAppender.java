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
package com.alibaba.csp.sentinel.eagleeye;

/**
 * @author jifeng
 */
final class SyncAppender extends EagleEyeAppender {

    private final EagleEyeAppender delegate;
    private final Object lock = new Object();

    public SyncAppender(EagleEyeAppender delegate) {
        this.delegate = delegate;
    }

    @Override
    public void append(String log) {
        synchronized (lock) {
            delegate.append(log);
        }
    }

    @Override
    public void flush() {
        synchronized (lock) {
            delegate.flush();
        }
    }

    @Override
    public void rollOver() {
        synchronized (lock) {
            delegate.rollOver();
        }
    }

    @Override
    public void reload() {
        synchronized (lock) {
            delegate.reload();
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            delegate.close();
        }
    }

    @Override
    public void cleanup() {
        delegate.cleanup();
    }

    @Override
    public String getOutputLocation() {
        return delegate.getOutputLocation();
    }

    @Override
    public String toString() {
        return "SyncAppender [appender=" + delegate + "]";
    }
}
