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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.test.ide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * BlacklistedClassesHandler performs processing of log messages to identify
 * which classes from a black list were loaded during application life cycle.
 *
 * The black list and the white list have to be specified in the separate text
 * files: each line is interpreted as classname, unless it is started with
 * non-identifier character. Leading and trailing spaces are ignored.
 *
 * Use getInstance and getBlacklistedClassesHandler methods to ensure that
 * only one incstance of BlacklistedClassesHandlerSingleton is used across
 * the different classloaders
 *
 * @author nenik, mrkam@netbeans.org
 *
 */
public class BlacklistedClassesHandlerSingleton extends Handler implements BlacklistedClassesHandler {

    private static BlacklistedClassesHandler instance = null;
    private boolean violation = false;
    // TODO: Is it necessary to use synchronizedMap? Should the list be synchronized?
    final private Map blacklist = Collections.synchronizedMap(new HashMap());
    final private Map whitelistViolators = Collections.synchronizedMap(new TreeMap());
    final private Set whitelist = Collections.synchronizedSortedSet(new TreeSet());
    final private Set previousWhitelist = Collections.synchronizedSortedSet(new TreeSet());
    final private Set newWhitelist = Collections.synchronizedSortedSet(new TreeSet());
    private boolean whitelistEnabled = false;
    private boolean generatingWhitelist = false;
    private String whitelistFileName;
    private boolean inited = false;
    private String newWhitelistFileName;
    private String previousWhitelistFileName = null;
    private File whitelistStorageDir = null;

    private BlacklistedClassesHandlerSingleton() {
    }

    private BlacklistedClassesHandlerSingleton(String blacklistFileName) {
        this(blacklistFileName, null);
    }

    private BlacklistedClassesHandlerSingleton(String blacklistFileName, String whitelistFileName) {
        this(blacklistFileName, whitelistFileName, false);
    }

    private BlacklistedClassesHandlerSingleton(String blacklistFileName, String whitelistFileName, boolean generateWhitelist) {
        initSingleton(blacklistFileName, whitelistFileName, generateWhitelist);
    }

    public synchronized boolean initSingleton(String blacklistFileName, String whitelistFileName, boolean generateWhitelist) {
        return initSingleton(blacklistFileName, whitelistFileName, null, generateWhitelist);
    }

