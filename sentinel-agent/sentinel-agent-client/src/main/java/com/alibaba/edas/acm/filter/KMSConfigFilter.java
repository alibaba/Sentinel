package com.alibaba.edas.acm.filter;

import com.alibaba.acm.shaded.com.aliyuncs.DefaultAcsClient;
import com.alibaba.acm.shaded.com.aliyuncs.auth.AlibabaCloudCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.AlibabaCloudCredentialsProvider;
import com.alibaba.acm.shaded.com.aliyuncs.auth.BasicSessionCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.InstanceProfileCredentialsProvider;
import com.alibaba.acm.shaded.com.aliyuncs.auth.StaticCredentialsProvider;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;
import com.alibaba.acm.shaded.com.aliyuncs.http.MethodType;
import com.alibaba.acm.shaded.com.aliyuncs.http.ProtocolType;
import com.alibaba.acm.shaded.com.aliyuncs.kms.model.v20160120.DecryptRequest;
import com.alibaba.acm.shaded.com.aliyuncs.kms.model.v20160120.EncryptRequest;
import com.alibaba.acm.shaded.com.aliyuncs.kms.model.v20160120.GenerateDataKeyRequest;
import com.alibaba.acm.shaded.com.aliyuncs.kms.model.v20160120.GenerateDataKeyResponse;
import com.alibaba.acm.shaded.com.aliyuncs.profile.DefaultProfile;
import com.alibaba.acm.shaded.com.aliyuncs.profile.IClientProfile;
import com.taobao.diamond.exception.DiamondException;
import com.taobao.diamond.manager.IConfigFilterChain;
import com.taobao.diamond.manager.IConfigRequest;
import com.taobao.diamond.manager.IConfigResponse;
import com.taobao.diamond.manager.IFilterConfig;
import com.taobao.diamond.manager.impl.ConfigRequest;
import com.taobao.diamond.manager.impl.ConfigResponse;
import com.taobao.diamond.utils.AESUtils;
import com.taobao.diamond.utils.StringUtils;
import com.alibaba.acm.shaded.org.json.JSONObject;
import com.alibaba.acm.shaded.org.json.JSONTokener;

import static com.taobao.diamond.common.Constants.CIPHER_KMS_AES_128_PREFIX;
import static com.taobao.diamond.common.Constants.CIPHER_PREFIX;
import static com.taobao.diamond.common.Constants.KMS_KEY_SPEC_AES_128;

public class KMSConfigFilter implements IACMConfigFilter {

	@Override
	public void doFilter(IConfigRequest request, IConfigResponse response, IConfigFilterChain filterChain) throws DiamondException {
		String dataId = null;
		String group = null;
		try {
			ConfigRequest requestTmp = (ConfigRequest) request;
			ConfigResponse responseTmp = (ConfigResponse) response;
			
			if (request != null && requestTmp.getDataId().startsWith(CIPHER_PREFIX)) {

				dataId = requestTmp.getDataId();
				group = requestTmp.getGroup();

				if (requestTmp.getContent() != null) {
					requestTmp.setContent(encrypt(keyId, requestTmp));
				}
			}
			
			filterChain.doFilter(requestTmp, responseTmp);

			if (responseTmp != null && responseTmp.getDataId().startsWith(CIPHER_PREFIX)) {

				dataId = responseTmp.getDataId();
				group = responseTmp.getGroup();

				if (responseTmp.getContent() != null) {
					responseTmp.setContent(decrypt(responseTmp));
				}
			}
		} catch (ClientException e) {
			String message = String.format("KMS error, dataId: %s, groupId: %s", dataId, group);
			throw new DiamondException(500, message, e);
		} catch (Exception e) {
			throw new DiamondException(e);
		}
	}

	private DefaultAcsClient kmsClient(String regionId, String accessKeyId, String accessKeySecret) {
		IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
		return new DefaultAcsClient(profile);
	}

	/**
	 * KMS 所需的 STS Token 让 aliyun-java-sdk-core 自动管理其周期
	 * https://help.aliyun.com/document_detail/59946.html
	 * https://help.aliyun.com/document_detail/59919.html
	 * 而与通过 STS Token 访问 ACM 的地方（diamond-client）我们自己维护周期，不引入 aliyun-java-sdk-core 的依赖
	 */
	private DefaultAcsClient kmsClient(String regionId, String ramRoleName) {
		IClientProfile profile = DefaultProfile.getProfile(regionId);
		AlibabaCloudCredentialsProvider alibabaCloudCredentialsProvider = new InstanceProfileCredentialsProvider(
			ramRoleName);
		return new DefaultAcsClient(profile, alibabaCloudCredentialsProvider);
	}

