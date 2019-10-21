package com.alibaba.jvm.sandbox.qatest.core.manager;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.ModuleException;
import com.alibaba.jvm.sandbox.core.CoreConfigure;
import com.alibaba.jvm.sandbox.core.CoreModule;
import com.alibaba.jvm.sandbox.core.manager.CoreModuleManager;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultCoreModuleManager;
import com.alibaba.jvm.sandbox.core.util.FeatureCodec;
import com.alibaba.jvm.sandbox.qatest.core.mock.EmptyCoreLoadedClassDataSource;
import com.alibaba.jvm.sandbox.qatest.core.mock.EmptyInstrumentation;
import com.alibaba.jvm.sandbox.qatest.core.mock.EmptyProviderManager;
import com.alibaba.jvm.sandbox.qatest.core.util.SandboxModuleJarBuilder;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.alibaba.jvm.sandbox.api.ModuleException.ErrorCode.MODULE_ACTIVE_ERROR;
import static com.alibaba.jvm.sandbox.qatest.core.manager.TracingLifeCycleModule.LifeCycleType.*;
import static java.io.File.createTempFile;
import static org.apache.commons.lang3.ArrayUtils.getLength;
import static org.junit.Assert.assertEquals;

public class CoreModuleManagerTestCase {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Information(id = "broken-on-cinit")
    public static class BrokenOnCInitModule implements Module {
        static {
            if (true) {
                throw new RuntimeException("BROKEN-ON-CINIT");
            }
        }
    }

    @Information(id = "broken-on-load")
    public static class BrokenOnLoadModule extends TracingLifeCycleModule implements Module {
        @Override
        public void onLoad() throws Throwable {
            super.onLoad();
            throw new IllegalAccessException("BROKEN-ON-LOAD");
        }
    }

    @Information(id = "broken-on-unload")
    public static class BrokenOnUnLoadModule extends TracingLifeCycleModule implements Module {
        @Override
        public void onUnload() {
            super.onUnload();
            throw new RuntimeException("BROKEN-ON-UNLOAD");
        }
    }

    @Information(id = "broken-on-active")
    public static class BrokenOnActiveModule extends TracingLifeCycleModule implements Module {
        @Override
        public void onActive() {
            super.onActive();
            throw new RuntimeException("BROKEN-ON-ACTIVE");
        }
    }

    @Information(id = "broken-on-lazy-active", isActiveOnLoad = false)
    public static class BrokenOnLazyActiveModule extends TracingLifeCycleModule implements Module {
        @Override
        public void onActive() {
            super.onActive();
            throw new RuntimeException("BROKEN-ON-LAZY-ACTIVE");
        }
    }

    @Information(id = "broken-on-frozen")
    public static class BrokenOnFrozenModule extends TracingLifeCycleModule implements Module {
        @Override
        public void onFrozen() {
            super.onFrozen();
            throw new RuntimeException("BROKEN-ON-FROZEN");
        }
    }

    @Information(id = "broken-on-load-completed")
    public static class BrokenOnLoadCompletedModule extends TracingLifeCycleModule implements Module {
        @Override
        public void loadCompleted() {
            super.loadCompleted();
            throw new RuntimeException("BROKEN-ON-LOAD-COMPLETED");
        }
    }

    @Information(id = "normal-module")
    public static class NormalModule extends TracingLifeCycleModule implements Module {

    }

    @Information(id = "normal-no-lazy-active-module", isActiveOnLoad = false)
    public static class NormalOnLazyActiveModule extends TracingLifeCycleModule implements Module {

    }

    private CoreConfigure buildingCoreConfigureWithUserModuleLib(final File... moduleJarFileArray) {

        final Set<String> moduleJarFilePathSet = new LinkedHashSet<String>();
        for (final File moduleJarFile : moduleJarFileArray) {
            moduleJarFilePathSet.add(moduleJarFile.getPath());
        }

        final Map<String, String> featureMap = new HashMap<String, String>();
        featureMap.put("user_module", StringUtils.join(moduleJarFilePathSet, ";"));
        featureMap.put("system_module", System.getProperty("user.home"));
        return CoreConfigure.toConfigure(
                new FeatureCodec(';', '=').toString(featureMap),
                null
        );
    }

    private File buildingModuleJarFileWithModuleClass(final File targetModuleJarFile,
                                                      final Class<? extends Module>... classOfModules) throws IOException {
        final SandboxModuleJarBuilder sandboxModuleJarBuilder = SandboxModuleJarBuilder.building(targetModuleJarFile);
        for (final Class<? extends Module> classOfModule : classOfModules) {
            sandboxModuleJarBuilder.putModuleClass(classOfModule);
        }
        return sandboxModuleJarBuilder.build();
    }


