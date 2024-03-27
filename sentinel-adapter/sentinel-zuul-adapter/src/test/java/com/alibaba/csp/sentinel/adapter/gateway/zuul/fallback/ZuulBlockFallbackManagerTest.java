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

package com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback;

import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/** @author tiger */
public class ZuulBlockFallbackManagerTest {

  private static final String ROUTE = "/test";

  private static final String DEFAULT_ROUTE = "*";

  static class MyNullResponseFallBackProvider implements ZuulBlockFallbackProvider<BlockResponse> {
    @Override
    public String getRoute() {
      return ROUTE;
    }

    @Override
    public BlockResponse fallbackResponse(String route, Throwable cause) {
      Map<String, Object> map = new HashMap<>(2);
      map.put("route", route);
      map.put("cause", cause);
      return BlockResponse.ok(map);
    }
  }

  @Test
  public void testRegisterProvider() throws Exception {
    MyNullResponseFallBackProvider myNullResponseFallBackProvider =
        new MyNullResponseFallBackProvider();
    ZuulBlockFallbackManager.registerProvider(myNullResponseFallBackProvider);
    Assert.assertEquals(myNullResponseFallBackProvider.getRoute(), ROUTE);
    Assert.assertNotNull(
        myNullResponseFallBackProvider.fallbackResponse(ROUTE, new FlowException("flow ex")));
  }

  @Test
  public void clear() {
    MyNullResponseFallBackProvider myNullResponseFallBackProvider =
        new MyNullResponseFallBackProvider();
    ZuulBlockFallbackManager.registerProvider(myNullResponseFallBackProvider);
    Assert.assertEquals(myNullResponseFallBackProvider.getRoute(), ROUTE);
    ZuulBlockFallbackManager.clear();
    Assert.assertEquals(
        ZuulBlockFallbackManager.getFallbackProvider(ROUTE).getRoute(), DEFAULT_ROUTE);
  }

  @Test
  public void clearCustomize() {
    MyNullResponseFallBackProvider myNullResponseFallBackProvider =
        new MyNullResponseFallBackProvider();
    ZuulBlockFallbackManager.registerProvider(myNullResponseFallBackProvider);
    Assert.assertEquals(myNullResponseFallBackProvider.getRoute(), ROUTE);
    ZuulBlockFallbackManager.clear();
    Assert.assertEquals(
        ZuulBlockFallbackManager.getFallbackProvider(ROUTE).getRoute(), DEFAULT_ROUTE);
  }
}
