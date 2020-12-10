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
package com.alibaba.csp.sentinel.tracing.core;

/**
 * Sentinel tracing constants
 *
 * @author Haojun Ren
 * @since 1.8.1
 */
public class SentinelTracingConstants {
    public static final String TRACER_NAME = "SENTINEL-TRACER";
    public static final String SPAN_NAME = "SENTINEL";
    public static final String ORIGIN = "origin";
    public static final String ASYNC = "async";
    public static final String RESOURCE_NAME = "resource.name";
    public static final String RESOURCE_SHOW_NAME = "resource.showname";
    public static final String RESOURCE_TYPE = "resource.type";
    public static final String ENTRY_TYPE = "entry.type";
    public static final String RULE_LIMIT_APP = "rule.limit.app";
    public static final String RULE = "rule";
    public static final String CAUSE = "cause";
    public static final String BLOCK_EXCEPTION = "block.exception";
    public static final String COUNT = "count";
    public static final String ARGS = "args";
}