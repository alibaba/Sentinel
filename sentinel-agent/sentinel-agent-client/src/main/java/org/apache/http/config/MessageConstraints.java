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

package org.apache.http.config;

import org.apache.http.util.Args;

/**
 * HTTP Message constraints: line length and header count.
 * <p>
 * Please note that line length is defined in bytes and not characters.
 * This is only relevant however when using non-standard HTTP charsets
 * for protocol elements such as UTF-8.
 * </p>
 *
 * @since 4.3
 */
public class MessageConstraints implements Cloneable {

    public static final MessageConstraints DEFAULT = new Builder().build();

    private final int maxLineLength;
    private final int maxHeaderCount;

    MessageConstraints(final int maxLineLength, final int maxHeaderCount) {
        super();
        this.maxLineLength = maxLineLength;
        this.maxHeaderCount = maxHeaderCount;
    }

    public int getMaxLineLength() {
        return maxLineLength;
    }

    public int getMaxHeaderCount() {
        return maxHeaderCount;
    }

    @Override
    protected MessageConstraints clone() throws CloneNotSupportedException {
        return (MessageConstraints) super.clone();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("[maxLineLength=").append(maxLineLength)
                .append(", maxHeaderCount=").append(maxHeaderCount)
                .append("]");
        return builder.toString();
    }

    public static MessageConstraints lineLen(final int max) {
        return new MessageConstraints(Args.notNegative(max, "Max line length"), -1);
    }

    public static MessageConstraints.Builder custom() {
        return new Builder();
    }

    public static MessageConstraints.Builder copy(final MessageConstraints config) {
        Args.notNull(config, "Message constraints");
        return new Builder()
            .setMaxHeaderCount(config.getMaxHeaderCount())
            .setMaxLineLength(config.getMaxLineLength());
    }

    public static class Builder {

        private int maxLineLength;
        private int maxHeaderCount;

        Builder() {
            this.maxLineLength = -1;
            this.maxHeaderCount = -1;
        }

        public Builder setMaxLineLength(final int maxLineLength) {
            this.maxLineLength = maxLineLength;
            return this;
        }

        public Builder setMaxHeaderCount(final int maxHeaderCount) {
            this.maxHeaderCount = maxHeaderCount;
            return this;
        }

        public MessageConstraints build() {
            return new MessageConstraints(maxLineLength, maxHeaderCount);
        }

    }

}
