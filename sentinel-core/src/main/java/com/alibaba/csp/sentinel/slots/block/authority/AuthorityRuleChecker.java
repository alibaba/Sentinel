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

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Rule checker for white/black list authority.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
final class AuthorityRuleChecker {

    static boolean passCheck(AuthorityRule rule, Context context) {
        String requester = context.getOrigin();
        boolean contain = false;

        // Empty origin or empty limitApp will pass.
        if (StringUtil.isEmpty(requester) || StringUtil.isEmpty(rule.getLimitApp())) {
            return true;
        }

        // Do exact match with origin name.
        contain = rule.getLimitApp().contains(requester);

        //The matching result is judged according to the Strategy
        switch (rule.getStrategy()) {
            case RuleConstant.AUTHORITY_BLACK:
                return !contain;
            case RuleConstant.AUTHORITY_WHITE:
                return contain;
            default:
                return true;

        }
    }


    private AuthorityRuleChecker() {}
}
