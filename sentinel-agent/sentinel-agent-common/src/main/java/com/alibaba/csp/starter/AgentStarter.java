package com.alibaba.csp.starter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
//import com.alibaba.csp.sentinel.init.InitExecutor2;
import com.alibaba.csp.sentinel.init.InitExecutor;

public class AgentStarter {
    public static final Pattern COMPILE = Pattern.compile("plugins/(.*).jar");
  //  private static final String SENTINEL_SPH0_API = "com/taobao/csp/ahas/sentinel/api/SphO";
  //  private static final String SENTINEL_SPHU_API = "com/taobao/csp/ahas/sentinel/api/SphU";
  //  private static final String SENTINEL_CONTEXT_UTIL_API = "com/taobao/csp/ahas/sentinel/api/ContextUtil";
    private static final String[] AGENT_APIS = {"com/taobao/csp/ahas/sentinel/api/SphO", "com/taobao/csp/ahas/sentinel/api/SphU", "com/taobao/csp/ahas/sentinel/api/ContextUtil"};

    public static void agentmain(String propertiesFilePath, Instrumentation inst) throws Throwable {
        premain(propertiesFilePath, inst);
    }


    public static void premain(String propertiesFilePath, Instrumentation inst)
            throws Throwable {
        boolean block = System.getProperty("ahas.sync", Boolean.FALSE.toString()).equalsIgnoreCase(Boolean.TRUE.toString());
        try {
            preProcess(inst);
            AgentLauncher.premain(propertiesFilePath, inst);
            Class<?> clazz = AgentLauncher.getSandboxClassLoader().loadClass("com.alibaba.csp.AgentBootstrap");
            Method method = clazz.getDeclaredMethod("launch", new Class[0]);
         //   method.invoke(null, new Object[0]);
            AgentLauncher.initModule(propertiesFilePath, inst);
      //      Context context = ContextUtil.getContext();
       //     InitExecutor2.doInit();
            InitExecutor.doInit();
        } catch (Throwable e) {
            e.printStackTrace();
            if (block) {
                throw e;
            }
        }
    }

    private static void preProcess(Instrumentation inst) throws IOException {
  //      addBridgeJarToClassPath(inst);
 //       addApiJarToClassPath(inst);
   //     addPluginJarToClassPath(inst);
    }

    private static void addBridgeJarToClassPath(Instrumentation instrProxy) throws IOException {
       // String[] bridges = {"ahas-service-bridge", "ahas-sentinel-bridge"};
        String[] bridges = {"ahas-sentinel-bridge"};
        for (String name : bridges) {
            JarFile jarFileInAgent = new JarFile(EmbeddedJarUtil.getJarFileInAgent(name, "ahas-bridge"));
            forceReplaceAgentApi(instrProxy, jarFileInAgent);
            addJarToClassPath(instrProxy, jarFileInAgent);
        }
    }

    private static void addApiJarToClassPath(Instrumentation instrProxy) throws IOException {
      //  String[] apiJars = {"ahas-sentinel-agent-api"};
        String[] apiJars = {""};
        for (String name : apiJars) {
            addJarToClassPath(instrProxy, new JarFile(EmbeddedJarUtil.getJarFileInAgent(name, "ahas-api")));
        }
    }

    private static void addPluginJarToClassPath(Instrumentation instrProxy) {
        List<String> agentJarFileNames = PluginJarUtils.findAgentJarFileNames(COMPILE);
        for (String agentJarFileName : agentJarFileNames) {
            String name = agentJarFileName.substring(0, agentJarFileName
                    .lastIndexOf('.'));
            if ((!name.contains("servlet")) && (!name.contains("sentinel-dubbo-adapter"))) {
                try {
                    addJarToSystemPath(instrProxy, new JarFile(EmbeddedJarUtil.getJarFileInAgent(name, "ahas-plugin")));
                } catch (Throwable localThrowable) {
                }
            }
        }
    }

    private static void addJarToClassPath(Instrumentation instrProxy, JarFile jarfile) {
        instrProxy.appendToBootstrapClassLoaderSearch(jarfile);
    }

    private static void addJarToSystemPath(Instrumentation instrProxy, JarFile jarfile) {
        instrProxy.appendToSystemClassLoaderSearch(jarfile);
    }

    private static void forceReplaceAgentApi(Instrumentation instrProxy, JarFile bridgeJarFile) throws IOException {
        for (String agentApi : AGENT_APIS) {
            JarEntry jarEntry = bridgeJarFile.getJarEntry(agentApi + ".class");
            if (jarEntry != null) {

                byte[] bytes = read(bridgeJarFile.getInputStream(jarEntry), true);
                instrProxy.addTransformer(new ApiClassTransformer(agentApi, bytes), true);
            }
        }
    }

    static byte[] read(InputStream input, boolean closeInputStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        EmbeddedJarUtil.copy(input, outStream, input.available(), closeInputStream);
        return outStream.toByteArray();
    }

    static final class ApiClassTransformer implements ClassFileTransformer {
        private Map<String, byte[]> apiBytes = new HashMap();

        ApiClassTransformer(String apiName, byte[] bytes) {
            this.apiBytes.put(apiName, bytes);
        }


        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            return (byte[]) this.apiBytes.get(className);
        }
    }

    static final class StatsClassTransformer
            implements ClassFileTransformer {
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            return null;
        }
    }
}
