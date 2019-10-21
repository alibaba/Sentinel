package com.alibaba.csp.starter;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;


public class AgentLauncher {
    private static final String SANDBOX_SPY_JAR = "sandbox-spy";
    private static final String LAUNCH_MODE_AGENT = "agent";
    private static final String LAUNCH_MODE_ATTACH = "attach";
    private static final String CLASS_OF_CORE_CONFIGURE = "com.alibaba.jvm.sandbox.core.CoreConfigure";
    private static volatile ClassLoader sandboxClassLoader;
    private static String SANDBOX_MODULE_PATH;
    private static String SANDBOX_PROVIDER_LIB_PATH;

    private static boolean isNotBlankString(String string) {
        return (null != string) &&
                (string.length() > 0) &&
                (!string.matches("^\\s*$"));
    }

    private static String getDefaultString(String string, String defaultString) {
        return isNotBlankString(string) ? string : defaultString;
    }


    public static void premain(String propertiesFilePath, Instrumentation inst) {
        main(inst);
    }


    public static void agentmain(String cfg, Instrumentation inst) {
        String[] cfgSegmentArray = cfg.split(";");
        String token = cfgSegmentArray.length >= 1 ? cfgSegmentArray[0] : "";
        if (token.matches("^\\s*$")) {
            throw new IllegalArgumentException("sandbox attach token was blank.");
        }
        main(inst);
    }


    public static void initModulePath()
            throws IOException {
        File module = EmbeddedJarUtil.getJarFileInAgent("ahas-sandbox-module", "ahas-module." +
                System.currentTimeMillis());
        SANDBOX_MODULE_PATH = module.getParent();
        SANDBOX_PROVIDER_LIB_PATH = module.getPath() + File.separator + "ahas-provider";
    }


    private static ClassLoader loadOrDefineClassLoader()
            throws Throwable {
        ClassLoader classLoader;
        if (null != sandboxClassLoader) {
            classLoader = sandboxClassLoader;

        } else {
            classLoader = new SandboxClassLoader();
        }
        sandboxClassLoader = classLoader;
        return classLoader;
    }

    public static ClassLoader getSandboxClassLoader() {
        return sandboxClassLoader;
    }

    public static void initModule(String cfg, Instrumentation inst) {
        try {
            String coreFeatureString = String.format(";system_module=%s;mode=%s;provider=%s;", new Object[]{SANDBOX_MODULE_PATH, "agent", SANDBOX_PROVIDER_LIB_PATH});


            String propertiesFilePath = getDefaultString(cfg, null);

            ClassLoader agentLoader = getSandboxClassLoader();
            Class<?> classOfConfigure = agentLoader.loadClass("com.alibaba.jvm.sandbox.core.CoreConfigure");


            Object objectOfCoreConfigure = classOfConfigure.getMethod("toConfigure", new Class[]{String.class, String.class}).invoke(null, new Object[]{coreFeatureString, propertiesFilePath});

            Class<?> clazz = agentLoader.loadClass("com.alibaba.csp.AgentModuleManager");
            Object moduleManager = clazz.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            clazz.getMethod("initManager", new Class[]{classOfConfigure, Instrumentation.class}).invoke(moduleManager, new Object[]{objectOfCoreConfigure, inst});
        } catch (Throwable e) {
            throw new RuntimeException("init module failed.", e);
        }
    }

    private static synchronized void main(Instrumentation inst) {
        try {
            inst.appendToBootstrapClassLoaderSearch(new JarFile(
                    EmbeddedJarUtil.getJarFileInAgent("sandbox-spy", null)));
            initModulePath();

            ClassLoader agentLoader = loadOrDefineClassLoader();
            Class<?> clazz = agentLoader.loadClass("com.alibaba.csp.AgentModuleManager");
            Object moduleManager = clazz.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            clazz.getMethod("initLogback", new Class[0]).invoke(moduleManager, new Object[0]);
        } catch (Throwable cause) {
            throw new RuntimeException("sandbox attach failed.", cause);
        }
    }
}


/* Location:              /Users/guxin/Downloads/ahas-java-agent.jar!/com/taobao/csp/ahas/starter/AgentLauncher.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */