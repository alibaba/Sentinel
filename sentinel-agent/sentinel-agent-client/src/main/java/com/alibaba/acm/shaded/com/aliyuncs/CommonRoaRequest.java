package com.alibaba.acm.shaded.com.aliyuncs;

import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;

public class CommonRoaRequest extends RoaAcsRequest<CommonResponse> {

    public CommonRoaRequest(String product) {
        super(product);
        setAcceptFormat(FormatType.JSON);
    }
    
    public CommonRoaRequest(String product, String version, String action) {
        super(product, version, action);
        setAcceptFormat(FormatType.JSON);
    }

    public CommonRoaRequest(String product, String version, String action, String locationProduct) {
        super(product, version, action, locationProduct);
        setAcceptFormat(FormatType.JSON);
    }

    public CommonRoaRequest(String product, String version, String action, String locationProduct,
                            String endpointType) {
        super(product, version, action, locationProduct, endpointType);
        setAcceptFormat(FormatType.JSON);
    }

    @Override
    public Class<CommonResponse> getResponseClass() {
        return CommonResponse.class;
    }
}
