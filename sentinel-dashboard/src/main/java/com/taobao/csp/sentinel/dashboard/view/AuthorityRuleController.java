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
package com.taobao.csp.sentinel.dashboard.view;

import com.taobao.csp.sentinel.dashboard.client.SentinelApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Eric Zhao
 * @since 0.2.1
 */
@RestController
@RequestMapping(value = "/authority")
public class AuthorityRuleController {

    private final Logger logger = LoggerFactory.getLogger(AuthorityRuleController.class);

    @Autowired
    private SentinelApiClient sentinelApiClient;
}
