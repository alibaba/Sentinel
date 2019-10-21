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
package com.alibaba.acm.shaded.com.aliyuncs.reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.utils.XmlUtils;

@Deprecated
public class XmlReader implements Reader {
    Map<String, String> map = new HashMap<String, String>();

    @Override
    public Map<String, String> read(String response, String endpoint) throws ClientException {
        Element root;
        try {
            root = XmlUtils.getRootElementFromString(response);
            read(root, endpoint, false);
        } catch (ParserConfigurationException e) {
            new ClientException("SDK.InvalidXMLParser", e.toString());
        } catch (SAXException e) {
            new ClientException("SDK.InvalidXMLFormat", e.toString());
        } catch (IOException e) {
            new ClientException("SDK.InvalidContent", e.toString());
        }
        return map;
    }
    
    public Map<String, String> readForHideArrayItem(String response, String endpoint) throws ClientException {
        return  read(response, endpoint);
    }

    private void read(Element element, String path, boolean appendPath) {
        path = buildPath(element, path, appendPath);
        List<Element> childElements = XmlUtils.getChildElements(element);

        if (childElements.size() == 0) {
            map.put(path, element.getTextContent());
            return;
        }

        List<Element> listElements = XmlUtils.getChildElements(element, childElements.get(0).getNodeName());
        if (listElements.size() > 1 && childElements.size() == listElements.size()) {//be list
            elementsAsList(childElements, path);
        } else if (listElements.size() == 1 && childElements.size() == 1) {//may be list
            elementsAsList(listElements, path);//as list
            read(childElements.get(0), path, true);//as not list
        } else {//not list
            for (Element childElement : childElements) {
                read(childElement, path, true);
            }
        }
    }

    private String buildPath(Element element, String path, boolean appendPath) {
        return appendPath ? path + "." + element.getNodeName() : path;
    }

    private void elementsAsList(List<Element> listElements, String path) {
        map.put(path + ".Length", String.valueOf(listElements.size()));
        for (int i = 0; i < listElements.size(); i++) {
            read(listElements.get(i), path + "[" + i + "]", false);
        }
    }
}
