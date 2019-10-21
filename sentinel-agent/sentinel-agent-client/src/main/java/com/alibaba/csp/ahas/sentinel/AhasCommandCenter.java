package com.alibaba.csp.ahas.sentinel;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandHandlerProvider;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.log.CommandCenterLog;
import com.alibaba.csp.sentinel.spi.SpiOrder;
import com.alibaba.csp.sentinel.transport.CommandCenter;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.transport.http.HttpEventTask;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@SpiOrder(-10000)
public class AhasCommandCenter implements CommandCenter {
    private static final int PORT_UNINITIALIZED = -1;

    private static final int DEFAULT_SERVER_SO_TIMEOUT = 3000;
    private static final int DEFAULT_PORT = 8719;

    @SuppressWarnings("rawtypes")
    private static final Map<String, CommandHandler> handlerMap = new ConcurrentHashMap<String, CommandHandler>();

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private ExecutorService executor = Executors.newSingleThreadExecutor(
            new NamedThreadFactory("sentinel-command-center-executor"));
    private ExecutorService bizExecutor;

    private ServerSocket socketReference;

    DefaultSentinelSdkService service = new DefaultSentinelSdkService();

    public AhasCommandCenter() {
        this.executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("sentinel-command-center-executor"));
        this.service = new DefaultSentinelSdkService();
    }
    @Override
    @SuppressWarnings("rawtypes")
    public void beforeStart() throws Exception {
        Map<String, CommandHandler> handlers = CommandHandlerProvider.getInstance().namedHandlers();
        registerCommands(handlers);
    }

    @Override
    public void start() throws Exception {
        int nThreads = Runtime.getRuntime().availableProcessors();
        this.bizExecutor = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(10),
                new NamedThreadFactory("sentinel-command-center-service-executor"),
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        CommandCenterLog.info("EventTask rejected");
                        throw new RejectedExecutionException();
                    }
                });

        Runnable serverInitTask = new Runnable() {
            int port;

            {
                try {
                    port = Integer.parseInt(TransportConfig.getPort());
                } catch (Exception e) {
                    port = DEFAULT_PORT;
                }
            }

            @Override
            public void run() {
                boolean success = false;
                ServerSocket serverSocket = getServerSocketFromBasePort(port);

                if (serverSocket != null) {
                    CommandCenterLog.info("[CommandCenter] Begin listening at port " + serverSocket.getLocalPort());
                    socketReference = serverSocket;
                    executor.submit(new ServerThread(serverSocket));
                    success = true;
                    port = serverSocket.getLocalPort();
                } else {
                    CommandCenterLog.info("[CommandCenter] chooses port fail, http command center will not work");
                }

                if (!success) {
                    port = PORT_UNINITIALIZED;
                }

                TransportConfig.setRuntimePort(port);
                executor.shutdown();
            }

        };

        new Thread(serverInitTask).start();
    }

    private static ServerSocket getServerSocketFromBasePort(int basePort) {
        int tryCount = 0;
        while (true) {
            try {
                ServerSocket server = new ServerSocket(basePort + tryCount / 3, 100);
                server.setReuseAddress(true);
                return server;
            } catch (IOException e) {
                tryCount++;
                try {
                    TimeUnit.MILLISECONDS.sleep(30);
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public void stop() throws Exception {
        if (socketReference != null) {
            try {
                socketReference.close();
            } catch (IOException e) {
                CommandCenterLog.warn("Error when releasing the server socket", e);
            }
        }
        bizExecutor.shutdownNow();
        executor.shutdownNow();
        TransportConfig.setRuntimePort(PORT_UNINITIALIZED);
        handlerMap.clear();
    }

    public static Set<String> getCommands() {
        return handlerMap.keySet();
    }

    class ServerThread extends Thread {

        private ServerSocket serverSocket;

        ServerThread(ServerSocket s) {
            this.serverSocket = s;
            setName("sentinel-courier-server-accept-thread");
        }

        @Override
        public void run() {
            while (true) {
                Socket socket = null;
                try {
                    socket = this.serverSocket.accept();
                    setSocketSoTimeout(socket);
                    HttpEventTask eventTask = new HttpEventTask(socket);
                    bizExecutor.submit(eventTask);
                } catch (Exception e) {
                    CommandCenterLog.info("Server error", e);
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (Exception e1) {
                            CommandCenterLog.info("Error when closing an opened socket", e1);
                        }
                    }
                    try {
                        // In case of infinite log.
                        Thread.sleep(10);
                    } catch (InterruptedException e1) {
                        // Indicates the task should stop.
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static CommandHandler getHandler(String commandName) {
        return handlerMap.get(commandName);
    }

    @SuppressWarnings("rawtypes")
    public static void registerCommands(Map<String, CommandHandler> handlerMap) {
        if (handlerMap != null) {
            for (Map.Entry<String, CommandHandler> e : handlerMap.entrySet()) {
                registerCommand(e.getKey(), e.getValue());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static void registerCommand(String commandName, CommandHandler handler) {
        if (StringUtil.isEmpty(commandName)) {
            return;
        }

        if (handlerMap.containsKey(commandName)) {
            CommandCenterLog.warn("Register failed (duplicate command): " + commandName);
            return;
        }

        handlerMap.put(commandName, handler);
    }

    private void setSocketSoTimeout(Socket socket) throws SocketException {
        if (socket != null) {
            socket.setSoTimeout(DEFAULT_SERVER_SO_TIMEOUT);
        }
    }


//   public void beforeStart() throws Exception {
//   }
//
//   public void start() throws Exception {
//      this.service.init();
//   }
//
//   public void stop() throws Exception {
//   }
}


//
//import com.alibaba.csp.sentinel.spi.SpiOrder;
//import com.alibaba.csp.sentinel.transport.CommandCenter;
//
///**
// * Sentinel command center based on Ahas gateway.
// *
// * @author leyou 2018/12/10
// */
//@SpiOrder(-10000)
//public class AhasCommandCenter implements CommandCenter {
//
//    DefaultSentinelSdkService service = new DefaultSentinelSdkService();
//
//    @Override
//    public void beforeStart() throws Exception { }
//
//    @Override
//    public void start() throws Exception {
//        service.init();
//    }
//
//    @Override
//    public void stop() throws Exception { }
//}
