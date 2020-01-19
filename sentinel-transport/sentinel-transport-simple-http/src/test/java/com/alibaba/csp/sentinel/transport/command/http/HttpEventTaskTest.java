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
package com.alibaba.csp.sentinel.transport.command.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import org.junit.Test;

import com.alibaba.csp.sentinel.command.CommandRequest;

public class HttpEventTaskTest {
    
    @Test
    public void processQueryString() {
        CommandRequest request;
        
        request = HttpEventTask.processQueryString(null);
        assertNotNull(request);
        
        request = HttpEventTask.processQueryString(null);
        assertNotNull(request);
        
        request = HttpEventTask.processQueryString("get /?a=1&b=2&c=3#mark HTTP/1.0");
        assertNotNull(request);
        assertEquals("1", request.getParam("a"));
        assertEquals("2", request.getParam("b"));
        assertEquals("3", request.getParam("c"));
        
        request = HttpEventTask.processQueryString("post /test?a=3&b=4&c=3#mark HTTP/1.0");
        assertNotNull(request);
        assertEquals("3", request.getParam("a"));
        assertEquals("4", request.getParam("b"));
        assertEquals("3", request.getParam("c"));
    }
    
    @Test
    public void removeAnchor() {
        assertNull(HttpEventTask.removeAnchor(null));
        assertEquals("", HttpEventTask.removeAnchor(""));
        assertEquals("", HttpEventTask.removeAnchor("#mark"));
        assertEquals("a", HttpEventTask.removeAnchor("a#mark"));
    }
    
    @Test
    public void parseSingleParam() {
        CommandRequest request;
        
        request = new CommandRequest();
        HttpEventTask.parseSingleParam(null, request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.parseSingleParam("", request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.parseSingleParam("a", request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.parseSingleParam("=", request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.parseSingleParam("a=", request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.parseSingleParam("=a", request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.parseSingleParam("test=", request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.parseSingleParam("=test", request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.parseSingleParam("a=1", request);
        assertEquals(1, request.getParameters().size());
        assertEquals("1", request.getParam("a"));
        
        request = new CommandRequest();
        HttpEventTask.parseSingleParam("a_+=1+", request);
        assertEquals(1, request.getParameters().size());
        assertEquals("1 ", request.getParam("a_ "));
    }
    
    @Test
    public void parseParams() {
        CommandRequest request;
        
        // mixed
        request = new CommandRequest();
        HttpEventTask.parseParams("a=1&&b&=3&&c=4&a_+1=3_3%20&%E7%9A%84=test%E7%9A%84#mark", request);
        assertEquals(4, request.getParameters().size());
        assertEquals("1", request.getParam("a"));
        assertNull(request.getParam("b"));
        assertEquals("4", request.getParam("c"));
        assertEquals("3_3 ", request.getParam("a_ 1"));
        assertEquals("test的", request.getParam("的"));
        
        request = new CommandRequest();
        HttpEventTask.parseParams(null, request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.parseParams("", request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.parseParams("&&b&=3&", request);
        assertEquals(0, request.getParameters().size());
    }
    
    @Test
    public void consumePostBody() throws IOException {
        CommandRequest request;
        
        request = new CommandRequest();
        HttpEventTask.consumePostBody(new StringReader(""), request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.consumePostBody(new StringReader("=a"), request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.consumePostBody(new StringReader("a="), request);
        assertEquals(0, request.getParameters().size());
        
        request = new CommandRequest();
        HttpEventTask.consumePostBody(new StringReader("a+=%20&b=%E7%9A%84"), request);
        assertEquals(2, request.getParameters().size());
        assertEquals(" ", request.getParam("a "));
        assertEquals("的", request.getParam("b"));
        
        {
            // Capacity test
            request = new CommandRequest();
            char[] buf = new char[1024 * 1024];
            Arrays.fill(buf, '&');
            String str = "a+=%20&b=%E7%9A%84";
            for (int i = 0, j = buf.length - 1024; i < str.length(); i ++, j ++) {
                buf[j] = str.charAt(i);
            }
            HttpEventTask.consumePostBody(new CharArrayReader(buf), request);
            assertEquals(2, request.getParameters().size());
            assertEquals(" ", request.getParam("a "));
            assertEquals("的", request.getParam("b"));
        }
    }
    
    @Test
    public void processHeaderField() {
        assertTrue(HttpEventTask.processHeaderField(null));
        assertTrue(HttpEventTask.processHeaderField(""));
        assertTrue(HttpEventTask.processHeaderField(":"));
        assertTrue(HttpEventTask.processHeaderField("a:1"));
        assertTrue(HttpEventTask.processHeaderField("a: 2"));
        assertTrue(HttpEventTask.processHeaderField("Content-Encoding: utf-8"));
        assertTrue(HttpEventTask.processHeaderField("Content-Type:application/x-www-form-urlencoded"));
        assertTrue(HttpEventTask.processHeaderField("Content-Type: application/x-www-form-urlencoded;charset=utf-8"));
        assertTrue(HttpEventTask.processHeaderField("Content-Type:application/x-www-form-urlencoded; charset=utf-8"));
        
        assertFalse(HttpEventTask.processHeaderField("Content-Type:application/json"));
        assertFalse(HttpEventTask.processHeaderField("Content-Type:application/json; charset=utf-8"));
        assertFalse(HttpEventTask.processHeaderField("Content-Type: application/json; charset=utf-8"));
    }
    
    @Test
    public void processPostRequest() throws IOException {
        CommandRequest request;
        
        request = new CommandRequest();
        request.addParam("a", "1");
        HttpEventTask.processPostRequest(new BufferedReader(new StringReader("")), request);
        assertEquals("1", request.getParam("a"));
        HttpEventTask.processPostRequest(new BufferedReader(new StringReader("Host: demo.com\r\n" + 
                "Accept: */*\r\n" + 
                "Accept-Language: en-us\r\n" + 
                "Accept-Encoding: gzip, deflate\r\n" + 
                "Content-Type: application/x-www-form-urlencoded; charset=UTF-8\r\n" + 
                "Connection: keep-alive\r\n" + 
                "Content-Length: 7\r\n" + 
                "\r\n" + 
                "a=3&b=5")), request);
        assertEquals("3", request.getParam("a"));
        assertEquals("5", request.getParam("b"));
        
        HttpEventTask.processPostRequest(new BufferedReader(new StringReader("Host: demo.com\r\n" + 
                "Accept: */*\r\n" + 
                "Accept-Language: en-us\r\n" + 
                "Accept-Encoding: gzip, deflate\r\n" + 
                "Content-Type: application/json\r\n" + 
                "Connection: keep-alive\r\n" + 
                "Content-Length: 7\r\n" + 
                "\r\n" + 
                "a=1&b=2")), request);
        assertEquals("3", request.getParam("a"));
        assertEquals("5", request.getParam("b"));
    }
}
