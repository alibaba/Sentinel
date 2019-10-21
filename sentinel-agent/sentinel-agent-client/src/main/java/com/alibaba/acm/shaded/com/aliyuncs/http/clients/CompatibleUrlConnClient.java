package com.alibaba.acm.shaded.com.aliyuncs.http.clients;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;

import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.http.CallBack;
import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpClientConfig;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpRequest;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpResponse;
import com.alibaba.acm.shaded.com.aliyuncs.http.IHttpClient;
import com.alibaba.acm.shaded.com.aliyuncs.http.MethodType;

/**
 * @author VK.Gao
 * @date 2018/03/28
 */
public class CompatibleUrlConnClient extends IHttpClient {

    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String CONTENT_MD5 = "Content-MD5";
    protected static final String CONTENT_LENGTH = "Content-Length";
    protected static final String ACCEPT_ENCODING = "Accept-Encoding";

    private SSLSocketFactory sslSocketFactory;

    public CompatibleUrlConnClient(HttpClientConfig clientConfig) throws ClientException {
        super(clientConfig);
    }

    protected void init(HttpClientConfig clientConfig) throws ClientException {
        this.sslSocketFactory = clientConfig.getSslSocketFactory();
        if (clientConfig.isIgnoreSSLCerts()) {
            this.ignoreSSLCertificate();
        }
    }

    @Override
    public HttpResponse syncInvoke(HttpRequest request) throws IOException {
        OutputStream out = null;
        InputStream content = null;
        HttpResponse response = null;
        HttpURLConnection httpConn = buildHttpConnection(request);

        try {
            httpConn.connect();
            if (null != request.getHttpContent() && request.getHttpContent().length > 0) {
                out = httpConn.getOutputStream();
                out.write(request.getHttpContent());
            }
            content = httpConn.getInputStream();
            response = new HttpResponse(httpConn.getURL().toString());
            parseHttpConn(response, httpConn, content);
            return response;
        } catch (IOException e) {
            content = httpConn.getErrorStream();
            response = new HttpResponse(httpConn.getURL().toString());
            parseHttpConn(response, httpConn, content);
            return response;
        } finally {
            if (content != null) { content.close(); }
            httpConn.disconnect();
        }
    }

    @Override
    public Future<HttpResponse> asyncInvoke(HttpRequest apiRequest, CallBack callback) throws IOException {
        throw new IllegalStateException("not supported");
    }

    public static HttpResponse compatibleGetResponse(HttpRequest request) throws IOException, ClientException {
        return new CompatibleUrlConnClient(null).syncInvoke(request);
    }

    private HttpURLConnection buildHttpConnection(HttpRequest request) throws IOException {
        String strUrl = request.getUrl();

        if (null == strUrl) {
            throw new IllegalArgumentException("URL is null for HttpRequest.");
        }
        if (null == request.getMethod()) {
            throw new IllegalArgumentException("Method is not set for HttpRequest.");
        }
        URL url = null;
        String[] urlArray = null;
        if (MethodType.POST.equals(request.getMethod()) && null == request.getHttpContent()) {
            urlArray = strUrl.split("\\?");
            url = new URL(urlArray[0]);
        } else {
            url = new URL(strUrl);
        }
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        HttpURLConnection httpConn = null;
        if (url.getProtocol().equalsIgnoreCase("https")) {
            if (sslSocketFactory != null) {
                Proxy proxy = getProxy("HTTPS_PROXY", request);
                HttpsURLConnection httpsConn = (HttpsURLConnection)url.openConnection(proxy);
                httpsConn.setSSLSocketFactory(sslSocketFactory);
                httpConn = httpsConn;
            }
        }

        if (httpConn == null) {
            Proxy proxy = getProxy("HTTP_PROXY", request);
            httpConn = (HttpURLConnection)url.openConnection(proxy);
        }

        httpConn.setRequestMethod(request.getMethod().toString());
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setUseCaches(false);

        if (request.getConnectTimeout() != null) {
            httpConn.setConnectTimeout(request.getConnectTimeout());
        }

        if (request.getReadTimeout() != null) {
            httpConn.setReadTimeout(request.getReadTimeout());
        }

        Map<String, String> mappedHeaders = request.getHeaders();
        httpConn.setRequestProperty(ACCEPT_ENCODING, "identity");
        for (Entry<String, String> entry : mappedHeaders.entrySet()) {
            httpConn.setRequestProperty(entry.getKey(), entry.getValue());
        }

        if (null != request.getHeaderValue(CONTENT_TYPE)) {
            httpConn.setRequestProperty(CONTENT_TYPE, request.getHeaderValue(CONTENT_TYPE));
        } else {
            String contentTypeValue = request.getContentTypeValue(request.getHttpContentType(), request.getEncoding());
            if (null != contentTypeValue) {
                httpConn.setRequestProperty(CONTENT_TYPE, contentTypeValue);
            }
        }

        if (MethodType.POST.equals(request.getMethod()) && null != urlArray && urlArray.length == 2) {
            httpConn.getOutputStream().write(urlArray[1].getBytes());
        }

        return httpConn;
    }

