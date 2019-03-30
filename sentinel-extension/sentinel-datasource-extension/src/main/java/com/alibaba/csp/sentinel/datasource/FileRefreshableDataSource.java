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

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * <p>
 * A {@link ReadableDataSource} based on file. This class will automatically
 * fetches the backend file every isModified period.
 * </p>
 * <p>
 * Limitations: Default read buffer size is 1 MB. If file size is greater than
 * buffer size, exceeding bytes will be ignored. Default charset is UTF-8.
 * </p>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
public class FileRefreshableDataSource<T> extends AutoRefreshDataSource<String, T> {

    private static final int MAX_SIZE = 1024 * 1024 * 4;
    private static final long DEFAULT_REFRESH_MS = 3000;
    private static final int DEFAULT_BUF_SIZE = 1024 * 1024;
    private static final Charset DEFAULT_CHAR_SET = Charset.forName("utf-8");

    private byte[] buf;
    private final Charset charset;
    private final File file;
    private final JarEntry jarEntry;
    private final boolean isFile;
    private final JarFile jarFile;

    private long lastModified = 0L;

    /**
     * Create a file based {@link ReadableDataSource} whose read buffer size is
     * 1MB, charset is UTF8, and read interval is 3 seconds.
     *
     * @param file         the file to read
     * @param configParser the config decoder (parser)
     */
    public FileRefreshableDataSource(File file, Converter<String, T> configParser) throws FileNotFoundException {
        this(file, configParser, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, DEFAULT_CHAR_SET);
    }

    public FileRefreshableDataSource(String fileName, Converter<String, T> configParser) throws FileNotFoundException {
        this(new File(fileName), configParser, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, DEFAULT_CHAR_SET);
    }

    public FileRefreshableDataSource(File file, Converter<String, T> configParser, int bufSize)
            throws FileNotFoundException {
        this(file, configParser, DEFAULT_REFRESH_MS, bufSize, DEFAULT_CHAR_SET);
    }

    public FileRefreshableDataSource(File file, Converter<String, T> configParser, Charset charset)
            throws FileNotFoundException {
        this(file, configParser, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, charset);
    }

    /**
     * @param jarPath      the jar to read
     * @param fileName     the file in jar to read
     * @param configParser the config decoder (parser)
     * @throws FileNotFoundException
     * @since 1.8
     */
    public FileRefreshableDataSource(String jarPath, String fileName, Converter<String, T> configParser)
            throws IOException {
        this(jarPath, fileName, configParser, DEFAULT_REFRESH_MS, DEFAULT_BUF_SIZE, DEFAULT_CHAR_SET);
    }


    public FileRefreshableDataSource(File file, Converter<String, T> configParser, long recommendRefreshMs, int bufSize,
                                     Charset charset) throws FileNotFoundException {
        super(configParser, recommendRefreshMs);
        if (bufSize <= 0 || bufSize > MAX_SIZE) {
            throw new IllegalArgumentException("bufSize must between (0, " + MAX_SIZE + "], but " + bufSize + " get");
        }
        if (file == null || file.isDirectory()) {
            throw new IllegalArgumentException("File can't be null or a directory");
        }
        if (charset == null) {
            throw new IllegalArgumentException("charset can't be null");
        }
        this.buf = new byte[bufSize];
        this.file = file;
        this.jarEntry = null;
        this.jarFile = null;
        this.isFile = true;
        this.charset = charset;
        // If the file does not exist, the last modified will be 0.
        this.lastModified = file.lastModified();
        firstLoad();
    }

    public FileRefreshableDataSource(String jarPath, String fileName, Converter<String, T> configParser, long recommendRefreshMs, int bufSize,
                                     Charset charset) throws IOException {
        super(configParser, recommendRefreshMs);
        if (bufSize <= 0 || bufSize > MAX_SIZE) {
            throw new IllegalArgumentException("bufSize must between (0, " + MAX_SIZE + "], but " + bufSize + " get");
        }

        if (charset == null) {
            throw new IllegalArgumentException("charset can't be null");
        }
        this.buf = new byte[bufSize];
        this.file = null;
        this.charset = charset;
        this.jarFile = new JarFile(jarPath);
        this.jarEntry = jarFile.getJarEntry(fileName);
        this.isFile = false;
        // If the file does not exist, the last modified will be 0.
        this.lastModified = jarEntry.getTime();
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
        if (!file.exists() && null == jarEntry) {
            // Will throw FileNotFoundException later.
            RecordLog.warn(String.format("[FileRefreshableDataSource] File does not exist: %s", isFile ? file.getAbsolutePath() : jarEntry.getName()));
        }
        InputStream inputStream = null;
        try {
            inputStream = isFile ? new FileInputStream(file) : jarFile.getInputStream(jarEntry);
            int streamLength = inputStream.available();
            if (streamLength > buf.length) {
                throw new IllegalStateException(file.getAbsolutePath() + " file size=" + streamLength
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
    protected boolean isModified() {
        long curLastModified = isFile ? file.lastModified() : jarEntry.getTime();
        if (curLastModified != this.lastModified) {
            this.lastModified = curLastModified;
            return true;
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        super.close();
        buf = null;
    }

}
