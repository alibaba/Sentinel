package com.alibaba.acm.shaded.com.aliyuncs.unmarshaller;

import com.alibaba.acm.shaded.com.aliyuncs.AcsResponse;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpResponse;

/**
 * @author VK.Gao
 * @date 2018/04/11
 */
public interface Unmarshaller {

    <T extends AcsResponse> T unmarshal(Class<T> clasz, HttpResponse httpResponse) throws ClientException;
}
