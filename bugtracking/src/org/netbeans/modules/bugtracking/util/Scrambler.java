/*****************************************************************************
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

 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.modules.bugtracking.util;

import java.io.ByteArrayOutputStream;

/**
 * Scrambles text (the password) using the standard scheme described in the
 * CVS protocol version 1.10. This encoding is trivial and should not be
 * used for security, but rather as a mechanism for avoiding inadvertant
 * compromise.
 * @author  Robert Greig, Tomas Stupka
 */
class Scrambler {

    private static final char [] characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    /**
     * The mapping array
     */
    private int[] shifts;

    /**
     * The single instance of this class (Singleton design pattern)
     */
    public static Scrambler instance;

    /**
     * Do not instantiate the scrambler directly. Use the getInstance() method
     */
    private Scrambler() {
        int i;
        shifts = new int[256];

        for (i = 0; i < 32; ++i) {
            shifts[i] = i;
        }

        shifts[i++] = 114;
        shifts[i++] = 120;
        shifts[i++] = 53;
        shifts[i++] = 79;
        shifts[i++] = 96;
        shifts[i++] = 109;
        shifts[i++] = 72;
        shifts[i++] = 108;
        shifts[i++] = 70;
        shifts[i++] = 64;
        shifts[i++] = 76;
        shifts[i++] = 67;
        shifts[i++] = 116;
        shifts[i++] = 74;
        shifts[i++] = 68;
        shifts[i++] = 87;
        shifts[i++] = 111;
        shifts[i++] = 52;
        shifts[i++] = 75;
        shifts[i++] = 119;
        shifts[i++] = 49;
        shifts[i++] = 34;
        shifts[i++] = 82;
        shifts[i++] = 81;
        shifts[i++] = 95;
        shifts[i++] = 65;
        shifts[i++] = 112;
        shifts[i++] = 86;
        shifts[i++] = 118;
        shifts[i++] = 110;
        shifts[i++] = 122;
        shifts[i++] = 105;
        shifts[i++] = 41;
        shifts[i++] = 57;
        shifts[i++] = 83;
        shifts[i++] = 43;
        shifts[i++] = 46;
        shifts[i++] = 102;
        shifts[i++] = 40;
        shifts[i++] = 89;
        shifts[i++] = 38;
        shifts[i++] = 103;
        shifts[i++] = 45;
        shifts[i++] = 50;
        shifts[i++] = 42;
        shifts[i++] = 123;
        shifts[i++] = 91;
        shifts[i++] = 35;
        shifts[i++] = 125;
        shifts[i++] = 55;
        shifts[i++] = 54;
        shifts[i++] = 66;
        shifts[i++] = 124;
        shifts[i++] = 126;
        shifts[i++] = 59;
        shifts[i++] = 47;
        shifts[i++] = 92;
        shifts[i++] = 71;
        shifts[i++] = 115;
        shifts[i++] = 78;
        shifts[i++] = 88;
        shifts[i++] = 107;
        shifts[i++] = 106;
        shifts[i++] = 56;
        shifts[i++] = 36;
        shifts[i++] = 121;
        shifts[i++] = 117;
        shifts[i++] = 104;
        shifts[i++] = 101;
        shifts[i++] = 100;
        shifts[i++] = 69;
        shifts[i++] = 73;
        shifts[i++] = 99;
        shifts[i++] = 63;
        shifts[i++] = 94;
        shifts[i++] = 93;
        shifts[i++] = 39;
        shifts[i++] = 37;
        shifts[i++] = 61;
        shifts[i++] = 48;
        shifts[i++] = 58;
        shifts[i++] = 113;
        shifts[i++] = 32;
        shifts[i++] = 90;
        shifts[i++] = 44;
        shifts[i++] = 98;
        shifts[i++] = 60;
        shifts[i++] = 51;
        shifts[i++] = 33;
        shifts[i++] = 97;
        shifts[i++] = 62;
        shifts[i++] = 77;
        shifts[i++] = 84;
        shifts[i++] = 80;
        shifts[i++] = 85;
        shifts[i++] = 223;
        shifts[i++] = 225;
        shifts[i++] = 216;
        shifts[i++] = 187;
        shifts[i++] = 166;
        shifts[i++] = 229;
        shifts[i++] = 189;
        shifts[i++] = 222;
        shifts[i++] = 188;
        shifts[i++] = 141;
        shifts[i++] = 249;
        shifts[i++] = 148;
        shifts[i++] = 200;
        shifts[i++] = 184;
        shifts[i++] = 136;
        shifts[i++] = 248;
        shifts[i++] = 190;
        shifts[i++] = 199;
        shifts[i++] = 170;
        shifts[i++] = 181;
        shifts[i++] = 204;
        shifts[i++] = 138;
        shifts[i++] = 232;
        shifts[i++] = 218;
        shifts[i++] = 183;
        shifts[i++] = 255;
        shifts[i++] = 234;
        shifts[i++] = 220;
        shifts[i++] = 247;
        shifts[i++] = 213;
        shifts[i++] = 203;
        shifts[i++] = 226;
        shifts[i++] = 193;
        shifts[i++] = 174;
        shifts[i++] = 172;
        shifts[i++] = 228;
        shifts[i++] = 252;
        shifts[i++] = 217;
        shifts[i++] = 201;
        shifts[i++] = 131;
        shifts[i++] = 230;
        shifts[i++] = 197;
        shifts[i++] = 211;
        shifts[i++] = 145;
        shifts[i++] = 238;
        shifts[i++] = 161;
        shifts[i++] = 179;
        shifts[i++] = 160;
        shifts[i++] = 212;
        shifts[i++] = 207;
        shifts[i++] = 221;
        shifts[i++] = 254;
        shifts[i++] = 173;
        shifts[i++] = 202;
        shifts[i++] = 146;
        shifts[i++] = 224;
        shifts[i++] = 151;
        shifts[i++] = 140;
        shifts[i++] = 196;
        shifts[i++] = 205;
        shifts[i++] = 130;
        shifts[i++] = 135;
        shifts[i++] = 133;
        shifts[i++] = 143;
        shifts[i++] = 246;
        shifts[i++] = 192;
        shifts[i++] = 159;
        shifts[i++] = 244;
        shifts[i++] = 239;
        shifts[i++] = 185;
        shifts[i++] = 168;
        shifts[i++] = 215;
        shifts[i++] = 144;
        shifts[i++] = 139;
        shifts[i++] = 165;
        shifts[i++] = 180;
        shifts[i++] = 157;
        shifts[i++] = 147;
        shifts[i++] = 186;
        shifts[i++] = 214;
        shifts[i++] = 176;
        shifts[i++] = 227;
        shifts[i++] = 231;
        shifts[i++] = 219;
        shifts[i++] = 169;
        shifts[i++] = 175;
        shifts[i++] = 156;
        shifts[i++] = 206;
        shifts[i++] = 198;
        shifts[i++] = 129;
        shifts[i++] = 164;
        shifts[i++] = 150;
        shifts[i++] = 210;
        shifts[i++] = 154;
        shifts[i++] = 177;
        shifts[i++] = 134;
        shifts[i++] = 127;
        shifts[i++] = 182;
        shifts[i++] = 128;
        shifts[i++] = 158;
        shifts[i++] = 208;
        shifts[i++] = 162;
        shifts[i++] = 132;
        shifts[i++] = 167;
        shifts[i++] = 209;
        shifts[i++] = 149;
        shifts[i++] = 241;
        shifts[i++] = 153;
        shifts[i++] = 251;
        shifts[i++] = 237;
        shifts[i++] = 236;
        shifts[i++] = 171;
        shifts[i++] = 195;
        shifts[i++] = 243;
        shifts[i++] = 233;
        shifts[i++] = 253;
        shifts[i++] = 240;
        shifts[i++] = 194;
        shifts[i++] = 250;
        shifts[i++] = 191;
        shifts[i++] = 155;
        shifts[i++] = 142;
        shifts[i++] = 137;
        shifts[i++] = 245;
        shifts[i++] = 235;
        shifts[i++] = 163;
        shifts[i++] = 242;
        shifts[i++] = 178;
        shifts[i++] = 152;
    }

