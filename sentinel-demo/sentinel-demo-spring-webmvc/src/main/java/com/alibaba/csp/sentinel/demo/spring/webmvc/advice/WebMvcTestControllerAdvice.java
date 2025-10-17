package com.alibaba.csp.sentinel.demo.spring.webmvc.advice;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebExceptionHandlerHelper;
import com.alibaba.csp.sentinel.demo.spring.webmvc.exception.WebMvcException;
import com.alibaba.csp.sentinel.demo.spring.webmvc.vo.ResultWrapper;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @author by lfy
 * @Date 2022/8/3 20:40
 */
@RestControllerAdvice
public class WebMvcTestControllerAdvice {

    @ExceptionHandler(WebMvcException.class)
    public ResultWrapper handlerWebMvcException(HttpServletRequest request, Throwable t) {
        SentinelWebExceptionHandlerHelper.handlerRequestException(request, t);
        return new ResultWrapper(500, t.getMessage());
    }
}