    private void parseHttpConn(HttpResponse response, HttpURLConnection httpConn,
                               InputStream content) throws IOException {
        byte[] buff = readContent(content);
        response.setStatus(httpConn.getResponseCode());
        Map<String, List<String>> headers = httpConn.getHeaderFields();
        for (Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (null == key) { continue; }
            List<String> values = entry.getValue();
            StringBuilder builder = new StringBuilder(values.get(0));
            for (int i = 1; i < values.size(); i++) {
                builder.append(",");
                builder.append(values.get(i));
            }
            response.putHeaderParameter(key, builder.toString());
        }
        String type = response.getHeaderValue("Content-Type");
        if (null != buff && null != type) {
            response.setEncoding("UTF-8");
            String[] split = type.split(";");
            response.setHttpContentType(FormatType.mapAcceptToFormat(split[0].trim()));
            if (split.length > 1 && split[1].contains("=")) {
                String[] codings = split[1].split("=");
                response.setEncoding(codings[1].trim().toUpperCase());
            }
        }
        response.setStatus(httpConn.getResponseCode());
        response.setHttpContent(buff, response.getEncoding(),
            response.getHttpContentType());
    }

    private byte[] readContent(InputStream content)
        throws IOException {
        if (content == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];

        while (true) {
            final int read = content.read(buff);
            if (read == -1) { break; }
            outputStream.write(buff, 0, read);
        }

        return outputStream.toByteArray();
    }

    @Override
    public void ignoreSSLCertificate() {
        HttpsCertIgnoreHelper.ignoreSSLCertificate();
    }

    @Override
    public void restoreSSLCertificate() {
        HttpsCertIgnoreHelper.restoreSSLCertificate();
    }

    @Override
    public void close() throws IOException {

    }

    private Proxy getProxy(String env, HttpRequest request) throws MalformedURLException, UnsupportedEncodingException {
        Proxy proxy = Proxy.NO_PROXY;
        String httpProxy = System.getenv(env);
        if (httpProxy != null) {
            URL proxyUrl = new URL(httpProxy);
            String userInfo = proxyUrl.getUserInfo();
            if (userInfo != null) {
                byte[] bytes = userInfo.getBytes("UTF-8");
                String auth = DatatypeConverter.printBase64Binary(bytes);
                request.putHeaderParameter("Proxy-Authorization", "Basic " + auth);
            }
            String hostname = proxyUrl.getHost();
            int port = proxyUrl.getPort();
            if (port == -1) {
                port = proxyUrl.getDefaultPort();
            }
            SocketAddress addr = new InetSocketAddress(hostname, port);
            proxy = new Proxy(Proxy.Type.HTTP, addr);
        }
        return proxy;
    }

    public static final class HttpsCertIgnoreHelper implements X509TrustManager, HostnameVerifier {

        private static HostnameVerifier defaultVerifier;
        private static SSLSocketFactory defaultSSLFactory;

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public static void restoreSSLCertificate() {
            if (null != defaultSSLFactory) {
                HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLFactory);
                HttpsURLConnection.setDefaultHostnameVerifier(defaultVerifier);
            }
        }

        public static void ignoreSSLCertificate() {
            try {
                HttpsCertIgnoreHelper trustAll = new HttpsCertIgnoreHelper();
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[] {trustAll}, new java.security.SecureRandom());
                if (null == defaultSSLFactory) {
                    defaultSSLFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
                    defaultVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
                }
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(trustAll);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed setting up all thrusting certificate manager.", e);
            } catch (KeyManagementException e) {
                throw new RuntimeException("Failed setting up all thrusting certificate manager.", e);
            }
        }
    }
}
