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

import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.util.SqlBasicVisitor;

import java.util.List;

/**
 * Calcite normal sql visitor
 * @author icodening
 * @date 2022.02.12
 */
public class SentinelSQLVisitor extends SqlBasicVisitor<Void> {

    public static final SentinelSQLVisitor INSTANCE = new SentinelSQLVisitor();

    @Override
    public Void visit(SqlCall call) {
        List<SqlNode> operandList = call.getOperandList();
        for (int i = 0; i < operandList.size(); i++) {
            SqlNode node = operandList.get(i);
            if (node == null) {
                continue;
            }
            if (!(node instanceof SqlLiteral)) {
                node.accept(INSTANCE);
            }
            if (node instanceof SqlLiteral) {
                call.setOperand(i, node.accept(SentinelSqlLiteral.INSTANCE));
            }
        }
        return null;
    }
}
