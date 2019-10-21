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
package com.alibaba.acm.shaded.com.aliyuncs.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class XmlUtils {

    public static Document getDocument(String payload)
        throws ParserConfigurationException, SAXException, IOException {
        if (payload == null || payload.length() < 1) { return null; }

        StringReader sr = new StringReader(payload);
        InputSource source = new InputSource(sr);

        return getDocument(source, null);
    }

    public static Document getDocument(InputSource xml, InputStream xsd)
        throws ParserConfigurationException, SAXException, IOException {
        Document doc = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            if (xsd != null) {
                dbf.setNamespaceAware(true);
            }

            DocumentBuilder builder = dbf.newDocumentBuilder();
            doc = builder.parse(xml);

            if (xsd != null) { validateXml(doc, xsd); }
        } finally {
            closeStream(xml.getByteStream());
        }

        return doc;
    }

    public static Element getRootElementFromString(String payload)
        throws ParserConfigurationException, SAXException, IOException {

        return getDocument(payload).getDocumentElement();
    }

    public static List<Element> getChildElements(Element parent, String tagName) {
        if (null == parent) { return null; }
        NodeList nodes = parent.getElementsByTagName(tagName);
        List<Element> elements = new ArrayList<Element>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getParentNode() == parent) { elements.add((Element)node); }
        }

        return elements;
    }

    public static List<Element> getChildElements(Element parent) {
        if (null == parent) { return null; }

        NodeList nodes = parent.getChildNodes();
        List<Element> elements = new ArrayList<Element>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) { elements.add((Element)node); }
        }

        return elements;
    }

    public static void validateXml(InputStream xml, InputStream xsd)
        throws SAXException, IOException, ParserConfigurationException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(xml);
            validateXml(doc, xsd);
        } finally {
            closeStream(xml);
            closeStream(xsd);
        }
    }

    public static void validateXml(Node root, InputStream xsd)
        throws SAXException, IOException {
        try {
            Source source = new StreamSource(xsd);
            Schema schema = SchemaFactory.newInstance(
                XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(source);

            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(root));
        } finally {
            closeStream(xsd);
        }
    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }
}