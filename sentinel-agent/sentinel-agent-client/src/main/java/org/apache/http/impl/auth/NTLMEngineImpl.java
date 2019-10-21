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
package org.apache.http.impl.auth;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EncodingUtils;

/**
 * Provides an implementation for NTLMv1, NTLMv2, and NTLM2 Session forms of the NTLM
 * authentication protocol.
 *
 * @since 4.1
 */
final class NTLMEngineImpl implements NTLMEngine {

    /** Unicode encoding */
    private static final Charset UNICODE_LITTLE_UNMARKED = CharsetUtils.lookup("UnicodeLittleUnmarked");
    /** Character encoding */
    private static final Charset DEFAULT_CHARSET = Consts.ASCII;

    // Flags we use; descriptions according to:
    // http://davenport.sourceforge.net/ntlm.html
    // and
    // http://msdn.microsoft.com/en-us/library/cc236650%28v=prot.20%29.aspx
    protected static final int FLAG_REQUEST_UNICODE_ENCODING = 0x00000001;      // Unicode string encoding requested
    protected static final int FLAG_REQUEST_TARGET = 0x00000004;                      // Requests target field
    protected static final int FLAG_REQUEST_SIGN = 0x00000010;  // Requests all messages have a signature attached, in NEGOTIATE message.
    protected static final int FLAG_REQUEST_SEAL = 0x00000020;  // Request key exchange for message confidentiality in NEGOTIATE message.  MUST be used in conjunction with 56BIT.
    protected static final int FLAG_REQUEST_LAN_MANAGER_KEY = 0x00000080;    // Request Lan Manager key instead of user session key
    protected static final int FLAG_REQUEST_NTLMv1 = 0x00000200; // Request NTLMv1 security.  MUST be set in NEGOTIATE and CHALLENGE both
    protected static final int FLAG_DOMAIN_PRESENT = 0x00001000;        // Domain is present in message
    protected static final int FLAG_WORKSTATION_PRESENT = 0x00002000;   // Workstation is present in message
    protected static final int FLAG_REQUEST_ALWAYS_SIGN = 0x00008000;   // Requests a signature block on all messages.  Overridden by REQUEST_SIGN and REQUEST_SEAL.
    protected static final int FLAG_REQUEST_NTLM2_SESSION = 0x00080000; // From server in challenge, requesting NTLM2 session security
    protected static final int FLAG_REQUEST_VERSION = 0x02000000;       // Request protocol version
    protected static final int FLAG_TARGETINFO_PRESENT = 0x00800000;    // From server in challenge message, indicating targetinfo is present
    protected static final int FLAG_REQUEST_128BIT_KEY_EXCH = 0x20000000; // Request explicit 128-bit key exchange
    protected static final int FLAG_REQUEST_EXPLICIT_KEY_EXCH = 0x40000000;     // Request explicit key exchange
    protected static final int FLAG_REQUEST_56BIT_ENCRYPTION = 0x80000000;      // Must be used in conjunction with SEAL


    /** Secure random generator */
    private static final java.security.SecureRandom RND_GEN;
    static {
        java.security.SecureRandom rnd = null;
        try {
            rnd = java.security.SecureRandom.getInstance("SHA1PRNG");
        } catch (final Exception ignore) {
        }
        RND_GEN = rnd;
    }

    /** The signature string as bytes in the default encoding */
    private static final byte[] SIGNATURE;

    static {
        final byte[] bytesWithoutNull = "NTLMSSP".getBytes(Consts.ASCII);
        SIGNATURE = new byte[bytesWithoutNull.length + 1];
        System.arraycopy(bytesWithoutNull, 0, SIGNATURE, 0, bytesWithoutNull.length);
        SIGNATURE[bytesWithoutNull.length] = (byte) 0x00;
    }

    private static final String TYPE_1_MESSAGE = new Type1Message().getResponse();

    /**
     * Returns the response for the given message.
     *
     * @param message
     *            the message that was received from the server.
     * @param username
     *            the username to authenticate with.
     * @param password
     *            the password to authenticate with.
     * @param host
     *            The host.
     * @param domain
     *            the NT domain to authenticate in.
     * @return The response.
     * @throws org.apache.http.HttpException
     *             If the messages cannot be retrieved.
     */
    static String getResponseFor(final String message, final String username, final String password,
            final String host, final String domain) throws NTLMEngineException {

        final String response;
        if (message == null || message.trim().equals("")) {
            response = getType1Message(host, domain);
        } else {
            final Type2Message t2m = new Type2Message(message);
            response = getType3Message(username, password, host, domain, t2m.getChallenge(), t2m
                    .getFlags(), t2m.getTarget(), t2m.getTargetInfo());
        }
        return response;
    }

    /**
     * Creates the first message (type 1 message) in the NTLM authentication
     * sequence. This message includes the user name, domain and host for the
     * authentication session.
     *
     * @param host
     *            the computer name of the host requesting authentication.
     * @param domain
     *            The domain to authenticate with.
     * @return String the message to add to the HTTP request header.
     */
    static String getType1Message(final String host, final String domain) throws NTLMEngineException {
        // For compatibility reason do not include domain and host in type 1 message
        //return new Type1Message(domain, host).getResponse();
        return TYPE_1_MESSAGE;
    }

    /**
     * Creates the type 3 message using the given server nonce. The type 3
     * message includes all the information for authentication, host, domain,
     * username and the result of encrypting the nonce sent by the server using
     * the user's password as the key.
     *
     * @param user
     *            The user name. This should not include the domain name.
     * @param password
     *            The password.
     * @param host
     *            The host that is originating the authentication request.
     * @param domain
     *            The domain to authenticate within.
     * @param nonce
     *            the 8 byte array the server sent.
     * @return The type 3 message.
     * @throws NTLMEngineException
     *             If {@encrypt(byte[],byte[])} fails.
     */
    static String getType3Message(final String user, final String password, final String host, final String domain,
            final byte[] nonce, final int type2Flags, final String target, final byte[] targetInformation)
            throws NTLMEngineException {
        return new Type3Message(domain, host, user, password, nonce, type2Flags, target,
                targetInformation).getResponse();
    }

    /** Strip dot suffix from a name */
    private static String stripDotSuffix(final String value) {
        if (value == null) {
            return null;
        }
        final int index = value.indexOf(".");
        if (index != -1) {
            return value.substring(0, index);
        }
        return value;
    }

    /** Convert host to standard form */
    private static String convertHost(final String host) {
        return stripDotSuffix(host);
    }

    /** Convert domain to standard form */
    private static String convertDomain(final String domain) {
        return stripDotSuffix(domain);
    }

    private static int readULong(final byte[] src, final int index) throws NTLMEngineException {
        if (src.length < index + 4) {
            throw new NTLMEngineException("NTLM authentication - buffer too small for DWORD");
        }
        return (src[index] & 0xff) | ((src[index + 1] & 0xff) << 8)
                | ((src[index + 2] & 0xff) << 16) | ((src[index + 3] & 0xff) << 24);
    }

    private static int readUShort(final byte[] src, final int index) throws NTLMEngineException {
        if (src.length < index + 2) {
            throw new NTLMEngineException("NTLM authentication - buffer too small for WORD");
        }
        return (src[index] & 0xff) | ((src[index + 1] & 0xff) << 8);
    }

