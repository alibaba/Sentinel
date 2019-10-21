package com.taobao.csp.ahas.service.util;

import sun.security.action.GetPropertyAction;

import java.io.*;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Map;

public class EmbeddedJarUtil {
   private static final Map<String, File> embeddedAgentJarFiles = new HashMap();
   private static final String TEMP_PARENT_DIR = "com.alibaba.csp.ahas";

   public static int copy(InputStream input, OutputStream output, int bufferSize, boolean closeStreams) throws IOException {
      try {
         byte[] buffer = new byte[bufferSize];
         int count = 0;

         int n;
         for(boolean var6 = false; -1 != (n = input.read(buffer)); count += n) {
            output.write(buffer, 0, n);
         }

         int var7 = count;
         return var7;
      } finally {
         if (closeStreams) {
            input.close();
            output.close();
         }

      }
   }

   private static File load(String jarNameWithoutExtension, String dir, ClassLoader classLoader) throws IOException {
      if (classLoader == null) {
         classLoader = EmbeddedJarUtil.class.getClassLoader();
      }

      InputStream jarStream = classLoader.getResourceAsStream(jarNameWithoutExtension + ".jar");
      if (jarStream == null) {
         throw new FileNotFoundException(jarNameWithoutExtension + ".jar");
      } else {
         File tempDir = null;
         File file;
         if (dir != null) {
            file = new File((String)AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")));
            tempDir = new File(file.getAbsolutePath() + File.separator + "com.alibaba.csp.ahas" + File.separator + dir);
            if (!tempDir.exists()) {
               tempDir.mkdirs();
            }
         }

         file = File.createTempFile(jarNameWithoutExtension, ".jar", tempDir);
         file.deleteOnExit();
         FileOutputStream out = new FileOutputStream(file);

         File var7;
         try {
            copy(jarStream, out, 8096, true);
            embeddedAgentJarFiles.put(jarNameWithoutExtension, file);
            var7 = file;
         } finally {
            out.close();
         }

         return var7;
      }
   }

   public static File getJarFileInAgent(String jarNameWithoutExtension, String dir, ClassLoader classLoader) throws IOException {
      return embeddedAgentJarFiles.containsKey(jarNameWithoutExtension) ? (File)embeddedAgentJarFiles.get(jarNameWithoutExtension) : load(jarNameWithoutExtension, dir, classLoader);
   }
}
