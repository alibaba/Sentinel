/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.http.conn;

import java.io.IOException;
import java.io.InputStream;

/**
 * A watcher for {@link EofSensorInputStream}. Each stream will notify its
 * watcher at most once.
 *
 * @since 4.0
 */
public interface EofSensorWatcher {

    /**
     * Indicates that EOF is detected.
     *
     * @param wrapped   the underlying stream which has reached EOF
     *
     * @return  {@code true} if {@code wrapped} should be closed,
     *          {@code false} if it should be left alone
     *
     * @throws IOException
     *         in case of an IO problem, for example if the watcher itself
     *         closes the underlying stream. The caller will leave the
     *         wrapped stream alone, as if {@code false} was returned.
     */
    boolean eofDetected(InputStream wrapped)
        throws IOException;

    /**
     * Indicates that the {@link EofSensorInputStream stream} is closed.
     * This method will be called only if EOF was <i>not</i> detected
     * before closing. Otherwise, {@link #eofDetected eofDetected} is called.
     *
     * @param wrapped   the underlying stream which has not reached EOF
     *
     * @return  {@code true} if {@code wrapped} should be closed,
     *          {@code false} if it should be left alone
     *
     * @throws IOException
     *         in case of an IO problem, for example if the watcher itself
     *         closes the underlying stream. The caller will leave the
     *         wrapped stream alone, as if {@code false} was returned.
     */
    boolean streamClosed(InputStream wrapped)
        throws IOException;

    /**
     * Indicates that the {@link EofSensorInputStream stream} is aborted.
     * This method will be called only if EOF was <i>not</i> detected
     * before aborting. Otherwise, {@link #eofDetected eofDetected} is called.
     * <p>
     * This method will also be invoked when an input operation causes an
     * IOException to be thrown to make sure the input stream gets shut down.
     * </p>
     *
     * @param wrapped   the underlying stream which has not reached EOF
     *
     * @return  {@code true} if {@code wrapped} should be closed,
     *          {@code false} if it should be left alone
     *
     * @throws IOException
     *         in case of an IO problem, for example if the watcher itself
     *         closes the underlying stream. The caller will leave the
     *         wrapped stream alone, as if {@code false} was returned.
     */
    boolean streamAbort(InputStream wrapped)
        throws IOException;

}
