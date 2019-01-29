/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.util.function;

/**
 * A tuple of 2 elements.
 */
public class Tuple2<R1, R2> {

    public final R1 r1;
    public final R2 r2;

    public Tuple2(R1 r1, R2 r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    /**
     * Factory method for creating a Tuple.
     *
     * @return new Tuple
     */
    public static <C1, C2> Tuple2<C1, C2> of(C1 c1, C2 c2) {
        return new Tuple2<C1, C2>(c1, c2);
    }

    /**
     * Swaps the element of this Tuple.
     *
     * @return a new Tuple where the first element is the second element of this Tuple and the second element is the first element of this Tuple.
     */
    public Tuple2<R2, R1> swap() {
        return new Tuple2<R2, R1>(this.r2, this.r1);
    }

    @Override
    public String toString() {
        return "Tuple2{" +
            "r1=" + r1 +
            ", r2=" + r2 +
            '}';
    }
}