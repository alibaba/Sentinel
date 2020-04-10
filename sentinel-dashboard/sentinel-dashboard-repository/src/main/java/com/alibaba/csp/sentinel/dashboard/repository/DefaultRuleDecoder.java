package com.alibaba.csp.sentinel.dashboard.repository;

import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import org.springframework.core.ResolvableType;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * @author cdfive
 */
//@Component
public class DefaultRuleDecoder<T extends RuleEntity> implements Converter<String, List<T>> {

    public DefaultRuleDecoder() {
        Class clazz = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        System.out.println(clazz.getSimpleName());
    }

    @Override
    public List<T> convert(String source) {
//        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
//        return JSON.parseObject(source, new TypeReference<List<T>>(){});
//        return (List<T>) JSON.parseArray(source, List.class);

//        Class clazz = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//        Class clazz = ResolvableType.forClass(this.getClass()).getGeneric(0).resolve();
        Class clazz = ResolvableType.forClass(this.getClass()).getSuperType().getGeneric(0).resolve();
        return JSON.parseArray(source, clazz);
    }
}
