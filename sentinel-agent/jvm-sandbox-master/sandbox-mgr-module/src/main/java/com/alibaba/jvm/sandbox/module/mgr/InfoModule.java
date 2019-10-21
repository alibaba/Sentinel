package com.alibaba.jvm.sandbox.module.mgr;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.annotation.Command;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.resource.ConfigInfo;
import org.kohsuke.MetaInfServices;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 沙箱信息模块
 *
 * @author luanjia@taobao.com
 */
@MetaInfServices(Module.class)
@Information(id = "sandbox-info", version = "0.0.4", author = "luanjia@taobao.com")
public class InfoModule implements Module {

    @Resource
    private ConfigInfo configInfo;

//    @Resource
//    private EventMonitor eventMonitor;

    //@Http("/version")
    @Command("version")
    public void version(final PrintWriter writer) throws IOException {

        final StringBuilder versionSB = new StringBuilder()
                .append("                    NAMESPACE : ").append(configInfo.getNamespace()).append("\n")
                .append("                      VERSION : ").append(configInfo.getVersion()).append("\n")
                .append("                         MODE : ").append(configInfo.getMode()).append("\n")
                .append("                  SERVER_ADDR : ").append(configInfo.getServerAddress().getHostName()).append("\n")
                .append("                  SERVER_PORT : ").append(configInfo.getServerAddress().getPort()).append("\n")
                .append("               UNSAFE_SUPPORT : ").append(configInfo.isEnableUnsafe() ? "ENABLE" : "DISABLE").append("\n")
                .append("                 SANDBOX_HOME : ").append(configInfo.getHome()).append("\n")
                .append("            SYSTEM_MODULE_LIB : ").append(configInfo.getSystemModuleLibPath()).append("\n")
                .append("              USER_MODULE_LIB : ").append(configInfo.getUserModuleLibPath()).append("\n")
                .append("          SYSTEM_PROVIDER_LIB : ").append(configInfo.getSystemProviderLibPath()).append("\n")
                .append("           EVENT_POOL_SUPPORT : ").append(configInfo.isEnableEventPool() ? "ENABLE" : "DISABLE");
//                       /*############################# : */
//        if (configInfo.isEnableEventPool()) {
//            versionSB
//                    .append("\n")
//                           /*############################# : */
//                    .append("  EVENT_POOL_PER_KEY_IDLE_MIN : ").append(configInfo.getEventPoolMinIdlePerEvent()).append("\n")
//                    .append("  EVENT_POOL_PER_KEY_IDLE_MAX : ").append(configInfo.getEventPoolMaxIdlePerEvent()).append("\n")
//                    .append(" EVENT_POOL_PER_KEY_TOTAL_MAX : ").append(configInfo.getEventPoolMaxTotalPerEvent()).append("\n")
//                    .append("             EVENT_POOL_TOTAL : ").append(configInfo.getEventPoolMaxTotal())
//            ;
//        }

        writer.println(versionSB.toString());
        writer.flush();

    }

    //@Http("/event-pool")
    @Command("event-pool")
    public void eventPool(final PrintWriter writer) throws IOException {

        for (Event.Type type : Event.Type.values()) {
            writer.println(String.format("%18s : %d / %d", type, 0, 0));
        }

    }

}
