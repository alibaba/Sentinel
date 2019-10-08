package com.alibaba.csp.sentinel.adapter.spring.webmvc.interceptor;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.servlet.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.adapter.servlet.config.WebServletConfig;
import com.alibaba.csp.sentinel.adapter.servlet.util.FilterUtil;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SpringWebMvcConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author zhangkai
 * @description
 * @date 2019-09-25
 */
public class SentinelSpringWebMvcInterceptor implements HandlerInterceptor {
  private boolean httpMethodSpecify = false;
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    Entry urlEntry = null;
    Entry httpMethodUrlEntry = null;

    try {
      String target = getTarget(request);

      UrlCleaner urlCleaner = WebCallbackManager.getUrlCleaner();
      if (urlCleaner != null) {
        target = urlCleaner.clean(target);
      }

      if (!StringUtil.isEmpty(target)) {
        // Parse the request origin using registered origin parser.
        String origin = parseOrigin(request);
        ContextUtil.enter(SpringWebMvcConfig.SPRING_WEB_MVC_CONTEXT_NAME, origin);
        urlEntry = SphU.entry(target, EntryType.IN);
        // Add method specification if necessary
        if (httpMethodSpecify) {
          httpMethodUrlEntry = SphU.entry(request.getMethod().toUpperCase() + ":" + target,
              EntryType.IN);
        }
      }
      return true;
    } catch (BlockException e) {
      // Return the block page, or redirect to another URL.
      WebCallbackManager.getUrlBlockHandler().blocked(request, response, e);
      return false;
    } catch (RuntimeException e2) {
      Tracer.traceEntry(e2, urlEntry);
      Tracer.traceEntry(e2, httpMethodUrlEntry);
      throw e2;
    } finally {
      if (httpMethodUrlEntry != null) {
        httpMethodUrlEntry.exit();
      }
      if (urlEntry != null) {
        urlEntry.exit();
      }
      ContextUtil.exit();
    }
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {

  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {

  }

  /**
   * Get target in HttpServletRequest
   * @see org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping
   * request.setAttribute(HandlerMapping.MATRIX_VARIABLES_ATTRIBUTE, matrixVars);
   * @param request
   * @return
   */
  protected String getTarget(HttpServletRequest request) {
    return (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
  }

  private String parseOrigin(HttpServletRequest request) {
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

  private static final String EMPTY_ORIGIN = "";
}
