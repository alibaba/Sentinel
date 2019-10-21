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
public class CreateKeyRequest extends RpcAcsRequest<CreateKeyResponse> {
	
	public CreateKeyRequest() {
		super("Kms", "2016-01-20", "CreateKey", "kms");
		setProtocol(ProtocolType.HTTPS);
	}

	private String keyUsage;

	private String origin;

	private String description;

	private String sTSToken;

	public String getKeyUsage() {
		return this.keyUsage;
	}

	public void setKeyUsage(String keyUsage) {
		this.keyUsage = keyUsage;
		if(keyUsage != null){
			putQueryParameter("KeyUsage", keyUsage);
		}
	}

	public String getOrigin() {
		return this.origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
		if(origin != null){
			putQueryParameter("Origin", origin);
		}
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
		if(description != null){
			putQueryParameter("Description", description);
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
	public Class<CreateKeyResponse> getResponseClass() {
		return CreateKeyResponse.class;
	}

}
