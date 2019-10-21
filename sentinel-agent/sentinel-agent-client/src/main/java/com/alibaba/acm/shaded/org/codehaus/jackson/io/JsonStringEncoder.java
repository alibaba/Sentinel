package com.alibaba.acm.shaded.org.codehaus.jackson.io;

import java.lang.ref.SoftReference;

import com.alibaba.acm.shaded.org.codehaus.jackson.util.BufferRecycler;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.ByteArrayBuilder;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.CharTypes;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.TextBuffer;

/**
 * Helper class used for efficient encoding of JSON String values (including
 * JSON field names) into Strings or UTF-8 byte arrays.
 *<p>
 * Note that methods in here are somewhat optimized, but not ridiculously so.
 * Reason is that conversion method results are expected to be cached so that
 * these methods will not be hot spots during normal operation.
 *
 * @since 1.6
 */
public final class JsonStringEncoder
{
    private final static char[] HEX_CHARS = CharTypes.copyHexChars();

    private final static byte[] HEX_BYTES = CharTypes.copyHexBytes();

    private final static int SURR1_FIRST = 0xD800;
    private final static int SURR1_LAST = 0xDBFF;
    private final static int SURR2_FIRST = 0xDC00;
    private final static int SURR2_LAST = 0xDFFF;

    private final static int INT_BACKSLASH = '\\';
    private final static int INT_U = 'u';
    private final static int INT_0 = '0';
    
    /**
     * This <code>ThreadLocal</code> contains a {@link java.lang.ref.SoftRerefence}
     * to a {@link BufferRecycler} used to provide a low-cost
     * buffer recycling between reader and writer instances.
     */
    final protected static ThreadLocal<SoftReference<JsonStringEncoder>> _threadEncoder
        = new ThreadLocal<SoftReference<JsonStringEncoder>>();

    /**
     * Lazily constructed text buffer used to produce JSON encoded Strings
     * as characters (without UTF-8 encoding)
     */
    protected TextBuffer _textBuffer;

    /**
     * Lazily-constructed builder used for UTF-8 encoding of text values
     * (quoted and unquoted)
     */
    protected ByteArrayBuilder _byteBuilder;
    
    /**
     * Temporary buffer used for composing quote/escape sequences
     */
    protected final char[] _quoteBuffer;
    
    /*
    /**********************************************************
    /* Construction, instance access
    /**********************************************************
     */
    
    public JsonStringEncoder()
    {
        _quoteBuffer = new char[6];
        _quoteBuffer[0] = '\\';
        _quoteBuffer[2] = '0';
        _quoteBuffer[3] = '0';
    }
    
