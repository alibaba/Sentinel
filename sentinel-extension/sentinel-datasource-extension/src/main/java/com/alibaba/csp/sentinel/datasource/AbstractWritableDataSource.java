package com.alibaba.csp.sentinel.datasource;

public abstract class AbstractWritableDataSource<S, T, R> extends AbstractDataSource<S, T> implements WritableDataSource<T> {

	private ConfigParser<T, R> writeParser;
	
	public AbstractWritableDataSource(ConfigParser<S, T> readParser, ConfigParser<T, R> writeParser, Class<?> type) {
		super(readParser);
		this.writeParser = writeParser;
		WritableDataSourceAdapter.registerDataSource(this, type);
	}
	
	public void writeDataSource(T values) throws Exception {
		if (writeParser == null) {
			throw new NullPointerException("writeParser is null Can't write");
		}
		writeResult(writeParser.parse(values));
	}
	
	public abstract void writeResult(R values) throws Exception;
}
