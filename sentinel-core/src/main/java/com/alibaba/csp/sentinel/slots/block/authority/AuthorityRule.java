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
package com.alibaba.csp.sentinel.slots.block.authority;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

/**
 * @author youji.zj
 */
public class AuthorityRule extends AbstractRule {

    /**
     * Mode: 0 for whitelist; 1 for blacklist.
     */
    private int strategy = RuleConstant.AUTHORITY_WHITE;

    public int getStrategy() {
        return strategy;
    }

    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof AuthorityRule)) { return false; }
        if (!super.equals(o)) { return false; }

        AuthorityRule rule = (AuthorityRule)o;

        return strategy == rule.strategy;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + strategy;
        return result;
    }

    @Override
    public boolean passCheck(Context context, DefaultNode node, int count, Object... args) {
        String requester = context.getOrigin();

        // Empty origin or empty limitApp will pass.
        if (StringUtil.isEmpty(requester) || StringUtil.isEmpty(this.getLimitApp())) {
            return true;
        }

        // Do exact match with origin name.
        int pos = this.getLimitApp().indexOf(requester);
        boolean contain = pos > -1;

        if (contain) {
            boolean exactlyMatch = false;
            String[] appArray = this.getLimitApp().split(",");
            for (String app : appArray) {
                if (requester.equals(app)) {
                    exactlyMatch = true;
                    break;
                }
            }

            contain = exactlyMatch;
        }

        if (strategy == RuleConstant.AUTHORITY_BLACK && contain) {
            return false;
        }

        if (strategy == RuleConstant.AUTHORITY_WHITE && !contain) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "AuthorityRule{" +
            "resource=" + getResource() +
            ", limitApp=" + getLimitApp() +
            ", strategy=" + strategy +
            "} ";
    }
}
