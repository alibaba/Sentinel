package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import java.util.Date;
import java.util.List;

import org.apache.http.impl.client.CloseableHttpClient;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.constant.ApolloOpenApiConstants;
import com.ctrip.framework.apollo.openapi.client.service.AppOpenApiService;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenEnvClusterDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/***
 * ApolloOpenApiClient 
 * @author Fx_demon
 */
public final class ApolloOpenApiUtilTest {
	
	static String serverAddr = "http://localhost:8070"; // portal url
	static int readTimeout = 3000;
	static String token = "55ce8ffc6ad01daddab26f11913fa4a22a9c669b"; // 申请的token -本应用：sentinel_ds
	static ApolloOpenApiClient client = ApolloOpenApiClient.newBuilder()
	                                                .withPortalUrl(serverAddr)
	                                                .withToken(token)
	                                                .withReadTimeout(readTimeout)
	                                                .build();
	
	public static void main(String[] args) {
		String appId = "SentinelApp";
		String env = "DEV";
	    String clusterName = "default";
	    String namespaceName = "application";
	    OpenNamespaceDTO openNameSpaceDTO = client.getNamespace(appId, env, clusterName, namespaceName);
	    List<OpenEnvClusterDTO> openEnvClusterDTOList = client.getEnvClusterInfo(appId);
//	    AppOpenApiService appService = client.app
	    System.out.println("openNameSpaceDTO:"+openNameSpaceDTO.getAppId()+"||"+openNameSpaceDTO.getItems().size());
	    
	    for(OpenItemDTO item:openNameSpaceDTO.getItems()) {
	    	System.out.println("key:"+item.getKey()+ "\nvalue:" + item.getValue());
	    }
	    
	    
	    OpenItemDTO itemDTO = new OpenItemDTO();
	    itemDTO.setKey("testFlowRule11");
	    itemDTO.setValue("000");
	    itemDTO.setDataChangeCreatedBy("apollo");
	    itemDTO.setDataChangeCreatedTime(new Date());
	    itemDTO.setDataChangeLastModifiedTime(new Date());
	    itemDTO.setDataChangeLastModifiedBy("apollo");
	    
	    /*
		//新增规则
	    client.createItem(appId, env, clusterName, namespaceName, itemDTO);
	    //修改规则
	    itemDTO.setValue("00011");
	    client.updateItem(appId, env, clusterName, namespaceName, itemDTO);
	    
	   //删除规则
	    String key = "testFlowRule";
	    String operator = "apollo";
		client.removeItem(appId, env, clusterName, namespaceName, key, operator); 
		*/
	    
	     
	    NamespaceReleaseDTO releaseDTO = new NamespaceReleaseDTO();
	    releaseDTO.setEmergencyPublish(true);
	    releaseDTO.setReleaseComment("");
	    releaseDTO.setReleasedBy("apollo");
	    releaseDTO.setReleaseTitle((new Date()).toLocaleString());
	    
		//发布规则
	    client.publishNamespace(appId, env, clusterName, namespaceName, releaseDTO );
	    
	}
	 
}
