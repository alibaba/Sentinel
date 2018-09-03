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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * <p>
 * A {@link WritableDataSource} based on file. This class will automatically fetches the backend file every refresh period.
 * </p>
 * <p>
 * Limitations: Default read buffer size is 1 MB. If file size is greater than buffer size, exceeding bytes will
 * be ignored. Default charset is UTF-8.
 * </p>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
public class FileRefreshableDataSource<T> extends AutoRefreshDataSource<String, T> implements WritableDataSource<T> {

    private static final int MAX_SIZE = 1024 * 1024 * 4;
    private static final long DEFAULT_REFRESH_MS = 3000;
    private static final int DEFAULT_BUF_SIZE = 1024 * 1024;
    private static final Charset DEFAULT_CHAR_SET = Charset.forName("utf-8");

    private byte[] buf;
    private final Charset charset;
    private final File file;

    private final Converter<T, String> configEncoder;

    /**
     * Create a file based {@link ReadableDataSource} whose read buffer size is 1MB, charset is UTF8,
     * and read interval is 3 seconds.
     *
     * @param file         the file to read
     * @param configParser the config decoder (parser)
     */
    public FileRefreshableDataSource(File file, Converter<String, T> configParser, Converter<T, String> configEncoder) throws FileNotFoundException {
        this(file, configParser, configEncoder, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, DEFAULT_CHAR_SET);
    }

    public FileRefreshableDataSource(String fileName, Converter<String, T> configParser, Converter<T, String> configEncoder)
        throws FileNotFoundException {
        this(new File(fileName), configParser, configEncoder, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, DEFAULT_CHAR_SET);
    }

    public FileRefreshableDataSource(File file, Converter<String, T> configParser, Converter<T, String> configEncoder, int bufSize)
        throws FileNotFoundException {
        this(file, configParser, configEncoder, DEFAULT_REFRESH_MS, bufSize, DEFAULT_CHAR_SET);
    }

    public FileRefreshableDataSource(File file, Converter<String, T> configParser, Converter<T, String> configEncoder, Charset charset)
        throws FileNotFoundException {
        this(file, configParser, configEncoder, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, charset);
    }

    public FileRefreshableDataSource(File file, Converter<String, T> configParser, Converter<T, String> configEncoder, long recommendRefreshMs,
                                     int bufSize, Charset charset) throws FileNotFoundException {
        super(configParser, recommendRefreshMs);
        if (bufSize <= 0 || bufSize > MAX_SIZE) {
            throw new IllegalArgumentException("bufSize must between (0, " + MAX_SIZE + "], but " + bufSize + " get");
        }
        if (file == null) {
            throw new IllegalArgumentException("file can't be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("charset can't be null");
        }
        if (configEncoder == null) {
            throw new IllegalArgumentException("Config encoder cannot be null");
        }
        this.buf = new byte[bufSize];
        this.file = file;
        this.charset = charset;
        this.configEncoder = configEncoder;
        firstLoad();
    }

    private void firstLoad() {
        try {
            T newValue = loadConfig();
            getProperty().updateValue(newValue);
        } catch (Throwable e) {
            RecordLog.info("loadConfig exception", e);
        }
    }

    @Override
    public String readSource() throws Exception {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            FileChannel channel = inputStream.getChannel();
            if (channel.size() > buf.length) {
                throw new IllegalStateException(file.getAbsolutePath() + " file size=" + channel.size()
                    + ", is bigger than bufSize=" + buf.length + ". Can't read");
            }
            int len = inputStream.read(buf);
            return new String(buf, 0, len, charset);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        buf = null;
    }

    @Override
    public void write(T value) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }
}
