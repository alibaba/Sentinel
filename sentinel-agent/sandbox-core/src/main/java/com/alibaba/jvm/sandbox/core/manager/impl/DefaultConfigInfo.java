package com.alibaba.jvm.sandbox.core.manager.impl;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.resource.ConfigInfo;
import com.alibaba.jvm.sandbox.core.CoreConfigure;
import com.alibaba.jvm.sandbox.core.server.ProxyCoreServer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

/**
 * 默认配置信息实现
 *
 * @author luanjia@taobao.com
 */
class DefaultConfigInfo implements ConfigInfo {

    private final CoreConfigure cfg;

    public DefaultConfigInfo(CoreConfigure cfg) {
        this.cfg = cfg;
    }

    @Override
    public String getNamespace() {
        return cfg.getNamespace();
    }

    @Override
    public Information.Mode getMode() {
        return cfg.getLaunchMode();
    }

    @Override
    public boolean isEnableUnsafe() {
        return cfg.isEnableUnsafe();
    }

    @Override
    public String getHome() {
        return cfg.getJvmSandboxHome();
    }

    @Override
    public String getModuleLibPath() {
        return getSystemModuleLibPath();
    }

    @Override
    public String getSystemModuleLibPath() {
        return cfg.getSystemModuleLibPath();
    }

    @Override
    public String getSystemProviderLibPath() {
        return cfg.getProviderLibPath();
    }

    @Override
    public String getUserModuleLibPath() {
        return cfg.getUserModuleLibPath();
    }

    @Override
    public String[] getUserModuleLibPaths() {
        return cfg.getUserModuleLibPaths();
    }

    @Override
    public boolean isEnableEventPool() {
        return false;
    }

    @Deprecated
    @Override
    public int getEventPoolKeyMin() {
        return 0;
    }

    @Deprecated
    @Override
    public int getEventPoolKeyMax() {
        return 0;
    }

    @Deprecated
    @Override
    public int getEventPoolTotal() {
        return 0;
    }

    @Override
    public int getEventPoolMaxTotal() {
        return 0;
    }

    @Override
    public int getEventPoolMinIdlePerEvent() {
        return 0;
    }

    @Override
    public int getEventPoolMaxIdlePerEvent() {
        return 0;
    }

    @Override
    public int getEventPoolMaxTotalPerEvent() {
        return 0;
    }

    @Override
    public InetSocketAddress getServerAddress() {
        try {
            return ProxyCoreServer.getInstance().getLocal();
        } catch (Throwable cause) {
            return new InetSocketAddress("0.0.0.0", 0);
        }
    }

    @Override
    public String getServerCharset() {
        return cfg.getServerCharset().name();
    }

    @Override
    public String getVersion() {
        final InputStream is = getClass().getResourceAsStream("/com/alibaba/jvm/sandbox/version");
        try {
            return IOUtils.toString(is);
        } catch (IOException e) {
            // impossible
            return "UNKNOW_VERSION";
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
