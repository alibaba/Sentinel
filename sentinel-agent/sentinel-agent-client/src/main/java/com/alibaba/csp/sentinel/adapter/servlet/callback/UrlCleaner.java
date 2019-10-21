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
package com.alibaba.csp.sentinel.adapter.servlet.callback;

/***
 * @author youji.zj
 */
public interface UrlCleaner {

    /***
     * <p>Process the url. Some path variables should be handled and unified.</p>
     * <p>e.g. collect_item_relation--10200012121-.html will be converted to collect_item_relation.html</p>
     *
     * @param originUrl original url
     * @return processed url
     */
    String clean(String originUrl);
}
