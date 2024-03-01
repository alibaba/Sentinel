package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.param;

import com.alibaba.csp.sentinel.webflow.param.RequestItemParser;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Web item parser for {@code HttpServletRequest}.
 *
 * @since 1.8.8
 */
public class HttpServletRequestItemParser implements RequestItemParser<HttpServletRequest> {

    @Override
    public String getPath(HttpServletRequest request) {
        return request.getPathInfo();
    }

    @Override
    public String getRemoteAddress(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    @Override
    public String getHeader(HttpServletRequest request, String key) {
        return request.getHeader(key);
    }

    @Override
    public String getUrlParam(HttpServletRequest request, String paramName) {
        return request.getParameter(paramName);
    }

    @Override
    public String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookieName == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie != null && cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public String getBodyValue(HttpServletRequest request, String bodyName) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            String str;
            StringBuilder wholeStr = new StringBuilder();
            while ((str = br.readLine()) != null) {
                wholeStr.append(str);
            }
            JSONObject jsonObject = JSONObject.parseObject(wholeStr.toString());
            return jsonObject.get(bodyName).toString();
        }catch (Throwable ignored){

        }finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException ignored) {

                }
            }
        }

        return null;
    }

    @Override
    public String getPathValue(HttpServletRequest request, String pathName) {
        try {
            Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            return  (String) pathVariables.get(pathName);
        }catch (Throwable ignored){

        }
        return null;
    }

}
