/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.jdbc;


/**
 * The context of the jdbc component (eg. DataSource、Connection、Statement),
 * you can add some extended attributes
 * @author icodening
 * @date 2022.02.09
 */
public interface JDBCContext {

    /**
     * set a attribute
     * @param name attribute name
     * @param object attribute
     */
    void setAttachment(String name, Object object);

    /**
     * get a attribute
     * @param name attribute name
     * @param defaultValue return defaultValue when without attribute
     * @return attribute value
     */
    <T> T getAttachment(String name, T defaultValue);

    /**
     * get a attribute by type, returns null when there is a attribute with the same name but the type does not match
     * @param name attribute name
     * @param attachmentType the java type of the attribute
     * @return attribute value
     */
    <T> T getAttachmentWithCast(String name, Class<T> attachmentType);

    /**
     * remove this component all extended attributes
     */
    void clearAttachments();

    default <T> T getAttachment(String name) {
        return getAttachment(name, null);
    }

}
