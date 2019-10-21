package com.alibaba.acm.shaded.com.aliyuncs;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;
import com.alibaba.acm.shaded.com.aliyuncs.http.MethodType;
import com.alibaba.acm.shaded.com.aliyuncs.http.ProtocolType;
import com.alibaba.acm.shaded.com.aliyuncs.regions.ProductDomain;

public class CommonRequest {

    private String                    version         = null;
    private String                    product         = null;
    private String                    action          = null;
    private String                    locationProduct = null;
    private String                    endpointType    = null;
    private String                    regionId        = null;
    private ProtocolType              protocol        = null;
    private final Map<String, String> queryParameters = new HashMap<String, String>();
    private final Map<String, String> bodyParameters  = new HashMap<String, String>();
    private final Map<String, String> headParameters  = new HashMap<String, String>();

    private Integer                   connectTimeout  = null;
    private Integer                   readTimeout     = null;
    private MethodType                method          = null;
    private FormatType                httpContentType = null;
    private byte[]                    httpContent     = null;
    private String                    encoding        = null;

    private String                    uriPattern      = null;
    private Map<String, String>       pathParameters  = new HashMap<String, String>();

    private String                    domain          = null;

    @SuppressWarnings("rawtypes")
    public AcsRequest buildRequest() {
        if (uriPattern != null) {
            CommonRoaRequest request = new CommonRoaRequest(product);
            request.setUriPattern(uriPattern);
            for (String pathParamKey : pathParameters.keySet()) {
                request.putPathParameter(pathParamKey, pathParameters.get(pathParamKey));
            }
            fillParams(request);

            return request;
        } else {
            CommonRpcRequest request = new CommonRpcRequest(product);
            fillParams(request);

            return request;
        }
    }

    @SuppressWarnings("rawtypes")
    private void fillParams(AcsRequest request) {
        request.putHeaderParameter("x-sdk-invoke-type", "common");

        if (version != null) {
            request.setVersion(version);
        }
        if (action != null) {
            request.setActionName(action);
        }
        if (regionId != null) {
            request.setRegionId(regionId);
        }
        if (locationProduct != null) {
            request.setLocationProduct(locationProduct);
        }
        if (endpointType != null) {
            request.setEndpointType(endpointType);
        }
        if (connectTimeout != null) {
            request.setConnectTimeout(connectTimeout);
        }
        if (readTimeout != null) {
            request.setReadTimeout(readTimeout);
        }
        if (method != null) {
            request.setMethod(method);
        }
        if (protocol != null) {
            request.setProtocol(protocol);
        }
        if (domain != null) {
            ProductDomain productDomain = new ProductDomain(product, domain);
            request.setProductDomain(productDomain);
        }
        if (httpContent != null) {
            request.setHttpContent(httpContent, encoding, httpContentType);
        }
        for (String queryParamKey : queryParameters.keySet()) {
            request.putQueryParameter(queryParamKey, queryParameters.get(queryParamKey));
        }
        for (String bodyParamKey : bodyParameters.keySet()) {
            request.putBodyParameter(bodyParamKey, bodyParameters.get(bodyParamKey));
        }
        for (String headParamKey : headParameters.keySet()) {
            request.putHeaderParameter(headParamKey, headParameters.get(headParamKey));
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLocationProduct() {
        return locationProduct;
    }

    public void setLocationProduct(String locationProduct) {
        this.locationProduct = locationProduct;
    }

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }

    public void putBodyParameter(String name, Object value) {
        setParameter(this.bodyParameters, name, value);
    }

    public void putQueryParameter(String name, String value) {
        setParameter(this.queryParameters, name, value);
    }

    public void putHeadParameter(String name, String value) {
        setParameter(this.headParameters, name, value);
    }

    private void setParameter(Map<String, String> map, String name, Object value) {
        if (null == map || null == name || null == value) {
            return;
        }
        map.put(name, String.valueOf(value));
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public MethodType getMethod() {
        return method;
    }

    public void setMethod(MethodType method) {
        this.method = method;
    }

    public String getUriPattern() {
        return uriPattern;
    }

    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
    }

    public void putPathParameter(String name, String value) {
        setParameter(this.pathParameters, name, value);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setHttpContent(byte[] content, String encoding, FormatType format) {
        if (content == null || encoding == null || format == null) {
            return;
        }
        this.httpContent = content;
        this.httpContentType = format;
        this.encoding = encoding;
    }

}
