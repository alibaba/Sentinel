/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.fail;

public class FileInJarReadableDataSourceCloseTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testCloseReleasesJarAndIsIdempotent() throws Exception {
        assertCloseReleasesJar(source -> source);
    }

    @Test
    public void testCloseReleasesJarAfterParserFailure() throws Exception {
        assertCloseReleasesJar(source -> {
            throw new IllegalArgumentException("Invalid configuration");
        });
    }

    private void assertCloseReleasesJar(Converter<String, String> converter) throws Exception {
        File jar = temporaryFolder.newFile();
        try (JarOutputStream output = new JarOutputStream(new FileOutputStream(jar))) {
            output.putNextEntry(new JarEntry("rules.json"));
            output.write("[]".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        FileInJarReadableDataSource<String> dataSource = new FileInJarReadableDataSource<>(
            jar.getAbsolutePath(), "rules.json", converter);
        Field field = FileInJarReadableDataSource.class.getDeclaredField("jarFile");
        field.setAccessible(true);
        JarFile jarFile = (JarFile) field.get(dataSource);
        try {
            dataSource.close();
            dataSource.close();
            try {
                jarFile.getJarEntry("rules.json");
                fail("The data source's JarFile should be closed");
            } catch (IllegalStateException expected) {
                // A closed JarFile rejects access to its entries.
            }
        } finally {
            jarFile.close();
        }
    }
}