    /**
     * Get an instance of the standard scrambler
     */
    public static Scrambler getInstance() {
        if (instance == null) {
            instance = new Scrambler();
        }
        return instance;
    }

    /**
     * Scramble text, turning it into a String of scrambled data
     * @return a String of scrambled data
     */
    public String scramble(String text) {
        StringBuffer buf = new StringBuffer("A"); //NOI18N

        if (text != null) {
            for (int i = 0; i < text.length(); ++i) {
                buf.append(scramble(text.charAt(i)));
            }
        }        
        return new String(encode(buf.toString().getBytes()));
    }
    
    public String descramble(String scrambledText) {        
        StringBuffer buf = new StringBuffer(); 
        if (scrambledText != null) {            
            byte[] decoded = decode(scrambledText); 
            for (int i = 1; i < decoded.length; ++i) {                
                buf.append(scramble((char)decoded[i]));
            }
        }
        return buf.toString();
    }    
    
    private char scramble(char ch) {               
        int i = ( shifts[ (((int) ch) &  0xFF) & 255] & 255 ) & 
                ( shifts[ (((int) ch) &  0x00FF) & 255] & 255 );
        return Character.toChars(i)[0];
    }     
    
    private byte[] decode(String str) {        
        return decode64(str);
    }
    
    private byte[] encode(byte[] encode) {        
        return encode64(encode).getBytes();
    }

