//package com.alibaba.csp.sentinel.dashboard.rule.converter;
//
//import com.alibaba.csp.sentinel.datasource.Converter;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.TypeReference;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * @author cdfive
// */
//@Component
//public class RuleDecoder<T> implements Converter<String, List<T>> {
//
//    @Override
//    public List<T> convert(String source) {
//        return JSON.parseObject(source, new TypeReference<List<T>>(){});
//    }
//}
