package com.alibaba.csp.sentinel.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class IdUtilTest {

  @Test
  public void truncate() {
        assertEquals("(foo),(bar),(baz)", IdUtil.truncate(".(foo).,(bar).,(baz)"));
  }
}


