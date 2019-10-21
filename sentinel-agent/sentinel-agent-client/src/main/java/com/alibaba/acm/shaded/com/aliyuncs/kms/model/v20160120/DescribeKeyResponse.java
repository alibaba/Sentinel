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

import com.alibaba.acm.shaded.com.aliyuncs.AcsResponse;
import com.alibaba.acm.shaded.com.aliyuncs.kms.transform.v20160120.DescribeKeyResponseUnmarshaller;
import com.alibaba.acm.shaded.com.aliyuncs.transform.UnmarshallerContext;

/**
 * @author auto create
 * @version 
 */
public class DescribeKeyResponse extends AcsResponse {

	private String requestId;

	private KeyMetadata keyMetadata;

	public String getRequestId() {
		return this.requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public KeyMetadata getKeyMetadata() {
		return this.keyMetadata;
	}

	public void setKeyMetadata(KeyMetadata keyMetadata) {
		this.keyMetadata = keyMetadata;
	}

	public static class KeyMetadata {

		private String creationDate;

		private String description;

		private String keyId;

		private String keyState;

		private String keyUsage;

		private String deleteDate;

		private String creator;

		private String arn;

		private String origin;

		private String materialExpireTime;

		public String getCreationDate() {
			return this.creationDate;
		}

		public void setCreationDate(String creationDate) {
			this.creationDate = creationDate;
		}

		public String getDescription() {
			return this.description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getKeyId() {
			return this.keyId;
		}

		public void setKeyId(String keyId) {
			this.keyId = keyId;
		}

		public String getKeyState() {
			return this.keyState;
		}

		public void setKeyState(String keyState) {
			this.keyState = keyState;
		}

		public String getKeyUsage() {
			return this.keyUsage;
		}

		public void setKeyUsage(String keyUsage) {
			this.keyUsage = keyUsage;
		}

		public String getDeleteDate() {
			return this.deleteDate;
		}

		public void setDeleteDate(String deleteDate) {
			this.deleteDate = deleteDate;
		}

		public String getCreator() {
			return this.creator;
		}

		public void setCreator(String creator) {
			this.creator = creator;
		}

		public String getArn() {
			return this.arn;
		}

		public void setArn(String arn) {
			this.arn = arn;
		}

		public String getOrigin() {
			return this.origin;
		}

		public void setOrigin(String origin) {
			this.origin = origin;
		}

		public String getMaterialExpireTime() {
			return this.materialExpireTime;
		}

		public void setMaterialExpireTime(String materialExpireTime) {
			this.materialExpireTime = materialExpireTime;
		}
	}

	@Override
	public DescribeKeyResponse getInstance(UnmarshallerContext context) {
		return	DescribeKeyResponseUnmarshaller.unmarshall(this, context);
	}
}
