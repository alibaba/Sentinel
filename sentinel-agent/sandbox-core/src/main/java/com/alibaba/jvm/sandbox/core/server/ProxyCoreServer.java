package com.alibaba.jvm.sandbox.core.server;

import com.alibaba.jvm.sandbox.core.CoreConfigure;
import com.alibaba.jvm.sandbox.core.server.jetty.JettyCoreServer;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;

public class ProxyCoreServer implements CoreServer {

    private final static Class<? extends CoreServer> classOfCoreServerImpl
            = JettyCoreServer.class;

    private final CoreServer proxy;

    private ProxyCoreServer(CoreServer proxy) {
        this.proxy = proxy;
    }


    @Override
    public boolean isBind() {
        return proxy.isBind();
    }

    @Override
    public void unbind() throws IOException {
        proxy.unbind();
    }

    @Override
    public InetSocketAddress getLocal() throws IOException {
        return proxy.getLocal();
    }

    @Override
    public void bind(CoreConfigure cfg, Instrumentation inst) throws IOException {
        proxy.bind(cfg, inst);
    }

    @Override
    public void destroy() {
        proxy.destroy();
    }

    @Override
    public String toString() {
        return "proxy:" + proxy.toString();
    }

    public static CoreServer getInstance() {
        try {
            return new ProxyCoreServer(
                    (CoreServer) classOfCoreServerImpl
                            .getMethod("getInstance")
                            .invoke(null)
            );
        } catch (Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

}
