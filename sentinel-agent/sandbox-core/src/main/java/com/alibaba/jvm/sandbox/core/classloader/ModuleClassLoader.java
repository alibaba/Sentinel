package com.alibaba.jvm.sandbox.core.classloader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;

import static com.alibaba.jvm.sandbox.core.util.SandboxReflectUtils.*;

/**
 * 模块类加载器
 * Created by luanjia on 16/10/5.
 */
public class ModuleClassLoader extends RoutingURLClassLoader {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final File moduleJarFile;
    private final File tempModuleJarFile;
    private final long checksumCRC32;

    private static File copyToTempFile(final File moduleJarFile) throws IOException {
        File tempFile = File.createTempFile("sandbox_module_jar_", ".jar");
        tempFile.deleteOnExit();
        FileUtils.copyFile(moduleJarFile, tempFile);
        return tempFile;
    }

    public ModuleClassLoader(final File moduleJarFile,
                             final ClassLoader sandboxClassLoader) throws IOException {
        this(moduleJarFile, copyToTempFile(moduleJarFile), sandboxClassLoader);
    }

    private ModuleClassLoader(final File moduleJarFile,
                              final File tempModuleJarFile,
                              final ClassLoader sandboxClassLoader) throws IOException {
        super(
                new URL[]{new URL("file:" + tempModuleJarFile.getPath())},
                new Routing(
                        sandboxClassLoader,
                        "^com\\.alibaba\\.jvm\\.sandbox\\.api\\..*",
                        "^javax\\.servlet\\..*",
                        "^javax\\.annotation\\.Resource.*$"
                )
        );
        this.checksumCRC32 = FileUtils.checksumCRC32(moduleJarFile);
        this.moduleJarFile = moduleJarFile;
        this.tempModuleJarFile = tempModuleJarFile;

        try {
            cleanProtectionDomainWhichCameFromModuleClassLoader();
            logger.debug("clean ProtectionDomain in {}'s acc success.", this);
        } catch (Throwable e) {
            logger.warn("clean ProtectionDomain in {}'s acc failed.", this, e);
        }

    }

    /**
     * 清理来自URLClassLoader.acc.ProtectionDomain[]中，来自上一个ModuleClassLoader的ProtectionDomain
     * 这样写好蛋疼，而且还有不兼容的风险，从JDK6+都必须要这样清理，但我找不出更好的办法。
     * 在重置沙箱时，遇到MgrModule模块无法正确卸载类的情况，主要的原因是在于URLClassLoader.acc.ProtectionDomain[]中包含了上一个ModuleClassLoader的引用
     * 所以必须要在这里清理掉，否则随着重置次数的增加，类会越累积越多
     */
    private void cleanProtectionDomainWhichCameFromModuleClassLoader() {

        // got ProtectionDomain[] from URLClassLoader's acc
        final AccessControlContext acc = unCaughtGetClassDeclaredJavaFieldValue(URLClassLoader.class, "acc", this);
        final ProtectionDomain[] protectionDomainArray = unCaughtInvokeMethod(
                unCaughtGetClassDeclaredJavaMethod(AccessControlContext.class, "getContext"),
                acc
        );

        // remove ProtectionDomain which loader is ModuleClassLoader
        final Set<ProtectionDomain> cleanProtectionDomainSet = new LinkedHashSet<ProtectionDomain>();
        if (ArrayUtils.isNotEmpty(protectionDomainArray)) {
            for (final ProtectionDomain protectionDomain : protectionDomainArray) {
                if (protectionDomain.getClassLoader() == null
                        || !StringUtils.equals(ModuleClassLoader.class.getName(), protectionDomain.getClassLoader().getClass().getName())) {
                    cleanProtectionDomainSet.add(protectionDomain);
                }
            }
        }

        // rewrite acc
        final AccessControlContext newAcc = new AccessControlContext(cleanProtectionDomainSet.toArray(new ProtectionDomain[]{}));
        unCaughtSetClassDeclaredJavaFieldValue(URLClassLoader.class, "acc", this, newAcc);

    }


    public void closeIfPossible() {

        try {

            // 如果是JDK7+的版本, URLClassLoader实现了Closeable接口，直接调用即可
            if (this instanceof Closeable) {
                logger.debug("JDK is 1.7+, use URLClassLoader[file={}].close()", moduleJarFile);
                try {
                    final Method closeMethod = unCaughtGetClassDeclaredJavaMethod(URLClassLoader.class, "close");
                    closeMethod.invoke(this);
                } catch (Throwable cause) {
                    logger.warn("close ModuleClassLoader[file={}] failed. JDK7+", moduleJarFile, cause);
                }
                return;
            }


            // 对于JDK6的版本，URLClassLoader要关闭起来就显得有点麻烦，这里弄了一大段代码来稍微处理下
            // 而且还不能保证一定释放干净了，至少释放JAR文件句柄是没有什么问题了
            try {
                logger.debug("JDK is less then 1.7+, use File.release()", moduleJarFile);
                final Object sun_misc_URLClassPath = unCaughtGetClassDeclaredJavaFieldValue(URLClassLoader.class, "ucp", this);
                final Object java_util_Collection = unCaughtGetClassDeclaredJavaFieldValue(sun_misc_URLClassPath.getClass(), "loaders", sun_misc_URLClassPath);

                for (Object sun_misc_URLClassPath_JarLoader :
                        ((Collection) java_util_Collection).toArray()) {
                    try {
                        final JarFile java_util_jar_JarFile = unCaughtGetClassDeclaredJavaFieldValue(
                                sun_misc_URLClassPath_JarLoader.getClass(),
                                "jar",
                                sun_misc_URLClassPath_JarLoader
                        );
                        java_util_jar_JarFile.close();
                    } catch (Throwable t) {
                        // if we got this far, this is probably not a JAR loader so skip it
                    }
                }

            } catch (Throwable cause) {
                logger.warn("close ModuleClassLoader[file={}] failed. probably not a HOTSPOT VM", moduleJarFile, cause);
            }

        } finally {

            // 在这里删除掉临时文件
            FileUtils.deleteQuietly(tempModuleJarFile);

        }

    }

    public File getModuleJarFile() {
        return moduleJarFile;
    }

    @Override
    public String toString() {
        return String.format("ModuleClassLoader[crc32=%s;file=%s;]", checksumCRC32, moduleJarFile);
    }

    public long getChecksumCRC32() {
        return checksumCRC32;
    }

}