package com.alibaba.csp.sentinel.dashboard.web.advice;

import com.alibaba.csp.sentinel.dashboard.common.exception.DashboardException;
import com.alibaba.csp.sentinel.dashboard.service.api.exception.DashboardServiceException;
import com.alibaba.csp.sentinel.dashboard.web.domain.Result;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * Handle exception for rest controller.
 *
 * Log known/unknown exception message.
 * When known exception occurs, return code -1, otherwise 500.
 *
 * @author cdfive
 */
@RestControllerAdvice
public class DashboardExceptionAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardExceptionAdvice.class);

    @ExceptionHandler
    public Result handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        if (e instanceof HttpRequestMethodNotSupportedException) {
            return handleException(Result.ofFail(-1, e.getMessage()), request, e);
        }

        if (e instanceof HttpMediaTypeException) {
            return handleException(Result.ofFail(-1, e.getMessage()), request, e);
        }

        if (e instanceof HttpMessageConversionException) {
            return handleException(Result.ofFail(-1, e.getMessage()), request, e);
        }

        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ve = (MethodArgumentNotValidException) e;
            FieldError fieldError = ve.getBindingResult().getFieldError();
            if (Objects.nonNull(fieldError)) {
                return handleException(Result.ofFail(-1, fieldError.getDefaultMessage()), request, e);
            }
        }

        if (e instanceof DashboardException) {
            return handleException(Result.ofFail(-1, e.getMessage()), request, e);
        }

        if (e instanceof DashboardServiceException) {
            return handleException(Result.ofFail(-1, e.getMessage()), request, e);
        }

        return handleUnknownException(Result.ofFail(500, "unknown exception"), request, e);
    }


    private Result handleException(Result result, HttpServletRequest request, Exception e) {
        LOGGER.error("[dashboard exception][{}]request uri={},response={}", e.getClass().getName()
                , request.getRequestURI(), JSON.toJSONString(result), e);
        return result;
    }


    private Result handleUnknownException(Result result, HttpServletRequest request, Exception e) {
        LOGGER.error("[dashboard unknown exception][{}]request uri={},response={}", e.getClass().getName()
                , request.getRequestURI(), JSON.toJSONString(result), e);
        return result;
    }
}
