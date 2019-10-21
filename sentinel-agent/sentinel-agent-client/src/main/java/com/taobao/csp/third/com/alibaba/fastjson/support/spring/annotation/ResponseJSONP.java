package com.taobao.csp.third.com.alibaba.fastjson.support.spring.annotation;

import com.taobao.csp.third.com.alibaba.fastjson.support.spring.JSONPResponseBodyAdvice;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;

/**
 * Created by SongLing.Dong on 7/22/2017.
 * @see JSONPResponseBodyAdvice
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResponseBody
public @interface ResponseJSONP {
    /**
     * The parameter's name of the callback method.
     */
    String callback() default "callback";
}
