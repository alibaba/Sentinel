package com.alibaba.acm.shaded.com.aliyuncs.unmarshaller;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.alibaba.acm.shaded.com.aliyuncs.AcsResponse;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpResponse;

/**
 * @author VK.Gao
 * @date 2018/04/11
 */
public class XmlUnmashaller implements Unmarshaller {

    @Override
    public <T extends AcsResponse> T unmarshal(Class<T> clazz, HttpResponse httpResponse) throws ClientException {
        String xmlContent = httpResponse.getHttpContentString();
        try {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            javax.xml.bind.Unmarshaller unmarshaller = jc.createUnmarshaller();
            T xmlPojo = (T)unmarshaller.unmarshal(new StringReader(xmlContent));

            return xmlPojo;
        } catch (JAXBException e) {
            throw newUnmarshalException(clazz, xmlContent, e);
        }
    }

    private ClientException newUnmarshalException(Class<?> clazz, String xmlContent, Exception e) {
        return new ClientException("SDK.UnmarshalFailed",
            "unmarshal response from xml content failed, clazz = " + clazz.getSimpleName() + ", origin response = " + xmlContent, e);
    }
}
