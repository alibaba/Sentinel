/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.acm.shaded.com.aliyuncs.kms.model.v20160120;

import com.alibaba.acm.shaded.com.aliyuncs.RpcAcsRequest;
import com.alibaba.acm.shaded.com.aliyuncs.http.ProtocolType;

/**
 * @author auto create
 * @version 
 */
public class EnableKeyRequest extends RpcAcsRequest<EnableKeyResponse> {
	
	public EnableKeyRequest() {
		super("Kms", "2016-01-20", "EnableKey", "kms");
		setProtocol(ProtocolType.HTTPS);
	}

	private String keyId;

	private String sTSToken;

	public String getKeyId() {
		return this.keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
		if(keyId != null){
			putQueryParameter("KeyId", keyId);
		}
	}

	public String getSTSToken() {
		return this.sTSToken;
	}

	public void setSTSToken(String sTSToken) {
		this.sTSToken = sTSToken;
		if(sTSToken != null){
			putQueryParameter("STSToken", sTSToken);
		}
	}

	@Override
	public Class<EnableKeyResponse> getResponseClass() {
		return EnableKeyResponse.class;
	}

}
