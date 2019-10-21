package com.taobao.diamond.manager.impl;

import java.util.List;
import com.google.common.collect.Lists;
import com.taobao.diamond.exception.DiamondException;
import com.taobao.diamond.manager.IConfigFilter;
import com.taobao.diamond.manager.IConfigFilterChain;
import com.taobao.diamond.manager.IConfigRequest;
import com.taobao.diamond.manager.IConfigResponse;

public class ConfigFilterChainManager implements IConfigFilterChain {

	private List<IConfigFilter> filters = Lists.newArrayList();

	public synchronized ConfigFilterChainManager addFilter(IConfigFilter filter) {
		// ���order��С˳�����
		int i = 0;
		while (i < this.filters.size()) {
			IConfigFilter currentValue = this.filters.get(i);
			if (currentValue.getFilterName().equals(filter.getFilterName())) {
				break;
			}
			if (filter.getOrder() >= currentValue.getOrder() && i < this.filters.size()) {
				i++;
			} else {
				this.filters.add(i, filter);
				break;
			}
		}

		if (i == this.filters.size()) {
			this.filters.add(i, filter);
		}
		return this;
	}
	

	@Override
	public void doFilter(IConfigRequest request, IConfigResponse response) throws DiamondException {
		new VirtualFilterChain(this.filters).doFilter(request, response);
	}

	private static class VirtualFilterChain implements IConfigFilterChain {

		private final List<? extends IConfigFilter> additionalFilters;

		private int currentPosition = 0;

		public VirtualFilterChain(List<? extends IConfigFilter> additionalFilters) {
			this.additionalFilters = additionalFilters;
		}

		@Override
		public void doFilter(final IConfigRequest request, final IConfigResponse response) throws DiamondException {
			if (this.currentPosition == this.additionalFilters.size()) {
				return;
			} else {
				this.currentPosition++;
				IConfigFilter nextFilter = this.additionalFilters.get(this.currentPosition - 1);
				nextFilter.doFilter(request, response, this);
			}
		}
	}

}
