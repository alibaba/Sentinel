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
package com.alibaba.csp.sentinel.datasource.xds.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.util.io.FileUtil;

/**
 * @author lwj
 * @since 2.0.0
 */
public class TestUtil {

    public static String podName = "details-v1-6997d94bb9-2c9hj";
    public static String namespace = "default";

    /**
     * 1.Modify the corresponding namespace podName in file
     * _sentinel-extension/sentinel-datasource-xds/src/test/java/com/alibaba/csp/sentinel/datasource/xds/util
     * /TestUtil.java_
     * <p>
     * 2.Change the token in file _sentinel-extension/sentinel-datasource-xds/src/test/resources/token_,
     * and token path in istio-proxy
     * > cat /var/run/secrets/tokens/istio-token
     * <p>
     * 3.Change the root-cert.pem in file
     * _sentinel-extension/sentinel-datasource-xds/src/test/resources/root-cert.pem_，and  root-cert.pem path in
     * istio-proxy
     * > cat /var/run/secrets/istio/root-cert.pem
     * <p>
     * 4.Modify istiod.istio-system.svc in local /etc/hosts
     * <p>
     * 5.Go test
     *
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static XdsConfigProperties createConfig() throws URISyntaxException, IOException {
        XdsConfigProperties xdsConfigProperties = XdsConfigProperties.getXdsDefaultXdsProperties();
        xdsConfigProperties.setCaAddr("istiod.istio-system.svc:15012");
        xdsConfigProperties.setPodName(podName);
        xdsConfigProperties.setNamespace(namespace);
        URL tokenPath = TestUtil.class.getClassLoader().getResource("token");
        xdsConfigProperties.setIstiodToken(
            FileUtil.readFileToString(new File(tokenPath.toURI()), StandardCharsets.UTF_8));
        xdsConfigProperties.setCaCertPath(TestUtil.class.getClassLoader().getResource("root-cert.pem").toString());
        return xdsConfigProperties;
    }
}
