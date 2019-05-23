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
package com.alibaba.csp.sentinel.cluster;

import java.util.Collection;

/**
 * Service interface of flow control.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public interface TokenService {

    /**
     * Request tokens from remote token server.
     *
     * @param ruleId the unique rule ID
     * @param acquireCount token count to acquire
     * @param prioritized whether the request is prioritized
     * @return result of the token request
     */
    TokenResult requestToken(Long ruleId, int acquireCount, boolean prioritized);

    /**
     * Request tokens for a specific parameter from remote token server.
     *
     * @param ruleId the unique rule ID
     * @param acquireCount token count to acquire
     * @param params parameter list
     * @return result of the token request
     */
    TokenResult requestParamToken(Long ruleId, int acquireCount, Collection<Object> params);
}
