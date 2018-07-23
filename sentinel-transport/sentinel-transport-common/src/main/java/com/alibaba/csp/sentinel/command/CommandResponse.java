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
package com.alibaba.csp.sentinel.command;

/**
 * Command response representation of command center.
 *
 * @param <R> type of the result
 * @author Eric Zhao
 */
public class CommandResponse<R> {

    private final boolean success;
    private final R result;
    private final Throwable exception;

    private CommandResponse(R result) {
        this(result, true, null);
    }

    private CommandResponse(R result, boolean success, Throwable exception) {
        this.success = success;
        this.result = result;
        this.exception = exception;
    }

    /**
     * Construct a successful response with given object.
     *
     * @param result result object
     * @param <T>    type of the result
     * @return constructed server response
     */
    public static <T> CommandResponse<T> ofSuccess(T result) {
        return new CommandResponse<T>(result);
    }

    /**
     * Construct a failed response with given exception.
     *
     * @param ex cause of the failure
     * @return constructed server response
     */
    public static <T> CommandResponse<T> ofFailure(Throwable ex) {
        return new CommandResponse<T>(null, false, ex);
    }

    /**
     * Construct a failed response with given exception.
     *
     * @param ex     cause of the failure
     * @param result additional message of the failure
     * @return constructed server response
     */
    public static <T> CommandResponse<T> ofFailure(Throwable ex, T result) {
        return new CommandResponse<T>(result, false, ex);
    }

    public boolean isSuccess() {
        return success;
    }

    public R getResult() {
        return result;
    }

    public Throwable getException() {
        return exception;
    }
}
