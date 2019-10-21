package com.alibaba.jvm.sandbox.qatest.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static com.alibaba.jvm.sandbox.qatest.core.util.QaClassUtils.toByteArray;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.StringUtils.replace;

public class JarBuilder {

    private final File targetJarFile;
    private final Manifest manifest = new Manifest();
    private final Map<String, byte[]> entryDataMap = new LinkedHashMap<String, byte[]>();

    public JarBuilder(File targetJarFile) {
        this.targetJarFile = targetJarFile;
    }

    public JarBuilder manifest(String name, String value) {
        manifest.getMainAttributes().putValue(name, value);
        return this;
    }

    public JarBuilder putEntry(Class<?> clazz) throws IOException {
        entryDataMap.put(
                replace(clazz.getName(), ".", "/") + ".class",
                toByteArray(clazz)
        );
        return this;
    }

    public JarBuilder putEntry(String name, byte[] dataArray) {
        entryDataMap.put(name, dataArray);
        return this;
    }

    public JarBuilder putEntry(String name, String text, Charset charset) {
        entryDataMap.put(name, text.getBytes(charset));
        return this;
    }

    public JarBuilder putEntry(String name, String text) {
        entryDataMap.put(name, text.getBytes());
        return this;
    }

    private static Manifest mergeJarManifest(final Manifest targetJarManifest) {
        Manifest mergeManifest = new Manifest();
        if (null != targetJarManifest) {
            mergeManifest.getEntries().putAll(targetJarManifest.getEntries());
        }
        mergeManifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        mergeManifest.getMainAttributes().putValue("Created-By", "QaJarUtils");
        mergeManifest.getMainAttributes().putValue("Author", "oldmanpushcart@gmail.com");
        return mergeManifest;
    }

    public File build() throws IOException {
        JarOutputStream jos = null;
        try {
            jos = new JarOutputStream(new FileOutputStream(targetJarFile), mergeJarManifest(manifest));
            for (Map.Entry<String, byte[]> entry : entryDataMap.entrySet()) {
                jos.putNextEntry(new JarEntry(entry.getKey()));
                jos.write(entry.getValue());
            }
        } finally {
            closeQuietly(jos);
        }
        return targetJarFile;
    }


    public static JarBuilder building(final File targetJarFile) {
        return new JarBuilder(targetJarFile);
    }

}
