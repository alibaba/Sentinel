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
import java.nio.charset.Charset;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * A {@link WritableDataSource} based on file.
 *
 * @param <T> data type
 * @author Eric Zhao
 * @since 0.2.0
 */
public class FileWritableDataSource<T> implements WritableDataSource<T> {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private final Converter<T, String> configEncoder;
    private final File file;
    private final Charset charset;

    private final Lock lock = new ReentrantLock(true);

    public FileWritableDataSource(String filePath, Converter<T, String> configEncoder) {
        this(new File(filePath), configEncoder);
    }

    public FileWritableDataSource(File file, Converter<T, String> configEncoder) {
        this(file, configEncoder, DEFAULT_CHARSET);
    }

    public FileWritableDataSource(File file, Converter<T, String> configEncoder, Charset charset) {
        if (file == null || file.isDirectory()) {
            throw new IllegalArgumentException("Bad file");
        }
        if (configEncoder == null) {
            throw new IllegalArgumentException("Config encoder cannot be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset cannot be null");
        }
        this.configEncoder = configEncoder;
        this.file = file;
        this.charset = charset;
    }

    @Override
    public void write(T value) throws Exception {
        lock.lock();
        try {
            String convertResult = configEncoder.convert(value);
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
                byte[] bytesArray = convertResult.getBytes(charset);

                RecordLog.info("[FileWritableDataSource] Writing to file {}: {}", file, convertResult);
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
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws Exception {
        // Nothing
    }
}
