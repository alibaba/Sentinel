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

package com.alibaba.csp.sentinel.adapter.gateway.zuul2.fallback;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Fall back response for {@link com.alibaba.csp.sentinel.slots.block.BlockException}
 *
 * @author tiger
 */
public class BlockResponse extends HashMap<String, Object> {

  /** HTTP status code. */
  private int code;
  /** HTTP response message */
  private String message;
  /** route */
  private String route;

  public static BlockResponse blockError(String route) {
    return error(429, "Sentinel block exception", route);
  }

  public static BlockResponse error(String route) {
    return error(500, "System Error", route);
  }

  public static BlockResponse error(int code, String message, String route) {
    BlockResponse response = new BlockResponse();
    response.put("code", code);
    response.put("message", message);
    response.put("route", route);
    return response;
  }

  public static BlockResponse ok(String msg) {
    BlockResponse r = new BlockResponse();
    r.put("msg", msg);
    return r;
  }

  public static BlockResponse ok(Map<String, Object> map) {
    BlockResponse response = new BlockResponse();
    response.putAll(map);
    return response;
  }

  public static BlockResponse ok() {
    return new BlockResponse();
  }

  @Override
  public BlockResponse put(String key, Object value) {
    super.put(key, value);
    return this;
  }

  public int getCode() {
    return (int) this.get("code");
  }

  public String getMessage() {
    return (String) this.get("message");
  }

  public String getRoute() {
    return (String) this.get("route");
  }

  @Override
  public String toString() {
    return JSONObject.toJSONString(this);
  }
}
