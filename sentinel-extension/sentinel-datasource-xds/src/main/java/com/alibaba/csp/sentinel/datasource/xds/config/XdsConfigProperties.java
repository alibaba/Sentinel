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
package com.alibaba.csp.sentinel.datasource.xds.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.alibaba.csp.sentinel.datasource.xds.constant.ConfigConstant;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.AsymCryptoType;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.HashType;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.JwtPolicyType;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.io.FileUtil;

/**
 * xds config
 *
 * @author lwj
 * @since 2.0.0
 */
public class XdsConfigProperties {

    /**
     * istiod host
     */
    private String host;
    /**
     * istiod port
     */
    private int port;

    /**
     * jwt Policy
     */
    private JwtPolicyType jwtPolicy;

    /**
     * jwt token for istiod.
     * If null, it will be read from the file according to jwtPolicy
     */
    private String istiodToken;

    /**
     * isito CA ip:port
     */
    private String caAddr;

    /**
     * ca cert file path
     */
    private String caCertPath;

    /**
     * k8s namespace
     */
    private String namespace;
    /**
     * k8s pod name
     */
    private String podName;

    /**
     * k8s cluster id
     */
    private String clusterId;

    /**
     * asymmetric encryption algorithm type
     */
    private AsymCryptoType asymCryptoType;

    /**
     * hash Type
     */
    private HashType hashType;

    /**
     * Null will be returned
     * if the time elapsed during obtaining the certificate
     * time unit second
     */
    private int getCertTimeoutS;

    /**
     * validity of certificate,
     * time unit second
     */
    private int certValidityTimeS;

    /**
     * the certificate is requested again after
     * certValidityTimeS * certPeriodRatio
     */
    private float certPeriodRatio;

    /**
     * the reconnection delay of the Xds client after disconnection.
     * time unit second
     */
    private int reconnectionDelayS;

    /**
     * xds Client initialization await time
     * time unit second
     */
    private int initAwaitTimeS;

    public XdsConfigProperties() {

    }

    /**
     * Build the configuration using the default values
     */
    public static XdsConfigProperties getXdsDefaultXdsProperties() {
        XdsConfigProperties config = new XdsConfigProperties();
        config.setHost(ConfigConstant.DEFAULT_HOST);
        config.setPort(ConfigConstant.DEFAULT_PORT);
        config.setJwtPolicy(ConfigConstant.DEFAULT_JWT_POLICY);
        config.setIstiodToken(ConfigConstant.DEFAULT_ISTIOD_TOKEN);
        config.setCaAddr(ConfigConstant.DEFAULT_CA_ADDR);
        config.setCaCertPath(ConfigConstant.DEFAULT_CA_CERT_PATH);
        config.setNamespace(ConfigConstant.DEFAULT_NAMESPACE);
        config.setPodName(ConfigConstant.DEFAULT_POD_NAME);
        config.setClusterId(ConfigConstant.DEFAULT_CLUSTER_ID);
        config.setAsymCryptoType(ConfigConstant.DEFAULT_ASYM_CYPTO_TYPE);
        config.setHashType(ConfigConstant.DEFAYLT_HASH_TYPE);
        config.setGetCertTimeoutS(ConfigConstant.DEFAULT_GET_CERT_TIMOUT_S);
        config.setCertValidityTimeS(ConfigConstant.DEFAULT_CERT_VALIDITY_TIME_S);
        config.setCertPeriodRatio(ConfigConstant.DEFAULT_CERT_PERIOD_RATIO);
        config.setReconnectionDelayS(ConfigConstant.DEFAULT_RECONNECTION_DELAY_S);
        config.setInitAwaitTimeS(ConfigConstant.DEFAULT_INIT_AWAIT_TIME_S);
        return config;
    }

