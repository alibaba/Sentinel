/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.servlet.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.csp.sentinel.adapter.servlet.config.WebServletConfig;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Util class for web servlet filter.
 *
 * @author zhaoyuguang
 * @author youji.zj
 * @author Eric Zhao
 */
public final class FilterUtil {

    private static final String PATH_SPLIT = "/";

    public static String filterTarget(HttpServletRequest request) {
        String pathInfo = getResourcePath(request);
        if (!pathInfo.startsWith(PATH_SPLIT)) {
            pathInfo = PATH_SPLIT + pathInfo;
        }

        if (PATH_SPLIT.equals(pathInfo)) {
            return pathInfo;
        }

        // Note: pathInfo should be converted to camelCase style.
        int lastSlashIndex = pathInfo.lastIndexOf("/");

        if (lastSlashIndex >= 0) {
            pathInfo = pathInfo.substring(0, lastSlashIndex) + "/"
                + StringUtil.trim(pathInfo.substring(lastSlashIndex + 1));
        } else {
            pathInfo = PATH_SPLIT + StringUtil.trim(pathInfo);
        }

        return pathInfo;
    }

    public static void blockRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuffer url = request.getRequestURL();

        if ("GET".equals(request.getMethod()) && StringUtil.isNotBlank(request.getQueryString())) {
            url.append("?").append(request.getQueryString());
        }

        if (StringUtil.isBlank(WebServletConfig.getBlockPage())) {
            writeDefaultBlockedPage(response, WebServletConfig.getBlockPageHttpStatus());
        } else {
            String redirectUrl = WebServletConfig.getBlockPage() + "?http_referer=" + url.toString();
            // Redirect to the customized block page.
            response.sendRedirect(redirectUrl);
        }
    }

    private static void writeDefaultBlockedPage(HttpServletResponse response, int httpStatus) throws IOException {
        response.setStatus(httpStatus);
        PrintWriter out = response.getWriter();
        out.print(DEFAULT_BLOCK_MSG);
        out.flush();
        out.close();
    }

    private static String getResourcePath(HttpServletRequest request) {
        String pathInfo = normalizeAbsolutePath(request.getPathInfo(), false);
        String servletPath = normalizeAbsolutePath(request.getServletPath(), pathInfo.length() != 0);

        return servletPath + pathInfo;
    }

    private static String normalizeAbsolutePath(String path, boolean removeTrailingSlash) throws IllegalStateException {
        return normalizePath(path, true, false, removeTrailingSlash);
    }

    private static String normalizePath(String path, boolean forceAbsolute, boolean forceRelative,
                                        boolean removeTrailingSlash) throws IllegalStateException {
        char[] pathChars = StringUtil.trimToEmpty(path).toCharArray();
        int length = pathChars.length;

        // Check path and slash.
        boolean startsWithSlash = false;
        boolean endsWithSlash = false;

        if (length > 0) {
            char firstChar = pathChars[0];
            char lastChar = pathChars[length - 1];

            startsWithSlash = firstChar == PATH_SPLIT.charAt(0) || firstChar == '\\';
            endsWithSlash = lastChar == PATH_SPLIT.charAt(0) || lastChar == '\\';
        }

        StringBuilder buf = new StringBuilder(length);
        boolean isAbsolutePath = forceAbsolute || !forceRelative && startsWithSlash;
        int index = startsWithSlash ? 0 : -1;
        int level = 0;

        if (isAbsolutePath) {
            buf.append(PATH_SPLIT);
        }

        while (index < length) {
            index = indexOfSlash(pathChars, index + 1, false);

            if (index == length) {
                break;
            }

            int nextSlashIndex = indexOfSlash(pathChars, index, true);

            String element = new String(pathChars, index, nextSlashIndex - index);
            index = nextSlashIndex;

            // Ignore "."
            if (".".equals(element)) {
                continue;
            }

            // Backtrack ".."
            if ("..".equals(element)) {
                if (level == 0) {
                    if (isAbsolutePath) {
                        throw new IllegalStateException(path);
                    } else {
                        buf.append("..").append(PATH_SPLIT);
                    }
                } else {
                    buf.setLength(pathChars[--level]);
                }

                continue;
            }

            pathChars[level++] = (char)buf.length();
            buf.append(element).append(PATH_SPLIT);
        }

        // remove the last "/"
        if (buf.length() > 0) {
            if (!endsWithSlash || removeTrailingSlash) {
                buf.setLength(buf.length() - 1);
            }
        }

        return buf.toString();
    }

    private static int indexOfSlash(char[] chars, int beginIndex, boolean slash) {
        int i = beginIndex;

        for (; i < chars.length; i++) {
            char ch = chars[i];

            if (slash) {
                if (ch == PATH_SPLIT.charAt(0) || ch == '\\') {
                    break; // if a slash
                }
            } else {
                if (ch != PATH_SPLIT.charAt(0) && ch != '\\') {
                    break; // if not a slash
                }
            }
        }

        return i;
    }

    public static final String DEFAULT_BLOCK_MSG = "Blocked by Sentinel (flow limiting)";

    private FilterUtil() {}
}
