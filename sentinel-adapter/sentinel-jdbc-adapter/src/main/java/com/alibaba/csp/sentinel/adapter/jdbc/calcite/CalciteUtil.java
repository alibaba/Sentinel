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
package com.alibaba.csp.sentinel.adapter.jdbc.calcite;

import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.apache.calcite.config.Lex.JAVA;

/**
 * Calcite replace SQL parameters util
 *
 * @author icodening
 * @date 2022.02.12
 */
public class CalciteUtil {

    private static final SqlParser.Config DEFAULT_CONFIG = SqlParser.config().withParserFactory(SqlParserImpl.FACTORY)
            .withCaseSensitive(false)
            .withLex(JAVA)
            .withQuoting(Quoting.DOUBLE_QUOTE)
            .withUnquotedCasing(Casing.TO_UPPER)
            .withQuotedCasing(Casing.UNCHANGED)
            .withConformance(SqlConformanceEnum.DEFAULT);

    private CalciteUtil() {
    }

    /**
     * warm up 'replaceSQLParameters', because the first replacement takes some of time
     */
    public static void warmUp() {
        try {
            String sql = "select 1";
            replaceSQLParameters(sql);
        } catch (Throwable ignore) {
        }
    }

    public static String replaceSQLParameters(String sql) throws Exception {
        return replaceSQLParameters(sql, DEFAULT_CONFIG);
    }

    /**
     * replace sql parameters
     * @return return origin sql when if sql parse error
     */
    public static String replaceSQLParametersWithoutException(String sql) {
        try {
            return replaceSQLParameters(sql, DEFAULT_CONFIG);
        } catch (Exception e) {
            return sql;
        }
    }

    /**
     * replace sql parameters.
     * example:
     * in:  select * from student where id = 1
     * out: SELECT * FROM `STUDENT` WHERE `ID` = '?'
     *
     * @param sql    origin sql
     * @param config SqlParser.Config
     * @return sql with replaced parameters
     * @throws Exception IOException and SqlParseException
     */
    public static String replaceSQLParameters(String sql, SqlParser.Config config) throws Exception {
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseStmt();
        sqlNode.accept(SentinelSQLVisitor.INSTANCE);
        BufferedReader bufferedReader = new BufferedReader(new StringReader(sqlNode.toString()));
        StringBuilder sqlStringBuilder = new StringBuilder(sqlNode.toString().length());
        for (String line; (line = bufferedReader.readLine()) != null; sqlStringBuilder.append(' ')) {
            sqlStringBuilder.append(line);
        }
        return sqlStringBuilder.substring(0, sqlStringBuilder.length() - 1);
    }
}
