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
package com.alibaba.csp.sentinel.dashboard.datasource.entity;

/**
 * @author Eric Zhao
 * @since 0.2.1
 */
public class SentinelVersion {
    private int majorVersion;
    private int minorVersion;
    private int fixVersion;
    private String postfix;
    
    public SentinelVersion() {
        this(0, 0, 0);
    }
    
    public SentinelVersion(int major, int minor, int fix) {
        this(major, minor, fix, null);
    }
    
    public SentinelVersion(int major, int minor, int fix, String postfix) {
        this.majorVersion = major;
        this.minorVersion = minor;
        this.fixVersion = fix;
        this.postfix = postfix;
    }
    
    /**
     * 000, 000, 000
     */
    public int getFullVersion() {
        return majorVersion * 1000000 + minorVersion * 1000 + fixVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public SentinelVersion setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
        return this;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public SentinelVersion setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
        return this;
    }

    public int getFixVersion() {
        return fixVersion;
    }

    public SentinelVersion setFixVersion(int fixVersion) {
        this.fixVersion = fixVersion;
        return this;
    }

    public String getPostfix() {
        return postfix;
    }

    public SentinelVersion setPostfix(String postfix) {
        this.postfix = postfix;
        return this;
    }

    public boolean greaterThan(SentinelVersion version) {
        if (version == null) {
            return true;
        }
        return getFullVersion() > version.getFullVersion();
    }

    public boolean greaterOrEqual(SentinelVersion version) {
        if (version == null) {
            return true;
        }
        return getFullVersion() >= version.getFullVersion();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        SentinelVersion that = (SentinelVersion)o;

        if (getFullVersion() != that.getFullVersion()) { return false; }
        return postfix != null ? postfix.equals(that.postfix) : that.postfix == null;
    }

    @Override
    public int hashCode() {
        int result = majorVersion;
        result = 31 * result + minorVersion;
        result = 31 * result + fixVersion;
        result = 31 * result + (postfix != null ? postfix.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SentinelVersion{" +
            "majorVersion=" + majorVersion +
            ", minorVersion=" + minorVersion +
            ", fixVersion=" + fixVersion +
            ", postfix='" + postfix + '\'' +
            '}';
    }
}
