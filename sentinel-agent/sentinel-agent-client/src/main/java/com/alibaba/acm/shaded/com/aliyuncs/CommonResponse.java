package com.alibaba.acm.shaded.com.aliyuncs;

import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ServerException;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpResponse;
import com.alibaba.acm.shaded.com.aliyuncs.transform.UnmarshallerContext;

public class CommonResponse extends AcsResponse {
    
    private String data;
    
    private int httpStatus;
    
    private HttpResponse httpResponse;

    @Override
    public AcsResponse getInstance(UnmarshallerContext context) throws ClientException, ServerException {
        return null;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

}
