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
import com.alibaba.csp.sentinel.util.AssertUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>
 * A {@link ReadableDataSource} based on jar file. This class can only read file initially when it loads file.
 * </p>
 * <p>
 * Limitations: Default read buffer size is 1 MB, while max allowed buffer size is 4MB.
 * File size should not exceed the buffer size, or exception will be thrown. Default charset is UTF-8.
 * </p>
 *
 * @author dingq
 * @author Eric Zhao
 * @since 1.6.0
 */
public class FileInJarReadableDataSource<T> extends AbstractDataSource<String, T> {

    private static final int MAX_SIZE = 1024 * 1024 * 4;
    private static final int DEFAULT_BUF_SIZE = 1024 * 1024;
    private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    private final Charset charset;
    private final String jarName;
    private final String fileInJarName;

    private byte[] buf;
    private JarEntry jarEntry;
    private JarFile jarFile;

    /**
     * @param jarName       the jar to read
     * @param fileInJarName the file in jar to read
     * @param configParser  the config decoder (parser)
     * @throws IOException if IO failure occurs
     */
    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser)
        throws IOException {
        this(jarName, fileInJarName, configParser, DEFAULT_BUF_SIZE, DEFAULT_CHARSET);
    }

    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser,
                                       int bufSize) throws IOException {
        this(jarName, fileInJarName, configParser, bufSize, DEFAULT_CHARSET);
    }

    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser,
                                       Charset charset) throws IOException {
        this(jarName, fileInJarName, configParser, DEFAULT_BUF_SIZE, charset);
    }

    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser,
                                       int bufSize, Charset charset) throws IOException {
        super(configParser);
        AssertUtil.assertNotBlank(jarName, "jarName cannot be blank");
        AssertUtil.assertNotBlank(fileInJarName, "fileInJarName cannot be blank");
        if (bufSize <= 0 || bufSize > MAX_SIZE) {
            throw new IllegalArgumentException("bufSize must between (0, " + MAX_SIZE + "], but " + bufSize + " get");
        }
        AssertUtil.notNull(charset, "charset can't be null");
        this.buf = new byte[bufSize];
        this.charset = charset;
        this.jarName = jarName;
        this.fileInJarName = fileInJarName;
        initializeJar();
        firstLoad();
    }

    @Override
    public String readSource() throws Exception {
        if (null == jarEntry) {
            // Will throw FileNotFoundException later.
            RecordLog.warn(String.format("[FileInJarReadableDataSource] File does not exist: %s", jarFile.getName()));
        }
        try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
            if (inputStream.available() > buf.length) {
                throw new IllegalStateException(String.format("Size of file <%s> exceeds the bufSize (%d): %d",
                    jarFile.getName(), buf.length, inputStream.available()));
            }
            int len = inputStream.read(buf);
            return new String(buf, 0, len, charset);
        }
    }

    private void firstLoad() {
        try {
            T newValue = loadConfig();
            getProperty().updateValue(newValue);
        } catch (Throwable e) {
            RecordLog.warn("[FileInJarReadableDataSource] Error when loading config", e);
        }
    }

    @Override
    public void close() throws Exception {
        buf = null;
    }

    private void initializeJar() throws IOException {
        this.jarFile = new JarFile(jarName);
        this.jarEntry = jarFile.getJarEntry(fileInJarName);
    }
}
