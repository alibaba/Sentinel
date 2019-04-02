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

import com.alibaba.csp.sentinel.log.RecordLog;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>
 * A {@link ReadableDataSource} based on jarfile. This class can only read file when it
 * run but  will not automatically refresh if it is changed.
 * </p>
 * <p>
 * Limitations: Default read buffer size is 1 MB. If file size is greater than
 * buffer size, exceeding bytes will be ignored. Default charset is UTF-8.
 * </p>
 *
 * @author dingq
 * @date 2019-03-30
 */
public class FileInJarReadableDataSource<T> extends AutoRefreshDataSource<String, T> {
    private static final int MAX_SIZE = 1024 * 1024 * 4;
    private static final long DEFAULT_REFRESH_MS = 3000;
    private static final int DEFAULT_BUF_SIZE = 1024 * 1024;
    private static final Charset DEFAULT_CHAR_SET = Charset.forName("utf-8");

    private byte[] buf;
    private JarEntry jarEntry;
    private JarFile jarFile;
    private final Charset charset;
    private final String jarName;
    private final String fileInJarName;

    /**
     * @param jarName      the jar to read
     * @param fileInJarName     the file in jar to read
     * @param configParser the config decoder (parser)
     * @throws FileNotFoundException
     */
    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser)
            throws IOException {
        this(jarName, fileInJarName, configParser, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, DEFAULT_CHAR_SET);
    }

    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser, int bufSize)
            throws IOException {
        this(jarName, fileInJarName, configParser, DEFAULT_REFRESH_MS, bufSize, DEFAULT_CHAR_SET);
    }

    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser, Charset charset)
            throws IOException {
        this(jarName, fileInJarName, configParser, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, charset);
    }

    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser, long recommendRefreshMs, int bufSize,
                                       Charset charset) throws IOException {
        super(configParser, recommendRefreshMs);
        if (bufSize <= 0 || bufSize > MAX_SIZE) {
            throw new IllegalArgumentException("bufSize must between (0, " + MAX_SIZE + "], but " + bufSize + " get");
        }
        if (charset == null) {
            throw new IllegalArgumentException("charset can't be null");
        }
        this.buf = new byte[bufSize];
        this.charset = charset;
        this.jarName = jarName;
        this.fileInJarName = fileInJarName;
        refreshJar();
        firstLoad();
    }

    @Override
    public String readSource() throws Exception {
        if (null == jarEntry) {
            // Will throw FileNotFoundException later.
            RecordLog.warn(String.format("[FileInJarReadableDataSource] File does not exist: %s", jarFile.getName()));
        }
        InputStream inputStream = null;
        try {
            inputStream = jarFile.getInputStream(jarEntry);
            if (inputStream.available() > buf.length) {
                throw new IllegalStateException(jarFile.getName() + " file size=" + inputStream.available()
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

    private void firstLoad() {
        try {
            T newValue = loadConfig();
            getProperty().updateValue(newValue);
        } catch (Throwable e) {
            RecordLog.info("loadConfig exception", e);
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        buf = null;
    }

    private void refreshJar() throws IOException {
        this.jarFile = new JarFile(jarName);
        this.jarEntry = jarFile.getJarEntry(fileInJarName);
    }
}
