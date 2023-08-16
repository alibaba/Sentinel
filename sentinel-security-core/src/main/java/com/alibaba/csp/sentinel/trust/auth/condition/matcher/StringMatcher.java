/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.trust.auth.condition.matcher;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author lwj
 * @since 2.0.0
 */
public class StringMatcher implements Matcher<String> {

    /**
     * Matched string.
     * If it is a regular match,
     * it is a regular match string
     */
    private final String key;

    private final MatcherType type;

    /**
     * Whether to ignore case
     */
    private final boolean ignoreCase;

    public StringMatcher(String key, MatcherType type, boolean ignoreCase) {
        if (ignoreCase) {
            key = key.toLowerCase(Locale.ROOT);
        }
        this.key = key;
        this.type = type;
        this.ignoreCase = ignoreCase;
    }

    public boolean match(String object) {
        if (StringUtil.isEmpty(object)) {
            return false;
        }
        if (ignoreCase) {
            object = object.toLowerCase(Locale.ROOT);
        }
        switch (type) {
            case EXACT:
                return object.equals(key);
            case PREFIX:
                return object.startsWith(key);
            case SUFFIX:
                return object.endsWith(key);
            case CONTAIN:
                return object.contains(key);
            case REGEX:
                try {
                    return Pattern.matches(key, object);
                } catch (Exception e) {
                    RecordLog.warn("Irregular matching,key={},str={}", key, object, e);
                    return false;
                }
            default:
                throw new UnsupportedOperationException(
                    "unsupported string compare operation");
        }
    }

    public String getKey() {
        return key;
    }

    public MatcherType getType() {
        return type;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StringMatcher that = (StringMatcher) o;
        return ignoreCase == that.ignoreCase && Objects.equals(key, that.key) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type, ignoreCase);
    }

    @Override
    public String toString() {
        return "StringMatcher{" +
            "key='" + key + '\'' +
            ", type=" + type +
            ", ignoreCase=" + ignoreCase +
            '}';
    }

    public enum MatcherType {

        /**
         * exact match.
         */
        EXACT("exact"),
        /**
         * prefix match.
         */
        PREFIX("prefix"),
        /**
         * suffix match.
         */
        SUFFIX("suffix"),
        /**
         * regex match.
         */
        REGEX("regex"),
        /**
         * contain match.
         */
        CONTAIN("contain");

        /**
         * type of matcher.
         */
        public final String key;

        MatcherType(String type) {
            this.key = type;
        }

        @Override
        public String toString() {
            return this.key;
        }

    }
}
