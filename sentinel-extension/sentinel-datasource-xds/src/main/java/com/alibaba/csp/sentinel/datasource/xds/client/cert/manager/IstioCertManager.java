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
package com.alibaba.csp.sentinel.datasource.xds.client.cert.manager;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import com.alibaba.csp.sentinel.datasource.xds.client.grpc.IstioCertificateRequest;
import com.alibaba.csp.sentinel.datasource.xds.client.grpc.IstioCertificateResponse;
import com.alibaba.csp.sentinel.datasource.xds.client.grpc.IstioCertificateServiceGrpc;
import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.AsymCryptoType;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.HashType;
import com.alibaba.csp.sentinel.datasource.xds.expection.CertificateException;
import com.alibaba.csp.sentinel.datasource.xds.property.repository.CertPairRepository;
import com.alibaba.csp.sentinel.datasource.xds.util.CertificateUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.trust.cert.CertPair;
import com.alibaba.csp.sentinel.util.StringUtil;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

/**
 * Istio certificate management
 *
 * @author lwj
 * @since 2.0.0
 */
public class IstioCertManager extends AbstractCertManager {

    public static final String CSR_REQUEST_BEGIN = "-----BEGIN CERTIFICATE REQUEST-----";
    public static final String CSR_REQUEST_END = "-----END CERTIFICATE REQUEST-----";

    public IstioCertManager(XdsConfigProperties xdsConfigProperties, CertPairRepository certPairRepository) {
        super(xdsConfigProperties, certPairRepository);
    }

    public static String getCSR(KeyPair localKeyPair, AsymCryptoType asymCryptoType, HashType hashType) {
        try {
            X500NameBuilder localX500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
            //Pass any non-empty string to have the result in the certificate's subject
            localX500NameBuilder.addRDN(BCStyle.CN, "CN");
            X500Name localX500Name = localX500NameBuilder.build();
            JcaPKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                localX500Name, localKeyPair.getPublic());
            JcaContentSignerBuilder csBuilder;
            csBuilder = new JcaContentSignerBuilder(
                hashType.getKey() + "with" + asymCryptoType.getPrimaryType().getKey());
            ContentSigner signer = csBuilder.build(localKeyPair.getPrivate());
            PKCS10CertificationRequest csr = p10Builder.build(signer);
            return CSR_REQUEST_BEGIN + "\n"
                + new String(Base64.getEncoder().encode(csr.getEncoded())) + "\n"
                + CSR_REQUEST_END + "\n";
        } catch (Exception e) {
            throw new CertificateException("Unable to generate CSR", e);
        }
    }

    public static ManagedChannel getManagedChannel(String caCert, String caAddr) {
        ManagedChannel channel = null;
        try {
            SslContext sslContext = null;
            if (StringUtil.isNotEmpty(caCert)) {
                sslContext = GrpcSslContexts.forClient()
                    .trustManager(new ByteArrayInputStream(caCert.getBytes(StandardCharsets.UTF_8)))
                    .build();
            } else {
                sslContext = GrpcSslContexts.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            }
            channel = NettyChannelBuilder.forTarget(caAddr)
                .sslContext(sslContext)
                .build();

        } catch (SSLException e) {
            throw new CertificateException("Create channel error", e);
        }
        return channel;
    }

    public static Metadata createHeader(String istiodToken, String clusterId) {
        Metadata header = new Metadata();
        Metadata.Key<String> key = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
        header.put(key, "Bearer " + istiodToken);

        key = Metadata.Key.of("ClusterID", Metadata.ASCII_STRING_MARSHALLER);
        header.put(key, clusterId);
        return header;
    }

    @Override
    protected CertPair doGetNewCertPair() {
        CertPair newCertPair = new CertPair();
        try {
            KeyPair localKeyPair = CertificateUtil.genKeyPair(xdsConfigProperties.getAsymCryptoType());
            newCertPair.setPrivateKey(localKeyPair.getPrivate());
            newCertPair.setRawPrivateKey(
                CertificateUtil.getRawPrivateKey(localKeyPair.getPrivate(), xdsConfigProperties.getAsymCryptoType()));
            String csr = getCSR(localKeyPair, xdsConfigProperties.getAsymCryptoType(),
                xdsConfigProperties.getHashType());
            ManagedChannel channel = getManagedChannel(xdsConfigProperties.getCaCert(),
                xdsConfigProperties.getCaAddr());
            Metadata header = createHeader(xdsConfigProperties.getIstiodToken(), xdsConfigProperties.getClusterId());

            IstioCertificateServiceGrpc.IstioCertificateServiceStub stub = IstioCertificateServiceGrpc
                .newStub(channel);
            stub = MetadataUtils.attachHeaders(stub, header);
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            long certExpireTime = System.currentTimeMillis() + xdsConfigProperties.getCertValidityTimeS() * 1000;
            stub.createCertificate(
                IstioCertificateRequest.newBuilder().setCsr(csr)
                    .setValidityDuration(xdsConfigProperties.getCertValidityTimeS())
                    .build(),
                new StreamObserver<IstioCertificateResponse>() {
                    @Override
                    public void onNext(
                        IstioCertificateResponse istioCertificateResponse) {
                        final int n = istioCertificateResponse.getCertChainCount();
                        List<Certificate> certificates = new ArrayList<>();
                        StringBuilder certChainRaw = new StringBuilder();

                        for (int i = 0; i < n; ++i) {
                            String rawCert = istioCertificateResponse.getCertChain(i);
                            certChainRaw.append(rawCert);
                            certificates.add(CertificateUtil.loadCertificate(rawCert));
                        }

                        newCertPair.setExpireTime(certExpireTime);
                        newCertPair.setRawCertificateChain(certChainRaw.toString().getBytes(StandardCharsets.UTF_8));
                        newCertPair.setCertificateChain(certificates.toArray(new Certificate[certificates.size()]));
                        RecordLog.info("[XdsDataSource] Send CSR to CA successfully", n);
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        RecordLog.info("[XdsDataSource] Send CSR to CA error", throwable);
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        if (countDownLatch.getCount() > 0) {
                            countDownLatch.countDown();
                        }
                    }
                });
            try {
                countDownLatch.await(xdsConfigProperties.getGetCertTimeoutS(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                RecordLog.error("[XdsDataSource] Wait for cert failed.", e);
            } finally {
                channel.shutdown();
            }
            if (0 == newCertPair.getExpireTime()) {
                return null;
            }
            return newCertPair;
        } catch (Exception e) {
            RecordLog.error("[XdsDataSource] Unable to get cert pair", e);
        }
        return null;
    }

}
