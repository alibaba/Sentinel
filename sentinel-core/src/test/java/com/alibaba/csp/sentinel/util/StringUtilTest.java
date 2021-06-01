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
package com.alibaba.csp.sentinel.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

  @Test
  public void testCapitalize() {
    Assert.assertNull(StringUtil.capitalize(null));
    Assert.assertEquals("Foo", StringUtil.capitalize("foo"));
  }

  @Test
  public void testEqualsIgnoreCase() {
    Assert.assertFalse(StringUtil.equalsIgnoreCase("", "BCCC"));
    Assert.assertFalse(StringUtil.equalsIgnoreCase(null, ""));
    Assert.assertTrue(StringUtil.equalsIgnoreCase("", ""));
    Assert.assertTrue(StringUtil.equalsIgnoreCase("BcCc", "BCCC"));
    Assert.assertTrue(StringUtil.equalsIgnoreCase(null, null));
  }

  @Test
  public void testEquals() {
    Assert.assertFalse(StringUtil.equals(null, ""));
    Assert.assertFalse(StringUtil.equals("\"", "\"#\"\"\"\"\"\""));
    Assert.assertTrue(StringUtil.equals(null, null));
  }

  @Test
  public void testIsBlank() {
    Assert.assertFalse(StringUtil.isBlank("!!!!"));
    Assert.assertTrue(StringUtil.isBlank(null));
    Assert.assertTrue(StringUtil.isBlank("\n\n"));
    Assert.assertTrue(StringUtil.isBlank(""));
  }

  @Test
  public void testIsEmpty() {
    Assert.assertFalse(StringUtil.isEmpty("bar"));
    Assert.assertTrue(StringUtil.isEmpty(""));
  }

  @Test
  public void testIsNotBlank() {
    Assert.assertFalse(StringUtil.isNotBlank(""));
    Assert.assertTrue(StringUtil.isNotBlank("\"###"));
  }

  @Test
  public void testIsNotEmpty() {
    Assert.assertFalse(StringUtil.isNotEmpty(""));
    Assert.assertTrue(StringUtil.isNotEmpty("foo"));
  }

  @Test
  public void testTrim() {
    Assert.assertNull(StringUtil.trim(null));
    Assert.assertEquals("", StringUtil.trim(""));
    Assert.assertEquals("foo", StringUtil.trim("foo  "));
  }

  @Test
  public void testTrimToEmpty() {
    Assert.assertEquals("", StringUtil.trimToEmpty(""));
    Assert.assertEquals("", StringUtil.trimToEmpty(null));
  }

}