    private void assertLoadedModule(final CoreModuleManager coreModuleManager,
                                    final String... exceptUniqueIds) {
        assertEquals(getLength(exceptUniqueIds), coreModuleManager.list().size());
        final Set<String> actualUniqueIdSet = new LinkedHashSet<String>();
        for (final CoreModule coreModule : coreModuleManager.list()) {
            actualUniqueIdSet.add(coreModule.getUniqueId());
        }

        assertEquals(
                new HashSet<String>(Arrays.asList(exceptUniqueIds)),
                actualUniqueIdSet
        );
    }

    private CoreModuleManager buildingCoreModuleManager(final File... moduleJarFiles) throws ModuleException {
        return new DefaultCoreModuleManager(
                buildingCoreConfigureWithUserModuleLib(moduleJarFiles),
                new EmptyInstrumentation(),
                new EmptyCoreLoadedClassDataSource(),
                new EmptyProviderManager()
        ).reset();
    }

    private void assertTracingLifeCycle(final CoreModuleManager coreModuleManager,
                                        final String uniqueId,
                                        final TracingLifeCycleModule.LifeCycleType... exceptLifeCycleTypes) {
        final TracingLifeCycleModule module = (TracingLifeCycleModule) coreModuleManager.get(uniqueId).getModule();
        module.assertTracing(exceptLifeCycleTypes);
    }

    private void assertTracingLifeCycle(final CoreModule coreModule,
                                        final TracingLifeCycleModule.LifeCycleType... exceptLifeCycleTypes) {
        final TracingLifeCycleModule module = (TracingLifeCycleModule) coreModule.getModule();
        module.assertTracing(exceptLifeCycleTypes);
    }

    @Test
    public void test$$CoreModuleManager$$ModuleLifeCycle() throws IOException, ModuleException {
        final CoreModuleManager coreModuleManager
                = buildingCoreModuleManager(
                buildingModuleJarFileWithModuleClass(
                        createTempFile("test-", ".jar"),
                        NormalModule.class
                ));

        assertLoadedModule(coreModuleManager, "normal-module");
        assertTracingLifeCycle(
                coreModuleManager,
                "normal-module",
                LOAD, ACTIVE, LOAD_COMPLETED
        );


        // 卸载模块
        assertTracingLifeCycle(
                coreModuleManager.unload(coreModuleManager.get("normal-module"), false),
                LOAD, ACTIVE, LOAD_COMPLETED, FROZEN, UNLOAD
        );


        // 重新刷新
        coreModuleManager.flush(false);
        assertLoadedModule(coreModuleManager, "normal-module");
        assertTracingLifeCycle(
                coreModuleManager,
                "normal-module",
                LOAD, ACTIVE, LOAD_COMPLETED
        );

        // 冻结-冻结-激活-激活-冻结
        coreModuleManager.frozen(coreModuleManager.get("normal-module"), false);
        coreModuleManager.active(coreModuleManager.get("normal-module"));
        coreModuleManager.frozen(coreModuleManager.get("normal-module"), false);
        assertTracingLifeCycle(
                coreModuleManager,
                "normal-module",
                LOAD, ACTIVE, LOAD_COMPLETED, FROZEN, ACTIVE, FROZEN
        );

    }

    @Test
    public void test$$CoreModuleManager$$loading() throws IOException, ModuleException {

        final CoreModuleManager coreModuleManager
                = buildingCoreModuleManager(
                buildingModuleJarFileWithModuleClass(
                        createTempFile("test-", ".jar"),
                        NormalModule.class,
                        BrokenOnCInitModule.class,
                        BrokenOnLazyActiveModule.class,
                        BrokenOnActiveModule.class,
                        BrokenOnFrozenModule.class,
                        BrokenOnLoadModule.class,
                        BrokenOnUnLoadModule.class,
                        BrokenOnLoadCompletedModule.class
                ));

        assertLoadedModule(
                coreModuleManager,
                "normal-module",
                "broken-on-lazy-active",
                "broken-on-unload",
                "broken-on-frozen",
                "broken-on-load-completed"
        );

    }


    @Information(id = "another-normal-module")
    public static class AnotherNormalModule implements Module {

    }

    @Information(id = "another-normal-module")
    public static class ModifyAnotherNormalModule implements Module {

    }

    @Test
    public void test$$CoreModuleManager$$forceFlush() throws IOException, ModuleException {

        final File anotherNormalModuleJarFile = buildingModuleJarFileWithModuleClass(
                createTempFile("test-", ".jar"),
                AnotherNormalModule.class
        );

        final CoreModuleManager coreModuleManager
                = buildingCoreModuleManager(
                buildingModuleJarFileWithModuleClass(
                        createTempFile("test-", ".jar"),
                        NormalModule.class
                ),
                anotherNormalModuleJarFile
        );

        assertLoadedModule(
                coreModuleManager,
                "normal-module",
                "another-normal-module"
        );

        Assert.assertTrue(anotherNormalModuleJarFile.delete());
        coreModuleManager.flush(true);

        assertLoadedModule(
                coreModuleManager,
                "normal-module"
        );

    }

