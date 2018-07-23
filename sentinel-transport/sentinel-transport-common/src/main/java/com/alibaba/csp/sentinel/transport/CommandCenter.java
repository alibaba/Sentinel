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
package com.alibaba.csp.sentinel.transport;

/**
 * @author Eric Zhao
 */
public interface CommandCenter {

    /**
     * Prepare and init for the command center (e.g. register commands).
     * This will be executed before starting.
     *
     * @throws Exception if error occurs
     */
    void beforeStart() throws Exception;

    /**
     * Start the command center in the background.
     * This method should NOT block.
     *
     * @throws Exception if error occurs
     */
    void start() throws Exception;

    /**
     * Stop the command center and do cleanup.
     *
     * @throws Exception if error occurs
     */
    void stop() throws Exception;
}
