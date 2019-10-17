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
import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelSpringMvcException;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.EntryContainer;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SpringMvcRequestOriginParser;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SpringMvcSentinelAopConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SpringMvcUrlCleaner;
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
  private static final String EMPTY_ORIGIN = "";
  private final static String COLON = ":";
  private SpringMvcSentinelAopConfig config;

  public SentinelSpringWebMvcInterceptor(SpringMvcSentinelAopConfig config) {
    this.config = config;
  }

  public SentinelSpringWebMvcInterceptor() {
    this.config = new SpringMvcSentinelAopConfig();
  }
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    Entry urlEntry = null;
    Entry httpMethodUrlEntry = null;

    try {
      String target = getTarget(request);

      SpringMvcUrlCleaner urlCleaner = config.getUrlCleaner();
      if (urlCleaner != null) {
        target = urlCleaner.clean(target);
      }

      if (!StringUtil.isEmpty(target)) {
        // Parse the request origin using registered origin parser.
        String origin = parseOrigin(request);
        ContextUtil.enter(config.getContextName(), origin);
        urlEntry = SphU.entry(target, EntryType.IN);
        // Add method specification if necessary
          if (config.isHttpMethodSpecify()) {
          httpMethodUrlEntry = SphU.entry(request.getMethod().toUpperCase() + COLON + target,
              EntryType.IN);
        }
      }
      final EntryContainer entryContainer = new EntryContainer().setUrlEntry(urlEntry)
          .setHttpMethodUrlEntry(httpMethodUrlEntry);
      setEntryContainerInReqeust(request, entryContainer);
      return true;
    } catch (BlockException e) {
      throw e;
    }
    catch (RuntimeException e1) {
      Tracer.traceEntry(e1, urlEntry);
      Tracer.traceEntry(e1, httpMethodUrlEntry);
      throw e1;
    }
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {
    EntryContainer entryContainer = getEntryContainerInReqeust(request);
    if (entryContainer.getUrlEntry() != null) {
      entryContainer.getUrlEntry().exit();
    }
    if (entryContainer.getUrlEntry() != null) {
      entryContainer.getUrlEntry().exit();
    }
    ContextUtil.exit();
  }

  private EntryContainer getEntryContainerInReqeust(HttpServletRequest request) {
    Object entityContainerObject = request.getAttribute(config.getRequestAttrKey());
    if (entityContainerObject == null) {
      throw new SentinelSpringMvcException("EntryContainer is null in request");
    }
    return (EntryContainer)entityContainerObject;
  }

  private void setEntryContainerInReqeust(HttpServletRequest request, EntryContainer entryContainer) {
    request.setAttribute(config.getRequestAttrKey(), entryContainer);
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

  protected String parseOrigin(HttpServletRequest request) {
    SpringMvcRequestOriginParser originParser = config.getOriginParser();
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
