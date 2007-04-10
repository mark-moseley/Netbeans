/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.utils.applications;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.FilesList;

/**
 *
 * @author Kirill Sorokin
 */
public class NetBeansUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static void addCluster(File nbLocation, String clusterName) throws IOException {
        File netbeansclusters = new File(nbLocation, NETBEANS_CLUSTERS);
        
        List<String> list = FileUtils.readStringList(netbeansclusters);
        for (String string: list) {
            if (string.equals(clusterName)) {
                return;
            }
        }
        list.add(clusterName);
        
        FileUtils.writeStringList(netbeansclusters, list);
    }
    
    public static void removeCluster(File nbLocation, String clusterName) throws IOException {
        File netbeansclusters = new File(nbLocation, NETBEANS_CLUSTERS);
        
        List<String> list = FileUtils.readStringList(netbeansclusters);
        list.remove(clusterName);
        
        FileUtils.writeStringList(netbeansclusters, list);
    }
    
    public static FilesList createProductId(File nbLocation) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        File productid = new File(nbCluster, PRODUCT_ID);
        
        return FileUtils.writeFile(productid, NB_IDE_ID);
    }
    
    public static FilesList addPackId(File nbLocation, String packId) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        File productid = new File(nbCluster, PRODUCT_ID);
        
        String id;
        if (!productid.exists()) {
            id = NB_IDE_ID;
        } else {
            id = FileUtils.readFile(productid).trim();
        }
        
        boolean packExists = false;
        for (String string: id.split(PACK_ID_SEPARATOR)) {
            if (string.equals(packId)) {
                packExists = true;
                break;
            }
        }
        
        if (!packExists) {
            id += PACK_ID_SEPARATOR + packId;
        }
        
        return FileUtils.writeFile(productid, id);
    }
    
    public static void removePackId(File nbLocation, String packId) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        File productid = new File(nbCluster, PRODUCT_ID);
        
        String id;
        if (!productid.exists()) {
            id = NB_IDE_ID;
        } else {
            id = FileUtils.readFile(productid).trim();
        }
        
        String[] components = id.split(PACK_ID_SEPARATOR);
        
        StringBuilder builder = new StringBuilder(components[0]);
        for (int i = 1; i < components.length; i++) {
            if (!components[i].equals(packId)) {
                builder.append(PACK_ID_SEPARATOR).append(components[i]);
            }
        }
        
        FileUtils.writeFile(productid, builder);
    }
    
    public static void removeProductId(File nbLocation) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        File productid = new File(nbCluster, PRODUCT_ID);
        
        FileUtils.deleteFile(productid);
    }
    
    public static FilesList createLicenseAcceptedMarker(File nbLocation) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        File license_accepted = new File(nbCluster, LICENSE_ACCEPTED);
        
        if (!license_accepted.exists()) {
            return FileUtils.writeFile(license_accepted, "");
        } else {
            return new FilesList();
        }
    }
    
    public static void removeLicenseAcceptedMarker(File nbLocation) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        if (nbCluster == null) {
            throw new IOException("The NetBeans branding cluster does not exist");
        }
        
        File license_accepted = new File(nbCluster, LICENSE_ACCEPTED);
        
        if (license_accepted.exists()) {
            FileUtils.deleteFile(license_accepted);
        }
    }
    
    public static void setJavaHome(File nbLocation, File javaHome) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        String contents = FileUtils.readFile(netbeansconf);
        
        String correctJavaHome = StringUtils.escapeRegExp(javaHome.getAbsolutePath());
        
        contents = contents.replaceAll(
                "#?" + NETBEANS_JDKHOME + "\".*?\"",
                NETBEANS_JDKHOME + "\"" + correctJavaHome + "\"");
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    public static void setUserDir(File nbLocation, File userDir) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        String contents = FileUtils.readFile(netbeansconf);
        
        String correctUserDir = StringUtils.escapeRegExp(userDir.getAbsolutePath());
        
        contents = contents.replaceAll(
                NETBEANS_USERDIR +
                "\".*?\"",
                NETBEANS_USERDIR +
                "\"" + correctUserDir + "\"");
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    public static void addJvmOption(File nbLocation, String optionName) throws IOException {
        addJvmOption(nbLocation, optionName, null, false);
    }
    
    public static void addJvmOption(File nbLocation, String optionName, String optionValue) throws IOException {
        addJvmOption(nbLocation, optionName, optionValue, false);
    }
    
    public static void addJvmOption(File nbLocation, String optionName, String optionValue, boolean quote) throws IOException {
        final File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        final String correctOptionName = StringUtils.escapeRegExp(optionName);
        
        String contents = FileUtils.readFile(netbeansconf);
        
        String newOption = "-J" + optionName + (optionValue != null ? "=" + (quote ? "\\\"" : "") + optionValue + (quote ? "\\\"" : "") : "");
        
        Matcher matcher = Pattern.compile(
                NETBEANS_OPTIONS +
                "\"(.*?)( )?(-J" + correctOptionName + "(?:=.*?(?= |(?<!\\\\)\"))?)( )?(.*)?\"").matcher(contents);
        
        if (!matcher.find()) {
            contents = contents.replace(
                    NETBEANS_OPTIONS + "\"",
                    NETBEANS_OPTIONS + "\"" + newOption + " ");
            contents = contents.replace(newOption + " \"", newOption + "\"");
        }
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    public static void setJvmOption(File nbLocation, String optionName) throws IOException {
        setJvmOption(nbLocation, optionName, null, false);
    }
    
    public static void setJvmOption(File nbLocation, String optionName, String optionValue) throws IOException {
        setJvmOption(nbLocation, optionName, optionValue, false);
    }
    
    public static void setJvmOption(File nbLocation, String optionName, String optionValue, boolean quote) throws IOException {
        final File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        final String correctOptionName = StringUtils.escapeRegExp(optionName);
        
        String contents = FileUtils.readFile(netbeansconf);
        
        String newOption = "-J" + optionName + (optionValue != null ? "=" + (quote ? "\\\"" : "") + optionValue + (quote ? "\\\"" : "") : "");
        
        Matcher matcher = Pattern.compile(NETBEANS_OPTIONS +
                "\"(.*?)( )?(-J" + correctOptionName + "(?:=.*?(?= |(?<!\\\\)\"))?)( )?(.*)?\"").matcher(contents);
        
        if (matcher.find()) {
            contents = matcher.replaceAll(
                    NETBEANS_OPTIONS + "\"$1$2" + StringUtils.escapeRegExp(newOption) + "$4$5\"");
        } else {
            contents = contents.replace(
                    NETBEANS_OPTIONS + "\"",
                    NETBEANS_OPTIONS + "\"" + newOption + " ");
            contents = contents.replace(newOption + " \"", newOption + "\"");
        }
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    public static void removeJvmOption(File nbLocation, String optionName) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        String contents = FileUtils.readFile(netbeansconf);
        
        String correctOptionName = StringUtils.escapeRegExp(optionName);
        
        Matcher matcher = Pattern.compile(NETBEANS_OPTIONS +
                "\"(?:.*?)(?: )?(-J" + correctOptionName + "(?:=.*?(?= |(?<!\\\\)\"))?)(?: )?(?:.*)?\"").matcher(contents);
        
        if (matcher.find()) {
            matcher.reset();
            while (matcher.find()) {
                contents = contents.replace(" " + matcher.group(1), "");
                contents = contents.replace(matcher.group(1) + " ", "");
                contents = contents.replace(matcher.group(1), "");
            }
        }
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    /**
     * Get JVM memory value.
     *
     * @param nbLocation NetBeans home directory
     * @param memoryType Memory type that can be one of the following values
     *          <ul><li> <code>MEMORY_XMX</code></li>
     *              <li> <code>MEMORY_XMS</code></li>
     *              <li> <code>MEMORY_XSS</code></li>
     *          </ul>
     * @return The size of memory in bytes. <br>
     *         If there is no such option then return 0;
     */
    public static long getJvmMemorySize(File nbLocation, String memoryType) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        String contents =  FileUtils.readFile(netbeansconf);
        
        Matcher memoryMatcher = Pattern.compile(
                StringUtils.NEW_LINE_PATTERN +
                NETBEANS_OPTIONS +
                "\"(.*?)-J" +
                memoryType +
                "(" + DIGITS_PATTERN + MEMORY_SUFFIX_PATTERN + ")" +
                "(.*?)\"").matcher(contents);
        return (memoryMatcher.find()) ? getJavaMemorySize(memoryMatcher.group(2)) : 0;
    }
    
    /**
     * Get JVM memory value. <br>
     * If value is <i>zero</i> then remove the jvm option from netbeans options<br><br>
     * @param nbLocation NetBeans home directory
     * @param memoryType Memory type that can be one of the following values
     *           <ul><li> <code>MEMORY_XMX</code></li>
     *              <li> <code>MEMORY_XMS</code></li>
     *              <li> <code>MEMORY_XSS</code></li>
     *          </ul>
     * @param value Size of memory to be set
     */
    public static void setJvmMemorySize(File nbLocation, String memoryType, long value) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        String contents = FileUtils.readFile(netbeansconf);
        
        Matcher matcher = Pattern.compile(
                "(\r\n|\n|\r)" +
                NETBEANS_OPTIONS +
                "\"(.*?)-J" +
                memoryType +
                "(" + DIGITS_PATTERN + MEMORY_SUFFIX_PATTERN + ")" +
                "(.*?)\"").matcher(contents);
        if(matcher.find()) {
            
            contents = matcher.replaceAll(
                    "$1" +
                    NETBEANS_OPTIONS +
                    "\""+
                    "$2" +
                    formatJavaMemoryString(memoryType, value) + "$4\"");
            
        }  else {
            matcher = Pattern.compile(
                    "(\r\n|\n|\r)" +
                    NETBEANS_OPTIONS +
                    "\""+
                    "(.*?)").matcher(contents);
            if(matcher.find()) {
                contents = matcher.replaceAll(
                        "$1" +
                        NETBEANS_OPTIONS +
                        "\"" +
                        formatJavaMemoryString(memoryType, value) + " $2");
            }
        }
        contents = contents.replace(
                NETBEANS_OPTIONS + "\" ",
                NETBEANS_OPTIONS + "\"");
        FileUtils.writeFile(netbeansconf, contents);
        
    }
    
    public static File getNbCluster(File nbLocation) {
        for (File child: nbLocation.listFiles()) {
            if (child.isDirectory() && child.getName().matches(NB_CLUSTER_PATTERN)) {
                return child;
            }
        }
        
        return null;
    }
    
    /**
     * Get resolved netbeans user directory
     * @param nbLocation NetBeans home directory
     * @throws IOException if can`t get netbeans default userdir
     */
    public static File getNetBeansUserDirFile(File nbLocation) throws IOException {
        String dir = getNetBeansUserDir(nbLocation);
        dir = dir.replace(USER_HOME_TOKEN, System.getProperty("user.home"));
        return new File(dir);
    }
    
    /**
     * Get netbeans user directory as it is written in netbeans.conf
     * @param nbLocation NetBeans home directory
     * @throws IOException if can`t get netbeans default userdir
     */
    public static String getNetBeansUserDir(File nbLocation) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        String contents = FileUtils.readFile(netbeansconf);
        Matcher matcher = Pattern.compile(
                NEW_LINE_PATTERN + SPACES_PATTERN +
                NETBEANS_USERDIR +
                "\"(.*?)\"").matcher(contents);
        if(matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            throw new IOException("Can`t get netbeans userdir from " + netbeansconf);
        }
    }
    
    /**
     * Get jdkhome as it is written in netbeans.conf
     * @param nbLocation NetBeans home directory
     * @return JDK location
     * @throws IOException if can`t get netbeans_jdkhome value of netbeans.conf
     */
    public static String getJavaHome(File nbLocation) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        String contents = FileUtils.readFile(netbeansconf);
        
        Matcher matcher = Pattern.compile(
                NEW_LINE_PATTERN + SPACES_PATTERN +
                NETBEANS_JDKHOME +
                "\"(.*?)\"").matcher(contents);
        
        if(matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            throw new IOException("Can`t get netbeans javahome from " + netbeansconf);
        }
    }
    
    /**
     * Check if NetBeans is running
     * @param nbLocation NetBeans home directory
     * @return True if NetBeans is running
     * @throws IOException if can`t say for sure whether it is running or not
     */
    public static boolean isNbRunning(File nbLocation) throws IOException {
        return FileUtils.exists(getLockFile(nbLocation));
    }
    
    public static File getLockFile(File nbLocation) throws IOException {
        return new File(getNetBeansUserDirFile(nbLocation), "lock");
    }
    
    public static void updateNetBeansHome(final File nbLocation) throws IOException {
        FileUtils.modifyFile(
                new File(nbLocation, NETBEANS_CONF), 
                NETBEANS_HOME_TOKEN, 
                nbLocation.getAbsolutePath());
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static long getJavaMemorySize(String sizeString) {
        String suffix = sizeString.substring(sizeString.length()-1);
        
        if(!suffix.matches(DIGITS_PATTERN)) {
            long value = new Long(sizeString.substring(0, sizeString.length()-1)).longValue();
            if(suffix.equalsIgnoreCase("k")) {
                value*=K;
            } else if(suffix.equalsIgnoreCase("m")) {
                value*=M;
            } else if(suffix.equalsIgnoreCase("g")) {
                value*=G;
            } else if(suffix.equalsIgnoreCase("t")) {
                value*=T;
            }
            return value;
        } else {
            return new Long(sizeString).longValue() * M; // default - megabytes
        }
    }
    
    private static String formatJavaMemoryString(String type, long size) {
        return (size > 0) ?
            "-J" + type + formatJavaMemoryString(size) :
            StringUtils.EMPTY_STRING;
    }
    
    private static String formatJavaMemoryString(long size) {
        if((size > T) && (size % T == 0)) {
            return StringUtils.EMPTY_STRING + (size/T) + "t";
        } else if((size > G) && (size % G == 0)) {
            return StringUtils.EMPTY_STRING + (size/G) + "g";
        } else if((size > M) && (size % M == 0)) {
            return StringUtils.EMPTY_STRING + (size/M) + "m";
        }  else if((size > K) && (size % K == 0)) {
            return StringUtils.EMPTY_STRING + (size/K) + "k";
        } else {
            if(size > (10 * M)) {
                // round up to the nearest M value
                return StringUtils.EMPTY_STRING + (size/M + 1) + "m";
            } else {
                // round up to the nearest K value
                return StringUtils.EMPTY_STRING + (size/K + 1) + "k";
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private NetBeansUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String NETBEANS_CLUSTERS =
            "etc/netbeans.clusters"; // NOI18N
    public static final String NETBEANS_CONF =
            "etc/netbeans.conf"; // NOI18N
    public static final String PRODUCT_ID =
            "config/productid"; // NOI18N
    public static final String LICENSE_ACCEPTED =
            "var/license_accepted"; // NOI18N
    
    public static final String DIGITS_PATTERN =
            "[0-9]+"; // NOI18N
    public static final String CLUSTER_NUMBER_PATTERN =
            DIGITS_PATTERN + "(\\." + DIGITS_PATTERN + ")?"; // NOI18N
    
    public static final String NB_CLUSTER_PATTERN =
            "nb" + CLUSTER_NUMBER_PATTERN; // NOI18N
    public static final String NEW_LINE_PATTERN =
            "[\r\n|\n|\r]"; // NOI18N
    public static final String SPACES_PATTERN =
            "\\ *"; // NOI18N
    
    public static final String NETBEANS_USERDIR =
            "netbeans_default_userdir="; // NOI18N
    public static final String NETBEANS_JDKHOME =
            "netbeans_jdkhome="; // NOI18N
    public static final String NETBEANS_OPTIONS =
            "netbeans_default_options="; // NOI18N
    
    public static final String NB_IDE_ID =
            "NB"; // NOI18N
    public static final String PACK_ID_SEPARATOR =
            "_"; // NOI18N
    public static final String MEMORY_XMX =
            "-Xmx"; // NOI18N
    public static final String MEMORY_XMS =
            "-Xms"; // NOI18N
    public static final String MEMORY_XSS =
            "-Xss"; // NOI18N
    public static final String USER_HOME_TOKEN =
            "${HOME}"; // NOI18N
    public static final String NETBEANS_HOME_TOKEN =
            "${NETBEANS_HOME}"; // NOI18N
    
    public static final long K =
            1024; // NOMAGI
    public static final long M =
            K * K;
    public static final long G =
            M * K;
    public static final long T =
            G * K;
    
    private static final String MEMORY_SUFFIX_PATTERN =
            "[kKmMgGtT]?"; // NOI18N
}
