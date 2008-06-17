/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.helper.EngineResources;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.cli.CLIHandler;
import org.netbeans.installer.utils.exceptions.XMLException;

/**
 *
 * @author Dmitry Lipin
 */
public final class EngineUtils {

    /**
     * Cache installer at NBI`s home directory.
     */
    public static File cacheEngine(Progress progress) throws IOException {
        File cachedEngine = getEngineLocation();

        if (!FileUtils.exists(cachedEngine)) {
            cacheEngine(cachedEngine, progress);
        }

        return cachedEngine;
    }
    public static Class getEngineMainClass() {
        return Installer.class;
    }

    public static void cacheEngine(File dest, Progress progress) throws IOException {
        LogManager.logIndent("cache engine data locally to run uninstall in the future");

        String filePrefix = "file:";
        String httpPrefix = "http://";
        String jarSep = "!/";

        String installerResource = ResourceUtils.getResourceClassName(getEngineMainClass());
        URL url = getEngineMainClass().getClassLoader().getResource(installerResource);
        if (url == null) {
            throw new IOException("No main Installer class in the engine");
        }

        LogManager.log(ErrorLevel.DEBUG, "NBI Engine URL for Installer.Class = " + url);
        LogManager.log(ErrorLevel.DEBUG, "URL Path = " + url.getPath());

        boolean needCache = true;
                        
        if ("jar".equals(url.getProtocol())) {
            LogManager.log("... running engine as a .jar file");
            // we run engine from jar, not from .class
            String path = url.getPath();
            String jarLocation;

            if (path.startsWith(filePrefix)) {
                LogManager.log("... classloader says that jar file is on the disk");
                if (path.indexOf(jarSep) != -1) {
                    jarLocation = path.substring(filePrefix.length(),
                            path.indexOf(jarSep + installerResource));
                    jarLocation = URLDecoder.decode(jarLocation, StringUtils.ENCODING_UTF8);
                    File jarfile = new File(jarLocation);
                    LogManager.log("... checking if it runs from cached engine");
                    if (jarfile.getAbsolutePath().equals(
                            dest.getAbsolutePath())) {
                        needCache = false; // we already run cached version
                    }
                    LogManager.log("... " + !needCache);
                } else {
                    throw new IOException("JAR path " + path +
                            " doesn`t contaion jar-separator " + jarSep);
                }
            } else if (path.startsWith(httpPrefix)) {
                LogManager.log("... classloader says that jar file is on remote server");
            }
        } else {
            // a quick hack to allow caching engine when run from the IDE (i.e.
            // as a .class) - probably to be removed later. Or maybe not...
            LogManager.log("... running engine as a .class file");
        }

        if (needCache) {
            cacheEngineJar(dest, progress);
        }

        LogManager.logUnindent("... finished caching engine data");
    }
    private static File getEngineLocation() {
        final String propName = EngineResources.LOCAL_ENGINE_PATH_PROPERTY;

        if (System.getProperty(propName) == null) {
            File cachedEngine = new File(Installer.getInstance().getLocalDirectory(), 
                    DEFAULT_ENGINE_JAR_NAME);
            System.setProperty(propName, cachedEngine.getAbsolutePath());
        } 
        return new File(System.getProperty(propName));       
    }
    
    private static void cacheEngineJar(File dest, Progress progress) throws IOException {
        LogManager.log("... starting copying engine content to the new jar file");
        String[] entries = StringUtils.splitByLines(
                StreamUtils.readStream(
                ResourceUtils.getResource(EngineResources.ENGINE_CONTENTS_LIST)));

        JarOutputStream jos = null;

        try {
            Manifest mf = new Manifest();
            mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, getEngineMainClass().getName());
            mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            mf.getMainAttributes().put(Attributes.Name.CLASS_PATH, StringUtils.EMPTY_STRING);

            dest.getParentFile().mkdirs();
            jos = new JarOutputStream(new FileOutputStream(dest), mf);
            LogManager.log("... total entries : " + entries.length);
            for (int i = 0; i < entries.length; i++) {
                progress.setPercentage((i * 100) / entries.length);
                String name = entries[i];
                if (name.length() > 0) {
                    String dataDir = EngineResources.DATA_DIRECTORY +
                            StringUtils.FORWARD_SLASH;
                    if (!name.startsWith(dataDir) || // all except "data/""
                            name.equals(dataDir) || // "data/"
                            name.matches(EngineResources.ENGINE_PROPERTIES_PATTERN) || // engine properties
                            name.equals(CLIHandler.OPTIONS_LIST)) { // additional CLI commands list
                        jos.putNextEntry(new JarEntry(name));
                        if (!name.endsWith(StringUtils.FORWARD_SLASH)) {
                            StreamUtils.transferData(ResourceUtils.getResource(name), jos);
                        }
                    }
                }
            }
            LogManager.log("... adding content list and some other stuff");

            jos.putNextEntry(new JarEntry(
                    EngineResources.DATA_DIRECTORY + StringUtils.FORWARD_SLASH +
                    Registry.DEFAULT_BUNDLED_REGISTRY_FILE_NAME));

            XMLUtils.saveXMLDocument(
                    Registry.getInstance().getEmptyRegistryDocument(),
                    jos);

            jos.putNextEntry(new JarEntry(EngineResources.ENGINE_CONTENTS_LIST));
            jos.write(StringUtils.asString(entries, SystemUtils.getLineSeparator()).getBytes());
        } catch (XMLException e) {
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        } finally {
            if (jos != null) {
                try {
                    jos.close();
                } catch (IOException ex) {
                    LogManager.log(ex);
                }

            }
        }

        LogManager.log("Installer Engine has been cached to " + dest);
    }
    
    public static final String DEFAULT_ENGINE_JAR_NAME = "nbi-engine.jar";//NOI18N    
    
}
