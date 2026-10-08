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
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;

public class FileInJarReadableDataSourceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testReadCompleteCompressedEntry() throws Exception {
        StringBuilder content = new StringBuilder();
        Random random = new Random(0);
        for (int i = 0; i < 65536; i++) {
            content.append((char) ('a' + random.nextInt(26)));
        }
        assertReadSource(content.toString(), 65536);
    }

    @Test
    public void testReadEmptyEntry() throws Exception {
        assertReadSource("", 16);
    }

    @Test
    public void testReadUtf8Entry() throws Exception {
        String content = "{\"resource\":\"资源\"}";
        assertReadSource(content, content.getBytes(StandardCharsets.UTF_8).length);
    }

    @Test(expected = IllegalStateException.class)
    public void testRejectEntryLargerThanBuffer() throws Exception {
        File jar = createJar("12345678");
        FileInJarReadableDataSource<String> dataSource = new FileInJarReadableDataSource<>(
            jar.getAbsolutePath(), "rules.json", source -> source, 7);
        try {
            dataSource.readSource();
        } finally {
            dataSource.close();
        }
    }

    private void assertReadSource(String content, int bufferSize) throws Exception {
        File jar = createJar(content);
        FileInJarReadableDataSource<String> dataSource = new FileInJarReadableDataSource<>(
            jar.getAbsolutePath(), "rules.json", source -> source, bufferSize);
        try {
            String source = dataSource.readSource();
            assertEquals(content.length(), source.length());
            assertEquals(content, source);
            assertEquals(content, dataSource.loadConfig());
        } finally {
            dataSource.close();
        }
    }

    private File createJar(String content) throws Exception {
        File jar = temporaryFolder.newFile();
        try (JarOutputStream output = new JarOutputStream(new FileOutputStream(jar))) {
            output.putNextEntry(new JarEntry("rules.json"));
            output.write(content.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return jar;
    }
}
