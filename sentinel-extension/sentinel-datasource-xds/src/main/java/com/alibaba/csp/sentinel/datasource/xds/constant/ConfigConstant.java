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
package com.alibaba.csp.sentinel.datasource.xds.constant;

import com.alibaba.csp.sentinel.datasource.xds.constant.type.AsymCryptoType;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.HashType;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.JwtPolicyType;

/**
 * @author lwj
 * @since 2.0.0
 */
public class ConfigConstant {

    public static String DEFAULT_HOST = "istiod.istio-system.svc";

    public static int DEFAULT_PORT = 15012;

    public static JwtPolicyType DEFAULT_JWT_POLICY = JwtPolicyType.FIRST_PARTY_JWT;

    public static String DEFAULT_ISTIOD_TOKEN = null;

    public static String DEFAULT_CA_ADDR = DEFAULT_HOST + ":" + DEFAULT_PORT;

    public static String DEFAULT_CA_CERT_PATH = "/var/run/secrets/istio/root-cert.pem";

    public static String DEFAULT_NAMESPACE = "default";

    public static String DEFAULT_POD_NAME = "sidecar";

    public static String DEFAULT_CLUSTER_ID = "Kubernetes";

    public static AsymCryptoType DEFAULT_ASYM_CYPTO_TYPE = AsymCryptoType.RSA_1024;

    public static HashType DEFAYLT_HASH_TYPE = HashType.SHA256;

    public static int DEFAULT_GET_CERT_TIMOUT_S = 30;
    /**
     * one day
     * 24 * 60 * 60
     */
    public static int DEFAULT_CERT_VALIDITY_TIME_S = 86400;

    public static float DEFAULT_CERT_PERIOD_RATIO = 0.8f;

    public static int DEFAULT_RECONNECTION_DELAY_S = 3;

    public static int DEFAULT_INIT_AWAIT_TIME_S = 30;

    public static String ENV_HOST = "SENTINEL_ISTIO_HOST";

    public static String ENV_HOST_PORT = "SENTINEL_ISTIO_PORT";

    public static String ENV_JWT_POLICY = "SENTINEL_JWT_POLICY";

    public static String ENV_ISTIOD_TOKEN = "SENTINEL_ISTIOD_POLICY";

    public static String ENV_CA_ADDR = "SENTINEL_CA_ADDR";

    public static String ENV_CA_CERT_PATH = "SENTINEL_CA_CERT_PATH";

    public static String ENV_NAMESPACE = "SENTINEL_NAMESPACE";

    public static String ENV_POD_NAME = "SENTINEL_POD_NAME";

    public static String ENV_CLUSTER_ID = "SENTINEL_CLUSTER_ID";

    public static String ENV_ASYM_CYPTO_TYPE = "SENTINEL_ASYM_CYPTO_TYPE";

    public static String ENV_HASH_TYPE = "SENTINEL_HASH_TYPE";

    public static String ENV_GET_CERT_TIMOUT_S = "SENTINEL_GET_CERT_TIMOUT_S";

    public static String ENV_CERT_VALIDITY_TIME_S = "SENTINEL_CERT_VALIDITY_TIME_S";

    public static String ENV_CERT_PERIOD_RATIO = "SENTINEL_CERT_PERIOD_RATIO";

    public static String ENV_RECONNECTION_DELAY_S = "SENTINEL_RECONNECTION_DELAY_S";

    public static String ENV_INIT_AWAIT_TIME_S = "SENTINEL_INIT_AWAIT_TIME_S";

    /**
     * k8s default environment
     */
    public static String[] K8S_ENV_POD_NAME = {"HOSTNAME", "POD_NAME"};

    public static String[] K8S_ENV_NAMESPACE = {"POD_NAMESPACE", "KUBERNETES_POD_NAMESPACE", "WORKLOAD_NAMESPACE"};
    public static String K8S_ENV_NAMESPACE_FILE_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";

}
