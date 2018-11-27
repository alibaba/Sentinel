package com.alibaba.csp.sentinel.cluster.server.connection;

import java.util.List;

import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * @author xuyue
 * @author Eric Zhao
 */
public class ScanIdleConnectionTask implements Runnable {

    private ConnectionPool connectionPool;

    public ScanIdleConnectionTask(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void run() {
        try {
            int idleSeconds = ClusterServerConfigManager.idleSeconds;
            long idleTime = idleSeconds * 1000;
            if (idleTime < 0) {
                idleTime = 600 * 1000;
            }
            long now = System.currentTimeMillis();
            List<Connection> connections = connectionPool.listAllConnection();
            for (Connection conn : connections) {
                if ((now - conn.getLastReadTime()) > idleTime) {
                    RecordLog.info(
                        String.format("[ScanIdleConnectionTask] The connection <%s:%d> has been idle for <%d>s. "
                            + "It will be closed now.", conn.getRemoteIP(), conn.getRemotePort(), idleSeconds)
                    );
                    conn.close();
                }
            }
        } catch (Throwable t) {
            // TODO: should log here.
        }
    }
}
