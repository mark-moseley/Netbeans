/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.DependencyType;
import org.netbeans.installer.utils.helper.Platform;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class StringUtils {
    ////////////////////////////////////////////////////////////////////////////
    // Static
    public static String format(
            final String message,
            final Object... arguments) {
        return MessageFormat.format(message, arguments);
    }
    
    public static String leftTrim(
            final String string) {
        return string.replaceFirst(LEFT_WHITESPACE, EMPTY_STRING);
    }
    
    public static String rightTrim(
            final String string) {
        return string.replaceFirst(RIGHT_WHITESPACE, EMPTY_STRING);
    }
    
    public static char fetchMnemonic(
            final String string) {
        int index = string.indexOf(MNEMONIC_CHAR);
        if ((index != -1) && (index < string.length() - 1)) {
            return string.charAt(index + 1);
        }
        
        return NO_MNEMONIC;
    }
    
    public static String stripMnemonic(
            final String string) {
        return string.replaceFirst(MNEMONIC, EMPTY_STRING);
    }
    
    public static String capitalizeFirst(
            final String string) {
        return EMPTY_STRING + Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
    
    public static String getGetterName(
            final String propertyName) {
        return "get" + capitalizeFirst(propertyName);
    }
    
    public static String getBooleanGetterName(
            final String propertyName) {
        return "is" + capitalizeFirst(propertyName);
    }
    
    public static String getSetterName(
            final String propertyName) {
        return "set" + capitalizeFirst(propertyName);
    }
    
    public static String getFilenameFromUrl(
            final String string) {
        String url = string.trim();
        
        int index = Math.max(
                url.lastIndexOf(FORWARD_SLASH),
                url.lastIndexOf(BACK_SLASH));
        int length = url.length();
        return (index > 0 && (index < length - 1)) ?
            url.substring(index + 1,  length) : null;
    }
    
    public static String formatSize(
            final long longBytes) {
        StringBuffer result = new StringBuffer();
        
        double bytes = (double) longBytes;
        
        // try as GB
        double gigabytes = bytes / 1024. / 1024. / 1024.;
        if (gigabytes > 1.) {
            return String.format("%.1f GB", gigabytes);
        }
        
        // try as MB
        double megabytes = bytes / 1024. / 1024.;
        if (megabytes > 1.) {
            return String.format("%.1f MB", megabytes);
        }
        
        // try as KB
        double kilobytes = bytes / 1024.;
        if (kilobytes > .5) {
            return String.format("%.1f KB", kilobytes);
        }
        
        // return as bytes
        return EMPTY_STRING + longBytes + " B";
    }
    
    public static String asHexString(
            final byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            
            String byteHex = Integer.toHexString(b);
            if (byteHex.length() == 1) {
                byteHex = "0" + byteHex;
            }
            if (byteHex.length() > 2) {
                byteHex = byteHex.substring(byteHex.length() - 2);
            }
            
            builder.append(byteHex);
        }
        
        return builder.toString();
    }
    
    public static String pad(
            final String string,
            final int number) {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < number; i++) {
            builder.append(string);
        }
        
        return builder.toString();
    }
    
    public static String escapeRegExp(
            final String string) {
        return string.replace(BACK_SLASH, BACK_SLASH + BACK_SLASH);
    }
    
    public static String readStream(
            final InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        
        byte[] buffer = new byte[1024];
        while (stream.available() > 0) {
            int read = stream.read(buffer);
            
            String readString = new String(buffer, 0, read);
            String[] strings = readString.split(NEW_LINE_PATTERN);
            for(int i=0;i<strings.length;i++) {
                builder.append(strings[i]);
                if ( i != strings.length - 1 ) {
                    builder.append(SystemUtils.getLineSeparator());
                }
            }
        }
        
        return builder.toString();
    }
    
    public static String httpFormat(
            final Date date) {
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).format(date);
    }
    
    public static String asPath(
            final Class clazz) {
        return clazz.getPackage().getName().replace('.', '/');
    }
    
    public static String replace(
            final String string,
            final String replacement,
            final int begin,
            final int end) {
        return string.substring(0, begin) + replacement + string.substring(end);
    }
    
    /**
     * Escapes the path using the platform-specific escape rules.
     *
     * @param path Path to escape.
     * @return Escaped path.
     */
    public static String escapePath(
            final String path) {
        String localPath = path;
        
        if (localPath.indexOf(' ') > -1) {
            if (SystemUtils.isWindows()) {
                localPath = QUOTE + localPath + QUOTE;
            } else {
                localPath = localPath.replace(SPACE,
                        BACK_SLASH + SPACE); //NOI18N
            }
        }
        
        return localPath;
    }
    
    /**
     * Joins a command string and its arguments into a single string using the
     * platform-specific rules.
     *
     * @param commandArray The command and its arguments.
     * @return The joined string.
     */
    public static String joinCommand(
            final String... commandArray) {
        StringBuffer command = new StringBuffer();
        
        for (int i = 0; i < commandArray.length; i++) {
            command.append(escapePath(commandArray[i]));
            if (i != commandArray.length - 1) {
                command.append(SPACE); //NOI18N
            }
        }
        
        return command.toString();
    }
    
    // object -> string .////////////////////////////////////////////////////////////
    public static String asString(
            final Throwable throwable) {
        StringWriter writer = new StringWriter();
        
        throwable.printStackTrace(new PrintWriter(writer));
        
        return writer.toString();
    }
    
    public static String asString(
            final List<? extends Object> objects) {
        return asString(objects, ", ");
    }
    
    public static String asString(
            final List<? extends Object> objects,
            final String separator) {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < objects.size(); i++) {
            result.append(objects.get(i).toString());
            
            if (i != objects.size() - 1) {
                result.append(separator);
            }
        }
        
        return result.toString();
    }
    
    public static String asString(
            final Object[] strings) {
        return asString(strings, ", ");
    }
    
    public static String asString(
            final Object[] strings,
            final String separator) {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < strings.length; i++) {
            result.append((strings[i]==null) ? EMPTY_STRING+null :
                strings[i].toString());
            
            if (i != strings.length - 1) {
                result.append(separator);
            }
        }
        
        return result.toString();
    }
    
    // base64 ///////////////////////////////////////////////////////////////////////
    public static String base64Encode(
            final String string) throws UnsupportedEncodingException {
        return base64Encode(string, ENCODING_UTF8);
    }
    
    public static String base64Encode(
            final String string,
            final String charset) throws UnsupportedEncodingException {
        final StringBuilder builder = new StringBuilder();
        final byte[] bytes = string.getBytes(charset);
        
        int i;
        for (i = 0; i < bytes.length - 2; i += 3) {
            int byte1 = bytes[i] & BIN_11111111;
            int byte2 = bytes[i + 1] & BIN_11111111;
            int byte3 = bytes[i + 2] & BIN_11111111;
            
            builder.append(
                    BASE64_TABLE[byte1 >> 2]);
            builder.append(
                    BASE64_TABLE[((byte1 << 4) & BIN_00110000) | (byte2 >> 4)]);
            builder.append(
                    BASE64_TABLE[((byte2 << 2) & BIN_00111100) | (byte3 >> 6)]);
            builder.append(
                    BASE64_TABLE[byte3 & BIN_00111111]);
        }
        
        if (i == bytes.length - 2) {
            int byte1 = bytes[i] & BIN_11111111;
            int byte2 = bytes[i + 1] & BIN_11111111;
            
            builder.append(
                    BASE64_TABLE[byte1 >> 2]);
            builder.append(
                    BASE64_TABLE[((byte1 << 4) & BIN_00110000) | (byte2 >> 4)]);
            builder.append(
                    BASE64_TABLE[(byte2 << 2) & BIN_00111100]);
            builder.append(
                    BASE64_PAD);
        }
        
        if (i == bytes.length - 1) {
            int byte1 = bytes[i] & BIN_11111111;
            
            builder.append(
                    BASE64_TABLE[byte1 >> 2]);
            builder.append(
                    BASE64_TABLE[(byte1 << 4) & BIN_00110000]);
            builder.append(
                    BASE64_PAD);
            builder.append(
                    BASE64_PAD);
        }
        
        return builder.toString();
    }
    
    public static String base64Decode(
            final String string) throws UnsupportedEncodingException {
        return base64Decode(string, ENCODING_UTF8);
    }
    
    public static String base64Decode(
            final String string,
            final String charset) throws UnsupportedEncodingException {
        int completeBlocksNumber = string.length() / 4;
        int missingBytesNumber = 0;
        
        if (string.endsWith("=")) {
            completeBlocksNumber--;
            missingBytesNumber++;
        }
        if (string.endsWith("==")) {
            missingBytesNumber++;
        }
        
        int decodedLength = (completeBlocksNumber * 3) + (3 - missingBytesNumber);
        byte[] decodedBytes = new byte[decodedLength];
        
        int encodedCounter = 0;
        int decodedCounter = 0;
        for (int i = 0; i < completeBlocksNumber; i++) {
            int byte1 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte2 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte3 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte4 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            
            decodedBytes[decodedCounter++] = (byte) ((byte1 << 2) | (byte2 >> 4));
            decodedBytes[decodedCounter++] = (byte) ((byte2 << 4) | (byte3 >> 2));
            decodedBytes[decodedCounter++] = (byte) ((byte3 << 6) | byte4);
        }
        
        if (missingBytesNumber == 1) {
            int byte1 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte2 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte3 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            
            decodedBytes[decodedCounter++] = (byte) ((byte1 << 2) | (byte2 >> 4));
            decodedBytes[decodedCounter++] = (byte) ((byte2 << 4) | (byte3 >> 2));
        }
        
        if (missingBytesNumber == 2) {
            int byte1 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte2 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            
            decodedBytes[decodedCounter++] = (byte) ((byte1 << 2) | (byte2 >> 4));
        }
        
        return new String(decodedBytes, charset);
    }
    
    // normal <-> ascii only ////////////////////////////////////////////////////////
    public static String parseAscii(final String string) {
        final Properties properties = new Properties();
        
        // we don't really care about enconding here, as the input string is
        // expected to be ASCII-only, which means it's the same for any encoding
        try {
            properties.load(new ByteArrayInputStream(("key=" + string).getBytes()));
        } catch (IOException e) {
            ErrorManager.notifyWarning(
                    "Cannot parse string",
                    e);
            return string;
        }
        
        return (String) properties.get("key");
    }
    
    public static String convertToAscii(final String string) {
        final Properties properties = new Properties();
        
        properties.put("uberkey", string);
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            properties.store(baos, EMPTY_STRING);
        } catch (IOException e) {
            ErrorManager.notifyWarning(
                    "Cannot convert string",
                    e);
            return string;
        }
        
        final Matcher matcher = Pattern.
                compile("uberkey=(.*)$", Pattern.MULTILINE).
                matcher(baos.toString());
        
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return string;
        }
    }
    
    // string -> object /////////////////////////////////////////////////////////////
    public static List<String> asList(
            final String string) {
        return asList(string, ", ");
    }
    
    public static List<String> asList(
            final String string, final String separator) {
        return Arrays.asList(string.split(separator));
    }
    
    public static Locale parseLocale(
            final String string) {
        final String[] parts = string.split("_");
        
        switch (parts.length) {
            case 1:
                return new Locale(parts[0]);
            case 2:
                return new Locale(parts[0], parts[1]);
            default:
                return new Locale(parts[0], parts[1], parts[2]);
        }
    }
    
    public static URL parseUrl(
            final String string) throws ParseException {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            throw new ParseException("Cannot parse URL", e);
        }
    }
    
    public static Platform parsePlatform(
            final String string) throws ParseException {
        for (Platform platform: Platform.values()) {
            if (platform.getName().equals(string)) {
                return platform;
            }
        }
        
        throw new ParseException("Platform \"" + string + "\" is not recognized.");
    }
    
    public static List<Platform> parsePlatforms(
            final String string) throws ParseException {
        final List<Platform> platforms = new ArrayList<Platform>();
        
        for (String name: asList(string, " ")) {
            final Platform platform = parsePlatform(name);
            
            if (!platforms.contains(platform)) {
                platforms.add(platform);
            }
        }
        
        return platforms;
    }
    
    public static Status parseStatus(
            final String string) throws ParseException {
        for (Status status: Status.values()) {
            if (status.getName().equals(string)) {
                return status;
            }
        }
        
        throw new ParseException("Cannot parse status: " + string);
    }
    
    public static DependencyType parseDependencyType(
            final String string) throws ParseException {
        for (DependencyType type: DependencyType.values()) {
            if (type.getName().equals(string)) {
                return type;
            }
        }
        
        throw new ParseException("Cannot parse dependency type: " + string);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String BACK_SLASH =
            "\\"; // NOI18N
    public static final String FORWARD_SLASH =
            "/"; // NOI18N
    public static final String DOUBLE_BACK_SLASH =
            "\\\\"; // NOI18N
    
    public static final String ENCODING_UTF8 =
            "UTF-8"; // NOI18N
    
    public static final String CR = "\r"; // NOI18N
    public static final String LF = "\n"; // NOI18N
    public static final String DOT = "."; // NOI18N
    public static final String EMPTY_STRING = ""; // NOI18N
    public static final String CRLF = CR + LF;
    public static final String CRLFCRLF = CRLF + CRLF;
    public static final String SPACE = " "; // NOI18N
    public static final String QUOTE = "\""; // NOI18N
    public static final String EQUAL = "="; // NOI18N
    
    public static final String NEW_LINE_PATTERN = "(?:\r\n|\n|\r)"; // NOI18N
    
    private static final String LEFT_WHITESPACE = "^\\s+"; // NOI18N
    private static final String RIGHT_WHITESPACE = "\\s+$"; // NOI18N
    
    
    private static final char MNEMONIC_CHAR = '&';
    private static final String MNEMONIC = "&"; // NOI18N
    private static final char NO_MNEMONIC = '\u0000';
    
    private static final char[] BASE64_TABLE = new char[] {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
        'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
        'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', '+', '/'
    };
    
    private static final byte[] BASE64_REVERSE_TABLE = new byte[] {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, 62, -1, -1, -1, 63, 52, 53,
        54, 55, 56, 57, 58, 59, 60, 61, -1, -1,
        -1, -1, -1, -1, -1,  0,  1,  2,  3,  4,
        5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
        25, -1, -1, -1, -1, -1, -1, 26, 27, 28,
        29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
        39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
        49, 50, 51
    };
    
    private static final char BASE64_PAD = '=';
    
    private static final int BIN_11111111 = 0xff;
    private static final int BIN_00110000 = 0x30;
    private static final int BIN_00111100 = 0x3c;
    private static final int BIN_00111111 = 0x3f;
}
