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
package org.apache.http.conn.util;

import java.util.Collections;
import java.util.List;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.util.Args;

/**
 * Public suffix is a set of DNS names or wildcards concatenated with dots. It represents
 * the part of a domain name which is not under the control of the individual registrant
 * <p>
 * An up-to-date list of suffixes can be obtained from
 * <a href="http://publicsuffix.org/">publicsuffix.org</a>
 *
 * @since 4.4
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class PublicSuffixList {

    private final DomainType type;
    private final List<String> rules;
    private final List<String> exceptions;

    /**
     * @since 4.5
     */
    public PublicSuffixList(final DomainType type, final List<String> rules, final List<String> exceptions) {
        this.type = Args.notNull(type, "Domain type");
        this.rules = Collections.unmodifiableList(Args.notNull(rules, "Domain suffix rules"));
        this.exceptions = Collections.unmodifiableList(exceptions != null ? exceptions : Collections.<String>emptyList());
    }

    public PublicSuffixList(final List<String> rules, final List<String> exceptions) {
        this(DomainType.UNKNOWN, rules, exceptions);
    }

    /**
     * @since 4.5
     */
    public DomainType getType() {
        return type;
    }

    public List<String> getRules() {
        return rules;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

}
