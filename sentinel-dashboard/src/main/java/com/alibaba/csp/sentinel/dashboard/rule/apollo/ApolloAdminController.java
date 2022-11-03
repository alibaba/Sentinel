package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenAppDTO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author FengJianxin
 * @since 1.8.6
 */
@RestController
@RequestMapping(value = "/admin/apollo")
public class ApolloAdminController {

    private static final Logger LOG = LoggerFactory.getLogger(ApolloAdminController.class);

    @Resource
    private ApolloProperties apolloProperties;
    @Resource
    private ApolloOpenApiClientProvider apolloOpenApiClientProvider;


    /**
     * 动态更新 apollo openapi 参数
     *
     * @param portalUrl
     * @param token
     */
    @RequestMapping("/openapi/modify")
    public Result<?> openApi(String portalUrl, String token) {
        if (StringUtils.isBlank(portalUrl) && StringUtils.isBlank(token)) {
            return Result.ofFail(-1, "parameter is empty");
        }
        if (StringUtils.isBlank(portalUrl)) {
            portalUrl = apolloProperties.getPortalUrl();
        }
        if (StringUtils.isBlank(token)) {
            token = apolloProperties.getToken();
        }
        ApolloOpenApiClient apolloOpenApiClient = apolloOpenApiClientProvider.set(portalUrl, token);
        try {
            List<OpenAppDTO> authorizedApps = apolloOpenApiClient.getAuthorizedApps();
            return Result.ofSuccess(authorizedApps);
        } catch (Exception e) {
            LOG.error("openapi settings error - portalUrl: {}, token: {}", portalUrl, token, e);
            return Result.ofFail(-1, "openapi 参数有误，请及时更正：" + e.getMessage());
        }
    }


}
