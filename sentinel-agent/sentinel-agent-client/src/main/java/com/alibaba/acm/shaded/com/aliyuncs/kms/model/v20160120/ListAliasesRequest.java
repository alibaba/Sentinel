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
public class ListAliasesRequest extends RpcAcsRequest<ListAliasesResponse> {
	
	public ListAliasesRequest() {
		super("Kms", "2016-01-20", "ListAliases", "kms");
		setProtocol(ProtocolType.HTTPS);
	}

	private Integer pageSize;

	private String sTSToken;

	private Integer pageNumber;

	public Integer getPageSize() {
		return this.pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
		if(pageSize != null){
			putQueryParameter("PageSize", pageSize.toString());
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

	public Integer getPageNumber() {
		return this.pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
		if(pageNumber != null){
			putQueryParameter("PageNumber", pageNumber.toString());
		}
	}

	@Override
	public Class<ListAliasesResponse> getResponseClass() {
		return ListAliasesResponse.class;
	}

}
