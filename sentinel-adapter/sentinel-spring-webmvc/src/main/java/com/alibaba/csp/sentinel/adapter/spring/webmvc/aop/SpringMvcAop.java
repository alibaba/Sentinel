package com.alibaba.csp.sentinel.adapter.spring.webmvc.aop;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.servlet.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import javax.servlet.http.HttpServletRequest;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

/**
 * @author zhangkai
 * @description
 * @date 2019-10-14
 */
public class SpringMvcAop {
  public Object aroundAop(ProceedingJoinPoint point) throws Throwable{
    //获取目标方法完整路径
    RequestAttributes ra = RequestContextHolder.getRequestAttributes();
    ServletRequestAttributes sra = (ServletRequestAttributes) ra;
    HttpServletRequest request = sra.getRequest();
    String target = getTarget(request);

    Entry urlEntry = null;
    try {
      UrlCleaner urlCleaner = WebCallbackManager.getUrlCleaner();
      if (urlCleaner != null) {
        target = urlCleaner.clean(target);
      }

      if (StringUtil.isNotEmpty(target)) {
        ContextUtil.enter("SDK", EMPTY_ORIGIN);
        urlEntry = SphU.entry(target, EntryType.IN);
      }
      //调用目标方法
      Object result = null;
      result = point.proceed();
      return result;
    } catch (BlockException e) {
      //被限流时的处理
      throw e;
    } catch (RuntimeException e2) {
      Tracer.traceEntry(e2, urlEntry);
      throw e2;
    } finally {
      if (urlEntry != null) {
        urlEntry.exit();
      }
      ContextUtil.exit();
    }
  }

  protected String getTarget(HttpServletRequest request) {
    return (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
  }
  protected static final String EMPTY_ORIGIN = "";
  protected static String parseOrigin(HttpServletRequest request) {
    RequestOriginParser originParser = WebCallbackManager.getRequestOriginParser();
    String origin = EMPTY_ORIGIN;
    if (originParser != null) {
      origin = originParser.parseOrigin(request);
      if (StringUtil.isEmpty(origin)) {
        return EMPTY_ORIGIN;
      }
    }
    return origin;
  }
}
