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
package com.alibaba.csp.sentinel.trust.auth.condition.matcher;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lwj
 * @since 2.0.0
 */
public class StringMatcherTest {

    @Test
    public void testExactMatch() {
        StringMatcher stringMatcher = new StringMatcher("Abc", StringMatcher.MatcherType.EXACT, true);
        Assert.assertTrue(stringMatcher.match("abc"));
        Assert.assertFalse(stringMatcher.match("abcd"));

        stringMatcher = new StringMatcher("Abc", StringMatcher.MatcherType.EXACT, false);
        Assert.assertTrue(stringMatcher.match("Abc"));
        Assert.assertFalse(stringMatcher.match("abc"));
        Assert.assertFalse(stringMatcher.match("abcd"));
    }

    @Test
    public void testPrefixMatch() {
        StringMatcher stringMatcher = new StringMatcher("Abc", StringMatcher.MatcherType.PREFIX, true);
        Assert.assertTrue(stringMatcher.match("abc"));
        Assert.assertTrue(stringMatcher.match("abcd"));
        Assert.assertFalse(stringMatcher.match("acdd"));

        stringMatcher = new StringMatcher("Abc", StringMatcher.MatcherType.PREFIX, false);
        Assert.assertTrue(stringMatcher.match("Abc"));
        Assert.assertTrue(stringMatcher.match("Abcd"));
        Assert.assertFalse(stringMatcher.match("abc"));
        Assert.assertFalse(stringMatcher.match("abcd"));
    }

    @Test
    public void testSuffixMatch() {
        StringMatcher stringMatcher = new StringMatcher("Abc", StringMatcher.MatcherType.SUFFIX, true);
        Assert.assertTrue(stringMatcher.match("abc"));
        Assert.assertTrue(stringMatcher.match("1abc"));
        Assert.assertFalse(stringMatcher.match("1acd"));

        stringMatcher = new StringMatcher("Abc", StringMatcher.MatcherType.SUFFIX, false);
        Assert.assertTrue(stringMatcher.match("Abc"));
        Assert.assertTrue(stringMatcher.match("1Abc"));
        Assert.assertFalse(stringMatcher.match("1abc"));
        Assert.assertFalse(stringMatcher.match("1abcd"));
    }

    @Test
    public void testContainMatch() {
        StringMatcher stringMatcher = new StringMatcher("Abc", StringMatcher.MatcherType.CONTAIN, true);
        Assert.assertTrue(stringMatcher.match("1abc1"));
        Assert.assertTrue(stringMatcher.match("abc"));
        Assert.assertFalse(stringMatcher.match("1acd1"));

        stringMatcher = new StringMatcher("Abc", StringMatcher.MatcherType.CONTAIN, false);
        Assert.assertTrue(stringMatcher.match("Abc"));
        Assert.assertTrue(stringMatcher.match("1Abc1"));
        Assert.assertFalse(stringMatcher.match("1abc1"));
        Assert.assertFalse(stringMatcher.match("1abcd1"));
    }

    @Test
    public void testRegexMatch() {
        StringMatcher stringMatcher = new StringMatcher("A.c", StringMatcher.MatcherType.REGEX, true);
        Assert.assertTrue(stringMatcher.match("amc"));
        Assert.assertTrue(stringMatcher.match("abc"));
        Assert.assertFalse(stringMatcher.match("acd"));

        stringMatcher = new StringMatcher("A.c", StringMatcher.MatcherType.REGEX, false);
        Assert.assertTrue(stringMatcher.match("Abc"));
        Assert.assertTrue(stringMatcher.match("A*c"));
        Assert.assertFalse(stringMatcher.match("abc"));
        Assert.assertFalse(stringMatcher.match("1abcd1"));
    }
}