package com.alibaba.csp.starter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Map;

import sun.security.action.GetPropertyAction;


public class EmbeddedJarUtil {
    private static final Map<String, File> embeddedAgentJarFiles = new HashMap();
    private static final String TEMP_PARENT_DIR = "com.alibaba.csp.ahas";

    public static int copy(InputStream input, OutputStream output, int bufferSize, boolean closeStreams) throws IOException {
        try {
            byte[] buffer = new byte[bufferSize];
            int count = 0;
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
            return count;
        } finally {
            if (closeStreams) {
                input.close();
                output.close();
            }
        }
    }


    private static File load(String jarNameWithoutExtension, String dir)
            throws IOException {
        InputStream jarStream = EmbeddedJarUtil.class.getClassLoader().getResourceAsStream(jarNameWithoutExtension + ".jar");
        if (jarStream == null) {
            throw new FileNotFoundException(jarNameWithoutExtension + ".jar");
        }
        File tempDir = null;
        if (dir != null) {
            File tmpdir = new File((String) AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")));
            tempDir = new File(tmpdir.getAbsolutePath() + File.separator + "com.alibaba.csp.ahas" + File.separator + dir);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
        }
        File file = File.createTempFile(jarNameWithoutExtension, ".jar", tempDir);
        file.deleteOnExit();
        OutputStream out = new FileOutputStream(file);
        try {
            copy(jarStream, out, 8096, true);
            embeddedAgentJarFiles.put(jarNameWithoutExtension, file);
            return file;
        } finally {
            out.close();
        }
    }


    public static File getJarFileInAgent(String jarNameWithoutExtension, String dir)
            throws IOException {
        if (embeddedAgentJarFiles.containsKey(jarNameWithoutExtension)) {
            return (File) embeddedAgentJarFiles.get(jarNameWithoutExtension);
        }
        return load(jarNameWithoutExtension, dir);
    }
}


