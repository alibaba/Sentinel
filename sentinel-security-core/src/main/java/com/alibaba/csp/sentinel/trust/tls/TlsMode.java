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
package com.alibaba.csp.sentinel.trust.tls;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TLS mode：
 * <ul>
 * <li>DISABLE：Connection is not tunneled.</li>
 * <li>PERMISSIVE：Connection can be either plaintext or mTLS tunnel.</li>
 * <li>STRICT：Connection is an mTLS tunnel (TLS with client cert must be presented).</li>
 * </ul>
 *
 * @author lwj
 * @since 2.0.0
 */
public final class TlsMode {

    private TlsType globalTls = TlsType.DISABLE;

    private Map<Integer, TlsType> portToTls = new HashMap<>();

    public TlsType getGlobalTls() {
        return globalTls;
    }

    public void setGlobalTls(TlsType tlsType) {
        globalTls = tlsType;
    }

    /**
     * Get the tls mode corresponding to the port,
     * or the global tls mode if it does not exist
     *
     * @param port
     * @return
     */
    public TlsType getPortTls(Integer port) {
        return portToTls.getOrDefault(port, globalTls);
    }

    public int getPortsSize() {
        return portToTls.size();
    }

    public void setPortTls(Integer port, TlsType tlsType) {
        portToTls.put(port, tlsType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TlsMode tlsMode = (TlsMode) o;
        return globalTls == tlsMode.globalTls && Objects.equals(portToTls, tlsMode.portToTls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(globalTls, portToTls);
    }

    @Override
    public String toString() {
        return "TlsMode{" +
            "globalTls=" + globalTls +
            ", portToTls=" + portToTls +
            '}';
    }

    public enum TlsType {
        STRICT(0, "Strict Mode"),
        PERMISSIVE(1, "Permissive Mode"),
        DISABLE(2, "Disable Mode"),
        ;
        public static Map<Integer, TlsType> map = new HashMap<>();

        static {
            for (TlsType tlsEnum : TlsType.values()) {
                map.put(tlsEnum.code, tlsEnum);
            }
        }

        private int code;
        private String msg;

        TlsType(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public static TlsType getFromCode(int code) {
            return map.get(code);
        }

        @Override
        public String toString() {
            return "TlsType{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

}
