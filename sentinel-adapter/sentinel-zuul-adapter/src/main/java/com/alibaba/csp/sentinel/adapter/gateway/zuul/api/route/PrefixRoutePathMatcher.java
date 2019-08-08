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
package com.alibaba.csp.sentinel.adapter.gateway.zuul.api.route;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

import com.netflix.zuul.context.RequestContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public class PrefixRoutePathMatcher implements Predicate<RequestContext> {

    private final String pattern;

    private final PathMatcher pathMatcher;
    private final boolean canMatch;

    public PrefixRoutePathMatcher(String pattern) {
        AssertUtil.assertNotBlank(pattern, "pattern cannot be blank");
        this.pattern = pattern;
        this.pathMatcher = new AntPathMatcher();
        this.canMatch = pathMatcher.isPattern(pattern);
    }

    @Override
    public boolean test(RequestContext context) {
        String path = context.getRequest().getServletPath();
        if (canMatch) {
            return pathMatcher.match(pattern, path);
        }
        return false;
    }

    public String getPattern() {
        return pattern;
    }
}
