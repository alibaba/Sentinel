package com.alibaba.csp.sentinel.datasource.acm;

import com.taobao.csp.third.com.alibaba.fastjson.JSON;
import com.taobao.csp.third.com.alibaba.fastjson.JSONObject;
import com.taobao.csp.third.com.alibaba.fastjson.TypeReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataAcmFormat {
   public static final String VERSION = "version";
   public static final String DATA = "data";
   private JSONObject jsonObject;

   public DataAcmFormat(String str) {
      this.jsonObject = JSON.parseObject(str);
   }

   public static <T> String toStringWithVersion(List<T> rules) {
      Map<String, Object> map = new HashMap();
      map.put("version", String.valueOf(System.currentTimeMillis()));
      map.put("data", rules);
      return JSON.toJSONString(map);
   }

   public String getData() {
      return this.jsonObject == null ? null : this.jsonObject.getString("data");
   }

   public String getVersion() {
      return this.jsonObject == null ? null : this.jsonObject.getString("version");
   }

   public <T> T parseRules(TypeReference<T> tTypeReference) {
      String data = this.getData();
      return data == null ? null : JSON.parseObject(data, tTypeReference);
   }
}
