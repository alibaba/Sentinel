package com.alibaba.acm.shaded.com.aliyuncs.unmarshaller;

import com.alibaba.acm.shaded.com.aliyuncs.AcsResponse;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpResponse;
import com.google.gson.Gson;

/**
 * @author VK.Gao
 * @date 2018/04/11
 */
public class JsonUnmashaller implements Unmarshaller {

    @Override
    public <T extends AcsResponse> T unmarshal(Class<T> clazz, HttpResponse httpResponse) throws ClientException {
        String jsonContent = httpResponse.getHttpContentString();
        return (new Gson()).fromJson(jsonContent, clazz);
    }


}
