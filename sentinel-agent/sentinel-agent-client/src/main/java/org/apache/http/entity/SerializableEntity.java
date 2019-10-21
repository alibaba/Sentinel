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

package org.apache.http.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.http.util.Args;

/**
 * A streamed entity that obtains its content from a {@link Serializable}.
 * The content obtained from the {@link Serializable} instance can
 * optionally be buffered in a byte array in order to make the
 * entity self-contained and repeatable.
 *
 * @since 4.0
 */
public class SerializableEntity extends AbstractHttpEntity {

    private byte[] objSer;

    private Serializable objRef;

    /**
     * Creates new instance of this class.
     *
     * @param ser input
     * @param bufferize tells whether the content should be
     *        stored in an internal buffer
     * @throws IOException in case of an I/O error
     */
    public SerializableEntity(final Serializable ser, final boolean bufferize) throws IOException {
        super();
        Args.notNull(ser, "Source object");
        if (bufferize) {
            createBytes(ser);
        } else {
            this.objRef = ser;
        }
    }

    /**
     * @since 4.3
     */
    public SerializableEntity(final Serializable ser) {
        super();
        Args.notNull(ser, "Source object");
        this.objRef = ser;
    }

    private void createBytes(final Serializable ser) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(ser);
        out.flush();
        this.objSer = baos.toByteArray();
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        if (this.objSer == null) {
            createBytes(this.objRef);
        }
        return new ByteArrayInputStream(this.objSer);
    }

    @Override
    public long getContentLength() {
        if (this.objSer ==  null) {
            return -1;
        } else {
            return this.objSer.length;
        }
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public boolean isStreaming() {
        return this.objSer == null;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        Args.notNull(outstream, "Output stream");
        if (this.objSer == null) {
            final ObjectOutputStream out = new ObjectOutputStream(outstream);
            out.writeObject(this.objRef);
            out.flush();
        } else {
            outstream.write(this.objSer);
            outstream.flush();
        }
    }

}
