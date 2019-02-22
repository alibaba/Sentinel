package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenEnvClusterDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;

/****
 * 拉取规则
 * @author Fx_demon
 * @param <T>
 */
public interface BaseDynamicRuleApolloProvider<T extends RuleEntity> extends DynamicRuleProvider<List<T>> {

	ApolloOpenApiClient getConfigService();

    Converter<String, List<T>> getDecoder();

    String getKey();
    
    @Override
    default List<T> getRules(String appName) throws Exception {
        AssertUtil.assertNotBlank(this.getKey(), "key cannot be blank");

        String appId = appName;
		String env = ApolloConfigUtil.env;
	    String clusterName =  ApolloConfigUtil.clusterName;
	    String namespaceName = ApolloConfigUtil.namespaceName;
	    OpenNamespaceDTO openNameSpaceDTO = getConfigService().getNamespace(appId, env, clusterName, namespaceName);
	    
        String rules = "";
        for(OpenItemDTO item:openNameSpaceDTO.getItems()) {
	    	System.out.println("key:"+item.getKey()+ "\nvalue:" + item.getValue());
	    	if((this.getKey()).equals(item.getKey()) ) {
	    		rules = item.getValue();
	    	}
	    }
        
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }

        return this.getDecoder().convert(rules);
    }
}
