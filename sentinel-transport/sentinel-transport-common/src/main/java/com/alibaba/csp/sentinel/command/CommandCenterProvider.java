package com.alibaba.csp.sentinel.command;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.CommandCenter;
import com.alibaba.csp.sentinel.util.SpiLoader;

/**
 * Provider for a universal {@link CommandCenter} instance.
 *
 * @author cdfive
 * @date 2019-01-11
 */
public class CommandCenterProvider {

    private static CommandCenter commandCenter = null;

    static {
        resolveInstance();
    }

    private static void resolveInstance() {
        CommandCenter resolveCommandCenter = SpiLoader.loadFirstInstance(CommandCenter.class);

        if (resolveCommandCenter == null) {
            RecordLog.warn("[CommandCenterProvider] No existing CommandCenter, resolve failed");
        } else {
            commandCenter = resolveCommandCenter;
            RecordLog.info("[CommandCenterProvider] CommandCenter resolved: " + resolveCommandCenter.getClass().getCanonicalName());
        }
    }

    public static CommandCenter getCommandCenter() {
        return commandCenter;
    }

    private CommandCenterProvider() {}
}
