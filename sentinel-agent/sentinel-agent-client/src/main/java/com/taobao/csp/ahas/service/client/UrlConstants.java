package com.taobao.csp.ahas.service.client;

import java.util.HashMap;
import java.util.Map;

public interface UrlConstants {
   Map<String, String> GATEWAY_MAP = new HashMap<String, String>() {
      {
         this.put("pre-cn-hangzhou", "pre.proxy.ahas.aliyun.com:9527");
         this.put("test-cn-hangzhou", "47.98.243.171:9527");
         this.put("prod-cn-hangzhou", "proxy.ahas.cn-hangzhou.aliyuncs.com:9527");
         this.put("prod-cn-beijing", "proxy.ahas.cn-beijing.aliyuncs.com:9527");
         this.put("prod-cn-shenzhen", "proxy.ahas.cn-shenzhen.aliyuncs.com:9527");
         this.put("prod-cn-shanghai", "proxy.ahas.cn-shanghai.aliyuncs.com:9527");
         this.put("prod-cn-public", "ahas-proxy.aliyuncs.com:8848");
         this.put("test-cn-public", "47.98.243.171:9527");
         this.put("pre-cn-public", "proxy.ahas.cn-hongkong.aliyuncs.com:8848");
      }
   };
   Map<String, String> ACM_MAP = new HashMap<String, String>() {
      {
         this.put("cn-qingdao", "addr-qd-internal.edas.aliyun.com");
         this.put("cn-beijing", "addr-bj-internal.edas.aliyun.com");
         this.put("cn-hangzhou", "addr-hz-internal.edas.aliyun.com");
         this.put("cn-shanghai", "addr-sh-internal.edas.aliyun.com");
         this.put("cn-shenzhen", "addr-sz-internal.edas.aliyun.com");
         this.put("cn-hongkong", "addr-hk-internal.edas.aliyuncs.com");
         this.put("ap-southeast-1", "addr-singapore-internal.edas.aliyun.com");
         this.put("us-west-1", "addr-us-west-1-internal.acm.aliyun.com");
         this.put("us-east-1", "addr-us-east-1-internal.acm.aliyun.com");
         this.put("cn-public", "acm.aliyun.com");
      }
   };
}
