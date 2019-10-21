package com.alibaba.jvm.sandbox.core.server.jetty;

import com.alibaba.jvm.sandbox.core.CoreConfigure;
import com.alibaba.jvm.sandbox.core.JvmSandbox;
import com.alibaba.jvm.sandbox.core.server.CoreServer;
import com.alibaba.jvm.sandbox.core.server.jetty.servlet.ModuleHttpServlet;
import com.alibaba.jvm.sandbox.core.server.jetty.servlet.WebSocketAcceptorServlet;
import com.alibaba.jvm.sandbox.core.util.Initializer;
import com.alibaba.jvm.sandbox.core.util.LogbackUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;

import static com.alibaba.jvm.sandbox.core.util.NetworkUtils.isPortInUsing;
import static java.lang.String.format;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

/**
 * Jetty实现的Http服务器
 *
 * @author luanjia@taobao.com
 */
public class JettyCoreServer implements CoreServer {

    private static volatile CoreServer coreServer;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Initializer initializer = new Initializer(true);

    private Server httpServer;
    private CoreConfigure cfg;
    private JvmSandbox jvmSandbox;

    /**
     * 单例
     *
     * @return CoreServer单例
     */
    public static CoreServer getInstance() {
        if (null == coreServer) {
            synchronized (CoreServer.class) {
                if (null == coreServer) {
                    coreServer = new JettyCoreServer();
                }
            }
        }
        return coreServer;
    }

    public boolean isBind() {
        return initializer.isInitialized();
    }

    @Override
    public void unbind() throws IOException {
        try {

            initializer.destroyProcess(new Initializer.Processor() {
                @Override
                public void process() throws Throwable {

                    if (null != httpServer) {

                        // stop http server
                        logger.info("{} is stopping", JettyCoreServer.this);
                        httpServer.stop();

                    }

                }
            });

            // destroy http server
            logger.info("{} is destroying", this);
            while (!httpServer.isStopped());
            httpServer.destroy();

        } catch (Throwable cause) {
            logger.warn("{} unBind failed.", this, cause);
            throw new IOException("unBind failed.", cause);
        }
    }

    @Override
    public InetSocketAddress getLocal() throws IOException {
        if (!isBind()
                || null == httpServer) {
            throw new IOException("server was not bind yet.");
        }

        SelectChannelConnector scc = null;
        final Connector[] connectorArray = httpServer.getConnectors();
        if (null != connectorArray) {
            for (final Connector connector : connectorArray) {
                if (connector instanceof SelectChannelConnector) {
                    scc = (SelectChannelConnector) connector;
                    break;
                }//if
            }//for
        }//if

        if (null == scc) {
            throw new IllegalStateException("not found SelectChannelConnector");
        }

        return new InetSocketAddress(
                scc.getHost(),
                scc.getLocalPort()
        );
    }

    /*
     * 初始化Jetty's ContextHandler
     */
    private void initJettyContextHandler() {
        final String namespace = cfg.getNamespace();
        final ServletContextHandler context = new ServletContextHandler(NO_SESSIONS);

        final String contextPath = "/sandbox/" + namespace;
        context.setContextPath(contextPath);
        context.setClassLoader(getClass().getClassLoader());

        // web-socket-servlet
        final String wsPathSpec = "/module/websocket/*";
        logger.info("initializing ws-http-handler. path={}", contextPath + wsPathSpec);
        //noinspection deprecation
        context.addServlet(
                new ServletHolder(new WebSocketAcceptorServlet(jvmSandbox.getCoreModuleManager())),
                wsPathSpec
        );

        // module-http-servlet
        final String pathSpec = "/module/http/*";
        logger.info("initializing http-handler. path={}", contextPath + pathSpec);
        context.addServlet(
                new ServletHolder(new ModuleHttpServlet(cfg, jvmSandbox.getCoreModuleManager())),
                pathSpec
        );

        httpServer.setHandler(context);
    }

    private void initHttpServer() {

        final String serverIp = cfg.getServerIp();
        final int serverPort = cfg.getServerPort();

        // 如果IP:PORT已经被占用，则无法继续被绑定
        // 这里说明下为什么要这么无聊加个这个判断，让Jetty的Server.bind()抛出异常不是更好么？
        // 比较郁闷的是，如果这个端口的绑定是"SO_REUSEADDR"端口可重用的模式，那么这个server是能正常启动，但无法正常工作的
        // 所以这里必须先主动检查一次端口占用情况，当然了，这里也会存在一定的并发问题，BUT，我认为这种概率事件我可以选择暂时忽略
        if (isPortInUsing(serverIp, serverPort)) {
            throw new IllegalStateException(format("address[%s:%s] already in using, server bind failed.",
                    serverIp,
                    serverPort
            ));
        }

        httpServer = new Server(new InetSocketAddress(serverIp, serverPort));
        QueuedThreadPool qtp = new QueuedThreadPool();
        // jetty线程设置为daemon，防止应用启动失败进程无法正常退出
        qtp.setDaemon(true);
        qtp.setName("sandbox-jetty-qtp-" + qtp.hashCode());
        httpServer.setThreadPool(qtp);
    }

    @Override
    public synchronized void bind(final CoreConfigure cfg, final Instrumentation inst) throws IOException {
        this.cfg = cfg;
        try {
            initializer.initProcess(new Initializer.Processor() {
                @Override
                public void process() throws Throwable {
                    LogbackUtils.init(
                            cfg.getNamespace(),
                            cfg.getCfgLibPath() + File.separator + "sandbox-logback.xml"
                    );
                    logger.info("initializing server. cfg={}", cfg);
                    jvmSandbox = new JvmSandbox(cfg, inst);
                    initHttpServer();
                    initJettyContextHandler();
                    httpServer.start();
                }
            });

            // 初始化加载所有的模块
            try {
                jvmSandbox.getCoreModuleManager().reset();
            } catch (Throwable cause) {
                logger.warn("reset occur error when initializing.", cause);
            }

            final InetSocketAddress local = getLocal();
            logger.info("initialized server. actual bind to {}:{}",
                    local.getHostName(),
                    local.getPort()
            );

        } catch (Throwable cause) {

            // 这里会抛出到目标应用层，所以在这里留下错误信息
            logger.warn("initialize server failed.", cause);

            // 对外抛出到目标应用中
            throw new IOException("server bind failed.", cause);
        }

        logger.info("{} bind success.", this);
    }

    @Override
    public void destroy() {

        // 关闭JVM-SANDBOX
        jvmSandbox.destroy();

        // 关闭HTTP服务器
        if (isBind()) {
            try {
                unbind();
            } catch (IOException e) {
                logger.warn("{} unBind failed when destroy.", this, e);
            }
        }

        // 关闭LOGBACK
        LogbackUtils.destroy();
    }

    @Override
    public String toString() {
        return format("server[%s:%s]", cfg.getServerIp(), cfg.getServerPort());
    }
}
