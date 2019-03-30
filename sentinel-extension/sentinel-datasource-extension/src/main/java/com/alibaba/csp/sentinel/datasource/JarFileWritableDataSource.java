package com.alibaba.csp.sentinel.datasource;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * A {@link WritableDataSource} based on file.
 * @param <T> data type
 * @author dingq
 * @date 2019-03-30
 */
public class JarFileWritableDataSource<T> implements WritableDataSource<T> {
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private final Converter<T, String> configEncoder;
    private final Charset charset;
    private final String jarName;
    private final String fileInJarName;
    private final String newJarName;
    private JarFile jarFile;


    private final Lock lock = new ReentrantLock(true);

    public JarFileWritableDataSource(String jarName, String fileInJarName, Converter<T, String> configEncoder) throws IOException {
        this(jarName, fileInJarName, configEncoder, DEFAULT_CHARSET);
    }

    public JarFileWritableDataSource(String jarName, String fileInJarName, Converter<T, String> configEncoder, Charset charset) throws IOException {
        if (configEncoder == null) {
            throw new IllegalArgumentException("Config encoder cannot be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset cannot be null");
        }
        this.configEncoder = configEncoder;
        this.charset = charset;
        this.jarName = jarName;
        this.fileInJarName = fileInJarName;
        this.jarFile = new JarFile(jarName);
        this.newJarName = jarName.replace(".jar","-new.jar");
    }

    @Override
    public void write(T value) throws Exception {
        lock.lock();
        try {
            JarOutputStream jos = null;
            try {
                String convertResult = configEncoder.convert(value);
                byte[] bytesArray = convertResult.getBytes(charset);
                // if you change one file in jar ,you should rewrite all files or other files will be null
                FileOutputStream fos = new FileOutputStream(newJarName);
                jos = new JarOutputStream(fos);
                Set<JarEntry> lists = getJarEntrySet();
                for (JarEntry jarEntry : lists) {
                    if (jarEntry.getName().equals(fileInJarName)) {
                        JarEntry newEntry = new JarEntry(jarEntry.getName());
                        jos.putNextEntry(newEntry);
                        jos.write(bytesArray, 0, bytesArray.length);
                    } else {
                        JarEntry newEntry = new JarEntry(jarEntry.getName());
                        jos.putNextEntry(newEntry);
                        byte[] bytes = inputstreamToByte(jarFile.getInputStream(jarEntry));
                        jos.write(bytes, 0, bytes.length);
                    }
                }

                jos.flush();

                //new jarFile overwrite old jarFile
                new File(jarName).delete();
                new File(newJarName).renameTo(new File(jarName));
            } finally {
                if (jos != null) {
                    try {
                        jos.close();
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

    }

    private Set<JarEntry> getJarEntrySet() {
        Set<JarEntry> sets = new LinkedHashSet<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            sets.add(entries.nextElement());
        }
        return sets;
    }

    private static byte[] inputstreamToByte(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[2048];
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
            os.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return os.toByteArray();
    }
}
