package com.alibaba.jvm.sandbox.module.debug;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.ModuleLifecycle;
import com.alibaba.jvm.sandbox.api.annotation.Command;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.filter.ExtFilter;
import com.alibaba.jvm.sandbox.api.http.printer.ConcurrentLinkedQueuePrinter;
import com.alibaba.jvm.sandbox.api.http.printer.Printer;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.alibaba.jvm.sandbox.api.resource.ModuleManager;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.util.Map;

import static com.alibaba.jvm.sandbox.module.debug.ParamSupported.getParameter;

/**
 * 不增强任何类，只为体验沙箱模块生命周期
 * ModuleLifecycle的方法是在模块发生变更前调用的
 * 在变更前需要做处理时，可以通过实现ModuleLifecycle接口进行控制
 * 在变更前不需要做任何处理时，可以不实现ModuleLifecycle接口
 * onLoad，load
 * onActivity，activity
 * onFrozen，frozen
 * onUnload，unload
 * loadCompleted
 */
@MetaInfServices(Module.class)
@Information(id = "debug-lifecycle", version = "0.0.1", author = "luanjia@taobao.com")
public class DebugLifeCycleModule implements Module, ModuleLifecycle{

    private final Logger lifeCLogger = LoggerFactory.getLogger("DEBUG-LIFECYCLE-LOGGER");

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    @Resource
    private ModuleManager moduleManager;

    @Override
    public void onLoad() throws Throwable {
        lifeCLogger.info("sandbox-module-debug-lifecycle onLoaded.");
    }

    @Override
    public void onUnload() throws Throwable {//卸载模块后，删除增强的内容
        lifeCLogger.info("sandbox-module-debug-lifecycle onUnload.");
    }

    @Override
    public void onActive() throws Throwable {
        lifeCLogger.info("sandbox-module-debug-lifecycle onActive.");
    }

    @Override
    public void onFrozen() throws Throwable {
        lifeCLogger.info("sandbox-module-debug-lifecycle onFrozen.");
    }

    @Override
    public void loadCompleted() {
        lifeCLogger.info("sandbox-module-debug-lifecycle loadCompleted.");
    }

    @Command("control")
    public void control(final Map<String, String> param, final PrintWriter writer){
        final Printer printer = new ConcurrentLinkedQueuePrinter(writer);

        // --- 解析参数 ---

        final String cnPattern = getParameter(param, "class");
        final String mnPattern = getParameter(param, "method");
        lifeCLogger.info("param.class={}", cnPattern);
        lifeCLogger.info("param.method={}", mnPattern);


        int watcherId = moduleEventWatcher.watch(
                new ExtFilter(){//不增强类，这里只是体验sandbox的生命周期，ExtFilter新增了增强接口的所有实现类，到boostrap ClassLoader中加载类 的能力

                    @Override
                    public boolean doClassFilter(int access, String javaClassName, String superClassTypeJavaClassName, String[] interfaceTypeJavaClassNameArray, String[] annotationTypeJavaClassNameArray) {
//                        if (cnPattern != null || !mnPattern.isEmpty())
//                            return javaClassName.matches(cnPattern);
                        return false;
                    }

                    @Override
                    public boolean doMethodFilter(int access, String javaMethodName, String[] parameterTypeJavaClassNameArray, String[] throwsTypeJavaClassNameArray, String[] annotationTypeJavaClassNameArray) {
//                        if (mnPattern != null || !mnPattern.isEmpty())
//                            return javaMethodName.matches(mnPattern);
                        return false;
                    }

                    @Override
                    public boolean isIncludeSubClasses() {//搜索子类或实现类
                        return true;
                    }

                    @Override
                    public boolean isIncludeBootstrap() {//搜索来自BootstrapClassLoader所加载的类
                        return true;
                    }
                },
                new EventListener() {//监听到的事件，不做任何处理
                    @Override
                    public void onEvent(Event event) throws Throwable {

                    }
                },
                new ModuleEventWatcher.Progress() {//如果有增强类，可以通过这里查看增强的进度
                    @Override
                    public void begin(int total) {
                        lifeCLogger.info("Begin to transform class,total={}", total);
                    }

                    @Override
                    public void progressOnSuccess(Class clazz, int index) {
                        lifeCLogger.info("Transform class success,class={},index={}", clazz.getName(), index);
                    }

                    @Override
                    public void progressOnFailed(Class clazz, int index, Throwable cause) {
                        lifeCLogger.error("Transform class fail,class={},index={}", clazz.getName(), index, cause);
                    }

                    @Override
                    public void finish(int cCnt, int mCnt) {
                        lifeCLogger.info("Finish to transform class,classCount={},methodCount={}", cCnt, mCnt);
                    }
                },
                Event.Type.BEFORE,
                Event.Type.LINE,
                Event.Type.RETURN,
                Event.Type.THROWS);

        lifeCLogger.info("Add watcher success,watcher id = [{}]", watcherId);

        try {
            // 模块load完成后，模块已经被激活
            lifeCLogger.info("after sandbox-module-debug-lifecycle load Completed，module isActivated = {}", moduleManager.isActivated("debug-lifecycle"));

            //冻结模块
            lifeCLogger.info("sandbox-module-debug-lifecycle start frozen");
            moduleManager.frozen("debug-lifecycle");
            lifeCLogger.info("sandbox-module-debug-lifecycle frozen is over");

            //激活模块
            lifeCLogger.info("sandbox-module-debug-lifecycle start active");
            moduleManager.active("debug-lifecycle");
            lifeCLogger.info("sandbox-module-debug-lifecycle active is over");

            //刷新模块
            lifeCLogger.info("sandbox-module-debug-lifecycle start flush");
            moduleManager.flush(false);
            lifeCLogger.info("sandbox-module-debug-lifecycle flush is over");

            //重置模块
            lifeCLogger.info("sandbox-module-debug-lifecycle start reset");
            moduleManager.reset();
            lifeCLogger.info("sandbox-module-debug-lifecycle reset is over");

        } catch (Throwable e) {
            lifeCLogger.error("sandbox lifecycle is fail, " + e.getCause());
        }
    }
}
