package com.alibaba.edas.acm.listener;

import java.util.concurrent.Executor;

public abstract class ConfigChangeListener implements
		ConfigChangeListenerAdapter {

	@Override
	public Executor getExecutor() {
		return null;
	}
}
