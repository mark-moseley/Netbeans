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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.lib.uihandler;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

/**
 *
 * @author Jindrich Sedek
 */
public class PasswdEncryption {

    private static final String delimiter = ":"; //NOI18N

    public static String encrypt(String text) throws IOException, GeneralSecurityException {
        return encrypt(text, getPublicKey());
    }

    public static byte[] encrypt(byte[] text) throws IOException, GeneralSecurityException {
        return encrypt(text, getPublicKey());
    }

    public static byte[] encrypt(byte[] text, PublicKey key) throws IOException, GeneralSecurityException {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // NOI18N
        rsaCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encoded = null;
        encoded = rsaCipher.doFinal(text);
        return encoded;
    }

    public static byte[] decrypt(byte[] text, PrivateKey key) throws IOException, GeneralSecurityException {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // NOI18N
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = null;
        decoded = rsaCipher.doFinal(text);
        return decoded;
    }

    public static String encrypt(String text, PublicKey key) throws IOException, GeneralSecurityException {
        byte[] encrypted = encrypt(text.getBytes(), key);
        return arrayToString(encrypted);
    }

    public static String decrypt(String text, PrivateKey key) throws IOException, GeneralSecurityException {
        byte[] decrypted = decrypt(stringToArray(text), key);
        return new String(decrypted);
    }

    private static String arrayToString(byte[] array) {
        String result = "";
        for (int i = 0; i < array.length; i++) {
            byte b = array[i];
            result = result.concat(Byte.toString(b) + delimiter);
        }
        return result;
    }

    private static byte[] stringToArray(String str) {
        String[] numbers = str.split(delimiter);
        byte[] result = new byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = Byte.parseByte(numbers[i]);
        }
        return result;
    }

    private static PublicKey getPublicKey() throws IOException, GeneralSecurityException {
        InputStream inputStr = PasswdEncryption.class.getResourceAsStream("pubKey"); // NOI18N
        byte[] encodedKey = new byte[inputStr.available()];
        inputStr.read(encodedKey);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA"); // NOI18N
        PublicKey publicKey = kf.generatePublic(publicKeySpec);
        return publicKey;
    }
}