    private static byte[] readSecurityBuffer(final byte[] src, final int index) throws NTLMEngineException {
        final int length = readUShort(src, index);
        final int offset = readULong(src, index + 4);
        if (src.length < offset + length) {
            throw new NTLMEngineException(
                    "NTLM authentication - buffer too small for data item");
        }
        final byte[] buffer = new byte[length];
        System.arraycopy(src, offset, buffer, 0, length);
        return buffer;
    }

    /** Calculate a challenge block */
    private static byte[] makeRandomChallenge() throws NTLMEngineException {
        if (RND_GEN == null) {
            throw new NTLMEngineException("Random generator not available");
        }
        final byte[] rval = new byte[8];
        synchronized (RND_GEN) {
            RND_GEN.nextBytes(rval);
        }
        return rval;
    }

    /** Calculate a 16-byte secondary key */
    private static byte[] makeSecondaryKey() throws NTLMEngineException {
        if (RND_GEN == null) {
            throw new NTLMEngineException("Random generator not available");
        }
        final byte[] rval = new byte[16];
        synchronized (RND_GEN) {
            RND_GEN.nextBytes(rval);
        }
        return rval;
    }

    protected static class CipherGen {

        protected final String domain;
        protected final String user;
        protected final String password;
        protected final byte[] challenge;
        protected final String target;
        protected final byte[] targetInformation;

        // Information we can generate but may be passed in (for testing)
        protected byte[] clientChallenge;
        protected byte[] clientChallenge2;
        protected byte[] secondaryKey;
        protected byte[] timestamp;

        // Stuff we always generate
        protected byte[] lmHash = null;
        protected byte[] lmResponse = null;
        protected byte[] ntlmHash = null;
        protected byte[] ntlmResponse = null;
        protected byte[] ntlmv2Hash = null;
        protected byte[] lmv2Hash = null;
        protected byte[] lmv2Response = null;
        protected byte[] ntlmv2Blob = null;
        protected byte[] ntlmv2Response = null;
        protected byte[] ntlm2SessionResponse = null;
        protected byte[] lm2SessionResponse = null;
        protected byte[] lmUserSessionKey = null;
        protected byte[] ntlmUserSessionKey = null;
        protected byte[] ntlmv2UserSessionKey = null;
        protected byte[] ntlm2SessionResponseUserSessionKey = null;
        protected byte[] lanManagerSessionKey = null;

        public CipherGen(final String domain, final String user, final String password,
            final byte[] challenge, final String target, final byte[] targetInformation,
            final byte[] clientChallenge, final byte[] clientChallenge2,
            final byte[] secondaryKey, final byte[] timestamp) {
            this.domain = domain;
            this.target = target;
            this.user = user;
            this.password = password;
            this.challenge = challenge;
            this.targetInformation = targetInformation;
            this.clientChallenge = clientChallenge;
            this.clientChallenge2 = clientChallenge2;
            this.secondaryKey = secondaryKey;
            this.timestamp = timestamp;
        }

        public CipherGen(final String domain, final String user, final String password,
            final byte[] challenge, final String target, final byte[] targetInformation) {
            this(domain, user, password, challenge, target, targetInformation, null, null, null, null);
        }

        /** Calculate and return client challenge */
        public byte[] getClientChallenge()
            throws NTLMEngineException {
            if (clientChallenge == null) {
                clientChallenge = makeRandomChallenge();
            }
            return clientChallenge;
        }

        /** Calculate and return second client challenge */
        public byte[] getClientChallenge2()
            throws NTLMEngineException {
            if (clientChallenge2 == null) {
                clientChallenge2 = makeRandomChallenge();
            }
            return clientChallenge2;
        }

        /** Calculate and return random secondary key */
        public byte[] getSecondaryKey()
            throws NTLMEngineException {
            if (secondaryKey == null) {
                secondaryKey = makeSecondaryKey();
            }
            return secondaryKey;
        }

        /** Calculate and return the LMHash */
        public byte[] getLMHash()
            throws NTLMEngineException {
            if (lmHash == null) {
                lmHash = lmHash(password);
            }
            return lmHash;
        }

        /** Calculate and return the LMResponse */
        public byte[] getLMResponse()
            throws NTLMEngineException {
            if (lmResponse == null) {
                lmResponse = lmResponse(getLMHash(),challenge);
            }
            return lmResponse;
        }

        /** Calculate and return the NTLMHash */
        public byte[] getNTLMHash()
            throws NTLMEngineException {
            if (ntlmHash == null) {
                ntlmHash = ntlmHash(password);
            }
            return ntlmHash;
        }

        /** Calculate and return the NTLMResponse */
        public byte[] getNTLMResponse()
            throws NTLMEngineException {
            if (ntlmResponse == null) {
                ntlmResponse = lmResponse(getNTLMHash(),challenge);
            }
            return ntlmResponse;
        }

        /** Calculate the LMv2 hash */
        public byte[] getLMv2Hash()
            throws NTLMEngineException {
            if (lmv2Hash == null) {
                lmv2Hash = lmv2Hash(domain, user, getNTLMHash());
            }
            return lmv2Hash;
        }

        /** Calculate the NTLMv2 hash */
        public byte[] getNTLMv2Hash()
            throws NTLMEngineException {
            if (ntlmv2Hash == null) {
                ntlmv2Hash = ntlmv2Hash(domain, user, getNTLMHash());
            }
            return ntlmv2Hash;
        }

        /** Calculate a timestamp */
        public byte[] getTimestamp() {
            if (timestamp == null) {
                long time = System.currentTimeMillis();
                time += 11644473600000l; // milliseconds from January 1, 1601 -> epoch.
                time *= 10000; // tenths of a microsecond.
                // convert to little-endian byte array.
                timestamp = new byte[8];
                for (int i = 0; i < 8; i++) {
                    timestamp[i] = (byte) time;
                    time >>>= 8;
                }
            }
            return timestamp;
        }

        /** Calculate the NTLMv2Blob */
        public byte[] getNTLMv2Blob()
            throws NTLMEngineException {
            if (ntlmv2Blob == null) {
                ntlmv2Blob = createBlob(getClientChallenge2(), targetInformation, getTimestamp());
            }
            return ntlmv2Blob;
        }

        /** Calculate the NTLMv2Response */
        public byte[] getNTLMv2Response()
            throws NTLMEngineException {
            if (ntlmv2Response == null) {
                ntlmv2Response = lmv2Response(getNTLMv2Hash(),challenge,getNTLMv2Blob());
            }
            return ntlmv2Response;
        }

        /** Calculate the LMv2Response */
        public byte[] getLMv2Response()
            throws NTLMEngineException {
            if (lmv2Response == null) {
                lmv2Response = lmv2Response(getLMv2Hash(),challenge,getClientChallenge());
            }
            return lmv2Response;
        }

        /** Get NTLM2SessionResponse */
        public byte[] getNTLM2SessionResponse()
            throws NTLMEngineException {
            if (ntlm2SessionResponse == null) {
                ntlm2SessionResponse = ntlm2SessionResponse(getNTLMHash(),challenge,getClientChallenge());
            }
            return ntlm2SessionResponse;
        }