	private String decrypt(ConfigResponse configResponse) throws Exception {
		String dataId = configResponse.getDataId();

		if (dataId.startsWith(CIPHER_KMS_AES_128_PREFIX)) {
			String encryptedDataKey = configResponse.getEncryptedDataKey();
			if (!StringUtils.isBlank(encryptedDataKey)) {
				String dataKey = decrypt(encryptedDataKey);
				return AESUtils.decrypt(configResponse.getContent(), dataKey, "UTF-8");
			}
		}

		return decrypt(configResponse.getContent());
	}

	private String decrypt(String content) throws ClientException {
		final DecryptRequest decReq = new DecryptRequest();
		decReq.setProtocol(ProtocolType.HTTPS);
		decReq.setAcceptFormat(FormatType.JSON);
		decReq.setMethod(MethodType.POST);
		decReq.setCiphertextBlob(content);
		return kmsClient.getAcsResponse(decReq).getPlaintext();
	}

	private String encrypt(String keyId, ConfigRequest configRequest) throws Exception {
		String dataId = configRequest.getDataId();

		if (dataId.startsWith(CIPHER_KMS_AES_128_PREFIX)) {
			GenerateDataKeyResponse generateDataKeyResponse = generateDataKey(keyId, KMS_KEY_SPEC_AES_128);
			configRequest.setEncryptedDataKey(generateDataKeyResponse.getCiphertextBlob());
			String dataKey = generateDataKeyResponse.getPlaintext();
			return AESUtils.encrypt(configRequest.getContent(), dataKey, "UTF-8");
		}

		return encrypt(keyId, configRequest.getContent());
	}

	private GenerateDataKeyResponse generateDataKey(String keyId, String keySpec) throws ClientException {
		GenerateDataKeyRequest generateDataKeyRequest = new GenerateDataKeyRequest();

		generateDataKeyRequest.setProtocol(ProtocolType.HTTPS);
		generateDataKeyRequest.setAcceptFormat(FormatType.JSON);
		generateDataKeyRequest.setMethod(MethodType.POST);

		generateDataKeyRequest.setKeyId(keyId);
		generateDataKeyRequest.setKeySpec(keySpec);
		return kmsClient.getAcsResponse(generateDataKeyRequest);
	}

	private String encrypt(String keyId, String plainText) throws ClientException {
		final EncryptRequest encReq = new EncryptRequest();
		encReq.setProtocol(ProtocolType.HTTPS);
		encReq.setAcceptFormat(FormatType.JSON);
		encReq.setMethod(MethodType.POST);
		encReq.setKeyId(keyId);
		encReq.setPlaintext(plainText);
		return kmsClient.getAcsResponse(encReq).getCiphertextBlob();
	}

	@Override
	public void init(IFilterConfig filterConfig) {
		keyId = (String) filterConfig.getInitParameter("keyId");
		String regionId = (String) filterConfig.getInitParameter("regionId");
		String ramRoleName = (String)filterConfig.getInitParameter("ramRoleName");
		String securityCredentials = (String)filterConfig.getInitParameter("securityCredentials");

		if (!StringUtils.isBlank(securityCredentials)) {
			// 用户自己维护 securityCredentials 的更新
			initKMSClientBySecurityCredentials(regionId, securityCredentials);
		} else if (!StringUtils.isBlank(ramRoleName)) {
			// 通过 ECS 实例 RAM 角色访问 ACM，比 AK/SK 的优先级高
			kmsClient = kmsClient(regionId, ramRoleName);
		} else {
			String accessKey = (String) filterConfig.getInitParameter("accessKey");
			String secretKey = (String) filterConfig.getInitParameter("secretKey");
			kmsClient = kmsClient(regionId, accessKey, secretKey);
		}

		Object orderObject = filterConfig.getInitParameter("order");
		if (orderObject != null) {
			order = (Integer) orderObject;
		}
	}

	private void initKMSClientBySecurityCredentials(String regionId, String securityCredentials) {
		JSONTokener jsonTokener = new JSONTokener(securityCredentials);
		JSONObject jsonObject = new JSONObject(jsonTokener);
		String accessKeyId = jsonObject.getString("AccessKeyId");
		String accessKeySecret = jsonObject.getString("AccessKeySecret");
		String securityToken = jsonObject.getString("SecurityToken");

		IClientProfile profile = DefaultProfile.getProfile(regionId);
		AlibabaCloudCredentials cloudCredentials = new BasicSessionCredentials(accessKeyId, accessKeySecret,
            securityToken);
		AlibabaCloudCredentialsProvider alibabaCloudCredentialsProvider = new StaticCredentialsProvider(
            cloudCredentials);
		kmsClient = new DefaultAcsClient(profile, alibabaCloudCredentialsProvider);
	}

	@Override
	public void deploy() {
		kmsClient = null;
	}
	
	private DefaultAcsClient kmsClient;
	private String keyId;
	private int order = 100;

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public String getFilterName() {
		return this.getClass().getName();
	}

}