    @Test
    public void test$$CoreModuleManager$$softFlush$$delete() throws IOException, ModuleException {

        final File anotherNormalModuleJarFile = buildingModuleJarFileWithModuleClass(
                createTempFile("test-", ".jar"),
                AnotherNormalModule.class
        );

        final CoreModuleManager coreModuleManager
                = buildingCoreModuleManager(
                buildingModuleJarFileWithModuleClass(
                        createTempFile("test-", ".jar"),
                        NormalModule.class
                ),
                anotherNormalModuleJarFile
        );

        assertLoadedModule(
                coreModuleManager,
                "normal-module",
                "another-normal-module"
        );

        Assert.assertTrue(anotherNormalModuleJarFile.delete());
        coreModuleManager.flush(false);
        assertLoadedModule(
                coreModuleManager,
                "normal-module"
        );

    }


    @Test
    public void test$$CoreModuleManager$$softFlush$$modify() throws IOException, ModuleException {

        final File anotherNormalModuleJarFile = buildingModuleJarFileWithModuleClass(
                createTempFile("test-", ".jar"),
                AnotherNormalModule.class
        );

        final CoreModuleManager coreModuleManager
                = buildingCoreModuleManager(
                buildingModuleJarFileWithModuleClass(
                        createTempFile("test-", ".jar"),
                        NormalModule.class
                ),
                anotherNormalModuleJarFile
        );

        assertLoadedModule(
                coreModuleManager,
                "normal-module",
                "another-normal-module"
        );

        buildingModuleJarFileWithModuleClass(
                anotherNormalModuleJarFile,
                ModifyAnotherNormalModule.class
        );
        coreModuleManager.flush(false);

        assertLoadedModule(
                coreModuleManager,
                "normal-module",
                "another-normal-module"
        );

        Assert.assertEquals(
                ModifyAnotherNormalModule.class.getName(),
                coreModuleManager.get("another-normal-module").getModule().getClass().getName()
        );

    }


    @Test(expected = ModuleException.class)
    public void test$$CoreModuleManager$$getThrowsExceptionIfNull() throws IOException, ModuleException {

        final CoreModuleManager coreModuleManager
                = buildingCoreModuleManager(
                buildingModuleJarFileWithModuleClass(
                        createTempFile("test-", ".jar"),
                        NormalModule.class,
                        BrokenOnCInitModule.class,
                        BrokenOnLazyActiveModule.class,
                        BrokenOnActiveModule.class,
                        BrokenOnFrozenModule.class,
                        BrokenOnLoadModule.class,
                        BrokenOnUnLoadModule.class,
                        BrokenOnLoadCompletedModule.class
                )
        );

        coreModuleManager.getThrowsExceptionIfNull("not-existed-module");
    }

    @Test
    public void test$$CoreModuleManager$$activeOnSuccess() throws IOException, ModuleException {

        final CoreModuleManager coreModuleManager
                = buildingCoreModuleManager(
                buildingModuleJarFileWithModuleClass(
                        createTempFile("test-", ".jar"),
                        NormalOnLazyActiveModule.class
                )
        );

        assertTracingLifeCycle(
                coreModuleManager,
                "normal-no-lazy-active-module",
                LOAD, LOAD_COMPLETED
        );

        final CoreModule normalNoLazyActiveCoreModule = coreModuleManager
                .getThrowsExceptionIfNull("normal-no-lazy-active-module");

        coreModuleManager.active(normalNoLazyActiveCoreModule);
        assertTracingLifeCycle(
                coreModuleManager,
                "normal-no-lazy-active-module",
                LOAD, LOAD_COMPLETED, ACTIVE
        );

        coreModuleManager.frozen(normalNoLazyActiveCoreModule, false);
        assertTracingLifeCycle(
                coreModuleManager,
                "normal-no-lazy-active-module",
                LOAD, LOAD_COMPLETED, ACTIVE, FROZEN
        );

    }

    @Test
    public void test$$CoreModuleManager$$activeOnFailed() throws IOException, ModuleException {


        final CoreModuleManager coreModuleManager
                = buildingCoreModuleManager(
                buildingModuleJarFileWithModuleClass(
                        createTempFile("test-", ".jar"),
                        BrokenOnLazyActiveModule.class
                )
        );

        assertTracingLifeCycle(
                coreModuleManager,
                "broken-on-lazy-active",
                LOAD, LOAD_COMPLETED
        );

        final CoreModule brokenOnLazyActiveCoreModule = coreModuleManager
                .getThrowsExceptionIfNull("broken-on-lazy-active");

        try {
            coreModuleManager.active(brokenOnLazyActiveCoreModule);
        } catch (ModuleException me) {
            Assert.assertEquals("broken-on-lazy-active", me.getUniqueId());
            Assert.assertEquals(MODULE_ACTIVE_ERROR, me.getErrorCode());
        }

        assertTracingLifeCycle(
                coreModuleManager,
                "broken-on-lazy-active",
                LOAD, LOAD_COMPLETED, ACTIVE
        );

    }


}
