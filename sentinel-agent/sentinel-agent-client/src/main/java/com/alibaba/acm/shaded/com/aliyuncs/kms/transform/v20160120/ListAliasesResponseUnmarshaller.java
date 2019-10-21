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

import java.util.ArrayList;
import java.util.List;

import com.alibaba.acm.shaded.com.aliyuncs.kms.model.v20160120.ListAliasesResponse;
import com.alibaba.acm.shaded.com.aliyuncs.kms.model.v20160120.ListAliasesResponse.Alias;
import com.alibaba.acm.shaded.com.aliyuncs.transform.UnmarshallerContext;


public class ListAliasesResponseUnmarshaller {

	public static ListAliasesResponse unmarshall(ListAliasesResponse listAliasesResponse, UnmarshallerContext context) {
		
		listAliasesResponse.setRequestId(context.stringValue("ListAliasesResponse.RequestId"));
		listAliasesResponse.setTotalCount(context.integerValue("ListAliasesResponse.TotalCount"));
		listAliasesResponse.setPageNumber(context.integerValue("ListAliasesResponse.PageNumber"));
		listAliasesResponse.setPageSize(context.integerValue("ListAliasesResponse.PageSize"));

		List<Alias> aliases = new ArrayList<Alias>();
		for (int i = 0; i < context.lengthValue("ListAliasesResponse.Aliases.Length"); i++) {
			Alias alias = new Alias();
			alias.setKeyId(context.stringValue("ListAliasesResponse.Aliases["+ i +"].KeyId"));
			alias.setAliasName(context.stringValue("ListAliasesResponse.Aliases["+ i +"].AliasName"));
			alias.setAliasArn(context.stringValue("ListAliasesResponse.Aliases["+ i +"].AliasArn"));

			aliases.add(alias);
		}
		listAliasesResponse.setAliases(aliases);
	 
	 	return listAliasesResponse;
	}
}