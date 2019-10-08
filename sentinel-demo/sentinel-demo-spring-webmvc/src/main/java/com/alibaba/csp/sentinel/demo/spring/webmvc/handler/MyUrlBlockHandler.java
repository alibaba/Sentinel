package com.alibaba.csp.sentinel.demo.spring.webmvc.handler;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.demo.spring.webmvc.vo.Result;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhangkai
 * @description
 * @date 2019-09-25
 */
public class MyUrlBlockHandler implements UrlBlockHandler {

  @Override
  public void blocked(HttpServletRequest request, HttpServletResponse response, BlockException ex)
      throws IOException {
    String jsonString = Result.blocked().toJsonString();
    response.getWriter().write(jsonString);
  }
}
