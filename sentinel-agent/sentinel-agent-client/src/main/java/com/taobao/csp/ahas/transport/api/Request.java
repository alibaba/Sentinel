package com.taobao.csp.ahas.transport.api;

import java.util.HashMap;
import java.util.Map;

public class Request {
   private static final String FROM_HEADER = "FR";
   private static final String CLIENT = "C";
   private static final String SERVER = "S";
   protected final Map<String, String> headers;
   protected final Map<String, String> params;

   public Request() {
      this(false);
   }

   public Request(boolean isClient) {
      this.headers = new HashMap();
      this.params = new HashMap();
      if (isClient) {
         this.headers.put("FR", "C");
      } else {
         this.headers.put("FR", "S");
      }

   }

   public Map<String, String> getHeaders() {
      return this.headers;
   }

   public Map<String, String> getParams() {
      return this.params;
   }

   public String getHeader(String key) {
      return (String)this.headers.get(key);
   }

   public String getParam(String key) {
      return (String)this.params.get(key);
   }

   public void removeHeader(String key) {
      this.headers.remove(key);
   }

   public void removeParam(String key) {
      this.params.remove(key);
   }

   public boolean fromClient() {
      return "C".equals(this.getHeader("FR"));
   }

   public boolean fromServer() {
      return "S".equals(this.getHeader("FR"));
   }

   public Request addHeader(String key, String value) {
      if (this.isBlank(key)) {
         throw new IllegalArgumentException("Parameter key cannot be empty");
      } else {
         this.headers.put(key, value);
         return this;
      }
   }

   public Request addParam(String key, String value) {
      if (this.isBlank(key)) {
         throw new IllegalArgumentException("Parameter key cannot be empty");
      } else {
         this.params.put(key, value);
         return this;
      }
   }

   private boolean isBlank(String value) {
      return value == null || value.trim().length() == 0;
   }
}
