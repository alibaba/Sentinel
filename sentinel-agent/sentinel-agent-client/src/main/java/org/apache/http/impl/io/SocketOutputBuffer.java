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

package org.apache.http.impl.io;

import java.io.IOException;
import java.net.Socket;

import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

/**
 * {@link org.apache.http.io.SessionOutputBuffer} implementation
 * bound to a {@link Socket}.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link SessionOutputBufferImpl}
 */
@Deprecated
public class SocketOutputBuffer extends AbstractSessionOutputBuffer {

    /**
     * Creates an instance of this class.
     *
     * @param socket the socket to write data to.
     * @param buffersize the size of the internal buffer. If this number is less
     *   than {@code 0} it is set to the value of
     *   {@link Socket#getSendBufferSize()}. If resultant number is less
     *   than {@code 1024} it is set to {@code 1024}.
     * @param params HTTP parameters.
     */
    public SocketOutputBuffer(
            final Socket socket,
            final int buffersize,
            final HttpParams params) throws IOException {
        super();
        Args.notNull(socket, "Socket");
        int n = buffersize;
        if (n < 0) {
            n = socket.getSendBufferSize();
        }
        if (n < 1024) {
            n = 1024;
        }
        init(socket.getOutputStream(), n, params);
    }

}
