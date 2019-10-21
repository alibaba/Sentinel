package com.alibaba.jvm.sandbox.qatest.core.util;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;

import java.io.File;
import java.io.IOException;

public class ModuleTests {

    @Information(id="test-module")
    public static final class TestModule implements Module {

    }

    public static void main(String... args) throws IOException {

        System.out.println(TestModule.class.getName());

        SandboxModuleJarBuilder.building(new File("/Users/vlinux/test-module.jar"))
                .putModuleClass(TestModule.class)
                .build();
    }

}
