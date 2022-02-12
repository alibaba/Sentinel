/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.jdbc;

import com.alibaba.csp.sentinel.adapter.jdbc.calcite.CalciteUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author icodening
 * @date 2022.02.12
 */
@Warmup(iterations = 10)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Group)
public class SQLParserBenchmarkTest {

    public static void main(String[] args) throws RunnerException {
        Options opts = new OptionsBuilder()
                .include(SQLParserBenchmarkTest.class.getSimpleName())
                .build();
        new Runner(opts).run();
    }

    @Setup
    public void setup() {
        CalciteUtil.warmUp();
    }

    @GroupThreads(5)
    @Group("Calcite")
    @Benchmark
    public void calciteTest() throws Exception {
        String sql = "select * from student where id = 1";
        CalciteUtil.replaceSQLParameters(sql);
    }
}