    /**
     * Read the build configuration from the environment variable
     * or use the default value if not available
     */
    public static XdsConfigProperties getFromXdsPropertiesEnv() {
        XdsConfigProperties config = getXdsDefaultXdsProperties();
        //Read the environment built into the k8s
        readFromK8sEnv(config);
        //Read from defined environment variables
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_HOST)).ifPresent(c -> config.setHost(c));
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_HOST_PORT)).ifPresent(
            c -> config.setPort(Integer.parseInt(c)));
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_JWT_POLICY)).ifPresent(c -> {
            JwtPolicyType jwtPolicy = JwtPolicyType.getByKey(c);
            if (null != jwtPolicy) {
                config.setJwtPolicy(jwtPolicy);
            }
        });

        Optional.ofNullable(System.getenv(ConfigConstant.ENV_ISTIOD_TOKEN)).ifPresent(c -> config.setIstiodToken(c));
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_CA_ADDR)).ifPresent(c -> config.setCaAddr(c));
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_CA_CERT_PATH)).ifPresent(c -> config.setCaCertPath(c));
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_NAMESPACE)).ifPresent(c -> config.setNamespace(c));
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_POD_NAME)).ifPresent(c -> config.setPodName(c));
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_CLUSTER_ID)).ifPresent(c -> config.setClusterId(c));

        Optional.ofNullable(System.getenv(ConfigConstant.ENV_ASYM_CYPTO_TYPE)).ifPresent(c -> {
            AsymCryptoType asymCryptoType = AsymCryptoType.getByKey(c);
            if (null != asymCryptoType) {
                config.setAsymCryptoType(asymCryptoType);
            }
        });

        Optional.ofNullable(System.getenv(ConfigConstant.ENV_HASH_TYPE)).ifPresent(c -> {
            HashType hashType = HashType.getByKey(c);
            if (null != hashType) {
                config.setHashType(hashType);
            }
        });

        Optional.ofNullable(System.getenv(ConfigConstant.ENV_GET_CERT_TIMOUT_S)).ifPresent(
            c -> config.setGetCertTimeoutS(Integer.parseInt(c)));
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_CERT_VALIDITY_TIME_S)).ifPresent(
            c -> config.setCertValidityTimeS(Integer.parseInt(c)));
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_CERT_PERIOD_RATIO)).ifPresent(
            c -> config.setCertPeriodRatio(Float.parseFloat(c)));
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_RECONNECTION_DELAY_S)).ifPresent(
            c -> config.setReconnectionDelayS(Integer.parseInt(c)));
        Optional.ofNullable(System.getenv(ConfigConstant.ENV_INIT_AWAIT_TIME_S)).ifPresent(
            c -> config.setInitAwaitTimeS(Integer.parseInt(c)));
        return config;
    }

    private static void readFromK8sEnv(XdsConfigProperties config) {
        String k8sPodName = null;
        for (String env : ConfigConstant.K8S_ENV_POD_NAME) {
            k8sPodName = System.getenv(env);
            if (null != k8sPodName) {
                break;
            }
        }
        if (null != k8sPodName) {
            config.setPodName(k8sPodName);
        }

        String k8sNamespace = null;
        for (String env : ConfigConstant.K8S_ENV_NAMESPACE) {
            k8sNamespace = System.getenv(env);
            if (null != k8sNamespace) {
                break;
            }
        }
        if (null == k8sNamespace) {
            File namespaceFile = new File(ConfigConstant.K8S_ENV_NAMESPACE_FILE_PATH);
            if (namespaceFile.canRead()) {
                try {
                    k8sNamespace = FileUtil.readFileToString(namespaceFile, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    RecordLog.error("[XdsDataSource] Read k8s namespace file error", e);
                }
            }
        }

        if (null != k8sNamespace) {
            config.setNamespace(k8sNamespace);
        }

    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public JwtPolicyType getJwtPolicy() {
        return jwtPolicy;
    }

    public void setJwtPolicy(JwtPolicyType jwtPolicy) {
        this.jwtPolicy = jwtPolicy;
    }

    public String getIstiodToken() {
        if (null != istiodToken) {
            return istiodToken;
        }
        File saFile = new File(jwtPolicy.getJwtPath());

        if (saFile.canRead()) {
            try {
                return FileUtil.readFileToString(saFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
                RecordLog.error("[XdsDataSource] Unable to read token file.", e);
            }
        }
        return "";
    }

    public void setIstiodToken(String istiodToken) {
        this.istiodToken = istiodToken;
    }

    public String getCaAddr() {
        return caAddr;
    }

    public void setCaAddr(String caAddr) {
        this.caAddr = caAddr;
    }

    public String getCaCertPath() {
        return caCertPath;
    }

    public void setCaCertPath(String caCertPath) {
        this.caCertPath = caCertPath;
    }

    public String getCaCert() {
        File caFile = new File(caCertPath);
        if (caFile.canRead()) {
            try {
                return FileUtil.readFileToString(caFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
                RecordLog.error("[XdsDataSource] read ca file error", e);
            }
        }
        return null;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public AsymCryptoType getAsymCryptoType() {
        return asymCryptoType;
    }

    public void setAsymCryptoType(AsymCryptoType asymCryptoType) {
        this.asymCryptoType = asymCryptoType;
    }

    public HashType getHashType() {
        return hashType;
    }

    public void setHashType(HashType hashType) {
        this.hashType = hashType;
    }

    public int getGetCertTimeoutS() {
        return getCertTimeoutS;
    }

    public void setGetCertTimeoutS(int getCertTimeoutS) {
        this.getCertTimeoutS = getCertTimeoutS;
    }

    public int getCertValidityTimeS() {
        return certValidityTimeS;
    }

    public void setCertValidityTimeS(int certValidityTimeS) {
        this.certValidityTimeS = certValidityTimeS;
    }

    public float getCertPeriodRatio() {
        return certPeriodRatio;
    }

    public void setCertPeriodRatio(float certPeriodRatio) {
        this.certPeriodRatio = certPeriodRatio;
    }

    public int getReconnectionDelayS() {
        return reconnectionDelayS;
    }

    public void setReconnectionDelayS(int reconnectionDelayS) {
        this.reconnectionDelayS = reconnectionDelayS;
    }

    public int getInitAwaitTimeS() {
        return initAwaitTimeS;
    }

    public void setInitAwaitTimeS(int initAwaitTimeS) {
        this.initAwaitTimeS = initAwaitTimeS;
    }
}
