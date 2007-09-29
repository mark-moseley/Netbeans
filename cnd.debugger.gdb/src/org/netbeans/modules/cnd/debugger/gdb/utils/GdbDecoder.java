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

package org.netbeans.modules.cnd.debugger.gdb.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * For localized files in UTF-8, gdb will send back a path with octets consisting
 * of a backslash followed by 3 digits. This decoder converts these pathnames from
 * gdb's format to Unicode's format. Without this conversion, we fail opening/displaying
 * the file.
 * 
 * The implementation of this class is loosely based on the UTF-8 decoder in the JDK.
 * 
 * @author gordonp
 */
public class GdbDecoder extends CharsetDecoder {
    
    public GdbDecoder(Charset cs, CharsetDecoder decoder) {
        super(cs, decoder.averageCharsPerByte(), decoder.maxCharsPerByte());
    }

    @Override
    protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        int lastb = 0;
        int b1, b2, b3;
        int v1, v2;

        byte[] sa = src.array();
        int sp = src.arrayOffset() + src.position();
        int sl = src.arrayOffset() + src.limit();

        char[] da = dst.array();
        int dp = dst.arrayOffset() + dst.position();
        int dl = dst.arrayOffset() + dst.limit();

        try {
            while (sp < sl) {
                b1 = sa[sp];
                switch ((b1 >> 4) & 0x0f) {
                    case 0: case 1: case 2: case 3:
                    case 4: case 5: case 6: case 7:
                        if (b1 == '\\' && lastb != '\\' &&
                                (v1 = getValue(sa, sp)) != -1 &&
                                (v2 = getValue(sa, sp + 4)) != -1) {
                            if (sl - sp < 2) {
                                return CoderResult.UNDERFLOW;
                            } else if (dl - dp < 1) {
                                return CoderResult.OVERFLOW;
                            }
                            da[dp++] = ((char) (((v1 & 0x1f) << 6) | ((v2 & 0x3f) << 0)));
                            lastb = 0;
                            sp += 8;
                        } else {
                            // 1 byte, 7 bits: 0xxxxxxx
                            if (dl - dp < 1) {
                                return CoderResult.OVERFLOW;
                            }
                            da[dp++] = (char) (b1 & 0x7f);
                            sp++;
                            lastb = b1;
                        }
                        continue;

                case 12: case 13:
                    // 2 bytes, 11 bits: 110xxxxx 10xxxxxx
                    if (sl - sp < 2) {
                        return CoderResult.UNDERFLOW;
                    } else	if (dl - dp < 1) {
                        return CoderResult.OVERFLOW;
                    } else if (!isContinuation(b2 = sa[sp + 1])) {
                        return CoderResult.malformedForLength(1);
                    }
                    da[dp++] = ((char)(((b1 & 0x1f) << 6) | ((b2 & 0x3f) << 0)));
                    sp += 2;
                    continue;

                case 14:
                    // 3 bytes, 16 bits: 1110xxxx 10xxxxxx 10xxxxxx
                    if (sl - sp < 3) {
                        return CoderResult.UNDERFLOW;
                    } else if (dl - dp < 1) {
                        return CoderResult.OVERFLOW;
                    } else if (!isContinuation(b2 = sa[sp + 1])) {
                        return CoderResult.malformedForLength(1);
                    } else if (!isContinuation(b3 = sa[sp + 2])) {
                        return CoderResult.malformedForLength(2);
                    }
                    da[dp++] = ((char)(((b1 & 0x0f) << 12) |
                                       ((b2 & 0x3f) << 06) |
                                       ((b3 & 0x3f) << 0)));
                    sp += 3;
                    continue;

                case 15:
                    throw new IllegalStateException(); // not currently supported (don't think gdb uses this)

                default:
                    return CoderResult.malformedForLength(1);
                }
            }

            return CoderResult.UNDERFLOW;
        } finally {
            src.position(sp - src.arrayOffset());
            dst.position(dp - dst.arrayOffset());
        }
    }

    /**
     * Return an int value from an escaped octet. These octets are returned
     * by gdb for localized paths and need to be converted to UTF-8.
     * 
     * @param bytes The byte array
     * @param i The starting index into the array
     * @return The numerical octet number or -1
     * @throws java.lang.NumberFormatException
     */
    private int getValue(byte[] bytes, int i) throws NumberFormatException {
        if (bytes[i] == '\\' && (i + 3) <= bytes.length &&
                Character.isDigit(bytes[i + 1]) &&
                Character.isDigit(bytes[i + 2]) &&
                Character.isDigit(bytes[i + 3])) {
            byte[] barray = new byte[3];
            System.arraycopy(bytes, i + 1, barray, 0, 3);
            String octet = new String(barray);
            return Integer.parseInt(octet, 8);
        } else {
            return -1;
        }
    }

    private boolean isContinuation(int b) {
        return ((b & 0xc0) == 0x80);
    }
}
