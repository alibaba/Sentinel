/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.alibaba.acm.shaded.com.aliyuncs.endpoint;

import com.alibaba.acm.shaded.com.aliyuncs.auth.AlibabaCloudCredentialsProvider;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ErrorCodeConstant;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ErrorMessageConstant;
import com.alibaba.acm.shaded.com.aliyuncs.profile.IClientProfile;

import java.io.InputStream;
import java.util.*;

public class ChainedEndpointResolver implements EndpointResolver {
    private static final String REGION_LIST_FILE = "regions.txt";
    protected List<EndpointResolverBase> endpointResolvers;

    public ChainedEndpointResolver(List<EndpointResolverBase> resolverChain) {
        this.endpointResolvers = resolverChain;
    }

    private void checkProductCode(String productCode) throws ClientException {
        boolean productCodeValid = false;
        for (EndpointResolverBase resolver : endpointResolvers) {
            if (resolver.isProductCodeValid(productCode.toLowerCase()))
                productCodeValid = true;
        }

        if (!productCodeValid) {
            throw new ClientException(
                    ErrorCodeConstant.SDK_ENDPOINT_RESOLVING_ERROR,
                    String.format(ErrorMessageConstant.ENDPOINT_NO_PRODUCT, productCode)
            );
        }
    }

    private void checkRegionId(String regionId) throws ClientException {
        boolean regionIdValid = false;
        for (EndpointResolverBase resolver : endpointResolvers) {
            if (resolver.isRegionIdValid(regionId))
                regionIdValid = true;
        }

        if (!regionIdValid) {
            throw new ClientException(
                    ErrorCodeConstant.SDK_ENDPOINT_RESOLVING_ERROR,
                    String.format(ErrorMessageConstant.INVALID_REGION_ID, regionId)
            );
        }
    }

    private String getAvailableRegionsHint(String productCode) {
        Set<String> availabeRegions = null;
        String availabeRegionsHint = "";
        for (EndpointResolverBase resolver : endpointResolvers) {
            availabeRegions = resolver.getValidRegionIdsByProduct(productCode);
            if (availabeRegions != null) {
                availabeRegionsHint = "\nOr you can use the other available regions:";
                for (String availabeRegion: availabeRegions) {
                    availabeRegionsHint += " " + availabeRegion;
                }
                break;
            }
        }
        return availabeRegionsHint;
    }

    public String resolve(ResolveEndpointRequest request) throws ClientException {
        for (EndpointResolverBase resolver : endpointResolvers) {
            String endpoint = resolver.resolve(request);
            if (endpoint != null) {
                return endpoint;
            }
        }

        checkProductCode(request.productCode);
        checkRegionId(request.regionId);

        throw new ClientException(
                ErrorCodeConstant.SDK_ENDPOINT_RESOLVING_ERROR,
                String.format(
                        ErrorMessageConstant.ENDPOINT_NO_REGION,
                        request.regionId,
                        request.productCode,
                        getAvailableRegionsHint(request.productCode)
                )
        );
    }
}
