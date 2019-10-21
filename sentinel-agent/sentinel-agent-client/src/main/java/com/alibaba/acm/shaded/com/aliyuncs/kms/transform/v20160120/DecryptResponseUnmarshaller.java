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

import com.alibaba.acm.shaded.com.aliyuncs.kms.model.v20160120.DecryptResponse;
import com.alibaba.acm.shaded.com.aliyuncs.transform.UnmarshallerContext;


public class DecryptResponseUnmarshaller {

	public static DecryptResponse unmarshall(DecryptResponse decryptResponse, UnmarshallerContext context) {
		
		decryptResponse.setRequestId(context.stringValue("DecryptResponse.RequestId"));
		decryptResponse.setPlaintext(context.stringValue("DecryptResponse.Plaintext"));
		decryptResponse.setKeyId(context.stringValue("DecryptResponse.KeyId"));
	 
	 	return decryptResponse;
	}
}