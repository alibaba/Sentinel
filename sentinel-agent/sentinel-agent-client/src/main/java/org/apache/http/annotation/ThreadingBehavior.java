/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.http.annotation;

/**
 Defines types of threading behavior enforced at runtime.
 */
public enum ThreadingBehavior {

    /**
     * Instances of classes with the given contract are expected to be fully immutable
     * and thread-safe.
     */
    IMMUTABLE,

    /**
     * Instances of classes with the given contract are expected to be immutable if their
     * dependencies injected at construction time are immutable and are expected to be thread-safe
     * if their dependencies are thread-safe.
     */
    IMMUTABLE_CONDITIONAL,

    /**
     * Instances of classes with the given contract are expected to be fully thread-safe.
     */
    SAFE,

    /**
     * Instances of classes with the given contract are expected to be thread-safe if their
     * dependencies injected at construction time are thread-safe.
     */
    SAFE_CONDITIONAL,

    /**
     * Instances of classes with the given contract are expected to be non thread-safe.
     */
    UNSAFE

}