    /**
     * Factory method for getting an instance; this is either recycled per-thread instance,
     * or a newly constructed one.
     */
    public static JsonStringEncoder getInstance()
    {
        SoftReference<JsonStringEncoder> ref = _threadEncoder.get();
        JsonStringEncoder enc = (ref == null) ? null : ref.get();

        if (enc == null) {
            enc = new JsonStringEncoder();
            _threadEncoder.set(new SoftReference<JsonStringEncoder>(enc));
        }
        return enc;
    }

    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    /**
     * Method that will quote text contents using JSON standard quoting,
     * and return results as a character array
     */
    public char[] quoteAsString(String input)
    {
        TextBuffer textBuffer = _textBuffer;
        if (textBuffer == null) {
            // no allocator; can add if we must, shouldn't need to
            _textBuffer = textBuffer = new TextBuffer(null);
        }
        char[] outputBuffer = textBuffer.emptyAndGetCurrentSegment();
        final int[] escCodes = CharTypes.get7BitOutputEscapes();
        final int escCodeCount = escCodes.length;
        int inPtr = 0;
        final int inputLen = input.length();
        int outPtr = 0;
 
        outer_loop:
        while (inPtr < inputLen) {
            tight_loop:
            while (true) {
                char c = input.charAt(inPtr);
                if (c < escCodeCount && escCodes[c] != 0) {
                    break tight_loop;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outputBuffer[outPtr++] = c;
                if (++inPtr >= inputLen) {
                    break outer_loop;
                }
            }
            // something to escape; 2 or 6-char variant? 
            int escCode = escCodes[input.charAt(inPtr++)];
            int length = _appendSingleEscape(escCode, _quoteBuffer);
            if ((outPtr + length) > outputBuffer.length) {
                int first = outputBuffer.length - outPtr;
                if (first > 0) {
                    System.arraycopy(_quoteBuffer, 0, outputBuffer, outPtr, first);
                }
                outputBuffer = textBuffer.finishCurrentSegment();
                int second = length - first;
                System.arraycopy(_quoteBuffer, first, outputBuffer, outPtr, second);
                outPtr += second;
            } else {
                System.arraycopy(_quoteBuffer, 0, outputBuffer, outPtr, length);
                outPtr += length;
            }
            
        }
        textBuffer.setCurrentLength(outPtr);
        return textBuffer.contentsAsArray();
    }

    /**
     * Will quote given JSON String value using standard quoting, encode
     * results as UTF-8, and return result as a byte array.
     */
    public byte[] quoteAsUTF8(String text)
    {
        ByteArrayBuilder byteBuilder = _byteBuilder;
        if (byteBuilder == null) {
            // no allocator; can add if we must, shouldn't need to
            _byteBuilder = byteBuilder = new ByteArrayBuilder(null);
        }
        int inputPtr = 0;
        int inputEnd = text.length();
        int outputPtr = 0;
        byte[] outputBuffer = byteBuilder.resetAndGetFirstSegment();
        
        main_loop:
        while (inputPtr < inputEnd) {
            final int[] escCodes = CharTypes.get7BitOutputEscapes();

            inner_loop: // ascii and escapes
            while (true) {
                int ch = text.charAt(inputPtr);
                if (ch > 0x7F || escCodes[ch] != 0) {
                    break inner_loop;
                }
                if (outputPtr >= outputBuffer.length) {
                    outputBuffer = byteBuilder.finishCurrentSegment();
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte) ch;
                if (++inputPtr >= inputEnd) {
                    break main_loop;
                }
            }                
            if (outputPtr >= outputBuffer.length) {
                outputBuffer = byteBuilder.finishCurrentSegment();
                outputPtr = 0;
            }
            // Ok, so what did we hit?
            int ch = (int) text.charAt(inputPtr++);
            if (ch <= 0x7F) { // needs quoting
                int escape = escCodes[ch];
                // ctrl-char, 6-byte escape...
                outputPtr = _appendByteEscape(ch, escape, byteBuilder, outputPtr);
                outputBuffer = byteBuilder.getCurrentSegment();
                continue main_loop;
            } else if (ch <= 0x7FF) { // fine, just needs 2 byte output
                outputBuffer[outputPtr++] = (byte) (0xc0 | (ch >> 6));
                ch = (0x80 | (ch & 0x3f));
            } else { // 3 or 4 bytes
                // Surrogates?
                if (ch < SURR1_FIRST || ch > SURR2_LAST) { // nope
                    outputBuffer[outputPtr++] = (byte) (0xe0 | (ch >> 12));
                    if (outputPtr >= outputBuffer.length) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputPtr = 0;
                    }
                    outputBuffer[outputPtr++] = (byte) (0x80 | ((ch >> 6) & 0x3f));
                    ch = (0x80 | (ch & 0x3f));
                } else { // yes, surrogate pair
                    if (ch > SURR1_LAST) { // must be from first range
                        _throwIllegalSurrogate(ch);
                    }
                    // and if so, followed by another from next range
                    if (inputPtr >= inputEnd) {
                        _throwIllegalSurrogate(ch);
                    }
                    ch = _convertSurrogate(ch, text.charAt(inputPtr++));
                    if (ch > 0x10FFFF) { // illegal, as per RFC 4627
                        _throwIllegalSurrogate(ch);
                    }
                    outputBuffer[outputPtr++] = (byte) (0xf0 | (ch >> 18));
                    if (outputPtr >= outputBuffer.length) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputPtr = 0;
                    }
                    outputBuffer[outputPtr++] = (byte) (0x80 | ((ch >> 12) & 0x3f));
                    if (outputPtr >= outputBuffer.length) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputPtr = 0;
                    }
                    outputBuffer[outputPtr++] = (byte) (0x80 | ((ch >> 6) & 0x3f));
                    ch = (0x80 | (ch & 0x3f));
                }
            }
            if (outputPtr >= outputBuffer.length) {
                outputBuffer = byteBuilder.finishCurrentSegment();
                outputPtr = 0;
            }
            outputBuffer[outputPtr++] = (byte) ch;
        }
        return _byteBuilder.completeAndCoalesce(outputPtr);
    }
    
    /**
     * Will encode given String as UTF-8 (without any quoting), return
     * resulting byte array.
     */
    public byte[] encodeAsUTF8(String text)
    {
        ByteArrayBuilder byteBuilder = _byteBuilder;
        if (byteBuilder == null) {
            // no allocator; can add if we must, shouldn't need to
            _byteBuilder = byteBuilder = new ByteArrayBuilder(null);
        }
        int inputPtr = 0;
        int inputEnd = text.length();
        int outputPtr = 0;
        byte[] outputBuffer = byteBuilder.resetAndGetFirstSegment();
        int outputEnd = outputBuffer.length;
        
        main_loop:
        while (inputPtr < inputEnd) {
            int c = text.charAt(inputPtr++);

            // first tight loop for ascii
            while (c <= 0x7F) {
                if (outputPtr >= outputEnd) {
                    outputBuffer = byteBuilder.finishCurrentSegment();
                    outputEnd = outputBuffer.length;
                    outputPtr = 0;
                }
                outputBuffer[outputPtr++] = (byte) c;
                if (inputPtr >= inputEnd) {
                    break main_loop;
                }
                c = text.charAt(inputPtr++);
            }

            // then multi-byte...
            if (outputPtr >= outputEnd) {
                outputBuffer = byteBuilder.finishCurrentSegment();
                outputEnd = outputBuffer.length;
                outputPtr = 0;
            }
            if (c < 0x800) { // 2-byte
                outputBuffer[outputPtr++] = (byte) (0xc0 | (c >> 6));
            } else { // 3 or 4 bytes
                // Surrogates?
                if (c < SURR1_FIRST || c > SURR2_LAST) { // nope
                    outputBuffer[outputPtr++] = (byte) (0xe0 | (c >> 12));
                    if (outputPtr >= outputEnd) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputEnd = outputBuffer.length;
                        outputPtr = 0;
                    }
                    outputBuffer[outputPtr++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                } else { // yes, surrogate pair
                    if (c > SURR1_LAST) { // must be from first range
                        _throwIllegalSurrogate(c);
                    }
                    // and if so, followed by another from next range
                    if (inputPtr >= inputEnd) {
                        _throwIllegalSurrogate(c);
                    }
                    c = _convertSurrogate(c, text.charAt(inputPtr++));
                    if (c > 0x10FFFF) { // illegal, as per RFC 4627
                        _throwIllegalSurrogate(c);
                    }
                    outputBuffer[outputPtr++] = (byte) (0xf0 | (c >> 18));
                    if (outputPtr >= outputEnd) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputEnd = outputBuffer.length;
                        outputPtr = 0;
                    }
                    outputBuffer[outputPtr++] = (byte) (0x80 | ((c >> 12) & 0x3f));
                    if (outputPtr >= outputEnd) {
                        outputBuffer = byteBuilder.finishCurrentSegment();
                        outputEnd = outputBuffer.length;
                        outputPtr = 0;
                    }
                    outputBuffer[outputPtr++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                }
            }
            if (outputPtr >= outputEnd) {
                outputBuffer = byteBuilder.finishCurrentSegment();
                outputEnd = outputBuffer.length;
                outputPtr = 0;
            }
            outputBuffer[outputPtr++] = (byte) (0x80 | (c & 0x3f));
        }
        return _byteBuilder.completeAndCoalesce(outputPtr);
    }
    
    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    private int _appendSingleEscape(int escCode, char[] quoteBuffer)
    {
        if (escCode < 0) { // control char, value -(char + 1)
            int value = -(escCode + 1);
            quoteBuffer[1] = 'u';
            // We know it's a control char, so only the last 2 chars are non-0
            quoteBuffer[4] = HEX_CHARS[value >> 4];
            quoteBuffer[5] = HEX_CHARS[value & 0xF];
            return 6;
        }
        quoteBuffer[1] = (char) escCode;
        return 2;
    }

    private int _appendByteEscape(int ch, int escCode, ByteArrayBuilder byteBuilder, int ptr)
    {
        byteBuilder.setCurrentSegmentLength(ptr);
        byteBuilder.append(INT_BACKSLASH);
        if (escCode < 0) { // standard escape
            byteBuilder.append(INT_U);
            if (ch > 0xFF) {
                int hi = (ch >> 8);
                byteBuilder.append(HEX_BYTES[hi >> 4]);
                byteBuilder.append(HEX_BYTES[hi & 0xF]);
                ch &= 0xFF;
            } else {
                byteBuilder.append(INT_0);
                byteBuilder.append(INT_0);
            }
            byteBuilder.append(HEX_BYTES[ch >> 4]);
            byteBuilder.append(HEX_BYTES[ch & 0xF]);
        } else { // 2-char simple escape
            byteBuilder.append((byte) escCode);
        }
        return byteBuilder.getCurrentSegmentLength();
    }
    
    /**
     * Method called to calculate UTF code point, from a surrogate pair.
     */
    private int _convertSurrogate(int firstPart, int secondPart)
    {
        // Ok, then, is the second part valid?
        if (secondPart < SURR2_FIRST || secondPart > SURR2_LAST) {
            throw new IllegalArgumentException("Broken surrogate pair: first char 0x"+Integer.toHexString(firstPart)+", second 0x"+Integer.toHexString(secondPart)+"; illegal combination");
        }
        return 0x10000 + ((firstPart - SURR1_FIRST) << 10) + (secondPart - SURR2_FIRST);
    }
    
    private void _throwIllegalSurrogate(int code)
    {
        if (code > 0x10FFFF) { // over max?
            throw new IllegalArgumentException("Illegal character point (0x"+Integer.toHexString(code)+") to output; max is 0x10FFFF as per RFC 4627");
        }
        if (code >= SURR1_FIRST) {
            if (code <= SURR1_LAST) { // Unmatched first part (closing without second part?)
                throw new IllegalArgumentException("Unmatched first part of surrogate pair (0x"+Integer.toHexString(code)+")");
            }
            throw new IllegalArgumentException("Unmatched second part of surrogate pair (0x"+Integer.toHexString(code)+")");
        }
        // should we ever get this?
        throw new IllegalArgumentException("Illegal character point (0x"+Integer.toHexString(code)+") to output");
    }

}
