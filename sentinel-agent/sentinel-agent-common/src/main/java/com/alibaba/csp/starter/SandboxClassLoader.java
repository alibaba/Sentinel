package com.alibaba.csp.starter;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

class SandboxClassLoader
        extends URLClassLoader {
    SandboxClassLoader() throws Throwable {
        super(getServiceJar());
    }

    private static URL[] getServiceJar() throws IOException {
      //  String[] coreJar = {"ahas-core", "sandbox-core", "ahas-sentinel-core"};
        String[] coreJar = {"sentinel-agent-core", "sandbox-core"};
        URL[] urls = new URL[coreJar.length + 1];
        urls[0] = SandboxClassLoader.class.getProtectionDomain().getCodeSource().getLocation();
        for (int i = 0; i < coreJar.length; i++) {
            urls[(i + 1)] = new URL("file:" + EmbeddedJarUtil.getJarFileInAgent(coreJar[i], "ahas-core"));
        }
        return urls;
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }
        try {
            Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Throwable e) {
        }
        return super.loadClass(name, resolve);
    }
}