        /** Calculate and return LM2 session response */
        public byte[] getLM2SessionResponse()
            throws NTLMEngineException {
            if (lm2SessionResponse == null) {
                final byte[] clntChallenge = getClientChallenge();
                lm2SessionResponse = new byte[24];
                System.arraycopy(clntChallenge, 0, lm2SessionResponse, 0, clntChallenge.length);
                Arrays.fill(lm2SessionResponse, clntChallenge.length, lm2SessionResponse.length, (byte) 0x00);
            }
            return lm2SessionResponse;
        }

        /** Get LMUserSessionKey */
        public byte[] getLMUserSessionKey()
            throws NTLMEngineException {
            if (lmUserSessionKey == null) {
                lmUserSessionKey = new byte[16];
                System.arraycopy(getLMHash(), 0, lmUserSessionKey, 0, 8);
                Arrays.fill(lmUserSessionKey, 8, 16, (byte) 0x00);
            }
            return lmUserSessionKey;
        }

        /** Get NTLMUserSessionKey */
        public byte[] getNTLMUserSessionKey()
            throws NTLMEngineException {
            if (ntlmUserSessionKey == null) {
                final MD4 md4 = new MD4();
                md4.update(getNTLMHash());
                ntlmUserSessionKey = md4.getOutput();
            }
            return ntlmUserSessionKey;
        }

        /** GetNTLMv2UserSessionKey */
        public byte[] getNTLMv2UserSessionKey()
            throws NTLMEngineException {
            if (ntlmv2UserSessionKey == null) {
                final byte[] ntlmv2hash = getNTLMv2Hash();
                final byte[] truncatedResponse = new byte[16];
                System.arraycopy(getNTLMv2Response(), 0, truncatedResponse, 0, 16);
                ntlmv2UserSessionKey = hmacMD5(truncatedResponse, ntlmv2hash);
            }
            return ntlmv2UserSessionKey;
        }

        /** Get NTLM2SessionResponseUserSessionKey */
        public byte[] getNTLM2SessionResponseUserSessionKey()
            throws NTLMEngineException {
            if (ntlm2SessionResponseUserSessionKey == null) {
                final byte[] ntlm2SessionResponseNonce = getLM2SessionResponse();
                final byte[] sessionNonce = new byte[challenge.length + ntlm2SessionResponseNonce.length];
                System.arraycopy(challenge, 0, sessionNonce, 0, challenge.length);
                System.arraycopy(ntlm2SessionResponseNonce, 0, sessionNonce, challenge.length, ntlm2SessionResponseNonce.length);
                ntlm2SessionResponseUserSessionKey = hmacMD5(sessionNonce,getNTLMUserSessionKey());
            }
            return ntlm2SessionResponseUserSessionKey;
        }

        /** Get LAN Manager session key */
        public byte[] getLanManagerSessionKey()
            throws NTLMEngineException {
            if (lanManagerSessionKey == null) {
                try {
                    final byte[] keyBytes = new byte[14];
                    System.arraycopy(getLMHash(), 0, keyBytes, 0, 8);
                    Arrays.fill(keyBytes, 8, keyBytes.length, (byte)0xbd);
                    final Key lowKey = createDESKey(keyBytes, 0);
                    final Key highKey = createDESKey(keyBytes, 7);
                    final byte[] truncatedResponse = new byte[8];
                    System.arraycopy(getLMResponse(), 0, truncatedResponse, 0, truncatedResponse.length);
                    Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
                    des.init(Cipher.ENCRYPT_MODE, lowKey);
                    final byte[] lowPart = des.doFinal(truncatedResponse);
                    des = Cipher.getInstance("DES/ECB/NoPadding");
                    des.init(Cipher.ENCRYPT_MODE, highKey);
                    final byte[] highPart = des.doFinal(truncatedResponse);
                    lanManagerSessionKey = new byte[16];
                    System.arraycopy(lowPart, 0, lanManagerSessionKey, 0, lowPart.length);
                    System.arraycopy(highPart, 0, lanManagerSessionKey, lowPart.length, highPart.length);
                } catch (final Exception e) {
                    throw new NTLMEngineException(e.getMessage(), e);
                }
            }
            return lanManagerSessionKey;
        }
    }

    /** Calculates HMAC-MD5 */
    static byte[] hmacMD5(final byte[] value, final byte[] key)
        throws NTLMEngineException {
        final HMACMD5 hmacMD5 = new HMACMD5(key);
        hmacMD5.update(value);
        return hmacMD5.getOutput();
    }

