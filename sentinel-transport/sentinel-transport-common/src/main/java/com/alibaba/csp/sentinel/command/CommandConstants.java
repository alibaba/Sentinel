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
package com.alibaba.csp.sentinel.command;

/**
 * @author Eric Zhao
 * @since 1.4.1
 */
public final class CommandConstants {

    public static final String VERSION_COMMAND = "version";

    public static final String MSG_INVALID_COMMAND = "Invalid command";
    public static final String MSG_UNKNOWN_COMMAND_PREFIX = "Unknown command";

    public static final String MSG_SUCCESS = "success";
    public static final String MSG_FAIL = "failed";

    private CommandConstants() {}
}
