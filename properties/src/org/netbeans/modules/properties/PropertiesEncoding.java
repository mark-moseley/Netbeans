/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.properties;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import static java.lang.Math.min;
import static java.nio.charset.CoderResult.OVERFLOW;
import static java.nio.charset.CoderResult.UNDERFLOW;

/**
 *
 * @author  Marian Petras
 */
final class PropertiesEncoding extends FileEncodingQueryImplementation {
    
    /*
     * TO DO:
     * 
     * DECODER
     * - leave some characters in the form of escape sequence, e.g. 0x00 - 0x1f
     * - allow decoding of supplementary characters (?)
     */
    
    private final Charset encoding;
    
    /** Creates a new instance of PropertiesEncoding */
    public PropertiesEncoding() {
        encoding = new PropCharset();
    }
    
    public Charset getEncoding(FileObject file) {
        return encoding;
    }
    
    Charset getEncoding() {
        return encoding;
    }
    
    /**
     *
     */
    static final class PropCharset extends Charset {
        
        PropCharset() {
            super("resource_bundle_charset", null);                     //NOI18N
        }

        public boolean contains(Charset charset) {
            return true;
        }

        public PropCharsetEncoder newEncoder() {
            return new PropCharsetEncoder(this);
        }
        
        public PropCharsetDecoder newDecoder() {
            return new PropCharsetDecoder(this);
        }

    }
    
    /**
     *
     * @author  Marian Petras
     */
    static final class PropCharsetEncoder extends CharsetEncoder {
        
        private static final int avgEncodedTokenLen = 3;
        private static final int maxEncodedTokenLen = 6;
        
        private static final int inBufSize = 8192;
        private static final int outBufSize = inBufSize * avgEncodedTokenLen;
        
        private final char[] inBuf = new char[inBufSize];
        private final byte[] outBuf = new byte[outBufSize];
        
        private int inBufPos, outBufPos;
        
        private boolean emptyIn;
        private boolean fullOut;
        private boolean emptyInBuf;
        
        PropCharsetEncoder(Charset charset) {
            super(charset, avgEncodedTokenLen, maxEncodedTokenLen);
        }
        
        {
            implReset();
        }
        
        @Override
        protected void implReset() {
            inBufPos = 0;
            outBufPos = 0;
            
            emptyIn = false;
            fullOut = false;
            emptyInBuf = true;
        }

        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            emptyIn = false;
            fullOut = false;
            try {
                readInLoop:
                for (;;) {
                    readIn(in);
                    for (;;) {
                        encodeBuf();
                        if (emptyInBuf && !emptyIn) {
                            continue readInLoop;
                        }
                        flushOutBuf(out);
                        if (fullOut) {
                            return OVERFLOW;
                        } else if (emptyInBuf && emptyIn) {
                            return UNDERFLOW;
                        }
                    }
                }
            } catch (BufferUnderflowException ex) {
                assert false;                           //this should not happen
                return UNDERFLOW;
            } catch (BufferOverflowException ex) {
                assert false;                           //this should not happen
                return OVERFLOW;
            }
        }
        
        @Override
        protected CoderResult implFlush(ByteBuffer out) {
            return flushOutBuf(out) ? OVERFLOW
                                    : UNDERFLOW;
        }
        
        /**
         * Reads and stores as many characters from the input buffer as possible.
         * If there are no more characters available in the input buffer,
         * sets flag variable {@link #emptyIn} to {@code true}.
         */
        private void readIn(CharBuffer in) {
            if (emptyIn) {
                return;
            }
            
            int inRemaining = in.remaining();
            if (inRemaining == 0) {
                emptyIn = true;
                return;
            }
            
            int bufRemaining = inBuf.length - inBufPos;
            if (bufRemaining == 0) {
                /* no space in inBuf */
                return;
            }
            
            int length = min(inRemaining, bufRemaining);
            in.get(inBuf, inBufPos, length);
            inBufPos += length;
            emptyInBuf = false;
            
            if (length == inRemaining) {
                assert in.remaining() == 0;
                emptyIn = true;
            }
        }
        
