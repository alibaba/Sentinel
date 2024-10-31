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
package com.alibaba.csp.sentinel.trust.auth.condition;

import java.util.Objects;

import com.alibaba.csp.sentinel.trust.auth.condition.matcher.Matcher;
import com.alibaba.csp.sentinel.trust.auth.rule.AuthType;

/**
 * @author lwj
 * @since 2.0.0
 */
public class AuthCondition {

    /**
     * authType , depending on the judgment, request values in different places
     */
    private final AuthType type;
    /**
     * If the request is header and claim,
     * it indicates which key to get from
     */
    private final String key;
    /**
     * Matcher, which is used to make matches after a request is received
     */
    private final Matcher matcher;

    public AuthCondition(AuthType type, Matcher matcher) {
        this.type = type;
        this.matcher = matcher;
        this.key = null;
    }

    public AuthCondition(AuthType type, String key, Matcher matcher) {
        this.type = type;
        this.matcher = matcher;
        this.key = key;

    }

    public AuthType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    @Override
    public String toString() {
        return "AuthCondition{" +
            "type=" + type +
            ", key='" + key + '\'' +
            ", matcher=" + matcher +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthCondition that = (AuthCondition) o;
        return type == that.type && Objects.equals(key, that.key) && Objects.equals(matcher, that.matcher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, key, matcher);
    }
}
