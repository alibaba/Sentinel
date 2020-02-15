/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dubbo.springboot.utils;

public class ConstStrings {
    public static String flowGradeString(int grade) {
        switch (grade) {
            case 0: return "FLOW_GRADE_THREAD";
            case 1: return "FLOW_GRADE_QPS";
            default: return "unknown flow grade";
        }
    }

    public static String degradeGradeString(int grade) {
        switch (grade) {
            case 0: return "DEGRADE_GRADE_RT";
            case 1: return "DEGRADE_GRADE_EXCEPTION_RATIO";
            case 2: return "DEGRADE_GRADE_EXCEPTION_COUNT";
            default: return "unknown degrade grade";
        }
    }

    public static String behaviorGradeString(int behavior) {
        switch (behavior) {
            case 0: return "CONTROL_BEHAVIOR_DEFAULT";
            case 1: return "CONTROL_BEHAVIOR_WARM_UP";
            case 2: return "CONTROL_BEHAVIOR_RATE_LIMITER";
            case 3: return "CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER";
            default: return "unknown behavior grade";
        }
    }
}
