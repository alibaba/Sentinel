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

package com.alibaba.csp.sentinel.log;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;


import static java.nio.ByteBuffer.wrap;
import static java.nio.channels.AsynchronousFileChannel.open;

/**
 * Asynchronous non-blocking write operations with a {@code CompletableFuture} based API.
 * These operations use an underlying {@code AsynchronousFileChannel}.
 * All methods are asynchronous including the {@code close()} which chains a continuation
 * on last resulting write {@code CompletableFuture} to close the {@code AsyncFileChannel} on completion.
 */
public class AsyncFileWriter extends Writer {

    final AsynchronousFileChannel asyncFile;
    /**
     * File position after last write operation completion.
     */
    private CompletableFuture<Integer> currentPosition = CompletableFuture.completedFuture(0);

    public AsyncFileWriter(AsynchronousFileChannel asyncFile) {
        this.asyncFile = asyncFile;
    }

    public AsyncFileWriter(Path file, StandardOpenOption...options) throws IOException {
        this(open(file, options));
    }


    public CompletableFuture<Integer> getPosition() {
        return currentPosition;
    }

    /**
     * Writes the given String appended with a newline separator
     * and returns a CompletableFuture of the final file index
     * after the completion of the corresponding write operation.
     */
    public void writeLine(CharSequence str) {
        write(str + System.lineSeparator());
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(cbuf.toString().getBytes(), off, len);
        write(buffer);
    }


    @Override
    public void write(String str) {
        write(str.getBytes());
    }
    /**
     * Writes the given String and returns a CompletableFuture of
     * the final file index after the completion of the corresponding
     * write operation.
     */


    @Override
    public void flush() throws IOException {
        asyncFile.force(true);
    }

    /**
     * Writes the given byte array and returns a CompletableFuture of
     * the final file index after the completion of the corresponding
     * write operation.
     */
    public CompletableFuture<Integer> write(byte[] bytes) {
        return write(wrap(bytes));
    }

    /**
     * Writes the given byte buffer and returns a CompletableFuture of
     * the final file index after the completion of the corresponding
     * write operation.
     */
    public CompletableFuture<Integer> write(ByteBuffer bytes) {
        /**
         * Wee need to update currentPosition field to keep track.
         * The currentPosition field is used on close() method, which chains
         * a continuation to close the AsyncFileChannel.
         */
        currentPosition = currentPosition.thenCompose(index -> {
            CompletableFuture<Integer> size = write(asyncFile, bytes, index);
            return size.thenApply(length -> length + index);
        });
        return currentPosition;
    }


    static CompletableFuture<Integer> write(
            AsynchronousFileChannel asyncFile,
            ByteBuffer buf,
            int position)
    {
        CompletableFuture<Integer> promise = new CompletableFuture<>();
        asyncFile.write(buf, position, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                promise.complete(result);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                promise.completeExceptionally(exc);
            }
        });
        return promise;
    }

    /**
     * Asynchronous close operation.
     * Chains a continuation on CompletableFuture resulting from last write operation,
     * which closes the AsyncFileChannel on completion.
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if(asyncFile != null) {
            currentPosition.whenComplete((res, ex) ->
            try {
                asyncFile.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            });
        }
    }
}