        /**
         * Encodes as many chars from the internal input buffer as possible.
         */
        private void encodeBuf() {
            if (emptyInBuf) {
                return;
            }
            
            int encodingInBufPos = 0;
            while ((encodingInBufPos < inBufPos)
                    && (outBufPos <= outBufSize - maxEncodedTokenLen)) {
                encodeChar(inBuf[encodingInBufPos++]);
            }
            
            int remainder = inBufPos - encodingInBufPos;
            if (remainder != 0) {
                System.arraycopy(inBuf, encodingInBufPos,
                                 inBuf, 0,
                                 remainder);
            }
            inBufPos = remainder;
            emptyInBuf = (inBufPos == 0);
        }
        
        /**
         * Writes as many as possible bytes from the {@code outBuf} to the given
         * {@code ByteBuffer} and removes the written bytes from {@code outBuf}.
         * 
         * @return  {@code true} if the given {@code out} buffer is overflown,
         *          {@code false} otherwise
         */
        private boolean flushOutBuf(ByteBuffer out) {
            if (fullOut) {
                return true;
            }
            
            int outRemaining = out.remaining();
            if (outRemaining == 0) {
                fullOut = true;
                return true;
            }
            
            if (outBufPos == 0) {
                /* nothing to flush */
                return false;
            }
            
            int length = min(outRemaining, outBufPos);
            out.put(outBuf, 0, length);
            
            int remainder = outBufPos - length;
            if (remainder != 0) {
                System.arraycopy(outBuf, length,
                                 outBuf, 0,
                                 remainder);
            }
            outBufPos = remainder;
            
            if (length == outRemaining) {
                assert out.remaining() == 0;
                fullOut = true;
            }
            
            return (remainder != 0);
        }
        
        private static final byte zeroByte = (byte) '0';
        private static final byte[] hexadecimalChars
                = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        
        private int encodeChar(final char c) {
            final int oldPos = outBufPos;
            final int cInt = (int) c;
            
            int index;
            
            if ((c == '\r') || (c == '\n') || (c == '\t') || (c == '\f')) {
                /*
                 * Do not translate Space, Tab and FF characters
                 * escape sequences - such translation might change semantics,
                 * which is not desirable (see issue #111530).
                 */
                outBuf[outBufPos++] = (byte) c;
            } else if ((c < '\u0020') || (c > '\u007e')) {
                outBuf[outBufPos++] = (byte) '\\';
                outBuf[outBufPos++] = (byte) 'u';
                if (c >= '\u0100') {
                    outBuf[outBufPos++] = hexadecimalChars[(cInt >> 12) & 0x000f];
                    outBuf[outBufPos++] = hexadecimalChars[(cInt >>  8) & 0x000f];
                } else {
                    outBuf[outBufPos++] = zeroByte;
                    outBuf[outBufPos++] = zeroByte;
                }
                outBuf[outBufPos++] = hexadecimalChars[(cInt >> 4) & 0x000f];
                outBuf[outBufPos++] = hexadecimalChars[cInt & 0x000f];
            } else {
                outBuf[outBufPos++] = (byte) c;
            }
            
            return outBufPos - oldPos;
        }
        
        byte[] encodeCharForTests(final char c) {
            reset();
            
            final int tokenLength = encodeChar(c);
            byte[] result = new byte[tokenLength];
            System.arraycopy(outBuf, 0, result, 0, tokenLength);
            return result;
        }
        
