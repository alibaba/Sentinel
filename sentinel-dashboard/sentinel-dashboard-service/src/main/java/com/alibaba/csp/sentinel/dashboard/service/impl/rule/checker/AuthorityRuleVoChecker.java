/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker;


import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.AddAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.DeleteAuthorityReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.authority.UpdateAuthorityReqVo;

import static com.alibaba.csp.sentinel.dashboard.service.impl.common.ParamChecker.*;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.AUTHORITY_BLACK;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.AUTHORITY_WHITE;

/**
 * @author cdfive
 */
public class AuthorityRuleVoChecker {

    public static void checkAdd(AddAuthorityReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotBlank(reqVo.getResource(), "resource");

        checkNotBlank(reqVo.getLimitApp(), "limitApp");

        checkNotNull(reqVo.getStrategy(), "strategy");
        checkInValues(reqVo.getStrategy(), "strategy", AUTHORITY_WHITE, AUTHORITY_BLACK);
    }

    public static void checkUpdate(UpdateAuthorityReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotBlank(reqVo.getLimitApp(), "limitApp");

        checkNotNull(reqVo.getStrategy(), "strategy");
        checkInValues(reqVo.getStrategy(), "strategy", AUTHORITY_WHITE, AUTHORITY_BLACK);
    }

    public static void checkDelete(DeleteAuthorityReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotNull(reqVo.getId(), "id");
        checkCondition(reqVo.getId() > 0, "id must be greater than 0");
    }
}
