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
package com.alibaba.csp.sentinel.log.jul;

/**
 * JUL logging levels.
 *
 * @author xue8
 */
public class Level extends java.util.logging.Level {
    private static final String defaultBundle = "sun.util.logging.resources.logging";

    public static final Level ERROR = new Level("ERROR", 1000);
    public static final Level WARNING = new Level("WARNING", 900);
    public static final Level INFO = new Level("INFO", 800);
    public static final Level DEBUG = new Level("DEBUG", 700);
    public static final Level TRACE = new Level("TRACE", 600);

    protected Level(String name, int value) {
        super(name, value, defaultBundle);
    }
}
