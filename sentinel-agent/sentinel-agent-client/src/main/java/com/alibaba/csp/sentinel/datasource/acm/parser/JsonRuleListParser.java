package com.alibaba.csp.sentinel.datasource.acm.parser;

import com.alibaba.csp.sentinel.datasource.acm.RulesAcmFormat;
import com.taobao.csp.third.com.alibaba.fastjson.TypeReference;
import com.alibaba.csp.sentinel.datasource.Converter;

public class JsonRuleListParser<T> implements Converter<String, T> {
   public T convert(String source) {
      return source == null ? null : (new RulesAcmFormat(source)).parseRules(new TypeReference<T>() {
      });
   }
}
