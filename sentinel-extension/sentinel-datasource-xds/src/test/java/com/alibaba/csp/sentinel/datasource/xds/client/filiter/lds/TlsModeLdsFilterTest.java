/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource.xds.client.filiter.lds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import com.alibaba.csp.sentinel.datasource.xds.property.repository.TlsModeRepository;
import com.alibaba.csp.sentinel.datasource.xds.util.TestUtil;
import com.alibaba.csp.sentinel.trust.tls.TlsMode;

import io.envoyproxy.envoy.config.listener.v3.Listener;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author lwj
 * @since 2.0.0
 */
public class TlsModeLdsFilterTest {

    /**
     * TlsModeLdsFilterTest1.ser  file contains yaml
     * ./resources/yaml/TlsModeLdsFilterTest1.yaml
     */

    @Test
    public void testResolve1() throws IOException, URISyntaxException, ClassNotFoundException {
        TlsMode tlsMode = getFromFile("TlsModeLdsFilterTest1.ser");

        assertEquals(2, tlsMode.getPortsSize());
        assertEquals(TlsMode.TlsType.STRICT, tlsMode.getPortTls(8080));
        assertEquals(TlsMode.TlsType.PERMISSIVE, tlsMode.getPortTls(9999));
        assertEquals(TlsMode.TlsType.DISABLE, tlsMode.getGlobalTls());
    }

    /**
     * TlsModeLdsFilterTest2.ser  file contains yaml
     * ./resources/yaml/TlsModeLdsFilterTest2.yaml
     *
     * @throws IOException
     * @throws URISyntaxException
     * @throws ClassNotFoundException
     */

    @Test
    public void testResolve2() throws IOException, URISyntaxException, ClassNotFoundException {
        TlsMode tlsMode = getFromFile("TlsModeLdsFilterTest2.ser");

        assertEquals(2, tlsMode.getPortsSize());
        assertEquals(TlsMode.TlsType.DISABLE, tlsMode.getPortTls(8080));
        assertEquals(TlsMode.TlsType.STRICT, tlsMode.getPortTls(9999));
        assertEquals(TlsMode.TlsType.PERMISSIVE, tlsMode.getGlobalTls());
    }

    /**
     * TlsModeLdsFilterTest3.ser  file contains yaml
     * ./resources/yaml/TlsModeLdsFilterTest3.yaml
     *
     * @throws IOException
     * @throws URISyntaxException
     * @throws ClassNotFoundException
     */

    @Test
    public void testResolve3() throws IOException, URISyntaxException, ClassNotFoundException {
        TlsMode tlsMode = getFromFile("TlsModeLdsFilterTest3.ser");

        assertEquals(2, tlsMode.getPortsSize());
        assertEquals(TlsMode.TlsType.PERMISSIVE, tlsMode.getPortTls(8080));
        assertEquals(TlsMode.TlsType.DISABLE, tlsMode.getPortTls(9999));
        assertEquals(TlsMode.TlsType.STRICT, tlsMode.getGlobalTls());
    }

    public TlsMode getFromFile(String fileName) throws IOException, ClassNotFoundException, URISyntaxException {
        URL serURL = TestUtil.class.getClassLoader().getResource(fileName);

        FileInputStream fileInputStream = new FileInputStream(new File(serURL.toURI()));
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        List<Listener> listeners = (List<Listener>) objectInputStream.readObject();

        TlsModeRepository tlsModeRepository = new TlsModeRepository();
        TlsModeLdsFilter tlsModeLdsFilter = new TlsModeLdsFilter(tlsModeRepository);
        tlsModeLdsFilter.resolve(listeners);
        TlsMode tlsMode = tlsModeRepository.getInstance();
        return tlsMode;
    }
}