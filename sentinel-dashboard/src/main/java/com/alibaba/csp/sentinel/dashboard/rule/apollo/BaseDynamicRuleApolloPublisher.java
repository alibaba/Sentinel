package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

/***
 * 推送规则 for Apollo 
 * @author Fx_demon
 * @param <T>
 */
public interface BaseDynamicRuleApolloPublisher<T extends RuleEntity> extends DynamicRulePublisher<List<T>> {

	ApolloOpenApiClient getConfigService();

    Converter<List<T>, String> getEncoder();

    String getKey();

    @Override
    default void publish(String appName, List<T> rules) throws Exception {
        AssertUtil.assertNotBlank(appName, "app name cannot be blank");
        AssertUtil.assertNotBlank(this.getKey(), "key cannot be blank");

        if (rules == null) {
            return;
        }

	    String appId = appName;
		String env = ApolloConfigUtil.env;
	    String clusterName =  ApolloConfigUtil.clusterName;
	    String namespaceName = ApolloConfigUtil.namespaceName;
        
        OpenItemDTO itemDTO = new OpenItemDTO();
	    itemDTO.setKey( this.getKey() );
	    itemDTO.setValue( this.getEncoder().convert(rules) );
	    itemDTO.setDataChangeCreatedBy("apollo");
	    itemDTO.setDataChangeCreatedTime(new Date());
	    itemDTO.setDataChangeLastModifiedTime(new Date());
	    itemDTO.setDataChangeLastModifiedBy("apollo");
	    
	    
	    OpenNamespaceDTO openNameSpaceDTO = getConfigService().getNamespace(appId, env, clusterName, namespaceName);
	    boolean keyExit = false;
        for(OpenItemDTO item:openNameSpaceDTO.getItems()) {
	    	if((this.getKey()).equals(item.getKey()) ) {
	    		keyExit = true;
	    		break;
	    	}
	    }
	    
        if(keyExit) {
        	//修改规则
    	    getConfigService().updateItem(appId, env, clusterName, namespaceName, itemDTO);
        }else {
        	//新增规则
    	    getConfigService().createItem(appId, env, clusterName, namespaceName, itemDTO);
        }
	    
        if(ApolloConfigUtil.releaseEnabled) {
        	NamespaceReleaseDTO releaseDTO = new NamespaceReleaseDTO();
    	    releaseDTO.setEmergencyPublish(true);
    	    releaseDTO.setReleaseComment("");
    	    releaseDTO.setReleasedBy("apollo");
    	    releaseDTO.setReleaseTitle((new Date()).toLocaleString());
    	    
    		//发布规则
    	    getConfigService().publishNamespace(appId, env, clusterName, namespaceName, releaseDTO );
        }
        
    }
}