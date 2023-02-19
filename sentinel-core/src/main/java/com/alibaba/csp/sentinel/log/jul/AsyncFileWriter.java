/*
 * MIT License
 *
 * Copyright (c) 2018, Miguel Gamboa (gamboa.pt)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
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
                    AsyncFiles.closeAfc(asyncFile)
            );
        }
    }
}