    /** Calculates RC4 */
    static byte[] RC4(final byte[] value, final byte[] key)
        throws NTLMEngineException {
        try {
            final Cipher rc4 = Cipher.getInstance("RC4");
            rc4.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "RC4"));
            return rc4.doFinal(value);
        } catch (final Exception e) {
            throw new NTLMEngineException(e.getMessage(), e);
        }
    }

    /**
     * Calculates the NTLM2 Session Response for the given challenge, using the
     * specified password and client challenge.
     *
     * @return The NTLM2 Session Response. This is placed in the NTLM response
     *         field of the Type 3 message; the LM response field contains the
     *         client challenge, null-padded to 24 bytes.
     */
    static byte[] ntlm2SessionResponse(final byte[] ntlmHash, final byte[] challenge,
            final byte[] clientChallenge) throws NTLMEngineException {
        try {
            final MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(challenge);
            md5.update(clientChallenge);
            final byte[] digest = md5.digest();

            final byte[] sessionHash = new byte[8];
            System.arraycopy(digest, 0, sessionHash, 0, 8);
            return lmResponse(ntlmHash, sessionHash);
        } catch (final Exception e) {
            if (e instanceof NTLMEngineException) {
                throw (NTLMEngineException) e;
            }
            throw new NTLMEngineException(e.getMessage(), e);
        }
    }

    /**
     * Creates the LM Hash of the user's password.
     *
     * @param password
     *            The password.
     *
     * @return The LM Hash of the given password, used in the calculation of the
     *         LM Response.
     */
    private static byte[] lmHash(final String password) throws NTLMEngineException {
        try {
            final byte[] oemPassword = password.toUpperCase(Locale.ROOT).getBytes(Consts.ASCII);
            final int length = Math.min(oemPassword.length, 14);
            final byte[] keyBytes = new byte[14];
            System.arraycopy(oemPassword, 0, keyBytes, 0, length);
            final Key lowKey = createDESKey(keyBytes, 0);
            final Key highKey = createDESKey(keyBytes, 7);
            final byte[] magicConstant = "KGS!@#$%".getBytes(Consts.ASCII);
            final Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
            des.init(Cipher.ENCRYPT_MODE, lowKey);
            final byte[] lowHash = des.doFinal(magicConstant);
            des.init(Cipher.ENCRYPT_MODE, highKey);
            final byte[] highHash = des.doFinal(magicConstant);
            final byte[] lmHash = new byte[16];
            System.arraycopy(lowHash, 0, lmHash, 0, 8);
            System.arraycopy(highHash, 0, lmHash, 8, 8);
            return lmHash;
        } catch (final Exception e) {
            throw new NTLMEngineException(e.getMessage(), e);
        }
    }

    /**
     * Creates the NTLM Hash of the user's password.
     *
     * @param password
     *            The password.
     *
     * @return The NTLM Hash of the given password, used in the calculation of
     *         the NTLM Response and the NTLMv2 and LMv2 Hashes.
     */
    private static byte[] ntlmHash(final String password) throws NTLMEngineException {
        if (UNICODE_LITTLE_UNMARKED == null) {
            throw new NTLMEngineException("Unicode not supported");
        }
        final byte[] unicodePassword = password.getBytes(UNICODE_LITTLE_UNMARKED);
        final MD4 md4 = new MD4();
        md4.update(unicodePassword);
        return md4.getOutput();
    }

    /**
     * Creates the LMv2 Hash of the user's password.
     *
     * @return The LMv2 Hash, used in the calculation of the NTLMv2 and LMv2
     *         Responses.
     */
    private static byte[] lmv2Hash(final String domain, final String user, final byte[] ntlmHash)
            throws NTLMEngineException {
        if (UNICODE_LITTLE_UNMARKED == null) {
            throw new NTLMEngineException("Unicode not supported");
        }
        final HMACMD5 hmacMD5 = new HMACMD5(ntlmHash);
        // Upper case username, upper case domain!
        hmacMD5.update(user.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
        if (domain != null) {
            hmacMD5.update(domain.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
        }
        return hmacMD5.getOutput();
    }

    /**
     * Creates the NTLMv2 Hash of the user's password.
     *
     * @return The NTLMv2 Hash, used in the calculation of the NTLMv2 and LMv2
     *         Responses.
     */
    private static byte[] ntlmv2Hash(final String domain, final String user, final byte[] ntlmHash)
            throws NTLMEngineException {
        if (UNICODE_LITTLE_UNMARKED == null) {
            throw new NTLMEngineException("Unicode not supported");
        }
        final HMACMD5 hmacMD5 = new HMACMD5(ntlmHash);
        // Upper case username, mixed case target!!
        hmacMD5.update(user.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
        if (domain != null) {
            hmacMD5.update(domain.getBytes(UNICODE_LITTLE_UNMARKED));
        }
        return hmacMD5.getOutput();
    }

    /**
     * Creates the LM Response from the given hash and Type 2 challenge.
     *
     * @param hash
     *            The LM or NTLM Hash.
     * @param challenge
     *            The server challenge from the Type 2 message.
     *
     * @return The response (either LM or NTLM, depending on the provided hash).
     */
    private static byte[] lmResponse(final byte[] hash, final byte[] challenge) throws NTLMEngineException {
        try {
            final byte[] keyBytes = new byte[21];
            System.arraycopy(hash, 0, keyBytes, 0, 16);
            final Key lowKey = createDESKey(keyBytes, 0);
            final Key middleKey = createDESKey(keyBytes, 7);
            final Key highKey = createDESKey(keyBytes, 14);
            final Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
            des.init(Cipher.ENCRYPT_MODE, lowKey);
            final byte[] lowResponse = des.doFinal(challenge);
            des.init(Cipher.ENCRYPT_MODE, middleKey);
            final byte[] middleResponse = des.doFinal(challenge);
            des.init(Cipher.ENCRYPT_MODE, highKey);
            final byte[] highResponse = des.doFinal(challenge);
            final byte[] lmResponse = new byte[24];
            System.arraycopy(lowResponse, 0, lmResponse, 0, 8);
            System.arraycopy(middleResponse, 0, lmResponse, 8, 8);
            System.arraycopy(highResponse, 0, lmResponse, 16, 8);
            return lmResponse;
        } catch (final Exception e) {
            throw new NTLMEngineException(e.getMessage(), e);
        }
    }

    /**
     * Creates the LMv2 Response from the given hash, client data, and Type 2
     * challenge.
     *
     * @param hash
     *            The NTLMv2 Hash.
     * @param clientData
     *            The client data (blob or client challenge).
     * @param challenge
     *            The server challenge from the Type 2 message.
     *
     * @return The response (either NTLMv2 or LMv2, depending on the client
     *         data).
     */
    private static byte[] lmv2Response(final byte[] hash, final byte[] challenge, final byte[] clientData)
            throws NTLMEngineException {
        final HMACMD5 hmacMD5 = new HMACMD5(hash);
        hmacMD5.update(challenge);
        hmacMD5.update(clientData);
        final byte[] mac = hmacMD5.getOutput();
        final byte[] lmv2Response = new byte[mac.length + clientData.length];
        System.arraycopy(mac, 0, lmv2Response, 0, mac.length);
        System.arraycopy(clientData, 0, lmv2Response, mac.length, clientData.length);
        return lmv2Response;
    }

    /**
     * Creates the NTLMv2 blob from the given target information block and
     * client challenge.
     *
     * @param targetInformation
     *            The target information block from the Type 2 message.
     * @param clientChallenge
     *            The random 8-byte client challenge.
     *
     * @return The blob, used in the calculation of the NTLMv2 Response.
     */
    private static byte[] createBlob(final byte[] clientChallenge, final byte[] targetInformation, final byte[] timestamp) {
        final byte[] blobSignature = new byte[] { (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00 };
        final byte[] reserved = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
        final byte[] unknown1 = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
        final byte[] unknown2 = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
        final byte[] blob = new byte[blobSignature.length + reserved.length + timestamp.length + 8
                + unknown1.length + targetInformation.length + unknown2.length];
        int offset = 0;
        System.arraycopy(blobSignature, 0, blob, offset, blobSignature.length);
        offset += blobSignature.length;
        System.arraycopy(reserved, 0, blob, offset, reserved.length);
        offset += reserved.length;
        System.arraycopy(timestamp, 0, blob, offset, timestamp.length);
        offset += timestamp.length;
        System.arraycopy(clientChallenge, 0, blob, offset, 8);
        offset += 8;
        System.arraycopy(unknown1, 0, blob, offset, unknown1.length);
        offset += unknown1.length;
        System.arraycopy(targetInformation, 0, blob, offset, targetInformation.length);
        offset += targetInformation.length;
        System.arraycopy(unknown2, 0, blob, offset, unknown2.length);
        offset += unknown2.length;
        return blob;
    }

    /**
     * Creates a DES encryption key from the given key material.
     *
     * @param bytes
     *            A byte array containing the DES key material.
     * @param offset
     *            The offset in the given byte array at which the 7-byte key
     *            material starts.
     *
     * @return A DES encryption key created from the key material starting at
     *         the specified offset in the given byte array.
     */
    private static Key createDESKey(final byte[] bytes, final int offset) {
        final byte[] keyBytes = new byte[7];
        System.arraycopy(bytes, offset, keyBytes, 0, 7);
        final byte[] material = new byte[8];
        material[0] = keyBytes[0];
        material[1] = (byte) (keyBytes[0] << 7 | (keyBytes[1] & 0xff) >>> 1);
        material[2] = (byte) (keyBytes[1] << 6 | (keyBytes[2] & 0xff) >>> 2);
        material[3] = (byte) (keyBytes[2] << 5 | (keyBytes[3] & 0xff) >>> 3);
        material[4] = (byte) (keyBytes[3] << 4 | (keyBytes[4] & 0xff) >>> 4);
        material[5] = (byte) (keyBytes[4] << 3 | (keyBytes[5] & 0xff) >>> 5);
        material[6] = (byte) (keyBytes[5] << 2 | (keyBytes[6] & 0xff) >>> 6);
        material[7] = (byte) (keyBytes[6] << 1);
        oddParity(material);
        return new SecretKeySpec(material, "DES");
    }

    /**
     * Applies odd parity to the given byte array.
     *
     * @param bytes
     *            The data whose parity bits are to be adjusted for odd parity.
     */
    private static void oddParity(final byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            final byte b = bytes[i];
            final boolean needsParity = (((b >>> 7) ^ (b >>> 6) ^ (b >>> 5) ^ (b >>> 4) ^ (b >>> 3)
                    ^ (b >>> 2) ^ (b >>> 1)) & 0x01) == 0;
            if (needsParity) {
                bytes[i] |= (byte) 0x01;
            } else {
                bytes[i] &= (byte) 0xfe;
            }
        }
    }

    /** NTLM message generation, base class */
    static class NTLMMessage {
        /** The current response */
        private byte[] messageContents = null;

        /** The current output position */
        private int currentOutputPosition = 0;

        /** Constructor to use when message contents are not yet known */
        NTLMMessage() {
        }

        /** Constructor to use when message contents are known */
        NTLMMessage(final String messageBody, final int expectedType) throws NTLMEngineException {
            messageContents = Base64.decodeBase64(messageBody.getBytes(DEFAULT_CHARSET));
            // Look for NTLM message
            if (messageContents.length < SIGNATURE.length) {
                throw new NTLMEngineException("NTLM message decoding error - packet too short");
            }
            int i = 0;
            while (i < SIGNATURE.length) {
                if (messageContents[i] != SIGNATURE[i]) {
                    throw new NTLMEngineException(
                            "NTLM message expected - instead got unrecognized bytes");
                }
                i++;
            }

            // Check to be sure there's a type 2 message indicator next
            final int type = readULong(SIGNATURE.length);
            if (type != expectedType) {
                throw new NTLMEngineException("NTLM type " + Integer.toString(expectedType)
                        + " message expected - instead got type " + Integer.toString(type));
            }

            currentOutputPosition = messageContents.length;
        }

        /**
         * Get the length of the signature and flags, so calculations can adjust
         * offsets accordingly.
         */
        protected int getPreambleLength() {
            return SIGNATURE.length + 4;
        }

        /** Get the message length */
        protected int getMessageLength() {
            return currentOutputPosition;
        }

        /** Read a byte from a position within the message buffer */
        protected byte readByte(final int position) throws NTLMEngineException {
            if (messageContents.length < position + 1) {
                throw new NTLMEngineException("NTLM: Message too short");
            }
            return messageContents[position];
        }

        /** Read a bunch of bytes from a position in the message buffer */
        protected void readBytes(final byte[] buffer, final int position) throws NTLMEngineException {
            if (messageContents.length < position + buffer.length) {
                throw new NTLMEngineException("NTLM: Message too short");
            }
            System.arraycopy(messageContents, position, buffer, 0, buffer.length);
        }

        /** Read a ushort from a position within the message buffer */
        protected int readUShort(final int position) throws NTLMEngineException {
            return NTLMEngineImpl.readUShort(messageContents, position);
        }

        /** Read a ulong from a position within the message buffer */
        protected int readULong(final int position) throws NTLMEngineException {
            return NTLMEngineImpl.readULong(messageContents, position);
        }

        /** Read a security buffer from a position within the message buffer */
        protected byte[] readSecurityBuffer(final int position) throws NTLMEngineException {
            return NTLMEngineImpl.readSecurityBuffer(messageContents, position);
        }

        /**
         * Prepares the object to create a response of the given length.
         *
         * @param maxlength
         *            the maximum length of the response to prepare, not
         *            including the type and the signature (which this method
         *            adds).
         */
        protected void prepareResponse(final int maxlength, final int messageType) {
            messageContents = new byte[maxlength];
            currentOutputPosition = 0;
            addBytes(SIGNATURE);
            addULong(messageType);
        }

        /**
         * Adds the given byte to the response.
         *
         * @param b
         *            the byte to add.
         */
        protected void addByte(final byte b) {
            messageContents[currentOutputPosition] = b;
            currentOutputPosition++;
        }

        /**
         * Adds the given bytes to the response.
         *
         * @param bytes
         *            the bytes to add.
         */
        protected void addBytes(final byte[] bytes) {
            if (bytes == null) {
                return;
            }
            for (final byte b : bytes) {
                messageContents[currentOutputPosition] = b;
                currentOutputPosition++;
            }
        }

        /** Adds a USHORT to the response */
        protected void addUShort(final int value) {
            addByte((byte) (value & 0xff));
            addByte((byte) (value >> 8 & 0xff));
        }

        /** Adds a ULong to the response */
        protected void addULong(final int value) {
            addByte((byte) (value & 0xff));
            addByte((byte) (value >> 8 & 0xff));
            addByte((byte) (value >> 16 & 0xff));
            addByte((byte) (value >> 24 & 0xff));
        }

        /**
         * Returns the response that has been generated after shrinking the
         * array if required and base64 encodes the response.
         *
         * @return The response as above.
         */
        String getResponse() {
            final byte[] resp;
            if (messageContents.length > currentOutputPosition) {
                final byte[] tmp = new byte[currentOutputPosition];
                System.arraycopy(messageContents, 0, tmp, 0, currentOutputPosition);
                resp = tmp;
            } else {
                resp = messageContents;
            }
            return EncodingUtils.getAsciiString(Base64.encodeBase64(resp));
        }

    }

    /** Type 1 message assembly class */
    static class Type1Message extends NTLMMessage {

        private final byte[] hostBytes;
        private final byte[] domainBytes;

        Type1Message(final String domain, final String host) throws NTLMEngineException {
            super();
            if (UNICODE_LITTLE_UNMARKED == null) {
                throw new NTLMEngineException("Unicode not supported");
            }
            // Strip off domain name from the host!
            final String unqualifiedHost = convertHost(host);
            // Use only the base domain name!
            final String unqualifiedDomain = convertDomain(domain);

            hostBytes = unqualifiedHost != null ?
                    unqualifiedHost.getBytes(UNICODE_LITTLE_UNMARKED) : null;
            domainBytes = unqualifiedDomain != null ?
                    unqualifiedDomain.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED) : null;
        }

        Type1Message() {
            super();
            hostBytes = null;
            domainBytes = null;
        }
        /**
         * Getting the response involves building the message before returning
         * it
         */
        @Override
        String getResponse() {
            // Now, build the message. Calculate its length first, including
            // signature or type.
            final int finalLength = 32 + 8 /*+ hostBytes.length + domainBytes.length */;

            // Set up the response. This will initialize the signature, message
            // type, and flags.
            prepareResponse(finalLength, 1);

            // Flags. These are the complete set of flags we support.
            addULong(
                    //FLAG_WORKSTATION_PRESENT |
                    //FLAG_DOMAIN_PRESENT |

                    // Required flags
                    //FLAG_REQUEST_LAN_MANAGER_KEY |
                    FLAG_REQUEST_NTLMv1 |
                    FLAG_REQUEST_NTLM2_SESSION |

                    // Protocol version request
                    FLAG_REQUEST_VERSION |

                    // Recommended privacy settings
                    FLAG_REQUEST_ALWAYS_SIGN |
                    //FLAG_REQUEST_SEAL |
                    //FLAG_REQUEST_SIGN |

                    // These must be set according to documentation, based on use of SEAL above
                    FLAG_REQUEST_128BIT_KEY_EXCH |
                    FLAG_REQUEST_56BIT_ENCRYPTION |
                    //FLAG_REQUEST_EXPLICIT_KEY_EXCH |

                    FLAG_REQUEST_UNICODE_ENCODING);

            // Domain length (two times).
            addUShort(/*domainBytes.length*/0);
            addUShort(/*domainBytes.length*/0);

            // Domain offset.
            addULong(/*hostBytes.length +*/ 32 + 8);

            // Host length (two times).
            addUShort(/*hostBytes.length*/0);
            addUShort(/*hostBytes.length*/0);

            // Host offset (always 32 + 8).
            addULong(32 + 8);

            // Version
            addUShort(0x0105);
            // Build
            addULong(2600);
            // NTLM revision
            addUShort(0x0f00);

            // Host (workstation) String.
            if (hostBytes != null) {
                addBytes(hostBytes);
            }
            // Domain String.
            if (domainBytes != null) {
                addBytes(domainBytes);
            }

            return super.getResponse();
        }

    }

    /** Type 2 message class */
    static class Type2Message extends NTLMMessage {
        protected byte[] challenge;
        protected String target;
        protected byte[] targetInfo;
        protected int flags;

        Type2Message(final String message) throws NTLMEngineException {
            super(message, 2);

            // Type 2 message is laid out as follows:
            // First 8 bytes: NTLMSSP[0]
            // Next 4 bytes: Ulong, value 2
            // Next 8 bytes, starting at offset 12: target field (2 ushort lengths, 1 ulong offset)
            // Next 4 bytes, starting at offset 20: Flags, e.g. 0x22890235
            // Next 8 bytes, starting at offset 24: Challenge
            // Next 8 bytes, starting at offset 32: ??? (8 bytes of zeros)
            // Next 8 bytes, starting at offset 40: targetinfo field (2 ushort lengths, 1 ulong offset)
            // Next 2 bytes, major/minor version number (e.g. 0x05 0x02)
            // Next 8 bytes, build number
            // Next 2 bytes, protocol version number (e.g. 0x00 0x0f)
            // Next, various text fields, and a ushort of value 0 at the end

            // Parse out the rest of the info we need from the message
            // The nonce is the 8 bytes starting from the byte in position 24.
            challenge = new byte[8];
            readBytes(challenge, 24);

            flags = readULong(20);

            if ((flags & FLAG_REQUEST_UNICODE_ENCODING) == 0) {
                throw new NTLMEngineException(
                        "NTLM type 2 message indicates no support for Unicode. Flags are: "
                                + Integer.toString(flags));
            }

            // Do the target!
            target = null;
            // The TARGET_DESIRED flag is said to not have understood semantics
            // in Type2 messages, so use the length of the packet to decide
            // how to proceed instead
            if (getMessageLength() >= 12 + 8) {
                final byte[] bytes = readSecurityBuffer(12);
                if (bytes.length != 0) {
                    try {
                        target = new String(bytes, "UnicodeLittleUnmarked");
                    } catch (final UnsupportedEncodingException e) {
                        throw new NTLMEngineException(e.getMessage(), e);
                    }
                }
            }

            // Do the target info!
            targetInfo = null;
            // TARGET_DESIRED flag cannot be relied on, so use packet length
            if (getMessageLength() >= 40 + 8) {
                final byte[] bytes = readSecurityBuffer(40);
                if (bytes.length != 0) {
                    targetInfo = bytes;
                }
            }
        }

        /** Retrieve the challenge */
        byte[] getChallenge() {
            return challenge;
        }

        /** Retrieve the target */
        String getTarget() {
            return target;
        }

        /** Retrieve the target info */
        byte[] getTargetInfo() {
            return targetInfo;
        }

        /** Retrieve the response flags */
        int getFlags() {
            return flags;
        }

    }

    /** Type 3 message assembly class */
    static class Type3Message extends NTLMMessage {
        // Response flags from the type2 message
        protected int type2Flags;

        protected byte[] domainBytes;
        protected byte[] hostBytes;
        protected byte[] userBytes;

        protected byte[] lmResp;
        protected byte[] ntResp;
        protected byte[] sessionKey;


        /** Constructor. Pass the arguments we will need */
        Type3Message(final String domain, final String host, final String user, final String password, final byte[] nonce,
                final int type2Flags, final String target, final byte[] targetInformation)
                throws NTLMEngineException {
            // Save the flags
            this.type2Flags = type2Flags;

            // Strip off domain name from the host!
            final String unqualifiedHost = convertHost(host);
            // Use only the base domain name!
            final String unqualifiedDomain = convertDomain(domain);

            // Create a cipher generator class.  Use domain BEFORE it gets modified!
            final CipherGen gen = new CipherGen(unqualifiedDomain, user, password, nonce, target, targetInformation);

            // Use the new code to calculate the responses, including v2 if that
            // seems warranted.
            byte[] userSessionKey;
            try {
                // This conditional may not work on Windows Server 2008 R2 and above, where it has not yet
                // been tested
                if (((type2Flags & FLAG_TARGETINFO_PRESENT) != 0) &&
                    targetInformation != null && target != null) {
                    // NTLMv2
                    ntResp = gen.getNTLMv2Response();
                    lmResp = gen.getLMv2Response();
                    if ((type2Flags & FLAG_REQUEST_LAN_MANAGER_KEY) != 0) {
                        userSessionKey = gen.getLanManagerSessionKey();
                    } else {
                        userSessionKey = gen.getNTLMv2UserSessionKey();
                    }
                } else {
                    // NTLMv1
                    if ((type2Flags & FLAG_REQUEST_NTLM2_SESSION) != 0) {
                        // NTLM2 session stuff is requested
                        ntResp = gen.getNTLM2SessionResponse();
                        lmResp = gen.getLM2SessionResponse();
                        if ((type2Flags & FLAG_REQUEST_LAN_MANAGER_KEY) != 0) {
                            userSessionKey = gen.getLanManagerSessionKey();
                        } else {
                            userSessionKey = gen.getNTLM2SessionResponseUserSessionKey();
                        }
                    } else {
                        ntResp = gen.getNTLMResponse();
                        lmResp = gen.getLMResponse();
                        if ((type2Flags & FLAG_REQUEST_LAN_MANAGER_KEY) != 0) {
                            userSessionKey = gen.getLanManagerSessionKey();
                        } else {
                            userSessionKey = gen.getNTLMUserSessionKey();
                        }
                    }
                }
            } catch (final NTLMEngineException e) {
                // This likely means we couldn't find the MD4 hash algorithm -
                // fail back to just using LM
                ntResp = new byte[0];
                lmResp = gen.getLMResponse();
                if ((type2Flags & FLAG_REQUEST_LAN_MANAGER_KEY) != 0) {
                    userSessionKey = gen.getLanManagerSessionKey();
                } else {
                    userSessionKey = gen.getLMUserSessionKey();
                }
            }

            if ((type2Flags & FLAG_REQUEST_SIGN) != 0) {
                if ((type2Flags & FLAG_REQUEST_EXPLICIT_KEY_EXCH) != 0) {
                    sessionKey = RC4(gen.getSecondaryKey(), userSessionKey);
                } else {
                    sessionKey = userSessionKey;
                }
            } else {
                sessionKey = null;
            }
            if (UNICODE_LITTLE_UNMARKED == null) {
                throw new NTLMEngineException("Unicode not supported");
            }
            hostBytes = unqualifiedHost != null ? unqualifiedHost.getBytes(UNICODE_LITTLE_UNMARKED) : null;
            domainBytes = unqualifiedDomain != null ? unqualifiedDomain
                    .toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED) : null;
            userBytes = user.getBytes(UNICODE_LITTLE_UNMARKED);
        }

        /** Assemble the response */
        @Override
        String getResponse() {
            final int ntRespLen = ntResp.length;
            final int lmRespLen = lmResp.length;

            final int domainLen = domainBytes != null ? domainBytes.length : 0;
            final int hostLen = hostBytes != null ? hostBytes.length: 0;
            final int userLen = userBytes.length;
            final int sessionKeyLen;
            if (sessionKey != null) {
                sessionKeyLen = sessionKey.length;
            } else {
                sessionKeyLen = 0;
            }

            // Calculate the layout within the packet
            final int lmRespOffset = 72;  // allocate space for the version
            final int ntRespOffset = lmRespOffset + lmRespLen;
            final int domainOffset = ntRespOffset + ntRespLen;
            final int userOffset = domainOffset + domainLen;
            final int hostOffset = userOffset + userLen;
            final int sessionKeyOffset = hostOffset + hostLen;
            final int finalLength = sessionKeyOffset + sessionKeyLen;

            // Start the response. Length includes signature and type
            prepareResponse(finalLength, 3);

            // LM Resp Length (twice)
            addUShort(lmRespLen);
            addUShort(lmRespLen);

            // LM Resp Offset
            addULong(lmRespOffset);

            // NT Resp Length (twice)
            addUShort(ntRespLen);
            addUShort(ntRespLen);

            // NT Resp Offset
            addULong(ntRespOffset);

            // Domain length (twice)
            addUShort(domainLen);
            addUShort(domainLen);

            // Domain offset.
            addULong(domainOffset);

            // User Length (twice)
            addUShort(userLen);
            addUShort(userLen);

            // User offset
            addULong(userOffset);

            // Host length (twice)
            addUShort(hostLen);
            addUShort(hostLen);

            // Host offset
            addULong(hostOffset);

            // Session key length (twice)
            addUShort(sessionKeyLen);
            addUShort(sessionKeyLen);

            // Session key offset
            addULong(sessionKeyOffset);

            // Flags.
            addULong(
                    //FLAG_WORKSTATION_PRESENT |
                    //FLAG_DOMAIN_PRESENT |

                    // Required flags
                    (type2Flags & FLAG_REQUEST_LAN_MANAGER_KEY) |
                    (type2Flags & FLAG_REQUEST_NTLMv1) |
                    (type2Flags & FLAG_REQUEST_NTLM2_SESSION) |

                    // Protocol version request
                    FLAG_REQUEST_VERSION |

                    // Recommended privacy settings
                    (type2Flags & FLAG_REQUEST_ALWAYS_SIGN) |
                    (type2Flags & FLAG_REQUEST_SEAL) |
                    (type2Flags & FLAG_REQUEST_SIGN) |

                    // These must be set according to documentation, based on use of SEAL above
                    (type2Flags & FLAG_REQUEST_128BIT_KEY_EXCH) |
                    (type2Flags & FLAG_REQUEST_56BIT_ENCRYPTION) |
                    (type2Flags & FLAG_REQUEST_EXPLICIT_KEY_EXCH) |

                    (type2Flags & FLAG_TARGETINFO_PRESENT) |
                    (type2Flags & FLAG_REQUEST_UNICODE_ENCODING) |
                    (type2Flags & FLAG_REQUEST_TARGET)
            );

            // Version
            addUShort(0x0105);
            // Build
            addULong(2600);
            // NTLM revision
            addUShort(0x0f00);

            // Add the actual data
            addBytes(lmResp);
            addBytes(ntResp);
            addBytes(domainBytes);
            addBytes(userBytes);
            addBytes(hostBytes);
            if (sessionKey != null) {
                addBytes(sessionKey);
            }

            return super.getResponse();
        }
    }

    static void writeULong(final byte[] buffer, final int value, final int offset) {
        buffer[offset] = (byte) (value & 0xff);
        buffer[offset + 1] = (byte) (value >> 8 & 0xff);
        buffer[offset + 2] = (byte) (value >> 16 & 0xff);
        buffer[offset + 3] = (byte) (value >> 24 & 0xff);
    }

    static int F(final int x, final int y, final int z) {
        return ((x & y) | (~x & z));
    }

    static int G(final int x, final int y, final int z) {
        return ((x & y) | (x & z) | (y & z));
    }

    static int H(final int x, final int y, final int z) {
        return (x ^ y ^ z);
    }

    static int rotintlft(final int val, final int numbits) {
        return ((val << numbits) | (val >>> (32 - numbits)));
    }

    /**
     * Cryptography support - MD4. The following class was based loosely on the
     * RFC and on code found at http://www.cs.umd.edu/~harry/jotp/src/md.java.
     * Code correctness was verified by looking at MD4.java from the jcifs
     * library (http://jcifs.samba.org). It was massaged extensively to the
     * final form found here by Karl Wright (kwright@metacarta.com).
     */
    static class MD4 {
        protected int A = 0x67452301;
        protected int B = 0xefcdab89;
        protected int C = 0x98badcfe;
        protected int D = 0x10325476;
        protected long count = 0L;
        protected byte[] dataBuffer = new byte[64];

        MD4() {
        }

        void update(final byte[] input) {
            // We always deal with 512 bits at a time. Correspondingly, there is
            // a buffer 64 bytes long that we write data into until it gets
            // full.
            int curBufferPos = (int) (count & 63L);
            int inputIndex = 0;
            while (input.length - inputIndex + curBufferPos >= dataBuffer.length) {
                // We have enough data to do the next step. Do a partial copy
                // and a transform, updating inputIndex and curBufferPos
                // accordingly
                final int transferAmt = dataBuffer.length - curBufferPos;
                System.arraycopy(input, inputIndex, dataBuffer, curBufferPos, transferAmt);
                count += transferAmt;
                curBufferPos = 0;
                inputIndex += transferAmt;
                processBuffer();
            }

            // If there's anything left, copy it into the buffer and leave it.
            // We know there's not enough left to process.
            if (inputIndex < input.length) {
                final int transferAmt = input.length - inputIndex;
                System.arraycopy(input, inputIndex, dataBuffer, curBufferPos, transferAmt);
                count += transferAmt;
                curBufferPos += transferAmt;
            }
        }

        byte[] getOutput() {
            // Feed pad/length data into engine. This must round out the input
            // to a multiple of 512 bits.
            final int bufferIndex = (int) (count & 63L);
            final int padLen = (bufferIndex < 56) ? (56 - bufferIndex) : (120 - bufferIndex);
            final byte[] postBytes = new byte[padLen + 8];
            // Leading 0x80, specified amount of zero padding, then length in
            // bits.
            postBytes[0] = (byte) 0x80;
            // Fill out the last 8 bytes with the length
            for (int i = 0; i < 8; i++) {
                postBytes[padLen + i] = (byte) ((count * 8) >>> (8 * i));
            }

            // Update the engine
            update(postBytes);

            // Calculate final result
            final byte[] result = new byte[16];
            writeULong(result, A, 0);
            writeULong(result, B, 4);
            writeULong(result, C, 8);
            writeULong(result, D, 12);
            return result;
        }

        protected void processBuffer() {
            // Convert current buffer to 16 ulongs
            final int[] d = new int[16];

            for (int i = 0; i < 16; i++) {
                d[i] = (dataBuffer[i * 4] & 0xff) + ((dataBuffer[i * 4 + 1] & 0xff) << 8)
                        + ((dataBuffer[i * 4 + 2] & 0xff) << 16)
                        + ((dataBuffer[i * 4 + 3] & 0xff) << 24);
            }

            // Do a round of processing
            final int AA = A;
            final int BB = B;
            final int CC = C;
            final int DD = D;
            round1(d);
            round2(d);
            round3(d);
            A += AA;
            B += BB;
            C += CC;
            D += DD;

        }

        protected void round1(final int[] d) {
            A = rotintlft((A + F(B, C, D) + d[0]), 3);
            D = rotintlft((D + F(A, B, C) + d[1]), 7);
            C = rotintlft((C + F(D, A, B) + d[2]), 11);
            B = rotintlft((B + F(C, D, A) + d[3]), 19);

            A = rotintlft((A + F(B, C, D) + d[4]), 3);
            D = rotintlft((D + F(A, B, C) + d[5]), 7);
            C = rotintlft((C + F(D, A, B) + d[6]), 11);
            B = rotintlft((B + F(C, D, A) + d[7]), 19);

            A = rotintlft((A + F(B, C, D) + d[8]), 3);
            D = rotintlft((D + F(A, B, C) + d[9]), 7);
            C = rotintlft((C + F(D, A, B) + d[10]), 11);
            B = rotintlft((B + F(C, D, A) + d[11]), 19);

            A = rotintlft((A + F(B, C, D) + d[12]), 3);
            D = rotintlft((D + F(A, B, C) + d[13]), 7);
            C = rotintlft((C + F(D, A, B) + d[14]), 11);
            B = rotintlft((B + F(C, D, A) + d[15]), 19);
        }

        protected void round2(final int[] d) {
            A = rotintlft((A + G(B, C, D) + d[0] + 0x5a827999), 3);
            D = rotintlft((D + G(A, B, C) + d[4] + 0x5a827999), 5);
            C = rotintlft((C + G(D, A, B) + d[8] + 0x5a827999), 9);
            B = rotintlft((B + G(C, D, A) + d[12] + 0x5a827999), 13);

            A = rotintlft((A + G(B, C, D) + d[1] + 0x5a827999), 3);
            D = rotintlft((D + G(A, B, C) + d[5] + 0x5a827999), 5);
            C = rotintlft((C + G(D, A, B) + d[9] + 0x5a827999), 9);
            B = rotintlft((B + G(C, D, A) + d[13] + 0x5a827999), 13);

            A = rotintlft((A + G(B, C, D) + d[2] + 0x5a827999), 3);
            D = rotintlft((D + G(A, B, C) + d[6] + 0x5a827999), 5);
            C = rotintlft((C + G(D, A, B) + d[10] + 0x5a827999), 9);
            B = rotintlft((B + G(C, D, A) + d[14] + 0x5a827999), 13);

            A = rotintlft((A + G(B, C, D) + d[3] + 0x5a827999), 3);
            D = rotintlft((D + G(A, B, C) + d[7] + 0x5a827999), 5);
            C = rotintlft((C + G(D, A, B) + d[11] + 0x5a827999), 9);
            B = rotintlft((B + G(C, D, A) + d[15] + 0x5a827999), 13);

        }

        protected void round3(final int[] d) {
            A = rotintlft((A + H(B, C, D) + d[0] + 0x6ed9eba1), 3);
            D = rotintlft((D + H(A, B, C) + d[8] + 0x6ed9eba1), 9);
            C = rotintlft((C + H(D, A, B) + d[4] + 0x6ed9eba1), 11);
            B = rotintlft((B + H(C, D, A) + d[12] + 0x6ed9eba1), 15);

            A = rotintlft((A + H(B, C, D) + d[2] + 0x6ed9eba1), 3);
            D = rotintlft((D + H(A, B, C) + d[10] + 0x6ed9eba1), 9);
            C = rotintlft((C + H(D, A, B) + d[6] + 0x6ed9eba1), 11);
            B = rotintlft((B + H(C, D, A) + d[14] + 0x6ed9eba1), 15);

            A = rotintlft((A + H(B, C, D) + d[1] + 0x6ed9eba1), 3);
            D = rotintlft((D + H(A, B, C) + d[9] + 0x6ed9eba1), 9);
            C = rotintlft((C + H(D, A, B) + d[5] + 0x6ed9eba1), 11);
            B = rotintlft((B + H(C, D, A) + d[13] + 0x6ed9eba1), 15);

            A = rotintlft((A + H(B, C, D) + d[3] + 0x6ed9eba1), 3);
            D = rotintlft((D + H(A, B, C) + d[11] + 0x6ed9eba1), 9);
            C = rotintlft((C + H(D, A, B) + d[7] + 0x6ed9eba1), 11);
            B = rotintlft((B + H(C, D, A) + d[15] + 0x6ed9eba1), 15);

        }

    }

    /**
     * Cryptography support - HMACMD5 - algorithmically based on various web
     * resources by Karl Wright
     */
    static class HMACMD5 {
        protected byte[] ipad;
        protected byte[] opad;
        protected MessageDigest md5;

        HMACMD5(final byte[] input) throws NTLMEngineException {
            byte[] key = input;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (final Exception ex) {
                // Umm, the algorithm doesn't exist - throw an
                // NTLMEngineException!
                throw new NTLMEngineException(
                        "Error getting md5 message digest implementation: " + ex.getMessage(), ex);
            }

            // Initialize the pad buffers with the key
            ipad = new byte[64];
            opad = new byte[64];

            int keyLength = key.length;
            if (keyLength > 64) {
                // Use MD5 of the key instead, as described in RFC 2104
                md5.update(key);
                key = md5.digest();
                keyLength = key.length;
            }
            int i = 0;
            while (i < keyLength) {
                ipad[i] = (byte) (key[i] ^ (byte) 0x36);
                opad[i] = (byte) (key[i] ^ (byte) 0x5c);
                i++;
            }
            while (i < 64) {
                ipad[i] = (byte) 0x36;
                opad[i] = (byte) 0x5c;
                i++;
            }

            // Very important: update the digest with the ipad buffer
            md5.reset();
            md5.update(ipad);

        }

        /** Grab the current digest. This is the "answer". */
        byte[] getOutput() {
            final byte[] digest = md5.digest();
            md5.update(opad);
            return md5.digest(digest);
        }

        /** Update by adding a complete array */
        void update(final byte[] input) {
            md5.update(input);
        }

        /** Update the algorithm */
        void update(final byte[] input, final int offset, final int length) {
            md5.update(input, offset, length);
        }

    }

    @Override
    public String generateType1Msg(
            final String domain,
            final String workstation) throws NTLMEngineException {
        return getType1Message(workstation, domain);
    }

    @Override
    public String generateType3Msg(
            final String username,
            final String password,
            final String domain,
            final String workstation,
            final String challenge) throws NTLMEngineException {
        final Type2Message t2m = new Type2Message(challenge);
        return getType3Message(
                username,
                password,
                workstation,
                domain,
                t2m.getChallenge(),
                t2m.getFlags(),
                t2m.getTarget(),
                t2m.getTargetInfo());
    }

}
