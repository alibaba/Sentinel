/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.acm.shaded.com.aliyuncs.http;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.utils.ParameterHelper;

public abstract class HttpMessage {

    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String CONTENT_MD5 = "Content-MD5";
    protected static final String CONTENT_LENGTH = "Content-Length";

    private String url = null;
    private MethodType method = null;
    protected FormatType httpContentType = null;
    protected byte[] httpContent = null;
    protected String encoding = null;
    protected Map<String, String> headers = new HashMap<String, String>();
    protected Integer connectTimeout = null;
    protected Integer readTimeout = null;

    public HttpMessage(String strUrl) {
        this.url = strUrl;
        this.headers = new HashMap<String, String>();
    }

    public HttpMessage(String strUrl, Map<String, String> tmpHeaders) {
        this.url = strUrl;
        if (null != tmpHeaders) { this.headers = tmpHeaders; }
    }

    public HttpMessage() {
    }

    public String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public FormatType getHttpContentType() {
        return httpContentType;
    }

    public void setHttpContentType(FormatType httpContentType) {
        this.httpContentType = httpContentType;
        if (null != this.httpContent || null != httpContentType) {
            this.headers.put(CONTENT_TYPE, getContentTypeValue(this.httpContentType, this.encoding));
        } else {
            this.headers.remove(CONTENT_TYPE);
        }
    }

    public MethodType getMethod() {
        return method;
    }

    public void setMethod(MethodType method) {
        this.method = method;
    }

    public byte[] getHttpContent() {
        return httpContent;
    }

    public String getHeaderValue(String name) {
        return this.headers.get(name);
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void putHeaderParameter(String name, String value) {
        if (null != name && null != value) { this.headers.put(name, value); }
    }

    public void setHttpContent(byte[] content, String encoding, FormatType format) {

        if (null == content) {
            this.headers.remove(CONTENT_MD5);
            this.headers.put(CONTENT_LENGTH, "0");
            this.headers.remove(CONTENT_TYPE);
            this.httpContentType = null;
            this.httpContent = null;
            this.encoding = null;
            return;
        }
        this.httpContent = content;
        this.encoding = encoding;
        String contentLen = String.valueOf(content.length);
        String strMd5 = ParameterHelper.md5Sum(content);
        if (null != format) {
            this.httpContentType = format;
        } else {
            this.httpContentType = FormatType.RAW;
        }
        this.headers.put(CONTENT_MD5, strMd5);
        this.headers.put(CONTENT_LENGTH, contentLen);
        this.headers.put(CONTENT_TYPE, getContentTypeValue(httpContentType, encoding));
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public String getContentTypeValue(FormatType contentType, String encoding) {
        if (null != contentType && null != encoding) {
            return FormatType.mapFormatToAccept(contentType) +
                ";charset=" + encoding.toLowerCase();
        } else if (null != contentType) {
            return FormatType.mapFormatToAccept(contentType);
        }
        return null;
    }

    public String getHttpContentString() throws ClientException {
        String stringContent = "";
        if(this.httpContent != null){
            try {
                if (this.encoding == null) {
                    stringContent = new String(this.httpContent);
                } else {
                    stringContent = new String(this.httpContent, this.encoding);
                }
            } catch (UnsupportedEncodingException exp) {
                throw new ClientException("SDK.UnsupportedEncoding", "Can not parse response due to un supported encoding.");
            }
        }

        return stringContent;
    }

}
