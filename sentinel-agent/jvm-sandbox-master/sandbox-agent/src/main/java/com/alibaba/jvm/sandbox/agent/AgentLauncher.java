package com.alibaba.jvm.sandbox.agent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * SandboxAgent启动器
 * <ul>
 * <li>这个类的所有静态属性都必须和版本、环境无关</li>
 * <li>这个类删除、修改方法时必须考虑多版本情况下，兼容性问题!</li>
 * </ul>
 *
 * @author luanjia@taobao.com
 */
public class AgentLauncher {

    private static String getSandboxCfgPath(String sandboxHome) {
        return sandboxHome + File.separatorChar + "cfg";
    }

    private static String getSandboxModulePath(String sandboxHome) {
        return sandboxHome + File.separatorChar + "module";
    }

    private static String getSandboxCoreJarPath(String sandboxHome) {
        return sandboxHome + File.separatorChar + "lib" + File.separator + "sandbox-core.jar";
    }

    private static String getSandboxSpyJarPath(String sandboxHome) {
        return sandboxHome + File.separatorChar + "lib" + File.separator + "sandbox-spy.jar";
    }

    private static String getSandboxPropertiesPath(String sandboxHome) {
        return getSandboxCfgPath(sandboxHome) + File.separator + "sandbox.properties";
    }

    private static String getSandboxProviderPath(String sandboxHome) {
        return sandboxHome + File.separatorChar + "provider";
    }


    // sandbox默认主目录
    private static final String DEFAULT_SANDBOX_HOME
            = new File(AgentLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile())
            .getParentFile()
            .getParent();

    private static final String SANDBOX_USER_MODULE_PATH
            = DEFAULT_SANDBOX_HOME
            + File.separator + "sandbox-module";

    // 启动模式: agent方式加载
    private static final String LAUNCH_MODE_AGENT = "agent";

    // 启动模式: attach方式加载
    private static final String LAUNCH_MODE_ATTACH = "attach";

    // 启动默认
    private static String LAUNCH_MODE;

    // agentmain上来的结果输出到文件${HOME}/.sandbox.token
    private static final String RESULT_FILE_PATH = System.getProperties().getProperty("user.home")
            + File.separator + ".sandbox.token";

    // 全局持有ClassLoader用于隔离sandbox实现
    private static volatile Map<String/*NAMESPACE*/, SandboxClassLoader> sandboxClassLoaderMap
            = new ConcurrentHashMap<String, SandboxClassLoader>();

    private static final String CLASS_OF_CORE_CONFIGURE = "com.alibaba.jvm.sandbox.core.CoreConfigure";
    // private static final String CLASS_OF_JETTY_CORE_SERVER = "com.alibaba.jvm.sandbox.core.server.jetty.JettyCoreServer";
    private static final String CLASS_OF_PROXY_CORE_SERVER = "com.alibaba.jvm.sandbox.core.server.ProxyCoreServer";


    /**
     * 启动加载
     *
     * @param featureString 启动参数
     *                      [namespace,prop]
     * @param inst          inst
     */
    public static void premain(String featureString, Instrumentation inst) {
        LAUNCH_MODE = LAUNCH_MODE_AGENT;
        install(toFeatureMap(featureString), inst);
    }

    /**
     * 动态加载
     *
     * @param featureString 启动参数
     *                      [namespace,token,ip,port,prop]
     * @param inst          inst
     */
    public static void agentmain(String featureString, Instrumentation inst) {
        LAUNCH_MODE = LAUNCH_MODE_ATTACH;
        final Map<String, String> featureMap = toFeatureMap(featureString);
        writeAttachResult(
                getNamespace(featureMap),
                getToken(featureMap),
                install(featureMap, inst)
        );
    }

