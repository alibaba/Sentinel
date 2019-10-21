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

package com.alibaba.acm.shaded.com.aliyuncs.kms.transform.v20160120;

import com.alibaba.acm.shaded.com.aliyuncs.kms.model.v20160120.CreateKeyResponse;
import com.alibaba.acm.shaded.com.aliyuncs.kms.model.v20160120.CreateKeyResponse.KeyMetadata;
import com.alibaba.acm.shaded.com.aliyuncs.transform.UnmarshallerContext;


public class CreateKeyResponseUnmarshaller {

	public static CreateKeyResponse unmarshall(CreateKeyResponse createKeyResponse, UnmarshallerContext context) {
		
		createKeyResponse.setRequestId(context.stringValue("CreateKeyResponse.RequestId"));

		KeyMetadata keyMetadata = new KeyMetadata();
		keyMetadata.setCreationDate(context.stringValue("CreateKeyResponse.KeyMetadata.CreationDate"));
		keyMetadata.setDescription(context.stringValue("CreateKeyResponse.KeyMetadata.Description"));
		keyMetadata.setKeyId(context.stringValue("CreateKeyResponse.KeyMetadata.KeyId"));
		keyMetadata.setKeyState(context.stringValue("CreateKeyResponse.KeyMetadata.KeyState"));
		keyMetadata.setKeyUsage(context.stringValue("CreateKeyResponse.KeyMetadata.KeyUsage"));
		keyMetadata.setDeleteDate(context.stringValue("CreateKeyResponse.KeyMetadata.DeleteDate"));
		keyMetadata.setCreator(context.stringValue("CreateKeyResponse.KeyMetadata.Creator"));
		keyMetadata.setArn(context.stringValue("CreateKeyResponse.KeyMetadata.Arn"));
		keyMetadata.setOrigin(context.stringValue("CreateKeyResponse.KeyMetadata.Origin"));
		keyMetadata.setMaterialExpireTime(context.stringValue("CreateKeyResponse.KeyMetadata.MaterialExpireTime"));
		createKeyResponse.setKeyMetadata(keyMetadata);
	 
	 	return createKeyResponse;
	}
}