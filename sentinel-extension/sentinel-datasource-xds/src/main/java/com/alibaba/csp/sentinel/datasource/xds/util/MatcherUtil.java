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
package com.alibaba.csp.sentinel.datasource.xds.util;

import com.alibaba.csp.sentinel.trust.auth.condition.matcher.IpMatcher;
import com.alibaba.csp.sentinel.trust.auth.condition.matcher.StringMatcher;
import com.alibaba.csp.sentinel.util.StringUtil;

import io.envoyproxy.envoy.config.core.v3.CidrRange;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;

/**
 * @author lwj
 * @since 2.0.0
 */
public final class MatcherUtil {

    private MatcherUtil() {
    }

    public static StringMatcher convStringMatcher(io.envoyproxy.envoy.type.matcher.v3.StringMatcher stringMatcher) {
        if (stringMatcher == null) {
            return null;
        }
        boolean ignoreCase = stringMatcher.getIgnoreCase();
        String exact = stringMatcher.getExact();
        String prefix = stringMatcher.getPrefix();
        String suffix = stringMatcher.getSuffix();
        String contains = stringMatcher.getContains();
        String regex = stringMatcher.getSafeRegex().getRegex();
        if (StringUtil.isNotBlank(exact)) {
            return new StringMatcher(exact, StringMatcher.MatcherType.EXACT, ignoreCase);
        }
        if (StringUtil.isNotBlank(prefix)) {
            return new StringMatcher(prefix, StringMatcher.MatcherType.PREFIX, ignoreCase);
        }
        if (StringUtil.isNotBlank(suffix)) {
            return new StringMatcher(suffix, StringMatcher.MatcherType.SUFFIX, ignoreCase);
        }
        if (StringUtil.isNotBlank(contains)) {
            return new StringMatcher(contains, StringMatcher.MatcherType.CONTAIN, ignoreCase);
        }
        if (StringUtil.isNotBlank(regex)) {
            return new StringMatcher(regex, StringMatcher.MatcherType.REGEX, ignoreCase);
        }
        return null;
    }

    public static StringMatcher convStringMatcher(HeaderMatcher headerMatcher) {
        return convStringMatcher(headerMatch2StringMatch(headerMatcher));
    }

    public static IpMatcher convertIpMatcher(CidrRange cidrRange) {
        return new IpMatcher(cidrRange.getPrefixLen().getValue(), cidrRange.getAddressPrefix());
    }

    public static StringMatcher convertHeaderMatcher(HeaderMatcher headerMatcher) {
        return convStringMatcher(headerMatch2StringMatch(headerMatcher));
    }

    public static io.envoyproxy.envoy.type.matcher.v3.StringMatcher headerMatch2StringMatch(
        HeaderMatcher headerMatcher) {
        if (headerMatcher == null) {
            return null;
        }
        if (headerMatcher.getPresentMatch()) {
            io.envoyproxy.envoy.type.matcher.v3.StringMatcher.Builder builder
                = io.envoyproxy.envoy.type.matcher.v3.StringMatcher
                .newBuilder();
            return builder.setSafeRegex(RegexMatcher.newBuilder().build())
                .setIgnoreCase(true).build();
        }
        if (!headerMatcher.hasStringMatch()) {
            io.envoyproxy.envoy.type.matcher.v3.StringMatcher.Builder builder
                = io.envoyproxy.envoy.type.matcher.v3.StringMatcher
                .newBuilder();
            String exactMatch = headerMatcher.getExactMatch();
            String containsMatch = headerMatcher.getContainsMatch();
            String prefixMatch = headerMatcher.getPrefixMatch();
            String suffixMatch = headerMatcher.getSuffixMatch();
            RegexMatcher safeRegex = headerMatcher.getSafeRegexMatch();
            if (!StringUtil.isEmpty(exactMatch)) {
                builder.setExact(exactMatch);
            } else if (!StringUtil.isEmpty(containsMatch)) {
                builder.setContains(containsMatch);
            } else if (!StringUtil.isEmpty(prefixMatch)) {
                builder.setPrefix(prefixMatch);
            } else if (!StringUtil.isEmpty(suffixMatch)) {
                builder.setSuffix(suffixMatch);
            } else if (safeRegex.isInitialized()) {
                builder.setSafeRegex(safeRegex);
            }
            return builder.setIgnoreCase(true).build();
        }
        return headerMatcher.getStringMatch();
    }

}
