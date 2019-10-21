/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.acm.shaded.com.aliyuncs.http;

import java.util.Arrays;

public enum FormatType {

    XML("application/xml", "text/xml"),
    JSON("application/json", "text/json"),
    RAW("application/octet-stream"),
    FORM("application/x-www-form-urlencoded");

    private String[] formats;

    FormatType(String... formats) {
        this.formats = formats;
    }

    public static String mapFormatToAccept(FormatType format) {
        return format.formats[0];
    }

    public static FormatType mapAcceptToFormat(String accept) {
        for (FormatType value : values()) {
            if (Arrays.asList(value.formats).contains(accept)) {
                return value;
            }
        }
        return FormatType.RAW;
    }
}
