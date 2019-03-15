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
package com.alibaba.csp.sentinel.dashboard.service;

/**
 * Interface about authentication and authorization
 *
 * @author Carpenter Lee
 */
public interface AuthService<R> {
    /**
     * Get the authentication user.
     *
     * @param request the request contains the user information
     * @return the auth user represent the current user, when the user is illegal, a null value will return.
     */
    AuthUser getAuthUser(R request);

    /**
     * privilege type.
     */
    enum PrivilegeType {
        /**
         * read rule
         */
        READ_RULE,
        /**
         * create or modify rule
         */
        WRITE_RULE,
        /**
         * delete rule
         */
        DELETE_RULE,
        /**
         * read metrics
         */
        READ_METRIC,
        /**
         * add machine
         */
        ADD_MACHINE,
        /**
         * equals all privileges above
         */
        ALL
    }

    /**
     * entity represents the current user
     */
    interface AuthUser {
        /**
         * query whether current user has the specific privilege to the target, the target
         * may be an app name or an ip address, or other destination.
         * <p>
         * This method will use return value to represent  whether user has the specific
         * privileges to the target, but to throw a RuntimeException to represent no auth
         * is also a good way.
         * </p>
         *
         * @param target        the target to check
         * @param privilegeType the privilege type to check
         * @return if current user has the specific privileges to the target, return true,
         * otherwise return false.
         */
        boolean authTarget(String target, PrivilegeType privilegeType);

        /**
         * check whether current user is super user
         *
         * @return if current user is super user return true, else return false.
         */
        boolean isSuperUser();

        /**
         * get current user's nick name.
         *
         * @return current user's nick name.
         */
        String getNickName();

        /**
         * get current user's login name.
         *
         * @return current user's login name.
         */
        String getLoginName();

        /**
         * get current user's employ id.
         *
         * @return current user's employ id.
         */
        String getEmpId();

    }
}
