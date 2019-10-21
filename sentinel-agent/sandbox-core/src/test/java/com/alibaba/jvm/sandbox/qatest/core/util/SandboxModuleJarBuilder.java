package com.alibaba.jvm.sandbox.qatest.core.util;

import com.alibaba.jvm.sandbox.api.Module;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class SandboxModuleJarBuilder extends JarBuilder {

    private final Set<String> moduleSpiSet = new LinkedHashSet<String>();

    public SandboxModuleJarBuilder(File targetJarFile) {
        super(targetJarFile);
    }

    public SandboxModuleJarBuilder putModuleClass(final Class<? extends Module> classOfModule) throws IOException {
        super.putEntry(classOfModule);
        moduleSpiSet.add(classOfModule.getName());
        return this;
    }

    private void putModuleSpi() {
        putEntry(
                "META-INF/services/com.alibaba.jvm.sandbox.api.Module",
                StringUtils.join(moduleSpiSet, "\n")
        );
    }

    @Override
    public File build() throws IOException {
        putModuleSpi();
        return super.build();
    }


    public static SandboxModuleJarBuilder building(final File targetJarFile) {
        return new SandboxModuleJarBuilder(targetJarFile);
    }

}
