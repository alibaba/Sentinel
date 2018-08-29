package com.alibaba.csp.sentinel.datasource;

public abstract class AbstractWritableDataSource<S, T, R> extends AbstractDataSource<S, T> implements WritableDataSource<T> {

	protected ConfigParser<T, R> writeParser;
	
	public AbstractWritableDataSource(ConfigParser<S, T> readParser, ConfigParser<T, R> writeParser, Class<?> type) {
		super(readParser);
		this.writeParser = writeParser;
		WritableDataSourceAdapter.registerDataSource(this, type);
	}
}
