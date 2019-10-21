/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.codec;

/**
 * Defines common decoding methods for String decoders.
 *
 * @version $Id: StringDecoder.java 1379145 2012-08-30 21:02:52Z tn $
 */
public interface StringDecoder extends Decoder {

    /**
     * Decodes a String and returns a String.
     *
     * @param source
     *            the String to decode
     * @return the encoded String
     * @throws DecoderException
     *             thrown if there is an error condition during the Encoding process.
     */
    String decode(String source) throws DecoderException;
}