        byte[] encodeStringForTests(final String s) throws CharacterCodingException {
            ByteBuffer resultBuf = encode(CharBuffer.wrap(s));
            byte[] resultBufArray = resultBuf.array();
            int resultBufPos = resultBuf.limit();
            if (resultBufPos == resultBufArray.length) {
                return resultBufArray;
            } else {
                byte[] result = new byte[resultBufPos];
                System.arraycopy(resultBufArray, 0, result, 0, resultBufPos);
                return result;
            }
        }
        
    }

    /**
     *
     */
    static final class PropCharsetDecoder extends CharsetDecoder {
        
        private final Logger log = Logger.getLogger(getClass().getName().replace('$', '.'));

        private static enum State {
            INITIAL,
            BACKSLASH,
            UNICODE,
        }
        
        private static final float avgCharsPerByte = 1.00f;
        private static final float maxCharsPerByte = 6.00f;
        private static final int maxCharsPerByteInt = 6;
        /*
         * Five chars are written to the output when a malformed unicode
         * sequence is detected. Unicode sequences are six bytes long;
         * if the first five bytes formed a valid sequence
         * (e.g. <backslash>, "u", "1", "2", "3") and the sixth byte is not
         * a hexadecimal digit, we transform the first five bytes
         * of the sequence to (five) characters and send them to the output.
         * (The sixth byte is re-read and handled in the next round
         * of the decoding cycle.)
         */
        
        private static final int inBufSize = 8192;
        private static final int outBufSize = inBufSize;
        
        private final byte[] inBuf = new byte[inBufSize];
        private final char[] outBuf = new char[outBufSize];
        
        private int inBufPos, outBufPos;
        
        private boolean emptyIn;
        private boolean fullOut;
        private boolean emptyInBuf;
        
        private State state;
        private int unicodeBytesRead;
        private int unicodeValue;
        
        /** used when flushing a unicode sequence to the out buffer */
        private char[] unicodeValueChars = new char[4];
        
        PropCharsetDecoder(Charset charset) {
            super(charset, avgCharsPerByte, maxCharsPerByte);
        }
        
        {
            implReset();
        }

        @Override
        protected void implReset() {
            log.finer("");
            log.finer("implReset() called");
            
            inBufPos = 0;
            outBufPos = 0;
            
            emptyIn = false;
            fullOut = false;
            emptyInBuf = true;
            
            state = State.INITIAL;
            unicodeBytesRead = 0;
        }

        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            log.finer("");
            log.finer("decodeLoop() called");
            if (log.isLoggable(Level.FINEST)) {
                String inCount  = String.format("%5d", in.remaining());
                String outCount = String.format("%5d", out.remaining());
                log.finest("    - input:  " + inCount  + " bytes");
                log.finest("    - output: " + outCount + " chars");
            }
            
            emptyIn = false;
            fullOut = false;
            try {
                readInLoop:
                for (;;) {
                    readIn(in);
                    for (;;) {
                        CoderResult coderResult = decodeBuf();
                        if (coderResult != null) {
                            return coderResult;
                        }
                        if (emptyInBuf && !emptyIn) {
                            continue readInLoop;
                        } else if (emptyIn && hasPendingCharacters()) {
                            handlePendingCharacters();
                        }
                        flushOutBuf(out);
                        if (fullOut) {
                            log.finest(" - returning OVERFLOW");
                            return OVERFLOW;
                        } else if (emptyInBuf && emptyIn && !hasPendingCharacters()) {
                            log.finest(" - returning UNDERFLOW");
                            return UNDERFLOW;
                        }
                    }
                }
            } catch (BufferUnderflowException ex) {
                assert false;                           //this should not happen
                return UNDERFLOW;
            } catch (BufferOverflowException ex) {
                assert false;                           //this should not happen
                return OVERFLOW;
            }
        }
        
        /**
         * This is method is only necessary because of bug in JDK 1.5.x that
         * {@code flush()} is not always called when it should be.
         * Once JDK 1.5.x is not supported, remove this method.
         * 
         * @see  #handlePendingCharacters()
         */
        private boolean hasPendingCharacters() {
            return state != State.INITIAL;
        }
        
        /**
         * This is method is only necessary because of bug in JDK 1.5.x that
         * {@code flush()} is not always called when it should be.
         * Once JDK 1.5.x is not supported, move this method's functionality
         * to method {@code implFlush()} and remove this method.
         * 
         * @see  <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6221056">JDK bug 6221056</a>
         * @see  <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4744247">JDK bug 4744247</a>
         */
        private void handlePendingCharacters() {
            log.finer("handlePendingCharacters()");
            if (!hasPendingCharacters()) {
                log.finer(" - no pending characters");
                return;
            }
            
            switch (state) {
                case INITIAL:
                    assert false;
                    break;
                case BACKSLASH:
                    log.finer(" - backslash pending");
                    outBuf[outBufPos++] = '\\';
                    break;
                case UNICODE:
                    log.finer(" - broken \\u.... sequence pending");
                    if (log.isLoggable(Level.FINEST)) {
                        log.finest("    - " + unicodeBytesRead + " unicode value bytes pending");
                    }
                    assert (unicodeBytesRead >= 0) && (unicodeBytesRead < 4);
                    flushUnicodeSequence();
                    break;
                default:
                    assert false;
                    break;
            }
            state = State.INITIAL;
        }

        @Override
        protected CoderResult implFlush(CharBuffer out) {
            log.finer("");
            log.finer("implFlush() called");
            fullOut = out.hasRemaining();
            return flushOutBuf(out) ? OVERFLOW
                                    : UNDERFLOW;
        }
        
        /**
         * Reads and stores as many characters from the input buffer as possible.
         * If there are no more characters available in the input buffer,
         * sets flag variable {@link #emptyIn} to {@code true}.
         */
        private void readIn(ByteBuffer in) {
            log.finer("filling inBuf: ");
            if (emptyIn) {
                log.finer(" - input empty (emptyIn already set)");
                return;
            }
            
            int inRemaining = in.remaining();
            if (inRemaining == 0) {
                log.finer(" - input empty (emptyIn will be set)");
                emptyIn = true;
                return;
            }
            
            int bufRemaining = inBuf.length - inBufPos;
            if (bufRemaining == 0) {
                log.finer(" - no space remaining in inBuf");
                /* no space in inBuf */
                return;
            }
            
            int length = min(inRemaining, bufRemaining);
            if (log.isLoggable(Level.FINER)) {
                log.finer(" - " + length + " bytes will be read");
            }
            in.get(inBuf, inBufPos, length);
            inBufPos += length;
            emptyInBuf = false;
            
            if (length == inRemaining) {
                assert in.remaining() == 0;
                log.finer(" - all remaining bytes were read (emptyIn will be set)");
                emptyIn = true;
            }
        }
        
        /**
         * Encodes as many chars from the internal input buffer as possible.
         */
        private CoderResult decodeBuf() {
            log.finer("decoding inBuf, writing to outBuf");
            if (emptyInBuf) {
                log.finer(" - inBuf is empty - nothing to decode");
                return null;
            }
            
            CoderResult result = null;
            int decodingInBufPos = 0;
            log.finest(" - decoding bytes:");
            log.finest("     - initial state: " + state);
            while ((decodingInBufPos < inBufPos)
                    && (outBufPos <= outBufSize - maxCharsPerByteInt)) {
                int decodedChars = decodeByte(inBuf[decodingInBufPos++]);
                if (log.isLoggable(Level.FINEST)) {
                    StringBuilder sb = new StringBuilder(60);
                    sb.append("     - byte 0x");
                    sb.append(hexavalue(inBuf[decodingInBufPos - 1]));
                    sb.append(" => ").append(state);
                    log.finest(sb.toString());
                }
                if (decodedChars < 0) {
                    /* put back the character following the broken sequence: */
                    decodingInBufPos--;
                    log.finer("          - last byte returned to be processed again");

                    unicodeBytesRead = 0;
                    unicodeValue = 0;
                    state = State.INITIAL;
                    //break;
                }
            }
            int remainder = inBufPos - decodingInBufPos;
            if (remainder != 0) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer(" - " + remainder + " bytes will remain in the inBuf");
                }
                System.arraycopy(inBuf, decodingInBufPos,
                                 inBuf, 0,
                                 remainder);
            } else {
                log.finer(" - all bytes were successfully decoded");
            }
            inBufPos = remainder;
            emptyInBuf = (inBufPos == 0);
            return result;
        }
        
        /**
         * 
         * @return  {@code true} if the given {@code out} buffer is overflown,
         *          {@code false} otherwise
         */
        private boolean flushOutBuf(CharBuffer out) {
            log.finer("flushing outBuf");
            if (outBufPos == 0) {
                /* nothing to flush */
                log.finer(" - outBuf is empty - nothing to flush");
                return false;
            }
            
            if (fullOut) {      //we know that (outBufPos != null)
                log.finer(" - output CharBuffer is full (fullOut already set)");
                return true;
            }
            
            int outRemaining = out.remaining();
            if (outRemaining == 0) {
                log.finer(" - output CharBuffer is full (fullOut will be set)");
                fullOut = true;
                return true;
            }
            
            int length = min(outRemaining, outBufPos);
            if (log.isLoggable(Level.FINER)) {
                log.finer(" - " + length + " chars will be written");
            }
            out.put(outBuf, 0, length);
            
            int remainder = outBufPos - length;
            if (remainder != 0) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer(" - " + remainder + " bytes will remain in the outBuf");
                }
                System.arraycopy(outBuf, length,
                                 outBuf, 0,
                                 remainder);
            } else {
                log.finer(" - all bytes were successfully flushed");
            }
            outBufPos = remainder;
            
            if (length == outRemaining) {
                assert out.remaining() == 0;
                log.finer(" - output CharBuffer is now full (fullOut will be set)");
                fullOut = true;
            }
            
            return (remainder != 0);
        }
        
        private static final String hexadecimalChars
                                    = "0123456789abcdefABCDEF";         //NOI18N
        
        private int decodeByte(final byte b) {
            final int oldPos = outBufPos;
            final int bInt = (b >= 0 ? b : b + 256);
            assert (bInt >= 0) && ((bInt & 0xff) == bInt);
            
            final char bChar = (char) bInt;
            
            switch (state) {
                case INITIAL:
                    if (bChar == '\\') {
                        state = State.BACKSLASH;
                    } else {
                        outBuf[outBufPos++] = bChar;
                        /* keep the state at INITIAL */
                    }
                    break;
                case BACKSLASH:
                    if (bChar == 'u') {
                        state = State.UNICODE;
                    } else {
                        outBuf[outBufPos++] = '\\';
                        outBuf[outBufPos++] = bChar;
                        state = State.INITIAL;
                    }
                    break;
                case UNICODE:
                    boolean malformed = false;
                    int index = hexadecimalChars.indexOf(bChar);
                    if (index >= 0) {
                        if (index > 15) {   //one of [A-F] used
                            index -= 6;     //transform to lowercase
                        }
                        assert index <= 15;
                        unicodeValue = (unicodeValue << 4) | index;
                        if (++unicodeBytesRead == 4) {
                            if ((unicodeValue == 0x20)
                                    || (unicodeValue == 0x09)
                                    || (unicodeValue == 0x0c)) {
                                unicodeValueChars[3] = bChar;
                                /*
                                 * Do not translate sequences \\u0020 (Space),
                                 * \\u0009 (Tab) and \\u000c (Form-feed).
                                 * Changing form of these unicode sequences
                                 * to one-character form might change
                                 * semantics, which is not desirable
                                 * (see issue #111530).
                                 */
                                flushUnicodeSequence();
                            } else {
                                outBuf[outBufPos++] = (char) unicodeValue;
                            }
                            state = State.INITIAL;
                        } else {
                            unicodeValueChars[unicodeBytesRead - 1] = bChar;
                            /* keep the state at UNICODE */
                        }
                    } else {
                        malformed = true;
                        /*
                         * send the malformed unicode sequence to the output
                         */
                        flushUnicodeSequence();
                        state = State.INITIAL;
                    }
                    if (state != State.UNICODE) {
                        unicodeBytesRead = 0;
                        unicodeValue = 0;
                        if (malformed) {
                            return -1;
                        }
                    }
                    break;
                default:
                    assert false;
                    break;
            }
            
            return outBufPos - oldPos;
        }

        /**
         * Writes the buffered Unicode sequence (possible incomplete)
         * to the output buffer ({@link #outBuf}).
         * It also resets fields {@link #unicodeBytesRead}
         * and {@link #unicodeValue} to {@code 0}.
         */
        private void flushUnicodeSequence() {
            outBuf[outBufPos++] = '\\';
            outBuf[outBufPos++] = 'u';
            for (int i = 0; i < unicodeBytesRead; i++) {
                outBuf[outBufPos++] = unicodeValueChars[i];
            }
            unicodeBytesRead = 0;
            unicodeValue = 0;
        }
        
        private static char[] hexavalue(byte b) {
            final int bInt = (b >= 0 ? b : b + 256);
            char[] result = new char[2];
            result[0] = hexadecimalChars.charAt(bInt / 16);
            result[1] = hexadecimalChars.charAt(bInt % 16);
            return result;
        }
        
        char[] decodeBytesForTests(final byte[] bytes) throws CharacterCodingException {
            CharBuffer resultBuf = decode(ByteBuffer.wrap(bytes));
            char[] resultBufArray = resultBuf.array();
            int resultBufPos = resultBuf.limit();
            if (resultBufPos == resultBufArray.length) {
                return resultBufArray;
            } else {
                char[] result = new char[resultBufPos];
                System.arraycopy(resultBufArray, 0, result, 0, resultBufPos);
                return result;
            }
        }
        
    }

}
