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
package org.apache.http.impl;

import org.apache.http.config.ConnectionConfig;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

/**
 * Connection support methods.
 *
 * @since 4.3
 */
public final class ConnSupport {

    public static CharsetDecoder createDecoder(final ConnectionConfig cconfig) {
        if (cconfig == null) {
            return null;
        }
        final Charset charset = cconfig.getCharset();
        final CodingErrorAction malformed = cconfig.getMalformedInputAction();
        final CodingErrorAction unmappable = cconfig.getUnmappableInputAction();
        if (charset != null) {
            return charset.newDecoder()
                    .onMalformedInput(malformed != null ? malformed : CodingErrorAction.REPORT)
                    .onUnmappableCharacter(unmappable != null ? unmappable: CodingErrorAction.REPORT);
        } else {
            return null;
        }
    }

    public static CharsetEncoder createEncoder(final ConnectionConfig cconfig) {
        if (cconfig == null) {
            return null;
        }
        final Charset charset = cconfig.getCharset();
        if (charset != null) {
            final CodingErrorAction malformed = cconfig.getMalformedInputAction();
            final CodingErrorAction unmappable = cconfig.getUnmappableInputAction();
            return charset.newEncoder()
                .onMalformedInput(malformed != null ? malformed : CodingErrorAction.REPORT)
                .onUnmappableCharacter(unmappable != null ? unmappable: CodingErrorAction.REPORT);
        } else {
            return null;
        }
    }

}