    public synchronized boolean initSingleton(String blacklistFileName, String whitelistFileName, String whitelistStorageDir, boolean generateWhitelist) {
        if (isInitialized()) {
            throw new Error("BlacklistedClassesHandler shouldn't be initialized twice!");
        }
        this.generatingWhitelist = generateWhitelist;
        this.whitelistFileName = whitelistFileName;

        new BlacklistedClassesViolationException("Dummy");

        if (whitelistStorageDir != null) {
            this.whitelistStorageDir = new File(whitelistStorageDir);
            this.whitelistStorageDir.mkdirs();
            try {
                newWhitelistFileName = new File(whitelistStorageDir, "whitelist" + System.currentTimeMillis() + ".txt").getCanonicalPath();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            File[] files = this.whitelistStorageDir.listFiles(new FileFilter() {

                long lastModified = 0;

                public boolean accept(File pathname) {
                    if (pathname.isFile() && pathname.getName().matches("whitelist.*\\.txt")) {
                        return pathname.lastModified() >= lastModified;
                    }
                    return false;
                }
            });
            if (files.length > 0) {
                previousWhitelistFileName = files[files.length - 1].getPath();
                loadWhiteList(previousWhitelistFileName, previousWhitelist);
            }
        }


        loadBlackList(blacklistFileName);
        loadWhiteList(this.whitelistFileName, whitelist);

        Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).info(this.toString());

        Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).info(
                blacklist.size() + " classes loaded to black list");
        if (this.whitelistEnabled) {
            Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).info(
                    whitelist.size() + " classes loaded to white list.");
        } else if (this.generatingWhitelist) {
            Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).info(
                    whitelist.size() + " classes loaded to white list. Whitelist is being generated.");
        } else {
            Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).info(
                    "White list disabled");
        }

        inited = true;
        return true;
    }

    public boolean initSingleton(String configFileName) {
        if (isInitialized()) {
            throw new Error("BlacklistedClassesHandler shouldn't be initialized twice!");
        }
        File configFile = new File(configFileName);
        Properties config = new Properties();
        try {
            config.loadFromXML(new BufferedInputStream(new FileInputStream(configFile)));
        } catch (FileNotFoundException fnfe) {
            Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).severe(configFileName + " file not found.");
            return false;
        } catch (java.io.IOException ioe) {
            Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).log(Level.SEVERE, "Failed to load " + configFileName, ioe);
            return false;
        }
        boolean configBlacklistEnabled = Boolean.parseBoolean(config.getProperty("blacklist.enabled"));
        boolean configWhitelistEnabled = Boolean.parseBoolean(config.getProperty("whitelist.enabled"));
        String configBlacklistFileName = configFile.getParent()
                + File.separator + config.getProperty("blacklist");
        String configWhitelistFileName = configFile.getParent()
                + File.separator + config.getProperty("whitelist");
        boolean configGenerateWhitelist = Boolean.parseBoolean(config.getProperty("generate.whitelist"));
        String configWhitelistStorageDir = config.getProperty("whitelist.storage.dir");
        boolean useWhitelistStorage = Boolean.parseBoolean(config.getProperty("use.whitelist.storage"));
        if (!configBlacklistEnabled) {
            configBlacklistFileName = null;
        }
        if (!configWhitelistEnabled) {
            configWhitelistFileName = null;
        }
        if (useWhitelistStorage && configWhitelistStorageDir == null) {
            configWhitelistStorageDir = System.getProperty("user.home") + File.separator + "whitelist_storage";
        } else {
            configWhitelistStorageDir = configFile.getParent() + File.separator + configWhitelistStorageDir;
        }
        if (configBlacklistEnabled || configWhitelistEnabled) {
            try {
                return initSingleton(configBlacklistFileName, configWhitelistFileName, configWhitelistStorageDir, configGenerateWhitelist);
            } catch (Exception ex) {
                Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).log(Level.SEVERE,
                        "Can't initialize BlacklistedClassesHandler due to the following exception:", ex);

            }
        }
        return false;
    }

    public boolean isInitialized() {
        return inited;
    }

     /**
     * Use this method to get existing instance of
     * BlacklistedClassesHandler. This method ensures that only one instance of
     * BlacklistedClassesHandler is shared across the different classloaders.
     *
     * @return existing instance of BlacklistedClassesHandler, null if there is
      *        no such an instance
     */
    public static synchronized BlacklistedClassesHandler getBlacklistedClassesHandler() {
        getInstance();
        if (instance != null && instance.isInitialized()) {
            return instance;
        } else {
            return null;
        }
    }

    /**
     * Use this method to get existing or new non-initialized instance of
     * BlacklistedClassesHandler. This method ensures that only one instance of
     * BlacklistedClassesHandler is shared across the different classloaders.
     *
     * Use initSingleton methods to initialize BlacklistedClassesHandler
     * @return existing or new non-initialized instance of
     *         BlacklistedClassesHandler
     */
    public static synchronized BlacklistedClassesHandler getInstance() {
        if (instance == null) {
            try {
                // TODO Is it really necessary to use proxies?
                ClassLoader myClassLoader = BlacklistedClassesHandlerSingleton.class.getClassLoader();
                ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();
                if (myClassLoader != parentClassLoader) {
                    Class otherClassInstance = parentClassLoader.loadClass(BlacklistedClassesHandlerSingleton.class.getName());
                    Method getInstanceMethod = otherClassInstance.getDeclaredMethod("getInstance", new Class[] { });
                    Object otherAbsoluteSingleton = getInstanceMethod.invoke(null, new Object[] { } );
                    instance = (BlacklistedClassesHandler) Proxy.newProxyInstance(myClassLoader,
                                                         new Class[] { BlacklistedClassesHandler.class },
                                                         new PassThroughProxyHandler(otherAbsoluteSingleton));
                } else {
                    instance = new BlacklistedClassesHandlerSingleton();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to get BlacklistedClassesHandler instance", e);
            }
        }

        return instance;
    }

    /**
     * Use this method to new initialized instance of
     * BlacklistedClassesHandler. This method ensures that only one instance of
     * BlacklistedClassesHandler is shared across the different classloaders.
     * And this instance must be initialized only once.
     * @param blacklistFileName if null blacklist checking is disabled
     * @return new initialized instance of BlacklistedClassesHandler. null if
     *         initialization failed.
     */
    public static synchronized BlacklistedClassesHandler getBlacklistedClassesHandler(String blacklistFileName) {
        return getBlacklistedClassesHandler(blacklistFileName, null);
    }

    /**
     * Use this method to new initialized instance of
     * BlacklistedClassesHandler. This method ensures that only one instance of
     * BlacklistedClassesHandler is shared across the different classloaders.
     * And this instance must be initialized only once.
     * @param blacklistFileName if null blacklist checking is disabled
     * @param whitelistFileName if null whitelist checking is disabled
     * @return new initialized instance of BlacklistedClassesHandler. null if
     *         initialization failed.
     */
    public static synchronized BlacklistedClassesHandler getBlacklistedClassesHandler(String blacklistFileName, String whitelistFileName) {
        return getBlacklistedClassesHandler(blacklistFileName, whitelistFileName, false);
    }

    /**
     * Use this method to new initialized instance of
     * BlacklistedClassesHandler. This method ensures that only one instance of
     * BlacklistedClassesHandler is shared across the different classloaders.
     * And this instance must be initialized only once.
     *
     * @param blacklistFileName if null blacklist checking is disabled
     * @param whitelistFileName if null whitelist checking is disabled
     * @param generateWhiteList if true whitelist checking is disabled. All
     *                          instantiated classes are added to the whitelist.
     *                          Use saveWhiteLists methods to save the list.
     * @return new initialized instance of BlacklistedClassesHandler. null if
     *         initialization failed.
     */
    public static synchronized BlacklistedClassesHandler getBlacklistedClassesHandler(String blacklistFileName, String whitelistFileName, boolean generateWhiteList) {
        getInstance();
        if (instance.initSingleton(blacklistFileName, whitelistFileName, generateWhiteList)) {
            return instance;
        } else {
            return null;
        }
    }

    public void publish(LogRecord record) {
        // We can't use logging in this method as it could cause LinkageError
        try {
            if (record != null && record.getMessage() != null) {
                if (record.getMessage().contains("initiated")) {
                    String className = (String) record.getParameters()[1];
                    if (blacklist.containsKey(className)) { //violator
                        Exception exc = new BlacklistedClassesViolationException(record.getParameters()[0].toString());
                        System.out.println("BlacklistedClassesHandler blacklist violator: " + className);
                        exc.printStackTrace();
                        synchronized (blacklist) {
                            // TODO: Probably we should synchronize by list
                            ((List) blacklist.get(className)).add(exc);
                        }
                        violation = true;
                    } else if (whitelistEnabled && !whitelist.contains(className)) {
                        Exception exc = new BlacklistedClassesViolationException(record.getParameters()[0].toString());
                        System.out.println("BlacklistedClassesHandler whitelist violator: " + className);
                        exc.printStackTrace();
                        synchronized (whitelistViolators) {
                            // TODO: Probably we should synchronize by list
                            if (whitelistViolators.containsKey(className)) {
                                ((List) whitelistViolators.get(className)).add(exc);
                            } else {
                                List exceptions = new ArrayList();
                                exceptions.add(exc);
                                whitelistViolators.put(className, exceptions);
                            }
                        }
                        violation = true;
                    } else if (generatingWhitelist) {
                        whitelist.add(className);
                    }
                    newWhitelist.add(className);
                } else if (record.getMessage().equalsIgnoreCase("LIST BLACKLIST VIOLATIONS")) {
                    logViolations();
                } else if (record.getMessage().equalsIgnoreCase("SAVE WHITELIST")) {
                    saveWhiteList();
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void flush() {
    }

    public void close() throws SecurityException {
        /* Ugly hack to leave the handler when configuration is reset */
        Logger logger = Logger.getLogger("org.netbeans.ProxyClassLoader"); // NOI18N
        logger.addHandler(this);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }

    public void register() {
        Logger logger = Logger.getLogger("org.netbeans.ProxyClassLoader"); // NOI18N
        logger.addHandler(this);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        System.setProperty("org.netbeans.ProxyClassLoader.level", "ALL"); // NOI18N
    }

    public void unregister() {
        Logger logger = Logger.getLogger("org.netbeans.ProxyClassLoader"); // NOI18N
        logger.removeHandler(this);
    }

    public boolean noViolations() {
        return !violation;
    }

    public boolean noViolations(boolean listViolations) {
        if (violation && listViolations) {
            logViolations();
        }
        return !violation;
    }

    public boolean noViolations(PrintStream out) {
        if (violation) {
            listViolations(out, true);
        }
        return !violation;
    }

    public void logViolations() {
	Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).warning(listViolations());
    }

    /**
     * Returns only list of violators but prints all the exceptions to out
     * @param out
     * @return
     */
    public String reportViolations(PrintStream out) {
        return reportViolations(new PrintWriter(out));
    }

    public String reportViolations(PrintWriter out) {
        listViolationsAsXML(out, true);
        return listViolations(false);
    }


    public String listViolations() {
        return listViolations(true);
    }

    public String listViolations(boolean listExceptions) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        listViolations(ps, listExceptions);
        ps.flush();
        return baos.toString();
    }

    public void listViolations(PrintStream out) {
        listViolations(out, false);
    }

    public void listViolations(PrintWriter out) {
        listViolations(out, false);
    }

    public void listViolations(PrintStream out, boolean listExceptions) {
        listViolations(new PrintWriter(out), listExceptions);
    }

    public void listViolations(PrintWriter out, boolean listExceptions) {
        out.println("BlacklistedClassesHandler identified the following violations:");
        listViolationsMap("Blacklist violations:", blacklist, out, listExceptions);
        listViolationsMap("Whitelist violations:", whitelistViolators, out, listExceptions);
        out.flush();
    }

    public void listViolationsAsXML(PrintWriter out, boolean listExceptions) {
        out.println("<report>");
        listViolationsMapAsXML("blacklist", blacklist, out, listExceptions);
        listViolationsMapAsXML("whitelist", whitelistViolators, out, listExceptions);
        out.println("</report>");
        out.flush();
    }

    private void listViolationsMap(String caption, Map map, PrintWriter out, boolean listExceptions) {
        long violationsCount = 0;
        out.println("  " + caption);
        synchronized (map) {
            final Set keySet = map.keySet();
            Iterator iter = keySet.iterator();
            while (iter.hasNext()) {
                String violator = (String) iter.next();
                if (((List) map.get(violator)).size() > 0) {
                    violationsCount++;
                    out.println("    " + violator);
                    if (listExceptions) {
                        final List exceptions = (List) map.get(violator);
                        Iterator iter2 = exceptions.iterator();
                        while (iter2.hasNext()) {
                            Exception ex = (Exception) iter2.next();
                            ex.printStackTrace(out);
                        }
                    }
                }
            }
        }
        if (violationsCount > 0) {
            out.println("    Total: " + violationsCount + " violation(s).");
        } else {
            out.println("    No violations");
        }

    }

    public String reportDifference() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        reportDifference(ps);
        ps.flush();
        return baos.toString();
    }

    public void reportDifference(PrintStream out) {
        reportDifference(new PrintWriter(out));
    }

    public void reportDifference(PrintWriter out) {
        Set list = whitelist;
        String filename = whitelistFileName;
        if (previousWhitelistFileName != null) {
            list = previousWhitelist;
            filename = previousWhitelistFileName;
        }
        out.println("Diff between " + filename + " and " + newWhitelistFileName);
        out.println("+++ Added:");
        synchronized (list) {
            synchronized (newWhitelist) {
                int i = 0;
                Iterator iter = newWhitelist.iterator();
                while (iter.hasNext()) {
                    String violator = (String) iter.next();
                    if (!list.contains(violator)) {
                        out.println("    " + violator);
                        ++i;
                    }
                }
                out.println("   Added " + i + " class(es)");
                out.println("--- Removed:");
                i = 0;
                iter = list.iterator();
                while (iter.hasNext()) {
                    String violator = (String) iter.next();
                    if (!newWhitelist.contains(violator)) {
                        out.println("    " + violator);
                        ++i;
                    }
                }
                out.println("   Removed " + i + " class(es)");
            }
        }
        out.flush();
    }

    private void listViolationsMapAsXML(String caption, Map map, PrintWriter out, boolean listExceptions) {
        out.println("  <" + caption + ">");
        if (map.size() > 0) {
            out.println("    <violators>");
            synchronized (map) {
                int i = 0;
                final Set keySet = map.keySet();
                Iterator iter = keySet.iterator();
                while (iter.hasNext()) {
                    String violator = (String) iter.next();
                    if (((List) map.get(violator)).size() > 0) {
                        out.println("      <violator class=\"" + violator + "\">");
                        if (listExceptions) {
                            final List exceptions = (List) map.get(violator);
                            Iterator iter2 = exceptions.iterator();
                            while (iter2.hasNext()) {
                                BlacklistedClassesViolationException ex = (BlacklistedClassesViolationException) iter2.next();
                                ex.printStackTraceAsXML(out);
                            }
                        }
                        out.println("      </violator>");
                    }
                }
            }
            out.println("    </violators>");
        } else {
            out.println("    <violators/>");
        }
        out.println("  </" + caption + ">");
    }

    public void resetViolations() {
        synchronized (blacklist) {
            final Set keySet = blacklist.keySet();
            Iterator iter = keySet.iterator();
            while (iter.hasNext()) {
                String violator = (String) iter.next();
                ((List) blacklist.get(violator)).clear();
            }
            violation = false;
        }
    }

    private void loadBlackList(String blacklistFileName) {
        BufferedReader reader = null;
        try {
            if (blacklistFileName != null) {
                reader = new BufferedReader(new FileReader(new File(blacklistFileName)));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    line = line.trim();
                    if (line.length() > 0 && Character.isJavaIdentifierStart(line.charAt(0))) {
                        blacklist.put(line, new ArrayList());
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadWhiteList(String whitelistFileName, Set list) {
        BufferedReader reader = null;
        try {
            if (whitelistFileName != null) {
                reader = new BufferedReader(new FileReader(new File(whitelistFileName)));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    line = line.trim();
                    if (line.length() > 0 && Character.isJavaIdentifierStart(line.charAt(0))) {
                        list.add(line);
                    }
                }
                if (!generatingWhitelist) {
                    whitelistEnabled = true;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BlacklistedClassesHandlerSingleton.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void saveWhiteList() {
        if (whitelistStorageDir != null) {
            saveWhiteList(newWhitelistFileName);
        } else {
        saveWhiteList(whitelistFileName);
    }
    }

    public void saveWhiteList(PrintStream out) {
        saveWhiteList(new PrintWriter(out));
    }

    public void saveWhiteList(String filename) {
        PrintStream ps = null;
        try {
            ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename)));
            saveWhiteList(ps);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BlacklistedClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (ps != null) {
                ps.flush();
                ps.close();
            }
        }
    }

    public void saveWhiteList(PrintWriter out) {
        synchronized (newWhitelist) {
            Iterator it = newWhitelist.iterator();
            while (it.hasNext()) {
                out.println(it.next());
            }
        }
        out.flush();
    }

    public boolean isGeneratingWhitelist() {
        return generatingWhitelist;
    }

    public boolean hasWhitelistStorage() {
        return whitelistStorageDir != null;
    }
}
