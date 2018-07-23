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
package com.alibaba.csp.sentinel.util;

/**
 * @author qinan.qn
 */
public final class IdUtil {

    public static String truncate(String id) {
        IdLexer lexer = new IdLexer(id);
        StringBuilder sb = new StringBuilder();
        String r;
        String temp = "";
        while ((r = lexer.nextToken()) != null) {
            if ("(".equals(r) || ")".equals(r) || ",".equals(r)) {
                sb.append(temp).append(r);
                temp = "";
            } else if (!".".equals(r)) {
                temp = r;
            }
        }

        return sb.toString();
    }

    private static class IdLexer {
        private String id;
        private int idx = 0;

        IdLexer(String id) {
            this.id = id;
        }

        String nextToken() {
            int oldIdx = idx;
            String result = null;
            while (idx != id.length()) {
                char curChar = id.charAt(idx);
                if (curChar == '.' || curChar == '(' || curChar == ')' || curChar == ',') {
                    if (idx == oldIdx) {
                        result = String.valueOf(curChar);
                        ++idx;
                        break;
                    } else {
                        result = id.substring(oldIdx, idx);
                        break;
                    }
                }
                ++idx;
            }
            return result;
        }
    }

    private IdUtil() {}
}