    /**
     * 写入本次attach的结果
     * <p>
     * NAMESPACE;TOKEN;IP;PORT
     * </p>
     *
     * @param namespace 命名空间
     * @param token     操作TOKEN
     * @param local     服务器监听[IP:PORT]
     */
    private static synchronized void writeAttachResult(final String namespace,
                                                       final String token,
                                                       final InetSocketAddress local) {
        final File file = new File(RESULT_FILE_PATH);
        if (file.exists()
                && (!file.isFile()
                || !file.canWrite())) {
            throw new RuntimeException("write to result file : " + file + " failed.");
        } else {
            FileWriter fw = null;
            try {
                fw = new FileWriter(file, true);
                fw.append(
                        format("%s;%s;%s;%s\n",
                                namespace,
                                token,
                                local.getHostName(),
                                local.getPort()
                        )
                );
                fw.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (null != fw) {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }


    private static synchronized ClassLoader loadOrDefineClassLoader(final String namespace,
                                                                    final String coreJar) throws Throwable {

        final SandboxClassLoader classLoader;

        // 如果已经被启动则返回之前启动的ClassLoader
        if (sandboxClassLoaderMap.containsKey(namespace)
                && null != sandboxClassLoaderMap.get(namespace)) {
            classLoader = sandboxClassLoaderMap.get(namespace);
        }

        // 如果未启动则重新加载
        else {
            classLoader = new SandboxClassLoader(namespace, coreJar);
            sandboxClassLoaderMap.put(namespace, classLoader);
        }

        return classLoader;
    }

    /**
     * 删除指定命名空间下的jvm-sandbox
     *
     * @param namespace 指定命名空间
     * @throws Throwable 删除失败
     */
    @SuppressWarnings("unused")
    public static synchronized void uninstall(final String namespace) throws Throwable {
        final SandboxClassLoader sandboxClassLoader = sandboxClassLoaderMap.get(namespace);
        if (null == sandboxClassLoader) {
            return;
        }

        // 关闭服务器
        final Class<?> classOfProxyServer = sandboxClassLoader.loadClass(CLASS_OF_PROXY_CORE_SERVER);
        classOfProxyServer.getMethod("destroy")
                .invoke(classOfProxyServer.getMethod("getInstance").invoke(null));

        // 关闭SandboxClassLoader
        sandboxClassLoader.closeIfPossible();
        sandboxClassLoaderMap.remove(namespace);
    }

    /**
     * 在当前JVM安装jvm-sandbox
     *
     * @param featureMap 启动参数配置
     * @param inst       inst
     * @return 服务器IP:PORT
     */
    private static synchronized InetSocketAddress install(final Map<String, String> featureMap,
                                                          final Instrumentation inst) {

        final String namespace = getNamespace(featureMap);
        final String propertiesFilePath = getPropertiesFilePath(featureMap);
        final String coreFeatureString = toFeatureString(featureMap);

        try {
            final String home = getSandboxHome(featureMap);
            // 将Spy注入到BootstrapClassLoader
            inst.appendToBootstrapClassLoaderSearch(new JarFile(new File(
                    getSandboxSpyJarPath(home)
                    // SANDBOX_SPY_JAR_PATH
            )));

            // 构造自定义的类加载器，尽量减少Sandbox对现有工程的侵蚀
            final ClassLoader sandboxClassLoader = loadOrDefineClassLoader(
                    namespace,
                    getSandboxCoreJarPath(home)
                    // SANDBOX_CORE_JAR_PATH
            );

            // CoreConfigure类定义
            final Class<?> classOfConfigure = sandboxClassLoader.loadClass(CLASS_OF_CORE_CONFIGURE);

            // 反序列化成CoreConfigure类实例
            final Object objectOfCoreConfigure = classOfConfigure.getMethod("toConfigure", String.class, String.class)
                    .invoke(null, coreFeatureString, propertiesFilePath);

            // CoreServer类定义
            final Class<?> classOfProxyServer = sandboxClassLoader.loadClass(CLASS_OF_PROXY_CORE_SERVER);

            // 获取CoreServer单例
            final Object objectOfProxyServer = classOfProxyServer
                    .getMethod("getInstance")
                    .invoke(null);

            // CoreServer.isBind()
            final boolean isBind = (Boolean) classOfProxyServer.getMethod("isBind").invoke(objectOfProxyServer);


            // 如果未绑定,则需要绑定一个地址
            if (!isBind) {
                try {
                    classOfProxyServer
                            .getMethod("bind", classOfConfigure, Instrumentation.class)
                            .invoke(objectOfProxyServer, objectOfCoreConfigure, inst);
                } catch (Throwable t) {
                    classOfProxyServer.getMethod("destroy").invoke(objectOfProxyServer);
                    throw t;
                }

            }

            // 返回服务器绑定的地址
            return (InetSocketAddress) classOfProxyServer
                    .getMethod("getLocal")
                    .invoke(objectOfProxyServer);


        } catch (Throwable cause) {
            throw new RuntimeException("sandbox attach failed.", cause);
        }

    }


    // ----------------------------------------------- 以下代码用于配置解析 -----------------------------------------------

    private static final String EMPTY_STRING = "";

    private static final String KEY_SANDBOX_HOME = "home";

    private static final String KEY_NAMESPACE = "namespace";
    private static final String DEFAULT_NAMESPACE = "default";

    private static final String KEY_SERVER_IP = "server.ip";
    private static final String DEFAULT_IP = "0.0.0.0";

    private static final String KEY_SERVER_PORT = "server.port";
    private static final String DEFAULT_PORT = "0";

    private static final String KEY_TOKEN = "token";
    private static final String DEFAULT_TOKEN = EMPTY_STRING;

    private static final String KEY_PROPERTIES_FILE_PATH = "prop";

    private static boolean isNotBlankString(final String string) {
        return null != string
                && string.length() > 0
                && !string.matches("^\\s*$");
    }

    private static boolean isBlankString(final String string) {
        return !isNotBlankString(string);
    }

    private static String getDefaultString(final String string, final String defaultString) {
        return isNotBlankString(string)
                ? string
                : defaultString;
    }

    private static Map<String, String> toFeatureMap(final String featureString) {
        final Map<String, String> featureMap = new LinkedHashMap<String, String>();

        // 不对空字符串进行解析
        if (isBlankString(featureString)) {
            return featureMap;
        }

        // KV对片段数组
        final String[] kvPairSegmentArray = featureString.split(";");
        if (kvPairSegmentArray.length <= 0) {
            return featureMap;
        }

        for (String kvPairSegmentString : kvPairSegmentArray) {
            if (isBlankString(kvPairSegmentString)) {
                continue;
            }
            final String[] kvSegmentArray = kvPairSegmentString.split("=");
            if (kvSegmentArray.length != 2
                    || isBlankString(kvSegmentArray[0])
                    || isBlankString(kvSegmentArray[1])) {
                continue;
            }
            featureMap.put(kvSegmentArray[0], kvSegmentArray[1]);
        }

        return featureMap;
    }

    private static String getDefault(final Map<String, String> map, final String key, final String defaultValue) {
        return null != map
                && !map.isEmpty()
                ? getDefaultString(map.get(key), defaultValue)
                : defaultValue;
    }

    private static String OS = System.getProperty("os.name").toLowerCase();

    private static boolean isWindows() {
        return OS.contains("win");
    }

    // 获取主目录
    private static String getSandboxHome(final Map<String, String> featureMap) {
        String home =  getDefault(featureMap, KEY_SANDBOX_HOME, DEFAULT_SANDBOX_HOME);
        if( isWindows() ){
            Matcher m = Pattern.compile("(?i)^[/\\\\]([a-z])[/\\\\]").matcher(home);
            if( m.find() ){
                home = m.replaceFirst("$1:/");
            }            
        }
        return home;
    }

    // 获取命名空间
    private static String getNamespace(final Map<String, String> featureMap) {
        return getDefault(featureMap, KEY_NAMESPACE, DEFAULT_NAMESPACE);
    }

    // 获取TOKEN
    private static String getToken(final Map<String, String> featureMap) {
        return getDefault(featureMap, KEY_TOKEN, DEFAULT_TOKEN);
    }

    // 获取容器配置文件路径
    private static String getPropertiesFilePath(final Map<String, String> featureMap) {
        return getDefault(
                featureMap,
                KEY_PROPERTIES_FILE_PATH,
                getSandboxPropertiesPath(getSandboxHome(featureMap))
                // SANDBOX_PROPERTIES_PATH
        );
    }

    // 如果featureMap中有对应的key值，则将featureMap中的[K,V]对合并到featureSB中
    private static void appendFromFeatureMap(final StringBuilder featureSB,
                                             final Map<String, String> featureMap,
                                             final String key,
                                             final String defaultValue) {
        if (featureMap.containsKey(key)) {
            featureSB.append(format("%s=%s;", key, getDefault(featureMap, key, defaultValue)));
        }
    }

    // 将featureMap中的[K,V]对转换为featureString
    private static String toFeatureString(final Map<String, String> featureMap) {
        final String sandboxHome = getSandboxHome(featureMap);
        final StringBuilder featureSB = new StringBuilder(
                format(
                        ";cfg=%s;system_module=%s;mode=%s;sandbox_home=%s;user_module=%s;provider=%s;namespace=%s;",
                        getSandboxCfgPath(sandboxHome),
                        // SANDBOX_CFG_PATH,
                        getSandboxModulePath(sandboxHome),
                        // SANDBOX_MODULE_PATH,
                        LAUNCH_MODE,
                        sandboxHome,
                        // SANDBOX_HOME,
                        SANDBOX_USER_MODULE_PATH,
                        getSandboxProviderPath(sandboxHome),
                        // SANDBOX_PROVIDER_LIB_PATH,
                        getNamespace(featureMap)
                )
        );

        // 合并IP(如有)
        appendFromFeatureMap(featureSB, featureMap, KEY_SERVER_IP, DEFAULT_IP);

        // 合并PORT(如有)
        appendFromFeatureMap(featureSB, featureMap, KEY_SERVER_PORT, DEFAULT_PORT);

        return featureSB.toString();
    }


}
