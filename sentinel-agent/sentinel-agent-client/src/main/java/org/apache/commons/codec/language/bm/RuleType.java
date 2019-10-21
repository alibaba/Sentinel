/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.codec.language.bm;

/**
 * Types of rule.
 *
 * @since 1.6
 * @version $Id: RuleType.java 1542813 2013-11-17 20:52:32Z tn $
 */
public enum RuleType {

    /** Approximate rules, which will lead to the largest number of phonetic interpretations. */
    APPROX("approx"),
    /** Exact rules, which will lead to a minimum number of phonetic interpretations. */
    EXACT("exact"),
    /** For internal use only. Please use {@link #APPROX} or {@link #EXACT}. */
    RULES("rules");

    private final String name;

    RuleType(final String name) {
        this.name = name;
    }

    /**
     * Gets the rule name.
     *
     * @return the rule name.
     */
    public String getName() {
        return this.name;
    }

}
