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

import java.net.IDN;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.util.Args;

/**
 * Utility class that can test if DNS names match the content of the Public Suffix List.
 * <p>
 * An up-to-date list of suffixes can be obtained from
 * <a href="http://publicsuffix.org/">publicsuffix.org</a>
 *
 * @see org.apache.http.conn.util.PublicSuffixList
 *
 * @since 4.4
 */
@Contract(threading = ThreadingBehavior.SAFE)
public final class PublicSuffixMatcher {

    private final Map<String, DomainType> rules;
    private final Map<String, DomainType> exceptions;

    public PublicSuffixMatcher(final Collection<String> rules, final Collection<String> exceptions) {
        this(DomainType.UNKNOWN, rules, exceptions);
    }

    /**
     * @since 4.5
     */
    public PublicSuffixMatcher(
            final DomainType domainType, final Collection<String> rules, final Collection<String> exceptions) {
        Args.notNull(domainType,  "Domain type");
        Args.notNull(rules,  "Domain suffix rules");
        this.rules = new ConcurrentHashMap<String, DomainType>(rules.size());
        for (final String rule: rules) {
            this.rules.put(rule, domainType);
        }
        this.exceptions = new ConcurrentHashMap<String, DomainType>();
        if (exceptions != null) {
            for (final String exception: exceptions) {
                this.exceptions.put(exception, domainType);
            }
        }
    }

    /**
     * @since 4.5
     */
    public PublicSuffixMatcher(final Collection<PublicSuffixList> lists) {
        Args.notNull(lists,  "Domain suffix lists");
        this.rules = new ConcurrentHashMap<String, DomainType>();
        this.exceptions = new ConcurrentHashMap<String, DomainType>();
        for (final PublicSuffixList list: lists) {
            final DomainType domainType = list.getType();
            final List<String> rules = list.getRules();
            for (final String rule: rules) {
                this.rules.put(rule, domainType);
            }
            final List<String> exceptions = list.getExceptions();
            if (exceptions != null) {
                for (final String exception: exceptions) {
                    this.exceptions.put(exception, domainType);
                }
            }
        }
    }

    private static boolean hasEntry(final Map<String, DomainType> map, final String rule, final DomainType expectedType) {
        if (map == null) {
            return false;
        }
        final DomainType domainType = map.get(rule);
        if (domainType == null) {
            return false;
        } else {
            return expectedType == null || domainType.equals(expectedType);
        }
    }

    private boolean hasRule(final String rule, final DomainType expectedType) {
        return hasEntry(this.rules, rule, expectedType);
    }

    private boolean hasException(final String exception, final DomainType expectedType) {
        return hasEntry(this.exceptions, exception, expectedType);
    }

    /**
     * Returns registrable part of the domain for the given domain name or {@code null}
     * if given domain represents a public suffix.
     *
     * @param domain
     * @return domain root
     */
    public String getDomainRoot(final String domain) {
        return getDomainRoot(domain, null);
    }

    /**
     * Returns registrable part of the domain for the given domain name or {@code null}
     * if given domain represents a public suffix.
     *
     * @param domain
     * @param expectedType expected domain type or {@code null} if any.
     * @return domain root
     *
     * @since 4.5
     */
    public String getDomainRoot(final String domain, final DomainType expectedType) {
        if (domain == null) {
            return null;
        }
        if (domain.startsWith(".")) {
            return null;
        }
        String domainName = null;
        String segment = domain.toLowerCase(Locale.ROOT);
        while (segment != null) {

            // An exception rule takes priority over any other matching rule.
            if (hasException(IDN.toUnicode(segment), expectedType)) {
                return segment;
            }

            if (hasRule(IDN.toUnicode(segment), expectedType)) {
                break;
            }

            final int nextdot = segment.indexOf('.');
            final String nextSegment = nextdot != -1 ? segment.substring(nextdot + 1) : null;

            if (nextSegment != null) {
                if (hasRule("*." + IDN.toUnicode(nextSegment), expectedType)) {
                    break;
                }
            }
            if (nextdot != -1) {
                domainName = segment;
            }
            segment = nextSegment;
        }
        return domainName;
    }

    /**
     * Tests whether the given domain matches any of entry from the public suffix list.
     */
    public boolean matches(final String domain) {
        return matches(domain, null);
    }

    /**
     * Tests whether the given domain matches any of entry from the public suffix list.
     *
     * @param domain
     * @param expectedType expected domain type or {@code null} if any.
     * @return {@code true} if the given domain matches any of the public suffixes.
     *
     * @since 4.5
     */
    public boolean matches(final String domain, final DomainType expectedType) {
        if (domain == null) {
            return false;
        }
        final String domainRoot = getDomainRoot(
                domain.startsWith(".") ? domain.substring(1) : domain, expectedType);
        return domainRoot == null;
    }

}
