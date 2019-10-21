package com.alibaba.csp.starter;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class PluginJarUtils {
    public static List<String> findAgentJarFileNames(Pattern pattern) {
        URL agentJarUrl = getAgentJarUrl();
        return findJarFileNames(agentJarUrl, pattern);
    }

    public static List<String> findJarFileNames(URL agentJarUrl, Pattern pattern) {
        JarFile jarFile = null;
        try {
            jarFile = getAgentJarFile(agentJarUrl);

            List<String> names = new ArrayList();
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                if (pattern.matcher(jarEntry.getName()).matches()) {
                    names.add(jarEntry.getName());
                }
            }
            return names;
        } catch (Exception localException) {
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException localIOException2) {
                }
            }
        }
        return Collections.emptyList();
    }

    public static URL getAgentJarUrl() {
        return PluginJarUtils.class.getProtectionDomain().getCodeSource().getLocation();
    }

    private static JarFile getAgentJarFile(URL agentJarUrl) {
        if (agentJarUrl == null) {
            return null;
        }
        try {
            return new JarFile(getAgentJarFileName(agentJarUrl));
        } catch (IOException localIOException) {
        }
        return null;
    }

    private static String getAgentJarFileName(URL agentJarUrl) {
        if (agentJarUrl == null) {
            return null;
        }
        try {
            return URLDecoder.decode(agentJarUrl.getFile().replace("+", "%2B"), "UTF-8");
        } catch (IOException localIOException) {
        }
        return null;
    }
}

