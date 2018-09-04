/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource;

import java.io.File;
import java.io.FileOutputStream;

/**
 * A {@link WritableDataSource} based on file.
 *
 * @param <T>
 *            data type
 * @author Eric Zhao
 * @since 0.2.0
 */
public class FileWritableDataSource<T> implements WritableDataSource<T> {

	private final Converter<T, String> configEncoder;
	private File file;

	public FileWritableDataSource(String filePath, Converter<T, String> configEncoder) {
		this(new File(filePath), configEncoder);
	}

	public FileWritableDataSource(File file, Converter<T, String> configEncoder) {
		if (file == null || file.isDirectory()) {
			throw new IllegalArgumentException("Bad file");
		}
		if (configEncoder == null) {
			throw new IllegalArgumentException("Config encoder cannot be null");
		}
		this.configEncoder = configEncoder;
		this.file = file;
	}

	@Override
	public void write(T value) throws Exception {
		if (configEncoder == null) {
			throw new NullPointerException("configEncoder is null Can't write");
		}
		synchronized (file) {
			String convertResult = configEncoder.convert(value);
			FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(file);
				byte[] bytesArray = convertResult.getBytes();
				outputStream.write(bytesArray);
				outputStream.flush();
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (Exception ignore) {
						// nothing
					}
				}
			}
		}
	}

	@Override
	public void close() throws Exception {
		// Nothing
	}
}