    private static String encode64(byte [] data) {
        return encode64(data, false);
    }

    private static String encode64(byte [] data, boolean useNewlines) {
        int length = data.length;
        StringBuffer sb = new StringBuffer(data.length * 3 / 2);

        int end = length - 3;
        int i = 0;
        int lineCount = 0;

        while (i <= end) {
            int d = ((((int) data[i]) & 0xFF) << 16) | ((((int) data[i + 1]) & 0xFF) << 8) | (((int) data[i + 2]) & 0xFF);
            sb.append(characters[(d >> 18) & 0x3F]);
            sb.append(characters[(d >> 12) & 0x3F]);
            sb.append(characters[(d >> 6) & 0x3F]);
            sb.append(characters[d & 0x3F]);
            i += 3;

            if (useNewlines && lineCount++ >= 14) {
                lineCount = 0;
                sb.append(System.getProperty("line.separator"));
            }
        }

        if (i == length - 2) {
            int d = ((((int) data[i]) & 0xFF) << 16) | ((((int) data[i + 1]) & 0xFF) << 8);
            sb.append(characters[(d >> 18) & 0x3F]);
            sb.append(characters[(d >> 12) & 0x3F]);
            sb.append(characters[(d >> 6) & 0x3F]);
            sb.append("=");
        } else if (i == length - 1) {
            int d = (((int) data[i]) & 0xFF) << 16;
            sb.append(characters[(d >> 18) & 0x3F]);
            sb.append(characters[(d >> 12) & 0x3F]);
            sb.append("==");
        }
        return sb.toString();
    }

    private static byte [] decode64(String s) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        decode64(s, bos);
        return bos.toByteArray();
}

    private static void decode64(String s, ByteArrayOutputStream bos) {
        int i = 0;
        int len = s.length();
        for (;;) {
            while (i < len && s.charAt(i) <= ' ') i++;
            if (i == len) break;
            int tri = (decode64(s.charAt(i)) << 18) + (decode64(s.charAt(i+1)) << 12) + (decode64(s.charAt(i+2)) << 6) + (decode64 (s.charAt(i+3)));
            bos.write((tri >> 16) & 255);
            if (s.charAt(i+2) == '=') break;
            bos.write((tri >> 8) & 255);
            if (s.charAt(i+3) == '=') break;
            bos.write(tri & 255);
            i += 4;
        }
    }

    private static int decode64(char c) {
        if (c >= 'A' && c <= 'Z') {
            return ((int) c) - 65;
        } else if (c >= 'a' && c <= 'z') {
            return ((int) c) - 97 + 26;
        } else if (c >= '0' && c <= '9') {
            return ((int) c) - 48 + 26 + 26;
        } else switch (c) {
            case '+':
                return 62;
            case '/':
                return 63;
            case '=':
                return 0;
            default:
                throw new RuntimeException("Base64: unexpected code: " + c);
        }
    }
}
