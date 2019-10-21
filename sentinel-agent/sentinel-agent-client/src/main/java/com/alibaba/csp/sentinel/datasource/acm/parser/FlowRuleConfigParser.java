package com.alibaba.csp.sentinel.datasource.acm.parser;

import com.alibaba.csp.sentinel.datasource.acm.RulesAcmFormat;
import com.taobao.csp.third.com.alibaba.fastjson.JSON;
import com.taobao.csp.third.com.alibaba.fastjson.TypeReference;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import java.util.List;

public class FlowRuleConfigParser implements Converter<String, List<FlowRule>> {
   public List<FlowRule> convert(String source) {
      if (source == null) {
         return null;
      } else {
         String data = (new RulesAcmFormat(source)).getData();
         RecordLog.info("[FlowRuleConfigParser] data: " + data);
         return (List)JSON.parseObject(data, new TypeReference<List<FlowRule>>() {
         });
      }
   }
}
