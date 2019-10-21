/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.util.Args;

/**
 * Builder for {@link javax.net.ssl.SSLContext} instances.
 * <p>
 * Please note: the default Oracle JSSE implementation of {@link SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}
 * accepts multiple key and trust managers, however only only first matching type is ever used.
 * See for example:
 * <a href="http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLContext.html#init%28javax.net.ssl.KeyManager[],%20javax.net.ssl.TrustManager[],%20java.security.SecureRandom%29">
 * SSLContext.html#init
 * </a>
 *
 * @since 4.4
 */
public class SSLContextBuilder {

    static final String TLS   = "TLS";

    private String protocol;
    private final Set<KeyManager> keymanagers;
    private final Set<TrustManager> trustmanagers;
    private SecureRandom secureRandom;

    public static SSLContextBuilder create() {
        return new SSLContextBuilder();
    }

    public SSLContextBuilder() {
        super();
        this.keymanagers = new LinkedHashSet<KeyManager>();
        this.trustmanagers = new LinkedHashSet<TrustManager>();
    }

    public SSLContextBuilder useProtocol(final String protocol) {
        this.protocol = protocol;
        return this;
    }

    public SSLContextBuilder setSecureRandom(final SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
        return this;
    }

    public SSLContextBuilder loadTrustMaterial(
            final KeyStore truststore,
            final TrustStrategy trustStrategy) throws NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmfactory.init(truststore);
        final TrustManager[] tms = tmfactory.getTrustManagers();
        if (tms != null) {
            if (trustStrategy != null) {
                for (int i = 0; i < tms.length; i++) {
                    final TrustManager tm = tms[i];
                    if (tm instanceof X509TrustManager) {
                        tms[i] = new TrustManagerDelegate(
                                (X509TrustManager) tm, trustStrategy);
                    }
                }
            }
            for (final TrustManager tm : tms) {
                this.trustmanagers.add(tm);
            }
        }
        return this;
    }

    public SSLContextBuilder loadTrustMaterial(
            final TrustStrategy trustStrategy) throws NoSuchAlgorithmException, KeyStoreException {
        return loadTrustMaterial(null, trustStrategy);
    }

    public SSLContextBuilder loadTrustMaterial(
            final File file,
            final char[] storePassword,
            final TrustStrategy trustStrategy) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        Args.notNull(file, "Truststore file");
        final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final FileInputStream instream = new FileInputStream(file);
        try {
            trustStore.load(instream, storePassword);
        } finally {
            instream.close();
        }
        return loadTrustMaterial(trustStore, trustStrategy);
    }

    public SSLContextBuilder loadTrustMaterial(
            final File file,
            final char[] storePassword) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        return loadTrustMaterial(file, storePassword, null);
    }

    public SSLContextBuilder loadTrustMaterial(
            final File file) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        return loadTrustMaterial(file, null);
    }

    public SSLContextBuilder loadTrustMaterial(
            final URL url,
            final char[] storePassword,
            final TrustStrategy trustStrategy) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        Args.notNull(url, "Truststore URL");
        final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final InputStream instream = url.openStream();
        try {
            trustStore.load(instream, storePassword);
        } finally {
            instream.close();
        }
        return loadTrustMaterial(trustStore, trustStrategy);
    }

    public SSLContextBuilder loadTrustMaterial(
            final URL url,
            final char[] storePassword) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        return loadTrustMaterial(url, storePassword, null);
    }

    public SSLContextBuilder loadKeyMaterial(
            final KeyStore keystore,
            final char[] keyPassword,
            final PrivateKeyStrategy aliasStrategy)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        final KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(keystore, keyPassword);
        final KeyManager[] kms =  kmfactory.getKeyManagers();
        if (kms != null) {
            if (aliasStrategy != null) {
                for (int i = 0; i < kms.length; i++) {
                    final KeyManager km = kms[i];
                    if (km instanceof X509ExtendedKeyManager) {
                        kms[i] = new KeyManagerDelegate((X509ExtendedKeyManager) km, aliasStrategy);
                    }
                }
            }
            for (final KeyManager km : kms) {
                keymanagers.add(km);
            }
        }
        return this;
    }

    public SSLContextBuilder loadKeyMaterial(
            final KeyStore keystore,
            final char[] keyPassword) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        return loadKeyMaterial(keystore, keyPassword, null);
    }

    public SSLContextBuilder loadKeyMaterial(
            final File file,
            final char[] storePassword,
            final char[] keyPassword,
            final PrivateKeyStrategy aliasStrategy) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        Args.notNull(file, "Keystore file");
        final KeyStore identityStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final FileInputStream instream = new FileInputStream(file);
        try {
            identityStore.load(instream, storePassword);
        } finally {
            instream.close();
        }
        return loadKeyMaterial(identityStore, keyPassword, aliasStrategy);
    }

    public SSLContextBuilder loadKeyMaterial(
            final File file,
            final char[] storePassword,
            final char[] keyPassword) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        return loadKeyMaterial(file, storePassword, keyPassword, null);
    }

    public SSLContextBuilder loadKeyMaterial(
            final URL url,
            final char[] storePassword,
            final char[] keyPassword,
            final PrivateKeyStrategy aliasStrategy) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        Args.notNull(url, "Keystore URL");
        final KeyStore identityStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final InputStream instream = url.openStream();
        try {
            identityStore.load(instream, storePassword);
        } finally {
            instream.close();
        }
        return loadKeyMaterial(identityStore, keyPassword, aliasStrategy);
    }

    public SSLContextBuilder loadKeyMaterial(
            final URL url,
            final char[] storePassword,
            final char[] keyPassword) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        return loadKeyMaterial(url, storePassword, keyPassword, null);
    }

    protected void initSSLContext(
            final SSLContext sslcontext,
            final Collection<KeyManager> keyManagers,
            final Collection<TrustManager> trustManagers,
            final SecureRandom secureRandom) throws KeyManagementException {
        sslcontext.init(
                !keyManagers.isEmpty() ? keyManagers.toArray(new KeyManager[keyManagers.size()]) : null,
                !trustManagers.isEmpty() ? trustManagers.toArray(new TrustManager[trustManagers.size()]) : null,
                secureRandom);
    }

    public SSLContext build() throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslcontext = SSLContext.getInstance(
                this.protocol != null ? this.protocol : TLS);
        initSSLContext(sslcontext, keymanagers, trustmanagers, secureRandom);
        return sslcontext;
    }

    static class TrustManagerDelegate implements X509TrustManager {

        private final X509TrustManager trustManager;
        private final TrustStrategy trustStrategy;

        TrustManagerDelegate(final X509TrustManager trustManager, final TrustStrategy trustStrategy) {
            super();
            this.trustManager = trustManager;
            this.trustStrategy = trustStrategy;
        }

        @Override
        public void checkClientTrusted(
                final X509Certificate[] chain, final String authType) throws CertificateException {
            this.trustManager.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(
                final X509Certificate[] chain, final String authType) throws CertificateException {
            if (!this.trustStrategy.isTrusted(chain, authType)) {
                this.trustManager.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return this.trustManager.getAcceptedIssuers();
        }

    }

    static class KeyManagerDelegate extends X509ExtendedKeyManager {

        private final X509ExtendedKeyManager keyManager;
        private final PrivateKeyStrategy aliasStrategy;

        KeyManagerDelegate(final X509ExtendedKeyManager keyManager, final PrivateKeyStrategy aliasStrategy) {
            super();
            this.keyManager = keyManager;
            this.aliasStrategy = aliasStrategy;
        }

        @Override
        public String[] getClientAliases(
                final String keyType, final Principal[] issuers) {
            return this.keyManager.getClientAliases(keyType, issuers);
        }

        public Map<String, PrivateKeyDetails> getClientAliasMap(
                final String[] keyTypes, final Principal[] issuers) {
            final Map<String, PrivateKeyDetails> validAliases = new HashMap<String, PrivateKeyDetails>();
            for (final String keyType: keyTypes) {
                final String[] aliases = this.keyManager.getClientAliases(keyType, issuers);
                if (aliases != null) {
                    for (final String alias: aliases) {
                        validAliases.put(alias,
                                new PrivateKeyDetails(keyType, this.keyManager.getCertificateChain(alias)));
                    }
                }
            }
            return validAliases;
        }

        public Map<String, PrivateKeyDetails> getServerAliasMap(
                final String keyType, final Principal[] issuers) {
            final Map<String, PrivateKeyDetails> validAliases = new HashMap<String, PrivateKeyDetails>();
            final String[] aliases = this.keyManager.getServerAliases(keyType, issuers);
            if (aliases != null) {
                for (final String alias: aliases) {
                    validAliases.put(alias,
                            new PrivateKeyDetails(keyType, this.keyManager.getCertificateChain(alias)));
                }
            }
            return validAliases;
        }

        @Override
        public String chooseClientAlias(
                final String[] keyTypes, final Principal[] issuers, final Socket socket) {
            final Map<String, PrivateKeyDetails> validAliases = getClientAliasMap(keyTypes, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, socket);
        }

        @Override
        public String[] getServerAliases(
                final String keyType, final Principal[] issuers) {
            return this.keyManager.getServerAliases(keyType, issuers);
        }

        @Override
        public String chooseServerAlias(
                final String keyType, final Principal[] issuers, final Socket socket) {
            final Map<String, PrivateKeyDetails> validAliases = getServerAliasMap(keyType, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, socket);
        }

        @Override
        public X509Certificate[] getCertificateChain(final String alias) {
            return this.keyManager.getCertificateChain(alias);
        }

        @Override
        public PrivateKey getPrivateKey(final String alias) {
            return this.keyManager.getPrivateKey(alias);
        }

        @Override
        public String chooseEngineClientAlias(
                final String[] keyTypes, final Principal[] issuers, final SSLEngine sslEngine) {
            final Map<String, PrivateKeyDetails> validAliases = getClientAliasMap(keyTypes, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, null);
        }

        @Override
        public String chooseEngineServerAlias(
                final String keyType, final Principal[] issuers, final SSLEngine sslEngine) {
            final Map<String, PrivateKeyDetails> validAliases = getServerAliasMap(keyType, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, null);
        }

    }

}
